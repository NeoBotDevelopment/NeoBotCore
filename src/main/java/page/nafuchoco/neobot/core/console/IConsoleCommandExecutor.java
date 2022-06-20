package page.nafuchoco.neobot.core.console;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IConsoleCommandExecutor {
    /**
     * @return Name of the command executor
     */
    @NotNull
    String getName();

    /**
     * @return Command Execuator aliases
     */
    @Nullable
    List<String> getAliases();

    /**
     * Execute the command.
     *
     * @param context The command context to use at runtime
     */
    void onInvoke(@NotNull ConsoleCommandContext context);
}
