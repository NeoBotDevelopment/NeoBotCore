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

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import page.nafuchoco.neobot.api.Launcher;
import page.nafuchoco.neobot.api.module.exception.InvalidModuleException;
import page.nafuchoco.neobot.api.module.exception.ModuleDuplicateException;
import page.nafuchoco.neobot.api.module.exception.UnknownDependencyException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class ModuleManager {
    private final Launcher launcher;
    private final ModuleRegistry moduleRegistry;
    private final ModuleLoader moduleLoader;
    private final List<File> files;
    private final List<GatewayIntent> additionalIntents;

    public ModuleManager(Launcher launcher, String moduleDir) {
        this.launcher = launcher;
        this.moduleRegistry = new ModuleRegistry();
        moduleLoader = new ModuleLoader(launcher, moduleRegistry, moduleDir);
        files = moduleLoader.searchModules();
        additionalIntents = new ArrayList<>();
    }

    /**
     * @return Returns all registered modules.
     */
    public List<NeoModule> getModules() {
        return moduleRegistry.getModules();
    }

    /**
     * Get a module.
     *
     * @param name Name of the module to get.
     * @return module
     */
    public NeoModule getModule(String name) {
        return moduleRegistry.getModule(name);
    }

    /**
     * Load all the modules found.
     */
    public void loadAllModules() {
        List<File> waitingModule = new ArrayList<>();
        while (!files.isEmpty()) {
            Iterator<File> iterator = files.iterator();
            iterator:
            while (iterator.hasNext()) {
                var file = iterator.next();
                ModuleDescription description;
                try {
                    description = moduleLoader.loadModuleDescription(file);
                } catch (InvalidModuleException e) {
                    log.warn("Failed to load the module information.: {}", file.getName(), e);
                    iterator.remove();
                    continue iterator;
                }

                if (!CollectionUtils.isEmpty(description.getLoadBefore())
                        && waitingModule.size() != IterableUtils.size(Collections.singleton(iterator))) {
                    for (String module : description.getLoadBefore()) {
                        if (moduleRegistry.getModule(module) == null) {
                            waitingModule.add(file);
                            continue iterator;
                        }
                        waitingModule.remove(file);
                    }
                }

                loadModule(file);
                iterator.remove();
            }
        }
    }

    /**
     * Load the module.
     *
     * @param file Jar file of the module.
     * @return Returns true if the module is successfully loaded.
     */
    public boolean loadModule(File file) {
        NeoModule module = null;
        try {
            module = moduleLoader.loadModule(file);
        } catch (UnknownDependencyException e) {
            log.error("It couldn't be load because the dependency couldn't be resolved.", e);
        } catch (InvalidModuleException e) {
            log.error("The module could not be loaded due to an incorrect format.", e);
        }

        if (module == null)
            return false;

        try {
            moduleRegistry.registerModule(module);
        } catch (ModuleDuplicateException e) {
            log.warn("The same module is already loaded.: {}", module.getDescription().getName());
            return false;
        }

        if (module.getDescription().getAdditionalIntents() != null)
            additionalIntents.addAll(module.getDescription().getAdditionalIntents());

        try {
            module.onLoad();
            log.info("Module loaded.: {}", module.getDescription().getName());
        } catch (Throwable e) {
            log.warn("An uncaught exception has been raised in \"onLoad\".", e);
            unloadModule(module);
            return false;
        }
        return true;
    }

    /**
     * Enable all loaded modules.
     */
    public void enableAllModules() {
        for (NeoModule module : moduleRegistry.getModules()) {
            enableModule(module);
        }
    }

    /**
     * Enables the module.
     *
     * @param name Module name to enable.
     * @return Returns true if the module was successfully enabled.
     * @throws IllegalArgumentException It will be thrown if the specified module does not exist.
     */
    public boolean enableModule(String name) {
        NeoModule module = moduleRegistry.getModule(name);
        if (module == null)
            throw new IllegalArgumentException("The specified module is not registered.");
        return enableModule(module);
    }

    /**
     * Enables the module.
     *
     * @param module The module to enable.
     * @return Returns true if the module was successfully enabled.
     */
    public boolean enableModule(NeoModule module) {
        return module.setEnable(true);
    }

    /**
     * Disable all loaded modules.
     */
    public void disableAllModules() {
        for (NeoModule module : moduleRegistry.getModules()) {
            disableModule(module);
        }
    }

    /**
     * Disables the module.
     *
     * @param name Module name to disable.
     * @return Returns true if the module has been successfully disabled.
     * @throws IllegalArgumentException It will be thrown if the specified module does not exist.
     */
    public boolean disableModule(String name) {
        NeoModule module = moduleRegistry.getModule(name);
        if (module == null)
            throw new IllegalArgumentException("The specified module is not registered.");
        return disableModule(module);
    }

    /**
     * Disables the module.
     *
     * @param module Modules to disable.
     * @return Returns true if the module has been successfully disabled.
     */
    public boolean disableModule(NeoModule module) {
        return module.setEnable(false);

    }

    /**
     * Unload all loaded modules.
     */
    public void unloadAllModules() {
        for (NeoModule module : moduleRegistry.getModules()) {
            unloadModule(module);
        }
    }

    /**
     * Unloads the module.
     *
     * @param name Module name to unload.
     * @throws IllegalArgumentException It will be thrown if the specified module does not exist.
     */
    public void unloadModule(String name) {
        NeoModule module = moduleRegistry.getModule(name);
        if (module == null)
            throw new IllegalArgumentException("The specified module is not registered.");
        unloadModule(module);
    }

    /**
     * Unloads the module.
     *
     * @param module Modules to unload
     */
    public void unloadModule(NeoModule module) {
        if (module.isEnable()) {
            log.warn("Unable to unload because the module is in a valid state.: {}", module.getDescription().getName());
        } else {
            try {
                ModuleClassLoader classLoader = module.getClassLoader();

                for (Thread thread : Thread.getAllStackTraces().keySet()) {
                    if (thread.getClass().getClassLoader() == classLoader) {
                        log.info("Stopping module thread: {}", thread.getName());
                        thread.interrupt();
                        thread.join(3000);
                        if (thread.isAlive())
                            thread.stop();
                    }
                }

                moduleRegistry.deleteModule(module);
                classLoader.close();
                log.info("Module unloaded.: {}", module.getDescription().getName());
            } catch (IOException | InterruptedException e) {
                log.error("An error occurred during the unloading of the module.", e);
            }
        }
    }

    public List<GatewayIntent> getAdditionalIntents() {
        return additionalIntents.stream().distinct().toList();
    }
}
