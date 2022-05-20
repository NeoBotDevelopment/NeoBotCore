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

package page.nafuchoco.neobot.core;

import io.sentry.Sentry;
import io.sentry.SentryOptions;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.apache.commons.lang3.StringUtils;
import page.nafuchoco.neobot.api.ConfigLoader;
import page.nafuchoco.neobot.api.DatabaseConnector;
import page.nafuchoco.neobot.api.Launcher;
import page.nafuchoco.neobot.api.NeoBot;
import page.nafuchoco.neobot.api.command.CommandRegistry;
import page.nafuchoco.neobot.api.command.SlashCommandEventHandler;
import page.nafuchoco.neobot.api.datastore.DataStoreManager;
import page.nafuchoco.neobot.api.module.ModuleManager;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Slf4j
public final class NeoBotLauncher implements Launcher {
    private final String version;
    private final NeoBotConfig configration;
    private final DatabaseConnector databaseConnector;
    private final DataStoreManager dataStoreManager;
    private final ShardManager discordApi;
    private final CommandRegistry commandRegistry = new CommandRegistry(this);
    private final ModuleManager moduleManager = new ModuleManager(this, "modules");

    public NeoBotLauncher() {
        version = NeoBotLauncher.class.getPackage().getImplementationVersion();
        NeoBot.setLauncher(this);
        log.info(getSystemInfo());

        // load configuration
        var configurationFile = new File("NeoBotCore.yaml");
        if (!configurationFile.exists()) {
            try (InputStream original = ClassLoader.getSystemResourceAsStream("NeoBotCore.yaml")) {
                Files.copy(original, configurationFile.toPath());
                log.info("The configuration file was not found, so a new file was created.");
                log.debug("Configuration file location: {}", configurationFile.getPath());
            } catch (IOException e) {
                log.error("The correct configuration file could not be retrieved from the executable.\n" +
                        "If you have a series of problems, please contact the developer.", e);
            }
        }
        configration = ConfigLoader.loadConfig(configurationFile, NeoBotConfig.class);

        // setup sentry for error reporting
        if (!StringUtils.isEmpty(configration.getSentryDsn())) {
            var options = new SentryOptions();
            options.setDsn(configration.getSentryDsn());
            Sentry.init(options);
        }

        // start connection to database
        log.info("Start a connection to the database.");
        databaseConnector = new DatabaseConnector(configration.getBasicConfig().getDatabase().getDatabaseType(),
                configration.getBasicConfig().getDatabase().getAddress(),
                configration.getBasicConfig().getDatabase().getDatabase(),
                configration.getBasicConfig().getDatabase().getUsername(),
                configration.getBasicConfig().getDatabase().getPassword());

        dataStoreManager = new DataStoreManager(databaseConnector);

        moduleManager.loadAllModules();

        // connect to discord
        var shardManagerBuilder =
                DefaultShardManagerBuilder.create(configration.getBasicConfig().getDiscordToken(), GatewayIntent.GUILD_MESSAGES);
        shardManagerBuilder.disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS);
        shardManagerBuilder.addEventListeners(new SlashCommandEventHandler(this, commandRegistry));
        ShardManager shardManager = null;

        try {
            log.info("Attempt to login to the Discord API.");
            shardManager = shardManagerBuilder.build();
            while (!shardManager.getStatus(0).equals(JDA.Status.CONNECTED))
                Thread.sleep(100);
        } catch (LoginException e) {
            log.error("Failed to authenticate the connection of the Discord API.", e);
            Runtime.getRuntime().exit(1);
        } catch (InterruptedException e) {
            log.error("An error occurred while waiting for the login process.", e);
            Runtime.getRuntime().exit(1);
        }

        discordApi = shardManager;
        log.info("Successfully connected to the Discord API.");
        log.debug("Ping! {}ms", discordApi.getAverageGatewayPing());
        discordApi.setPresence(OnlineStatus.INVISIBLE, null);

        moduleManager.enableAllModules();
        discordApi.setPresence(OnlineStatus.ONLINE, null);

        // register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down the system...");
            moduleManager.disableAllModules();
            discordApi.shutdown();
            if (databaseConnector != null)
                databaseConnector.close();
            log.info("See you again!");
        }));
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getSystemInfo() {
        long max = Runtime.getRuntime().maxMemory() / 1048576L;
        long total = Runtime.getRuntime().totalMemory() / 1048576L;
        long free = Runtime.getRuntime().freeMemory() / 1048576L;
        long used = total - free;

        val builder = new StringBuilder();
        builder.append("\n====== System Info ======\n");
        builder.append("Operating System:      ").append(System.getProperty("os.name")).append("\n");
        builder.append("JVM Version:           ").append(System.getProperty("java.version")).append("\n");
        builder.append("NeoBotCore Version:    ").append(getVersion()).append("\n\n");
        builder.append("====== Memory Info ======\n");
        builder.append("Reserved memory:       ").append(total).append("MB\n");
        builder.append("  -> Used:             ").append(used).append("MB\n");
        builder.append("  -> Free:             ").append(free).append("MB\n");
        builder.append("Max. reserved memory:  ").append(max).append("MB\n");

        return builder.toString();
    }

    @Override
    public DatabaseConnector getDatabaseConnector() {
        return databaseConnector;
    }

    @Override
    public DataStoreManager getDataStoreManager() {
        return dataStoreManager;
    }

    @Override
    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    @Override
    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    @Override
    public ShardManager getDiscordApi() {
        return discordApi;
    }

    @Override
    public void queueCommandRegister() {
        commandRegistry.queue();
    }
}
