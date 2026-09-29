package smsadminui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage extends BasePage {

    private static final By SUBMIT = By.cssSelector("form.login-card button[type=submit]");
    private static final By ERROR = By.cssSelector(".login-card p.error");
    private static final By HEADING = By.cssSelector(".login-card h1");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open() {
        navigateTo("/login");
        find(HEADING);
        return this;
    }

    public void loginAs(String email, String password) {
        type(fieldByLabel("Email"), email);
        type(fieldByLabel("Password"), password);
        click(SUBMIT);
    }

    /** Clicks submit with both fields left blank - native `required` validation
     * should block the form from ever reaching Vue's submit handler. */
    public void submitEmpty() {
        click(SUBMIT);
    }

    /** Waits up to the full timeout for the (async) login failure to render - use
     * after an attempt expected to fail. */
    public boolean hasError() {
        return hasErrorWithin(Duration.ofSeconds(10));
    }

    /** Short wait for asserting an error's *absence* after an action that never
     * calls the API at all (e.g. a blocked empty-field submit) - avoids paying
     * the full 10s timeout for a negative check with nothing to wait for. */
    public boolean hasErrorQuickCheck() {
        return hasErrorWithin(Duration.ofSeconds(2));
    }

    private boolean hasErrorWithin(Duration timeout) {
        try {
            new WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOfElementLocated(ERROR));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public String errorText() {
        return find(ERROR).getText();
    }
}
