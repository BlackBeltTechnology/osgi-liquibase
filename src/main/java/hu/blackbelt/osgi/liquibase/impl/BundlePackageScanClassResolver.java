package hu.blackbelt.osgi.liquibase.impl;

import liquibase.servicelocator.DefaultPackageScanClassResolver;
import liquibase.servicelocator.PackageScanFilter;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

import java.util.Collection;
import java.util.Set;

/**
 * Package scan resolver that works with OSGI frameworks.
 */
public class BundlePackageScanClassResolver extends DefaultPackageScanClassResolver {

    private final BundleWiring bundleWiring;

    public BundlePackageScanClassResolver(Bundle bundle) {
        this.bundleWiring = bundle.adapt(BundleWiring.class);
    }

    @Override
    protected void find(PackageScanFilter test, String packageNamePar, Set<Class<?>> classes) {
        String packageName = packageNamePar.replace('.', '/');

        Collection<String> names =
                bundleWiring.listResources(packageName, "*.class", BundleWiring.LISTRESOURCES_RECURSE);
        if (names == null) {
            return;
        }
        ClassLoader bundleClassLoader = bundleWiring.getClassLoader();
        for (String name : names) {
            String fixedName = name.substring(0, name.indexOf('.')).replace('/', '.');

            try {
                Class<?> klass = bundleClassLoader.loadClass(fixedName);
                if (test.matches(klass)) {
                    classes.add(klass);
                }
            } catch (ClassNotFoundException e) {
                log.debug("Cant load class: " + e.getMessage());
            }

        }

    }
}
