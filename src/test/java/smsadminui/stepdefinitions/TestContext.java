package smsadminui.stepdefinitions;

import smsadminui.utils.AdminFixtures;

/**
 * Scenario-scoped memory of fixtures provisioned earlier in the same
 * scenario - lets steps in different classes (nav login, senders, bulk
 * campaigns) chain off state set up by an earlier step, the same way a page
 * object's own fields carry state within one class.
 */
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
