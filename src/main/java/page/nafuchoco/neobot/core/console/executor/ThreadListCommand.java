package page.nafuchoco.neobot.core.console.executor;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import page.nafuchoco.neobot.core.console.ConsoleCommandContext;
import page.nafuchoco.neobot.core.console.ConsoleCommandExecutor;

@Slf4j
public class ThreadListCommand extends ConsoleCommandExecutor {

    public ThreadListCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(@NotNull ConsoleCommandContext context) {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            log.info("Found active thread: {} ({})", thread, thread.getClass().getClassLoader());
        }
    }
}
