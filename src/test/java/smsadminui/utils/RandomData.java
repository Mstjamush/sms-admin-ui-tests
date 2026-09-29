package smsadminui.utils;

import java.util.UUID;

public final class RandomData {

    private RandomData() {
    }

    public static String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public static String uniqueMsisdn() {
        long suffix = Math.abs(UUID.randomUUID().getLeastSignificantBits()) % 100_000_000L;
        return String.format("2547%08d", suffix);
    }
}
