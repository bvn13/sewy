package me.bvn13.sewy.command;

import java.time.Instant;

public class PingCommand extends AbstractCommand {
    private final long time;

    public PingCommand() {
        this.time = Instant.now().toEpochMilli();
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "PingCommand{" +
                "time=" + time +
                '}';
    }
}
