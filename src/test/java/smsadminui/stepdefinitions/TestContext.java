package smsadminui.stepdefinitions;

import smsadminui.utils.AdminFixtures;

public class TestContext {

    private static final ThreadLocal<AdminFixtures.ProvisionedClientAdmin> CLIENT_ADMIN = new ThreadLocal<>();

    private TestContext() {
    }

    public static void setClientAdmin(AdminFixtures.ProvisionedClientAdmin admin) {
        CLIENT_ADMIN.set(admin);
    }

    public static AdminFixtures.ProvisionedClientAdmin clientAdmin() {
        AdminFixtures.ProvisionedClientAdmin admin = CLIENT_ADMIN.get();
        if (admin == null) throw new IllegalStateException("No client administrator provisioned in this scenario yet");
        return admin;
    }

    public static void clear() {
        CLIENT_ADMIN.remove();
    }
}
