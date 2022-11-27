/*
 * Copyright 2022 NAFU_at
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package page.nafuchoco.neobot.core.datastore;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import page.nafuchoco.neobot.api.DatabaseConnector;
import page.nafuchoco.neobot.api.datastore.DataStore;
import page.nafuchoco.neobot.api.datastore.DataStoreBuilder;
import page.nafuchoco.neobot.api.datastore.DataStoreManager;

import java.util.HashMap;
import java.util.Map;

public class DefaultDataStoreManager implements DataStoreManager {
    private final DatabaseConnector connector;
    private final Map<String, DataStore> dataStoreMap;

    public DefaultDataStoreManager(DatabaseConnector connector) {
        this.connector = connector;
        dataStoreMap = new HashMap<>();
    }

    @Override
    public DataStoreBuilder createDataStoreBuilder() {
        return new DefaultDataStoreBuilder(connector);
    }

    @Override
    public @Nullable DataStore getDataStore(@NotNull String name) {
        return dataStoreMap.get(name);
    }

    @Override
    public void registerDataStore(@NotNull DataStore dataStore) {
        dataStoreMap.put(dataStore.getName(), dataStore);
    }
}
