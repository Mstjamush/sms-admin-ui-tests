package smsadminui.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import smsadminui.pages.UsersPage;
import smsadminui.utils.AdminFixtures;
import smsadminui.utils.RandomData;
import smsadminui.utils.WebDriverFactory;

public class UsersSteps {

    private String userEmail;
    private String foreignUserEmail;

    private UsersPage page() {
        return new UsersPage(WebDriverFactory.getDriver());
    }

    @Given("a user has been provisioned for a different client")
    public void aUserHasBeenProvisionedForADifferentClient() {
        AdminFixtures.ProvisionedClientAdmin otherAdmin = AdminFixtures.provisionClientAdministrator();
        foreignUserEmail = otherAdmin.user().email();
    }

    @When("I open the Users page")
    public void iOpenTheUsersPage() {
        page().open();
    }

    @When("I open the new user form")
    public void iOpenTheNewUserForm() {
        page().openNewUserForm();
    }

    @When("I create a new user with role {string}")
    public void iCreateANewUserWithRole(String roleName) {
        String suffix = RandomData.uniqueSuffix();
        userEmail = "qa.ui.user." + suffix + "@example.com";

        UsersPage page = page();
        page.openNewUserForm();
        page.fillNewUserForm("QA UI User", userEmail, "QaPass123!", RandomData.uniqueMsisdn(), roleName);
        page.submitNewUserForm();
    }

    @When("I try to create a new user with a short password")
    public void iTryToCreateANewUserWithAShortPassword() {
        UsersPage page = page();
        page.openNewUserForm();
        page.fillNewUserForm("QA UI User", "qa.ui.user." + RandomData.uniqueSuffix() + "@example.com",
                "abc12", RandomData.uniqueMsisdn(), "Sales Agent");
        page.submitNewUserForm();
    }

    @Then("the user I just created should appear in the user list")
    public void theUserIJustCreatedShouldAppearInTheUserList() {
        Assert.assertTrue("Expected to find user: " + userEmail, page().hasUserRow(userEmail));
    }

    @Then("there should be no new-user button on the page")
    public void thereShouldBeNoNewUserButtonOnThePage() {
        Assert.assertFalse(page().hasNewUserButton());
    }

    @Then("the new user form should still be open")
    public void theNewUserFormShouldStillBeOpen() {
        Assert.assertTrue(page().isNewUserFormOpen());
    }

    @Then("the role dropdown should not offer {string}")
    public void theRoleDropdownShouldNotOffer(String roleName) {
        Assert.assertFalse(page().availableRoleOptions().contains(roleName));
    }

    @Then("the user from the other client should not appear in the user list")
    public void theUserFromTheOtherClientShouldNotAppearInTheUserList() {
        Assert.assertTrue("Did not expect to see foreign user: " + foreignUserEmail,
                page().neverShowsUserRow(foreignUserEmail));
    }

    @Then("the user list should show a Client column")
    public void theUserListShouldShowAClientColumn() {
        Assert.assertTrue(page().hasClientColumn());
    }

    @Then("the user list should not show a Client column")
    public void theUserListShouldNotShowAClientColumn() {
        Assert.assertFalse(page().hasClientColumn());
    }
}
