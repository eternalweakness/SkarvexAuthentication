package org.skarvex.auth.core.service;

public class TimeService {

    private final long BLOCK_SESSION_TIME = 300;

    private TimeService() {}

    public static String formatDuration(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;

        if (minutes <= 0) {
            return remainingSeconds + " sec";
        }

        return minutes + " min " + remainingSeconds + " sec";
    }

}
