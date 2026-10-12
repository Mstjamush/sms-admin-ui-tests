package smsadminui.utils;

import smsadminui.config.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * The one place this suite touches the API's database. Billing periods end at
 * midnight (or at the end of the week or month), and no endpoint can move the
 * clock, so a scenario that needs an invoice ages the usage it has just made:
 * its period is then complete, and POST /invoices/generate (what the scheduler
 * does every minute) bills it. Nothing else here reads or writes state.
 */
public final class Db {

    private Db() {
    }

    private static String url() {
        return "jdbc:mysql://" + Config.get("db.host") + ":" + Config.get("db.port") + "/" + Config.get("db.name")
                + "?useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=3000";
    }

    /** True when the configured database accepts a connection (decides whether the invoice scenarios run or skip). */
    public static boolean reachable() {
        try (Connection ignored = connect()) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(url(), Config.get("db.user"), Config.get("db.password"));
    }

    /**
     * Moves every not-yet-invoiced usage row of the client {@code days} days into the past, so its period is complete, and
     * forgets how far the client has been invoiced: a running scheduler may already have moved that cursor to the start of
     * the current period, which would leave the aged usage for the next invoice instead of this one.
     */
    public static int ageUsage(int clientId, int days) {
        String age = "UPDATE billing_usage SET created_at = created_at - INTERVAL ? DAY WHERE client_id = ? AND invoice_id IS NULL";
        String rewind = "UPDATE client_billing_settings SET invoiced_through = NULL WHERE client_id = ?";
        try (Connection c = connect()) {
            int rows;
            try (PreparedStatement ps = c.prepareStatement(age)) {
                ps.setInt(1, days);
                ps.setInt(2, clientId);
                rows = ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(rewind)) {
                ps.setInt(1, clientId);
                ps.executeUpdate();
            }
            return rows;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not age the usage of client " + clientId + ": " + e.getMessage(), e);
        }
    }
}
