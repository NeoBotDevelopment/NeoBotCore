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

package page.nafuchoco.neobot.api.module;

import page.nafuchoco.neobot.api.Launcher;
import page.nafuchoco.neobot.api.command.CommandExecutor;

import java.io.File;
import java.io.InputStream;

public abstract class NeoModule implements Module {
    private final ClassLoader classLoader;
    private Launcher launcher;
    private ModuleDescription description;
    private File dataFolder;
    private NeoModuleLogger logger;
    private boolean isEnabled;

    protected NeoModule() {
        classLoader = this.getClass().getClassLoader();
        if (!(classLoader instanceof ModuleClassLoader))
            throw new IllegalStateException("The module must be loaded with a \"ModuleClassLoader\".");
        ((ModuleClassLoader) classLoader).initialize(this);
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @Override
    public boolean isEnable() {
        return isEnabled;
    }

    protected boolean setEnable(boolean enabled) {
        if (isEnabled != enabled) {
            isEnabled = enabled;
            if (isEnabled) {
                try {
                    onEnable();
                    getModuleLogger().info("Module is now enabled.: {}", getDescription().getName());
                    return true;
                } catch (Throwable e) {
                    getModuleLogger().error("An uncaught exception has been raised with \"onEnable\".: {}\n", getDescription().getName(), e);
                }
            } else {
                try {
                    onDisable();
                    getModuleLogger().info("Module is now disabled.: {}", getDescription().getName());
                    return true;
                } catch (Throwable e) {
                    getModuleLogger().error("An uncaught exception has been raised with \"onDisable\".: {}\n", getDescription().getName(), e);
                }
            }
        }
        return false;
    }

    @Override
    public final void registerCommand(String groupName, CommandExecutor executor) {
        try {
            launcher.getCommandRegistry().registerCommand(executor, groupName, this);
        } catch (NullPointerException e) {
            throw new IllegalStateException("This method is not available in \"onLoad\".");
        }
    }

    @Override
    public void queueCommandRegister() {
        launcher.queueCommandRegister();
    }

    @Override
    public final void removeCommand(CommandExecutor executor) {
        try {
            launcher.getCommandRegistry().removeCommand(executor, this);
        } catch (NullPointerException e) {
            throw new IllegalStateException("This method is not available in \"onLoad\".");
        }
    }

    @Override
    public final void removeCommands() {
        try {
            launcher.getCommandRegistry().removeCommands(this);
        } catch (NullPointerException e) {
            throw new IllegalStateException("This method is not available in \"onLoad\".");
        }
    }

    @Override
    public ModuleDescription getDescription() {
        return description;
    }

    @Override
    public Launcher getLauncher() {
        return launcher;
    }

    @Override
    public InputStream getResources(String filename) {
        return classLoader.getResourceAsStream(filename);
    }

    @Override
    public File getDataFolder() {
        if (!dataFolder.exists())
            dataFolder.mkdirs();
        return dataFolder;
    }

    @Override
    public ModuleClassLoader getClassLoader() {
        return (ModuleClassLoader) classLoader;
    }

    @Override
    public NeoModuleLogger getModuleLogger() {
        return logger;
    }

    final void init(Launcher launcher, ModuleDescription description) {
        this.launcher = launcher;
        this.description = description;
        dataFolder = new File("modules/", description.getName());
        logger = new NeoModuleLogger(this);
    }
}
