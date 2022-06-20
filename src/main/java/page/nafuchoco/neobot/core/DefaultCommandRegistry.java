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

import lombok.extern.slf4j.Slf4j;
import page.nafuchoco.neobot.api.Launcher;
import page.nafuchoco.neobot.api.command.CommandExecutor;
import page.nafuchoco.neobot.api.command.CommandGroup;
import page.nafuchoco.neobot.api.command.ICommandRegistry;
import page.nafuchoco.neobot.api.module.NeoModule;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class DefaultCommandRegistry extends CommandRegistrar implements ICommandRegistry {
    private final Map<String, CommandGroup> groups = new LinkedHashMap<>();

    public DefaultCommandRegistry(Launcher launcher) {
        super(launcher);
    }

    @Override
    public void registerCommand(CommandExecutor executor, NeoModule module) {
        registerCommand(executor, null, module);
    }

    @Override
    public void registerCommand(CommandExecutor executor, String groupName, NeoModule module) {
        log.debug("Register command: {}", executor.getName());
        for (CommandGroup g : groups.values()) {
            if (g.getCommands().contains(executor))
                throw new IllegalArgumentException("Cannot register a command executor that has already been registered in another command group.");
        }

        CommandGroup group = groups.computeIfAbsent(groupName, key -> new CommandGroup(groupName));
        group.registerCommand(executor, module);

        registerCommandToDiscord(executor);
    }

    @Override
    public void removeCommand(String name, NeoModule module) {
        for (CommandGroup g : groups.values())
            g.removeCommand(name, module);
    }

    @Override
    public void removeCommand(CommandExecutor executor, NeoModule module) {
        for (CommandGroup g : groups.values())
            g.removeCommand(executor, module);
    }

    @Override
    public void removeCommands(NeoModule module) {
        for (CommandGroup g : groups.values())
            g.removeCommands(module);
    }

    @Override
    public void deleteCommandGroup(String groupName) {
        groups.remove(groupName);
    }

    @Override
    public void deleteCommandGroup(CommandGroup commandGroup) {
        groups.remove(commandGroup.getGroupName());
    }

    @Override
    public List<CommandGroup> getCommandGroups() {
        return new ArrayList<>(groups.values());
    }

    @Override
    public List<String> getCommandGroupsNames() {
        return new ArrayList<>(groups.keySet());
    }

    @Override
    public List<CommandExecutor> getCommands() {
        return groups.values().stream().flatMap(v -> v.getCommands().stream()).distinct().toList();
    }

    @Override
    public CommandGroup getCommandGroup(String groupName) {
        return groups.get(groupName);
    }

    @Override
    public CommandExecutor getExecutor(String name) {
        CommandExecutor executor = null;
        List<CommandGroup> groupList = new ArrayList<>(groups.values());
        for (int i = 0; i < groupList.size(); i++) {
            if (executor != null)
                break;
            CommandGroup group = groupList.get(i);
            if (group != null && !group.isEnabled())
                continue;
            executor = group.getExecutor(name);
        }
        return executor;
    }
}
