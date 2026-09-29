package smsadminui.utils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import smsadminui.config.Config;

public final class AdminApiClient {

    static {
        HttpLogging.install();
    }

    private static String adminToken;

    private AdminApiClient() {
    }

    public static RequestSpecification baseSpec() {
        return RestAssured.given().baseUri(Config.get("api.base.url"));
    }

    public static RequestSpecification unauthSpec() {
        return baseSpec().contentType(ContentType.JSON);
    }

    public static RequestSpecification authedSpec() {
        if (adminToken == null || adminToken.isBlank()) {
            throw new IllegalStateException("No admin token - log in first (see AdminFixtures)");
        }
        return baseSpec().contentType(ContentType.JSON).header("Authorization", "Bearer " + adminToken);
    }

    public static void setAdminToken(String token) {
        adminToken = token;
    }

    public static String token() {
        return adminToken;
    }
}
