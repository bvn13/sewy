package me.bvn13.sewy.command;

import java.time.Instant;

public class PongCommand extends AbstractCommand {
    private final long time;
    private long pingTime;

    public PongCommand(PingCommand ping) {
        this.pingTime = ping.getTime();
        this.time = Instant.now().toEpochMilli();
    }

    public long getTime() {
        return time;
    }

    public long getPingTime() {
        return pingTime;
    }

    public void setPingTime(long pingTime) {
        this.pingTime = pingTime;
    }

    public long getLatency() {
        return time - pingTime;
    }

    @Override
    public String toString() {
        return "PongCommand{" +
                "time=" + time +
                ", pingTime=" + pingTime +
                ", latency=" + getLatency() +
                '}';
    }
}
