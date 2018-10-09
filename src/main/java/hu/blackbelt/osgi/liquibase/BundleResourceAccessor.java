package hu.blackbelt.osgi.liquibase;

import liquibase.resource.ResourceAccessor;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BundleResourceAccessor implements ResourceAccessor {

    private Bundle bundle;
    private ClassLoader bundleClassLoader;

    public BundleResourceAccessor(Bundle bundlePar) {
        this.bundle = bundlePar;
        BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
        this.bundleClassLoader = bundleWiring.getClassLoader();
    }

    @Override
    public Set<InputStream> getResourcesAsStream(String path) throws IOException {
        Set ret = new HashSet();
        ret.add(bundle.getEntry(path).openStream());
        return ret;
    }

    @Override
    public Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
        return new HashSet(Collections.list(bundle.findEntries(path, "*", true)));
    }

    @Override
    public ClassLoader toClassLoader() {
        return bundleClassLoader;
    }
}
