package smsadminui.stepdefinitions;

import io.cucumber.java.en.Then;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import smsadminui.utils.WebDriverFactory;

import java.time.Duration;

/** Assertions shared across every admin screen (Clients/Senders/Users/Bulk Campaigns),
 * which all follow the same `.page` wrapper markup - see BasePage. */
public class CommonSteps {

    private static final By PAGE_ERROR = By.cssSelector(".page > p.error");

    @Then("there should be no page error")
    public void thereShouldBeNoPageError() {
        boolean present = !WebDriverFactory.getDriver().findElements(PAGE_ERROR).isEmpty();
        Assert.assertFalse("Expected no error message on the page", present);
    }

    /** Waits for the (async) API failure to render, rather than racing it -
     * a plain instant check would be flaky here. */
    @Then("there should be a page error")
    public void thereShouldBeAPageError() {
        try {
            new WebDriverWait(WebDriverFactory.getDriver(), Duration.ofSeconds(10))
                    .until(driver -> !driver.findElements(PAGE_ERROR).isEmpty());
        } catch (TimeoutException e) {
            Assert.fail("Expected an error message on the page, but none appeared within 10s");
        }
    }

    /** Waits rather than checking instantly - sms-admin-ui's routes are
     * lazy-loaded (`component: () => import(...)`), so back-to-back
     * client-side navigations (no full page reload) can have a brief gap
     * before the URL actually updates, especially the first time a given
     * route's chunk loads in a session. An instant check races that. */
    @Then("the page URL should contain {string}")
    public void thePageUrlShouldContain(String fragment) {
        try {
            new WebDriverWait(WebDriverFactory.getDriver(), Duration.ofSeconds(10))
                    .until(ExpectedConditions.urlContains(fragment));
        } catch (TimeoutException e) {
            String url = WebDriverFactory.getDriver().getCurrentUrl();
            Assert.fail("Expected URL to contain '" + fragment + "' within 10s, was: " + url);
        }
    }
}
