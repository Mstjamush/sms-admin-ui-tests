package smsadminui.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Properties props = new Properties();

    static {
        try (InputStream is = Config.class.getClassLoader().getResourceAsStream("testrail.properties")) {
            props.load(is);
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Failed to load testrail.properties", e);
        }
    }

    public static String get(String key) {
        String fromEnv = System.getenv(toEnvVarName(key));
        if (fromEnv != null && !fromEnv.isBlank()) return fromEnv;

        String fromSystemProperty = System.getProperty(key);
        if (fromSystemProperty != null && !fromSystemProperty.isBlank()) return fromSystemProperty;

        return props.getProperty(key);
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key).trim());
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    private static String toEnvVarName(String key) {
        return key.toUpperCase().replace('.', '_');
    }
}
