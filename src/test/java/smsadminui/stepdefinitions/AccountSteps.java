package smsadminui.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import smsadminui.pages.LoginPage;
import smsadminui.pages.UiKit;
import smsadminui.utils.AdminFixtures;
import smsadminui.utils.RandomData;
import smsadminui.utils.WebDriverFactory;

import java.time.Duration;

public class AccountSteps {

    private WebDriver driver() {
        return WebDriverFactory.getDriver();
    }

    private UiKit ui() {
        return new UiKit(driver());
    }

    private WebDriverWait waitUpTo(int seconds) {
        return new WebDriverWait(driver(), Duration.ofSeconds(seconds));
    }

    @When("I open the {string} page")
    public void iOpenThePage(String path) {
        ui().open(path);
    }

    @Then("the {string} tile should read {string}")
    public void theTileShouldRead(String label, String value) {
        waitUpTo(10).until(d -> value.equals(ui().tileValue(label)));
    }

    @Then("the balance chip should show {string}")
    public void theBalanceChipShouldShow(String text) {
        waitUpTo(10).until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".balance-chip"), text));
    }

    @Then("a success message should say {string}")
    public void aSuccessMessageShouldSay(String text) {
        waitUpTo(10).until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".toast.success .text"), text));
    }

    @Then("the daily breakdown should have {int} rows")
    public void theDailyBreakdownShouldHaveRows(int rows) {
        waitUpTo(10).until(d -> d.findElements(By.cssSelector(".daily tbody tr")).size() == rows);
    }

    @Then("my API client id should be shown and the token revealable")
    public void myApiClientIdShouldBeShown() {
        String clientId = waitUpTo(10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".kv dd code"))).getText();
        Assert.assertTrue("client id: " + clientId, clientId.matches("\\d+"));
        driver().findElement(By.cssSelector("button[aria-label='Show token']")).click();
        String token = driver().findElement(By.cssSelector("code.token")).getText();
        Assert.assertEquals("token length", 64, token.length());
    }

    @Then("the signing guide should describe {string} and show an example using {string}")
    public void theSigningGuideShouldDescribe(String scheme, String code) {
        String steps = waitUpTo(10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ol.steps"))).getText();
        Assert.assertTrue("Signing steps: " + steps, steps.contains(scheme));
        Assert.assertFalse("Signing steps still describe X-Api-Key: " + steps, steps.contains("X-Api-Key"));
        String example = driver().findElement(By.cssSelector("pre.code")).getText();
        Assert.assertTrue("Example: " + example, example.contains(code));
    }

    @When("I change my password through the Password link")
    public void iChangeMyPasswordThroughThePasswordLink() {
        String current = TestContext.clientAdmin().user().password();
        String next = "QA-UI-" + java.util.UUID.randomUUID();
        ScenarioData.put("newPassword", next);
        waitUpTo(10).until(ExpectedConditions.elementToBeClickable(By.xpath("//a[normalize-space(.)='Password']"))).click();
        waitUpTo(10).until(ExpectedConditions.urlContains("/change-password"));
        java.util.List<org.openqa.selenium.WebElement> fields = driver().findElements(By.cssSelector("form input[type=password]"));
        fields.get(0).sendKeys(current);
        fields.get(1).sendKeys(next);
        fields.get(2).sendKeys(next);
        driver().findElement(By.cssSelector("form button[type=submit]")).click();
        waitUpTo(15).until(ExpectedConditions.urlContains("/dashboard"));
    }

    @Then("I can log out and sign in again with the new password")
    public void iCanLogOutAndSignInWithTheNewPassword() {
        driver().findElement(By.xpath("//button[normalize-space(.)='Log out']")).click();
        smsadminui.pages.LoginPage login = new smsadminui.pages.LoginPage(driver()).open();
        login.loginAs(TestContext.clientAdmin().user().email(), ScenarioData.<String>get("newPassword"));
        login.waitForUrlToContain("/dashboard");
    }

    @Then("the nav bar should show a {string} link")
    public void theNavBarShouldShowALink(String label) {
        Assert.assertFalse(label, driver().findElements(By.xpath("//nav//a[normalize-space(.)='" + label + "']")).isEmpty());
    }

    @Then("the nav bar should not show a {string} link")
    public void theNavBarShouldNotShowALink(String label) {
        Assert.assertTrue(label, driver().findElements(By.xpath("//nav//a[normalize-space(.)='" + label + "']")).isEmpty());
    }
}
