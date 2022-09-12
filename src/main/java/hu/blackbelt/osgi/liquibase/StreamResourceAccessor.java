package hu.blackbelt.osgi.liquibase;

/*-
 * #%L
 * OSGi liquibase
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import liquibase.resource.AbstractResourceAccessor;
import liquibase.resource.InputStreamList;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;


@RequiredArgsConstructor
public class StreamResourceAccessor extends AbstractResourceAccessor {

    @NonNull
    private final Map<String, InputStream> streams;

    @Override
    public InputStreamList openStreams(String relativeTo, String streamPath) throws IOException {
        InputStreamList returnList = new InputStreamList();

        streams.entrySet().stream()
                .filter(s -> escape(s.getKey())
                        .equals(escape(streamPath)))
                .map(s -> s.getValue()).forEach(s -> {
                    returnList.add(URI.create(escape(streamPath)), s);
                });
        return returnList;
    }

    @Override
    public SortedSet<String> list(String relativeTo, String path, boolean recursive, boolean includeFiles, boolean includeDirectories) {
        return describeLocations();
    }

    @Override
    public SortedSet<String> describeLocations() {
        SortedSet<String> returnSet = new TreeSet<>();
        streams.keySet().forEach(k ->
            returnSet.add(escape(k))
        );
        return returnSet;
    }

    String escape(String str) {
        return str.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}\\-\\_\\.]", "_");
    }
}
