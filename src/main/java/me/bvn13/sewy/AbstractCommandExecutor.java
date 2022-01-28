package me.bvn13.sewy;

import me.bvn13.sewy.command.AbstractCommand;

/**
 * Interface to describe command executor.
 * Every command encountered by {@link CommandClientListener}
 * will be sent into such executor provided
 * in {@link Client#Client(java.lang.String, int, java.lang.Class)}
 * or {@link Server#Server(java.lang.String, int, java.lang.Class)}
 * while instantiating Client and Server
 * @param <T>
 */
@FunctionalInterface
public interface AbstractCommandExecutor<T extends AbstractCommand> {
    /**
     * Command handler
     * @param command incoming command
     * @return response on incoming command is another command for corresponding side
     */
    AbstractCommand onCommand(T command);
}
