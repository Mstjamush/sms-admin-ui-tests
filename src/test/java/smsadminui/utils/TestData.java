package smsadminui.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

public final class TestData {

    private TestData() {
    }

    public static String path(String name) {
        URL url = TestData.class.getClassLoader().getResource("testdata/" + name);
        if (url == null) throw new IllegalStateException("Missing test resource: testdata/" + name);
        try {
            return Path.of(url.toURI()).toAbsolutePath().toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public static byte[] bytes(String name) {
        try (InputStream is = TestData.class.getClassLoader().getResourceAsStream("testdata/" + name)) {
            if (is == null) throw new IllegalStateException("Missing test resource: testdata/" + name);
            return is.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
