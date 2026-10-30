package smsadminui.stepdefinitions;

import io.cucumber.java.en.Then;
import org.junit.Assert;
import smsadminui.pages.DashboardPage;
import smsadminui.pages.NavBar;
import smsadminui.utils.WebDriverFactory;

public class DashboardSteps {

    private DashboardPage page() {
        return new DashboardPage(WebDriverFactory.getDriver());
    }

    @Then("the top bar should name my client")
    public void theTopBarShouldNameMyClient() {
        String expectedClientName = TestContext.clientAdmin().client().clientName();
        String shown = new NavBar(WebDriverFactory.getDriver()).organisationText();
        Assert.assertTrue("Expected the top bar to name '" + expectedClientName + "', was: " + shown,
                shown.contains(expectedClientName));
    }

    @Then("the dashboard should greet me")
    public void theDashboardShouldGreetMe() {
        Assert.assertTrue("Dashboard heading: " + page().heading(), page().hasGreeting());
    }

    @Then("the dashboard should show the {string} figure")
    public void theDashboardShouldShowTheFigure(String label) {
        Assert.assertTrue("No '" + label + "' figure on the dashboard", page().hasFigure(label));
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
