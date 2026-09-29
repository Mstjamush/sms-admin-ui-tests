package smsadminui.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import smsadminui.config.Config;
import smsadminui.pages.DashboardPage;
import smsadminui.pages.LoginPage;
import smsadminui.utils.WebDriverFactory;

public class LoginSteps {

    private LoginPage page() {
        return new LoginPage(WebDriverFactory.getDriver());
    }

    @Given("I am on the login page")
    public void iAmOnTheLoginPage() {
        page().open();
    }

    @When("I log in as the configured super admin")
    public void iLogInAsTheConfiguredSuperAdmin() {
        page().loginAs(Config.get("admin.super_admin.email"), Config.get("admin.super_admin.password"));
    }

    @When("I log in with email {string} and password {string}")
    public void iLogInWithEmailAndPassword(String email, String password) {
        page().loginAs(email, password);
    }

    @When("I submit the login form with both fields empty")
    public void iSubmitTheLoginFormWithBothFieldsEmpty() {
        page().submitEmpty();
    }

    @Then("I should be on the dashboard")
    public void iShouldBeOnTheDashboard() {
        page().waitForUrlToContain("/dashboard");
    }

    @Then("I should be redirected to {string}")
    public void iShouldBeRedirectedTo(String path) {
        page().waitForUrlToContain(path);
    }

    @Then("the dashboard should welcome {string}")
    public void theDashboardShouldWelcome(String namePart) {
        DashboardPage dashboard = new DashboardPage(WebDriverFactory.getDriver());
        Assert.assertTrue("Expected dashboard heading to contain '" + namePart + "', was: " + dashboard.heading(),
                dashboard.heading().contains(namePart));
    }

    @Then("I should see a login error")
    public void iShouldSeeALoginError() {
        Assert.assertTrue("Expected a login error message", page().hasError());
    }

    @Then("I should not see a login error")
    public void iShouldNotSeeALoginError() {
        Assert.assertFalse("Expected no login error message", page().hasErrorQuickCheck());
    }

    @Then("I should still be on the login page")
    public void iShouldStillBeOnTheLoginPage() {
        // Waits rather than checking instantly - covers both "never left" (empty-
        // field submit) and "redirected back" (route guard after a direct/logged-out
        // navigation), which takes a moment to land.
        page().waitForUrlToContain("/login");
    }
}
