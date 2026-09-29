package smsadminui.utils;

import org.jboss.logging.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import smsadminui.config.Config;

import java.time.Duration;

/**
 * One Chrome instance per test thread (Selenium Manager - built into
 * Selenium 4.6+ - resolves a matching chromedriver automatically, no
 * separate driver-management dependency needed). Headless by default
 * (browser.headless in testrail.properties); set it to false while writing
 * or debugging a scenario to watch it run.
 */
public final class WebDriverFactory {

    private static final Logger LOG = Logger.getLogger(WebDriverFactory.class);
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private WebDriverFactory() {
    }

    public static WebDriver getDriver() {
        if (DRIVER.get() == null) {
            boolean headless = Config.getBoolean("browser.headless");
            LOG.infof("Starting Chrome (headless=%b)", headless);

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--window-size=1440,1000");
            if (headless) {
                options.addArguments("--headless=new");
            }
            // Sandboxed/CI environments commonly need these to launch at all.
            options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu");

            WebDriver driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(Duration.ZERO); // explicit waits only - see BasePage
            DRIVER.set(driver);
        }
        return DRIVER.get();
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            DRIVER.remove();
        }
    }
}
