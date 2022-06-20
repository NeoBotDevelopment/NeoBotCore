package page.nafuchoco.neobot.core.console;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;

public record ConsoleCommandContext(String trigger, String[] args, ConsoleCommandExecutor commandExecutor) {
    @Override
    public String toString() {
        return "ConsoleCommandContext{" +
                "trigger='" + trigger + '\'' +
                ", args=" + Arrays.toString(args) +
                ", commandExecutor=" + commandExecutor +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ConsoleCommandContext that = (ConsoleCommandContext) o;

        return new EqualsBuilder().append(trigger, that.trigger).append(args, that.args).append(commandExecutor, that.commandExecutor).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(trigger).append(args).append(commandExecutor).toHashCode();
    }
}
