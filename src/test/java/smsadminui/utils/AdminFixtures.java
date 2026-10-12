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

    /** A company that registered itself through the public form: PENDING until a Super Administrator approves it. */
    public record RegisteredCompany(int clientId, String clientName, String reference, String adminEmail, String adminPassword) {
    }

    /** Tokens a prepaid client is given when a scenario needs one that can send: 1000 pages at KES 1.00 each. */
    public static final int DEFAULT_TOKENS = 1000;

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

    /**
     * Caller must already be authenticated as the super admin. A prepaid client on its own price of KES 1.00 a token, active at
     * once and with no tokens: POST /clients needs billing_model plus a band or token_price (prepaid), or default_rate_per_sms
     * (postpaid), and answers 201.
     */
    public static ProvisionedClient createClient() {
        Map<String, Object> billing = new LinkedHashMap<>();
        billing.put("billing_model", "PREPAID");
        billing.put("token_price", "1.00");
        return postClient(billing);
    }

    /** Caller must already be authenticated as the super admin. Postpaid: every send is billed at {@code ratePerSms}, invoiced daily. */
    public static ProvisionedClient createPostpaidClient(String ratePerSms) {
        Map<String, Object> billing = new LinkedHashMap<>();
        billing.put("billing_model", "POSTPAID");
        billing.put("default_rate_per_sms", ratePerSms);
        billing.put("billing_period", "DAILY");
        billing.put("invoice_due_days", 0);
        return postClient(billing);
    }

    private static ProvisionedClient postClient(Map<String, Object> billing) {
        String suffix = RandomData.uniqueSuffix();
        String name = "QA UI Client " + suffix;
        String email = "qa.client." + suffix + "@example.com";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("client_name", name);
        body.put("client_email", email);
        body.put("address", "Nairobi");
        body.put("country", "KENYA");
        body.putAll(billing);

        Response resp = AdminApiClient.authedSpec().body(JsonUtil.toJsonObject(body)).post("/api/v1/admin/clients");
        if (resp.statusCode() != 201) {
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

    /** A prepaid client with a Client Administrator and {@link #DEFAULT_TOKENS} tokens (billing is enforced: no tokens, no sends). */
    public static ProvisionedClientAdmin provisionClientAdministrator() {
        loginAsSuperAdmin();
        ProvisionedClient client = createClient();
        ProvisionedUser admin = createUser(client.clientId(), AdminRoles.CLIENT_ADMIN);
        adjustTokens(client.clientId(), DEFAULT_TOKENS, "Automated UI suite credit");
        return new ProvisionedClientAdmin(client, admin);
    }

    /** A postpaid client (KES 0.50 an SMS page, invoiced daily, due at once) with a Client Administrator. It is billed, never refused. */
    public static ProvisionedClientAdmin provisionPostpaidClientAdministrator() {
        loginAsSuperAdmin();
        ProvisionedClient client = createPostpaidClient("0.50");
        ProvisionedUser admin = createUser(client.clientId(), AdminRoles.CLIENT_ADMIN);
        return new ProvisionedClientAdmin(client, admin);
    }

    /** Adds (or, negative, takes away) tokens, as the Super Administrator signed in through {@link #loginAsSuperAdmin()}. */
    public static void adjustTokens(int clientId, int tokens, String reason) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tokens", tokens);
        body.put("reason", reason);
        Response resp = AdminApiClient.authedSpec().body(JsonUtil.toJsonObject(body))
                .post("/api/v1/admin/billing/clients/" + clientId + "/tokens/adjust");
        if (resp.statusCode() != 200) {
            throw new IllegalStateException("Fixture: token adjustment failed (" + resp.statusCode() + "): " + resp.asString());
        }
    }

    /** Registers a company through the public form (no sign-in), as a prepaid company on the Starter band or a postpaid one. */
    public static RegisteredCompany registerCompany(boolean postpaid) {
        String suffix = RandomData.uniqueSuffix();
        String name = "QA UI Registered " + suffix;
        String email = "qa.ui.registered." + suffix + "@example.com";
        String password = "A-long-QA-password-1";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("client_name", name);
        body.put("client_email", "qa.ui.company." + suffix + "@example.com");
        body.put("address", "Nairobi");
        body.put("country", "KENYA");
        if (postpaid) {
            body.put("billing_model", "POSTPAID");
            body.put("billing_period", "DAILY");
        } else {
            body.put("billing_model", "PREPAID");
            body.put("token_band_id", starterBandId());
        }
        body.put("full_names", "QA UI Registrant");
        body.put("email_address", email);
        body.put("msisdn", RandomData.uniqueMsisdn());
        body.put("password", password);

        Response resp = AdminApiClient.unauthSpec().body(JsonUtil.toJsonObject(body)).post("/api/v1/public/onboarding");
        if (resp.statusCode() != 201) {
            throw new IllegalStateException("Fixture: registration failed (" + resp.statusCode() + "): " + resp.asString());
        }
        String reference = resp.jsonPath().getString("reference");
        return new RegisteredCompany(Integer.parseInt(reference.substring(2)), name, reference, email, password);
    }

    private static int starterBandId() {
        Response resp = AdminApiClient.unauthSpec().get("/api/v1/public/onboarding/options");
        java.util.List<Integer> ids = resp.jsonPath().getList("bands.findAll { it.name == 'Starter' }.id", Integer.class);
        if (ids == null || ids.isEmpty()) {
            throw new IllegalStateException("Fixture: no Starter price band is open for registration: " + resp.asString());
        }
        return ids.get(0);
    }

    /** Price bands can't overlap and only 1 to 99 tokens is free, so a band a scenario created and never archived would block the next run. */
    public static void archiveLeftoverQaBands() {
        loginAsSuperAdmin();
        Response resp = AdminApiClient.authedSpec().get("/api/v1/admin/token-bands");
        java.util.List<Integer> ids = resp.jsonPath().getList("findAll { it.name.startsWith('QA UI band ') }.id", Integer.class);
        for (int id : ids) {
            AdminApiClient.authedSpec().body("{\"status\":\"ARCHIVED\"}").put("/api/v1/admin/token-bands/" + id);
        }
    }

    /** The signed-in user's API client id (user_mapId), for sending as that user through the public API. */
    public static int apiClientIdOf(ProvisionedUser user) {
        login(user.email(), user.password());
        Response resp = AdminApiClient.authedSpec().get("/api/v1/admin/api-credentials");
        if (resp.statusCode() != 200) {
            throw new IllegalStateException("Fixture: api-credentials failed (" + resp.statusCode() + "): " + resp.asString());
        }
        return resp.jsonPath().getInt("api_client_id");
    }

    /** One billed SMS as the user (X-Api-Client; the API under test runs with AUTH_TEST_MODE, which trusts the header). */
    public static void sendBilledSms(int apiClientId, String senderId, String to) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("to", to);
        body.put("message", "Sent by the UI suite");
        body.put("senderId", senderId);
        Response resp = AdminApiClient.baseSpec().contentType(io.restassured.http.ContentType.JSON)
                .header("X-Api-Client", String.valueOf(apiClientId)).body(JsonUtil.toJsonObject(body)).post("/api/v1/sms");
        if (resp.statusCode() != 202) {
            throw new IllegalStateException("Fixture: billed send failed (" + resp.statusCode() + "): " + resp.asString());
        }
    }

    /** Invoices every completed billing period of the client (what the scheduler does every minute), as the Super Administrator. */
    public static int generateInvoices(int clientId) {
        loginAsSuperAdmin();
        Response resp = AdminApiClient.authedSpec().queryParam("client_id", clientId).post("/api/v1/admin/invoices/generate");
        if (resp.statusCode() != 200) {
            throw new IllegalStateException("Fixture: invoice generation failed (" + resp.statusCode() + "): " + resp.asString());
        }
        return resp.jsonPath().getInt("issued");
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

    /** The client administrator asks to pay for tokens by cash, bank transfer or cheque: a PENDING request for a Super Administrator to confirm. */
    public static void requestTokensAs(ProvisionedUser clientAdmin, String amountKes) {
        login(clientAdmin.email(), clientAdmin.password());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("method", "OFFLINE");
        body.put("amount", amountKes);
        body.put("note", "QA UI purchase request");
        Response resp = AdminApiClient.authedSpec().body(JsonUtil.toJsonObject(body)).post("/api/v1/admin/tokens/purchases");
        if (resp.statusCode() != 201) {
            throw new IllegalStateException("Fixture: token purchase request failed (" + resp.statusCode() + "): " + resp.asString());
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
