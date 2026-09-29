package smsadminui.stepdefinitions;

import io.cucumber.java.en.Then;
import org.junit.Assert;
import smsadminui.pages.DashboardPage;
import smsadminui.utils.WebDriverFactory;

public class DashboardSteps {

    private DashboardPage page() {
        return new DashboardPage(WebDriverFactory.getDriver());
    }

    @Then("the dashboard subtitle should mention my client")
    public void theDashboardSubtitleShouldMentionMyClient() {
        String expectedClientName = TestContext.clientAdmin().client().clientName();
        String subtitle = page().subtitleText();
        Assert.assertTrue("Expected subtitle to mention '" + expectedClientName + "', was: " + subtitle,
                subtitle.contains(expectedClientName));
    }

    @Then("the dashboard should show a Clients card")
    public void theDashboardShouldShowAClientsCard() {
        Assert.assertTrue(page().hasClientsCard());
    }

    @Then("the dashboard should not show a Clients card")
    public void theDashboardShouldNotShowAClientsCard() {
        Assert.assertFalse(page().hasClientsCard());
    }
}
