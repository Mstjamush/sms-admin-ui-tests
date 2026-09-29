package smsadminui.utils;

import io.restassured.RestAssured;

public final class HttpLogging {

    private static boolean installed = false;

    private HttpLogging() {
    }

    public static synchronized void install() {
        if (installed) return;
        RestAssured.filters(new ApiLoggingFilter());
        installed = true;
    }
}
