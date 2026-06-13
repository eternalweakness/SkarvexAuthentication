package org.skarvex.auth.core.util;

import java.time.Duration;

public class TimeUtil {

    private TimeUtil() {}

    public static String formatDuration(Duration seconds) {
        long minutes = seconds.toMinutes();
        long remainingSeconds = seconds.toSecondsPart();

        if (minutes <= 0) {
            return remainingSeconds + " sec";
        }

        return minutes + " min " + remainingSeconds + " sec";
    }

}
