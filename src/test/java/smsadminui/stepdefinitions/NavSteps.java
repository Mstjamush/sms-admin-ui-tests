package smsadminui.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import smsadminui.config.Config;
import smsadminui.pages.LoginPage;
import smsadminui.pages.NavBar;
import smsadminui.utils.AdminFixtures;
import smsadminui.utils.WebDriverFactory;

public class NavSteps {

    private NavBar navBar() {
        return new NavBar(WebDriverFactory.getDriver());
    }

    @Given("I am logged in as a newly provisioned client administrator")
    public void iAmLoggedInAsANewlyProvisionedClientAdministrator() {
        AdminFixtures.ProvisionedClientAdmin admin = AdminFixtures.provisionClientAdministrator();
        TestContext.setClientAdmin(admin);

        LoginPage loginPage = new LoginPage(WebDriverFactory.getDriver()).open();
        loginPage.loginAs(admin.user().email(), admin.user().password());
        loginPage.waitForUrlToContain("/dashboard");
    }

    @Then("the nav bar should show a Clients link")
    public void theNavBarShouldShowAClientsLink() {
        Assert.assertTrue(navBar().hasClientsLink());
    }

    @Then("the nav bar should not show a Clients link")
    public void theNavBarShouldNotShowAClientsLink() {
        Assert.assertFalse(navBar().hasClientsLink());
    }

    @Then("the nav bar should show a Senders link")
    public void theNavBarShouldShowASendersLink() {
        Assert.assertTrue(navBar().hasSendersLink());
    }

    @Then("the nav bar should show a Bulk Messages link")
    public void theNavBarShouldShowABulkMessagesLink() {
        Assert.assertTrue(navBar().hasBulkMessagesLink());
    }

    @Then("the nav bar should show a Users link")
    public void theNavBarShouldShowAUsersLink() {
        Assert.assertTrue(navBar().hasUsersLink());
    }

    @Then("the nav bar role badge should read {string}")
    public void theNavBarRoleBadgeShouldRead(String expected) {
        Assert.assertEquals(expected, navBar().roleBadgeText());
    }

    @When("I log out")
    public void iLogOut() {
        NavBar nav = navBar();
        nav.logout();
        nav.waitForUrlToContain("/login");
    }

    @When("I visit the dashboard directly")
    public void iVisitTheDashboardDirectly() {
        WebDriverFactory.getDriver().get(Config.get("ui.base.url") + "/dashboard");
    }

    @When("I visit the Clients page directly by URL")
    public void iVisitTheClientsPageDirectlyByUrl() {
        WebDriverFactory.getDriver().get(Config.get("ui.base.url") + "/clients");
    }

    @When("I visit the Senders page directly by URL while logged out")
    public void iVisitTheSendersPageDirectlyByUrlWhileLoggedOut() {
        WebDriverFactory.getDriver().get(Config.get("ui.base.url") + "/senders");
    }

    @When("I click the Clients nav link")
    public void iClickTheClientsNavLink() {
        navBar().goToClients();
    }

    @When("I click the Senders nav link")
    public void iClickTheSendersNavLink() {
        navBar().goToSenders();
    }

    @When("I click the Bulk Messages nav link")
    public void iClickTheBulkMessagesNavLink() {
        navBar().goToBulkMessages();
    }

    @When("I click the Users nav link")
    public void iClickTheUsersNavLink() {
        navBar().goToUsers();
    }
}
