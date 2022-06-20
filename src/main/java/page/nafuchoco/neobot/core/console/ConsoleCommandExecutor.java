package page.nafuchoco.neobot.core.console;

import java.util.List;

public abstract class ConsoleCommandExecutor implements IConsoleCommandExecutor {
    private final String name;
    private final List<String> aliases;

    protected ConsoleCommandExecutor(String name, String... aliases) {
        this.name = name;
        this.aliases = List.of(aliases);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }
}
