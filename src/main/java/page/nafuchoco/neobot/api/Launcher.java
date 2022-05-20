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

package page.nafuchoco.neobot.api;

import net.dv8tion.jda.api.sharding.ShardManager;
import page.nafuchoco.neobot.api.command.CommandRegistry;
import page.nafuchoco.neobot.api.datastore.DataStoreManager;
import page.nafuchoco.neobot.api.module.ModuleManager;

public interface Launcher {

    /**
     * Get the version string of NeoBotCore.
     *
     * @return the version string of NeoBotCore.
     */
    String getVersion();

    /**
     * Get information about the system on which NeoBotCore is running.
     *
     * @return information about the system on which NeoBotCore is running.
     */
    String getSystemInfo();

    DatabaseConnector getDatabaseConnector();

    DataStoreManager getDataStoreManager();

    CommandRegistry getCommandRegistry();

    ModuleManager getModuleManager();

    ShardManager getDiscordApi();

    void queueCommandRegister();
}
