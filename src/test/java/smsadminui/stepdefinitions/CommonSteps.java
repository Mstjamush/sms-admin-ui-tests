package smsadminui.stepdefinitions;

import io.cucumber.java.en.Then;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import smsadminui.utils.WebDriverFactory;

import java.time.Duration;

public class CommonSteps {

    private static final By PAGE_ERROR = By.cssSelector(".page > p.error");

    @Then("there should be no page error")
    public void thereShouldBeNoPageError() {
        boolean present = !WebDriverFactory.getDriver().findElements(PAGE_ERROR).isEmpty();
        Assert.assertFalse("Expected no error message on the page", present);
    }

    @Then("there should be a page error")
    public void thereShouldBeAPageError() {
        try {
            new WebDriverWait(WebDriverFactory.getDriver(), Duration.ofSeconds(10))
                    .until(driver -> !driver.findElements(PAGE_ERROR).isEmpty());
        } catch (TimeoutException e) {
            Assert.fail("Expected an error message on the page, but none appeared within 10s");
        }
    }

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
