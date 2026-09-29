package smsadminui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import smsadminui.config.Config;

import java.time.Duration;
import java.util.List;

/**
 * Shared waits/locators for every page object. sms-admin-ui has no
 * data-testid attributes, but every form consistently follows
 * `&lt;label&gt;Field name &lt;input/&gt;&lt;/label&gt;` (see fieldByLabel) and every admin
 * screen wraps content in `.page` with an inline `p.error` for API failures
 * and a `table.card` for its list - both captured here once instead of
 * per page.
 */
public abstract class BasePage {

    private static final By PAGE_ERROR = By.cssSelector(".page > p.error");
    private static final By TABLE_ROWS = By.cssSelector("table.card tbody tr");

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    protected void navigateTo(String path) {
        driver.get(Config.get("ui.base.url") + path);
    }

    protected WebElement find(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected List<WebElement> findAll(By locator) {
        return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void type(By locator, String text) {
        WebElement el = find(locator);
        el.clear();
        el.sendKeys(text);
    }

    protected boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    /** The input/select/textarea that's a direct child of a &lt;label&gt; containing this text. */
    protected By fieldByLabel(String labelText) {
        return By.xpath(String.format(
                "//label[contains(normalize-space(.), %s)]/*[self::input or self::select or self::textarea]",
                xpathLiteral(labelText)));
    }

    protected List<WebElement> tableRows() {
        return findAll(TABLE_ROWS);
    }

    /**
     * Waits for a table row containing this text to appear, tolerating
     * StaleElementReferenceException along the way. A one-shot
     * tableRows().stream().anyMatch(...) races Vue's re-render after the
     * list's async reload (POST completes -> load() re-fetches -> DOM
     * replaced) - checking too early misses a row that hasn't landed yet,
     * and checking mid-re-render can throw stale-element on elements the
     * previous render already replaced. This absorbs both.
     */
    protected boolean waitForTableRowContaining(String text) {
        try {
            return wait.until(driver -> {
                try {
                    return tableRows().stream().anyMatch(row -> row.getText().contains(text));
                } catch (StaleElementReferenceException e) {
                    return false;
                }
            });
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** The negative-assertion counterpart - waits out the same settling window,
     * then confirms the row never appeared, rather than racing a single
     * instant check against ongoing re-renders. */
    protected boolean tableNeverGetsRowContaining(String text, Duration settleTime) {
        long deadline = System.currentTimeMillis() + settleTime.toMillis();
        while (System.currentTimeMillis() < deadline) {
            try {
                if (tableRows().stream().anyMatch(row -> row.getText().contains(text))) return false;
            } catch (StaleElementReferenceException ignored) {
                // mid re-render - fall through and retry
            }
        }
        return true;
    }

    public boolean hasPageError() {
        return isPresent(PAGE_ERROR);
    }

    public String pageErrorText() {
        return find(PAGE_ERROR).getText();
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    public void waitForUrlToContain(String fragment) {
        wait.until(ExpectedConditions.urlContains(fragment));
    }

    /** Safe XPath string literal even when the value itself contains a single quote. */
    private static String xpathLiteral(String value) {
        if (!value.contains("'")) return "'" + value + "'";
        if (!value.contains("\"")) return "\"" + value + "\"";
        String[] parts = value.split("'");
        StringBuilder sb = new StringBuilder("concat(");
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(", \"'\", ");
            sb.append("'").append(parts[i]).append("'");
        }
        return sb.append(")").toString();
    }
}
