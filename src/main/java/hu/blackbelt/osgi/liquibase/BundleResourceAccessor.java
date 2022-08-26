package hu.blackbelt.osgi.liquibase;

import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.InputStreamList;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class BundleResourceAccessor extends ClassLoaderResourceAccessor {

    final Bundle bundle;
    public BundleResourceAccessor(Bundle bundlePar) {
        super(bundlePar.adapt(BundleWiring.class).getClassLoader());
        this.bundle = bundlePar;
    }

    @Override
    public InputStreamList openStreams(String relativeTo, String streamPath) throws IOException {
        InputStreamList inputStreams = super.openStreams(relativeTo, streamPath);
        if (inputStreams.size() == 0) {
            URL url = bundle.getEntry(streamPath);
            if (url != null) {
                try {
                    return new InputStreamList(url.toURI(), url.openStream());
                } catch (URISyntaxException e) {
                }
            }
        }
        return inputStreams;
    }
}

