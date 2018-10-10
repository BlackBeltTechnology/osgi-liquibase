package liquibase.servicelocator;

import hu.blackbelt.osgi.liquibase.impl.BundlePackageScanClassResolver;
import hu.blackbelt.osgi.liquibase.BundleResourceAccessor;
import liquibase.exception.ServiceNotFoundException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.Logger;
import liquibase.logging.core.DefaultLogger;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Modified version of the original ServiceLocator class coming from Liquibase to make it work inside an OSGi
 * environment. It is not a problem to reimplement a class in this way as based on the specification this class will be
 * used for sure as this entry is before the embedded jars classes on the classpath of the bundle. It is not good to set
 * the instance of the ServiceLocator from a BundleActivator as bundles may not be started during calling a refresh on
 * bundles that use the packages of this one.
 */
public class ServiceLocator {

    private static ServiceLocator instance;

    static {
        reset();
    }

    private ResourceAccessor resourceAccessor;

    private Map<Class, List<Class>> classesBySuperclass;
    private List<String> packagesToScan;

    // cannot look up regular logger because you get a stackoverflow since
    private Logger logger = new DefaultLogger();

    // we are in the servicelocator
    private PackageScanClassResolver classResolver;

    protected ServiceLocator() {
        this.classResolver = defaultClassLoader();
        setResourceAccessor(new ClassLoaderResourceAccessor());
    }

    protected ServiceLocator(ResourceAccessor accessor) {
        this.classResolver = defaultClassLoader();
        setResourceAccessor(accessor);
    }

    protected ServiceLocator(PackageScanClassResolver classResolver) {
        this.classResolver = classResolver;
        setResourceAccessor(new ClassLoaderResourceAccessor());
    }

    protected ServiceLocator(PackageScanClassResolver classResolver, ResourceAccessor accessor) {
        this.classResolver = classResolver;
        setResourceAccessor(accessor);
    }

    public static ServiceLocator getInstance() {
        return instance;
    }

    public static void setInstance(ServiceLocator newInstance) {
        instance = newInstance;
    }

    private PackageScanClassResolver defaultClassLoader() {
        if (WebSpherePackageScanClassResolver.isWebSphereClassLoader(this.getClass().getClassLoader())) {
            logger.debug("Using WebSphere Specific Class Resolver");
            return new WebSpherePackageScanClassResolver("liquibase/parser/core/xml/dbchangelog-2.0.xsd");
        } else {
            return new DefaultPackageScanClassResolver();
        }
    }

    public void setResourceAccessor(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
        this.classesBySuperclass = new HashMap<Class, List<Class>>();

        this.classResolver.setClassLoaders(new HashSet<ClassLoader>(Arrays.asList(new ClassLoader[] { resourceAccessor
                .toClassLoader() })));

        packagesToScan = new ArrayList<String>();

        if (packagesToScan.size() == 0) {
            addPackageToScan("liquibase.change");
            addPackageToScan("liquibase.changelog");
            addPackageToScan("liquibase.database");
            addPackageToScan("liquibase.parser");
            addPackageToScan("liquibase.precondition");
            addPackageToScan("liquibase.datatype");
            addPackageToScan("liquibase.serializer");
            addPackageToScan("liquibase.sqlgenerator");
            addPackageToScan("liquibase.executor");
            addPackageToScan("liquibase.snapshot");
            addPackageToScan("liquibase.logging");
            addPackageToScan("liquibase.diff");
            addPackageToScan("liquibase.structure");
            addPackageToScan("liquibase.structurecompare");
            addPackageToScan("liquibase.lockservice");
            addPackageToScan("liquibase.sdk.database");
            addPackageToScan("liquibase.ext");
        }
    }

    public void addPackageToScan(String packageName) {
        logger.debug("Adding package to scan: " + packageName);
        packagesToScan.add(packageName);
    }

    public Class findClass(Class requiredInterface) throws ServiceNotFoundException {
        Class[] classes = findClasses(requiredInterface);
        if (PrioritizedService.class.isAssignableFrom(requiredInterface)) {
            PrioritizedService returnObject = null;
            for (Class clazz : classes) {
                PrioritizedService newInstance;
                try {
                    newInstance = (PrioritizedService) clazz.newInstance();
                } catch (Exception e) {
                    throw new UnexpectedLiquibaseException(e);
                }

                if (returnObject == null || newInstance.getPriority() > returnObject.getPriority()) {
                    returnObject = newInstance;
                }
            }

            if (returnObject == null) {
                throw new ServiceNotFoundException("Could not find implementation of " + requiredInterface.getName());
            }
            return returnObject.getClass();
        }

        if (classes.length != 1) {
            throw new ServiceNotFoundException("Could not find unique implementation of " + requiredInterface.getName()
                    + ".  Found " + classes.length + " implementations");
        }

        return classes[0];
    }

    public <T> Class<? extends T>[] findClasses(Class<T> requiredInterface) throws ServiceNotFoundException {
        logger.debug("ServiceLocator.findClasses for " + requiredInterface.getName());

        try {
            Class.forName(requiredInterface.getName());

            if (!classesBySuperclass.containsKey(requiredInterface)) {
                classesBySuperclass.put(requiredInterface, findClassesImpl(requiredInterface));
            }
        } catch (Exception e) {
            throw new ServiceNotFoundException(e);
        }

        List<Class> classes = classesBySuperclass.get(requiredInterface);
        HashSet<Class> uniqueClasses = new HashSet<Class>(classes);
        return uniqueClasses.toArray(new Class[uniqueClasses.size()]);
    }

    public Object newInstance(Class requiredInterface) throws ServiceNotFoundException {
        try {
            return findClass(requiredInterface).newInstance();
        } catch (Exception e) {
            throw new ServiceNotFoundException(e);
        }
    }

    private List<Class> findClassesImpl(Class requiredInterface) throws Exception {
        logger.debug("ServiceLocator finding classes matching interface " + requiredInterface.getName());

        List<Class> classes = new ArrayList<Class>();

        classResolver.addClassLoader(resourceAccessor.toClassLoader());
        for (Class<?> clazz : classResolver.findImplementations(requiredInterface,
                packagesToScan.toArray(new String[packagesToScan.size()]))) {
            if (clazz.getAnnotation(LiquibaseService.class) != null
                    && clazz.getAnnotation(LiquibaseService.class).skip()) {
                continue;
            }

            if (!Modifier.isAbstract(clazz.getModifiers()) && !Modifier.isInterface(clazz.getModifiers())
                    && Modifier.isPublic(clazz.getModifiers())) {
                try {
                    clazz.getConstructor();
                    logger.debug(clazz.getName() + " matches " + requiredInterface.getName());

                    classes.add(clazz);
                } catch (NoSuchMethodException e) {
                    logger.info(String.format("Can not use {} "
                            + " as a Liquibase service because it does not have a no-argument constructor", clazz));
                } catch (NoClassDefFoundError e) {
                    String message =
                           String.format("Can not use {} as a Liquibase service because {} "
                                    + " is not in the classpath", clazz, e.getMessage().replace("/", "."));
                    if (e.getMessage().startsWith("org/yaml/snakeyaml")) {
                        logger.info(message);
                    } else {
                        logger.warning(message);
                    }
                }
            }
        }

        return classes;
    }

    public static void reset() {
        instance = null;
        try {
            Bundle bundle = FrameworkUtil.getBundle(ServiceLocator.class);
            if (bundle != null) {
                BundlePackageScanClassResolver classResolver = new BundlePackageScanClassResolver(bundle);
                ResourceAccessor resourceAccessor = new BundleResourceAccessor(bundle);
                instance = new ServiceLocator(classResolver, resourceAccessor);
            } else {
                instance = new ServiceLocator();
            }
        } catch (NoClassDefFoundError e) {
            // We are not in OSGi environment

        }
    }

    protected Logger getLogger() {
        return logger;
    }
}
