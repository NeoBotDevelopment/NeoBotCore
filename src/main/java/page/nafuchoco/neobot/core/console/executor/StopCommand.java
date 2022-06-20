package page.nafuchoco.neobot.core.console.executor;

import org.jetbrains.annotations.NotNull;
import page.nafuchoco.neobot.core.console.ConsoleCommandContext;
import page.nafuchoco.neobot.core.console.ConsoleCommandExecutor;

public class StopCommand extends ConsoleCommandExecutor {

    public StopCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(@NotNull ConsoleCommandContext context) {
        Runtime.getRuntime().exit(0);
    }
}
