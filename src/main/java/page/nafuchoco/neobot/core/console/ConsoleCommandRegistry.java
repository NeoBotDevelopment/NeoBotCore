package page.nafuchoco.neobot.core.console;

import page.nafuchoco.neobot.api.command.CommandExecutor;
import page.nafuchoco.neobot.api.module.NeoModule;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConsoleCommandRegistry {
    private final Map<NeoModule, Map<String, ConsoleCommandExecutor>> executors;

    public ConsoleCommandRegistry() {
        executors = new LinkedHashMap<>();
    }

    public void registerCommand(ConsoleCommandExecutor executor, NeoModule module) {
        executors.computeIfAbsent(module, key -> new LinkedHashMap<>()).put(executor.getName(), executor);
    }

    public void removeCommand(String name, NeoModule module) {
        if (executors.containsKey(module)) {
            ConsoleCommandExecutor executor = executors.get(module).get(name);
            executors.get(module).remove(executor.getName());
        }
    }

    public void removeCommand(CommandExecutor executor, NeoModule module) {
        if (executors.containsKey(module)) {
            executors.get(module).remove(executor.getName());
        }
    }

    public void removeCommands(NeoModule module) {
        executors.remove(module);
    }

    public List<ConsoleCommandExecutor> getCommands() {
        return executors.values().stream().flatMap(v -> v.values().stream()).distinct().toList();
    }

    public ConsoleCommandExecutor getExecutor(String name) {
        ConsoleCommandExecutor executor = null;
        List<NeoModule> modules = new ArrayList<>(executors.keySet());
        for (int i = modules.size() - 1; i >= 0; i--) {
            if (executor != null)
                break;
            NeoModule module = modules.get(i);
            if (module != null && !module.isEnable())
                continue;
            Map<String, ConsoleCommandExecutor> reg = executors.get(module);
            if (reg != null)
                executor = reg.get(name);
        }
        return executor;
    }
}
