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

    protected By fieldByLabel(String labelText) {
        return By.xpath(String.format(
                "//label[contains(normalize-space(.), %s)]/*[self::input or self::select or self::textarea]",
                xpathLiteral(labelText)));
    }

    protected List<WebElement> tableRows() {
        return findAll(TABLE_ROWS);
    }

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
