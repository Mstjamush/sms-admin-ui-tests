package smsadminui.stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import smsadminui.config.Config;
import smsadminui.utils.WebDriverFactory;

import java.time.Duration;

/** The pages anyone can open (the landing page and pricing) and the English / French switch. */
public class LandingSteps {

    private WebDriver driver() {
        return WebDriverFactory.getDriver();
    }

    private WebDriverWait waitUpTo(int seconds) {
        return new WebDriverWait(driver(), Duration.ofSeconds(seconds));
    }

    @When("I open the landing page")
    public void iOpenTheLandingPage() {
        driver().get(Config.get("ui.base.url") + "/");
        waitUpTo(10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("main h1")));
    }

    @When("I visit the landing page directly")
    public void iVisitTheLandingPageDirectly() {
        driver().get(Config.get("ui.base.url") + "/");
    }

    @When("I follow the {string} link")
    public void iFollowTheLink(String text) {
        waitUpTo(10).until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[normalize-space(.)=" + literal(text) + "]"))).click();
    }

    @When("I switch the language to {string}")
    public void iSwitchTheLanguageTo(String label) {
        waitUpTo(10).until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'lang')]//button[normalize-space(.)=" + literal(label) + "]"))).click();
    }

    @Then("the page headline should read {string}")
    public void thePageHeadlineShouldRead(String headline) {
        // A two-line headline ("Send once." / "Know who got it.") is compared as one sentence.
        waitUpTo(10).until(d -> headline.equals(d.findElement(By.cssSelector("main h1")).getText().replaceAll("\\s+", " ")));
    }

    @Then("the page should offer {string}")
    public void thePageShouldOffer(String text) {
        waitUpTo(10).until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//a[normalize-space(.)=" + literal(text) + "]")));
    }

    @Then("the example campaign should show {string}")
    public void theExampleCampaignShouldShow(String text) {
        String shown = waitUpTo(10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("main figure"))).getText();
        Assert.assertTrue("Example campaign: " + shown, shown.contains(text));
    }

    private static String literal(String value) {
        return value.contains("'") ? "\"" + value + "\"" : "'" + value + "'";
    }
}
