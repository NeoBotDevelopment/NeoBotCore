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
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    private final Launcher launcher;
    private final Map<String, CommandGroup> groups = new LinkedHashMap<>();

    public DefaultCommandRegistry(Launcher launcher) {
        super(launcher);
        this.launcher = launcher;
    }

    @Override
    public void registerCommand(@NotNull CommandExecutor executor, @Nullable NeoModule module, @Nullable String groupName, @Nullable Guild guild) {
        log.debug("Register command: {} ({})", executor.getName(), guild != null ? guild.getName() : "Global");
        for (CommandGroup g : groups.values()) {
            if (g.getCommands().contains(executor))
                throw new IllegalArgumentException("Cannot register a command executor that has already been registered in another command group.");
        }

        CommandGroup group = groups.computeIfAbsent(groupName, key -> new CommandGroup(groupName));
        group.registerCommand(executor, module, guild);

        registerCommandToDiscord(executor, guild);
    }

    @Override
    public void removeCommand(@NotNull String name, @Nullable NeoModule module, @Nullable Guild guild) {
        for (CommandGroup g : groups.values())
            unregisterCommandFromDiscord(g.removeCommand(name, module, guild), guild);
    }

    @Override
    public void removeCommand(@NotNull CommandExecutor executor, @Nullable NeoModule module, @Nullable Guild guild) {
        for (CommandGroup g : groups.values())
            g.removeCommand(executor, module, guild);
        unregisterCommandFromDiscord(executor, guild);
    }

    @Override
    public void removeCommands(@Nullable NeoModule neoModule, @Nullable Guild guild) {

    }

    @Override
    public void removeCommands(NeoModule module) {
        for (CommandGroup g : groups.values())
            g.removeCommands(module).forEach((guild, executors) -> {
                var guildObj = guild != null ? launcher.getDiscordApi().getGuildById(guild) : null;
                executors.forEach(executor -> unregisterCommandFromDiscord(executor, guildObj));
            });
    }

    @Override
    public void deleteCommandGroup(String groupName) {
        CommandGroup group = groups.remove(groupName);
        if (group != null)
            group.getCommands().forEach(executor -> unregisterCommandFromDiscord(executor, null));

    }

    @Override
    public void deleteCommandGroup(CommandGroup commandGroup) {
        groups.remove(commandGroup.getGroupName()).getCommands().forEach(executor -> unregisterCommandFromDiscord(executor, null));
    }

    @NotNull
    @Override
    public List<CommandGroup> getCommandGroups() {
        return new ArrayList<>(groups.values());
    }

    @NotNull
    @Override
    public List<String> getCommandGroupsNames() {
        return new ArrayList<>(groups.keySet());
    }

    @Override
    public CommandGroup getCommandGroup(String groupName) {
        return groups.get(groupName);
    }

    @NotNull
    @Override
    public List<CommandExecutor> getCommands() {
        return groups.values().stream().flatMap(v -> v.getCommands().stream()).distinct().toList();
    }

    @Override
    public @NotNull List<CommandExecutor> getCommands(@Nullable NeoModule neoModule) {
        return groups.values().stream().flatMap(v -> v.getCommands(neoModule).stream()).distinct().toList();
    }

    @Override
    public @NotNull List<CommandExecutor> getCommands(@Nullable Guild guild) {
        return groups.values().stream().flatMap(v -> v.getCommands(guild).stream()).distinct().toList();
    }

    @Override
    public @Nullable CommandExecutor getExecutor(@Nullable Guild guild, String name) {
        CommandExecutor executor = null;
        List<CommandGroup> groupList = new ArrayList<>(groups.values());
        for (int i = 0; i < groupList.size(); i++) {
            if (executor != null)
                break;
            CommandGroup group = groupList.get(i);
            if (group != null && !group.isEnabled())
                continue;
            executor = group.getExecutor(guild, name);
        }
        return executor;
    }
}
