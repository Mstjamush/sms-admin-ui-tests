package smsadminui.utils;

import io.restassured.response.Response;
import smsadminui.config.Config;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AdminFixtures {

    private AdminFixtures() {
    }

    public record ProvisionedClient(int clientId, String clientName, String clientEmail) {
    }

    public record ProvisionedUser(int userId, String email, String password) {
    }

    public record ProvisionedClientAdmin(ProvisionedClient client, ProvisionedUser user) {
    }

    public static void loginAsSuperAdmin() {
        login(Config.get("admin.super_admin.email"), Config.get("admin.super_admin.password"));
    }

    public static void login(String email, String password) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("email", email);
        body.put("password", password);

        Response resp = AdminApiClient.unauthSpec()
                .body(JsonUtil.toJsonObject(body))
                .post("/api/v1/admin/auth/login");

        if (resp.statusCode() != 200) {
            throw new IllegalStateException(
                    "Fixture login failed (" + resp.statusCode() + "): " + resp.asString()
                    + ". Check admin.super_admin.email/password in testrail.properties"
                    + " - run `python seed_admin.py` in bulksms-api first if you haven't.");
        }
        AdminApiClient.setAdminToken(resp.jsonPath().getString("token"));
    }

    /** Caller must already be authenticated as the super admin. */
    public static ProvisionedClient createClient() {
        String suffix = RandomData.uniqueSuffix();
        String name = "QA UI Client " + suffix;
        String email = "qa.client." + suffix + "@example.com";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("client_name", name);
        body.put("client_email", email);
        body.put("address", "Nairobi");
        body.put("country", "KENYA");

        Response resp = AdminApiClient.authedSpec().body(JsonUtil.toJsonObject(body)).post("/api/v1/admin/clients");
        if (resp.statusCode() != 200) {
            throw new IllegalStateException("Fixture: create client failed (" + resp.statusCode() + "): " + resp.asString());
        }
        return new ProvisionedClient(resp.jsonPath().getInt("client_id"), name, email);
    }

    public static ProvisionedUser createUser(Integer clientId, int roleId) {
        String suffix = RandomData.uniqueSuffix();
        String email = "qa.user." + suffix + "@example.com";
        String password = "QaPass123!";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email_address", email);
        body.put("full_names", "QA UI Fixture User");
        body.put("password", password);
        body.put("msisdn", RandomData.uniqueMsisdn());
        body.put("role_id", roleId);
        if (clientId != null) body.put("client_id", clientId);

        Response resp = AdminApiClient.authedSpec().body(JsonUtil.toJsonObject(body)).post("/api/v1/admin/users");
        if (resp.statusCode() != 200) {
            throw new IllegalStateException("Fixture: create user failed (" + resp.statusCode() + "): " + resp.asString());
        }
        return new ProvisionedUser(resp.jsonPath().getInt("user_id"), email, password);
    }

    public static ProvisionedClientAdmin provisionClientAdministrator() {
        loginAsSuperAdmin();
        ProvisionedClient client = createClient();
        ProvisionedUser admin = createUser(client.clientId(), AdminRoles.CLIENT_ADMIN);
        // Billing is enforced: a client with no credit is refused (402), so
        // every provisioned client starts with KES 1000.
        topUp(client.clientId(), "1000");
        return new ProvisionedClientAdmin(client, admin);
    }

    public static void topUp(int clientId, String amountKes) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", amountKes);
        body.put("reference", "QA-UI-" + RandomData.uniqueSuffix());
        Response resp = AdminApiClient.authedSpec().body(JsonUtil.toJsonObject(body))
                .post("/api/v1/admin/billing/clients/" + clientId + "/topup");
        if (resp.statusCode() != 200) {
            throw new IllegalStateException("Fixture: top-up failed (" + resp.statusCode() + "): " + resp.asString());
        }
    }

    public static int provisionListFor(ProvisionedUser clientAdmin, String name, String testdataFile) {
        login(clientAdmin.email(), clientAdmin.password());
        Response resp = AdminApiClient.baseSpec().header("Authorization", "Bearer " + AdminApiClient.token())
                .multiPart("name", name)
                .multiPart("file", testdataFile, TestData.bytes(testdataFile))
                .post("/api/v1/admin/lists");
        if (resp.statusCode() != 200) {
            throw new IllegalStateException("Fixture: list upload failed (" + resp.statusCode() + "): " + resp.asString());
        }
        return resp.jsonPath().getInt("list.id");
    }

    public static void requestBundleAs(ProvisionedUser clientAdmin, String amountKes) {
        login(clientAdmin.email(), clientAdmin.password());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", amountKes);
        body.put("note", "QA UI bundle request");
        Response resp = AdminApiClient.authedSpec().body(JsonUtil.toJsonObject(body)).post("/api/v1/admin/bundle-requests");
        if (resp.statusCode() != 200) {
            throw new IllegalStateException("Fixture: bundle request failed (" + resp.statusCode() + "): " + resp.asString());
        }
    }

    public static String provisionSenderFor(ProvisionedUser clientAdmin, String shortCode) {
        login(clientAdmin.email(), clientAdmin.password());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("short_code", shortCode);
        body.put("send_type_id", 1); // Transactional - seeded by seed_admin.py
        body.put("keywords", "QA");

        Response resp = AdminApiClient.authedSpec().body(JsonUtil.toJsonObject(body)).post("/api/v1/admin/senders");
        if (resp.statusCode() != 200) {
            throw new IllegalStateException("Fixture: create sender failed (" + resp.statusCode() + "): " + resp.asString());
        }
        return resp.jsonPath().getString("short_code");
    }
}
