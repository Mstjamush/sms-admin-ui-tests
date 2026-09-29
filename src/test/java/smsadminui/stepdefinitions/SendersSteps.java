package smsadminui.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import smsadminui.pages.SendersPage;
import smsadminui.utils.AdminFixtures;
import smsadminui.utils.RandomData;
import smsadminui.utils.WebDriverFactory;

public class SendersSteps {

    private String shortCode;
    private String foreignShortCode;

    private SendersPage page() {
        return new SendersPage(WebDriverFactory.getDriver());
    }

    @Given("a sender has been provisioned for a different client")
    public void aSenderHasBeenProvisionedForADifferentClient() {
        AdminFixtures.ProvisionedClientAdmin otherAdmin = AdminFixtures.provisionClientAdministrator();
        foreignShortCode = "QAUI" + RandomData.uniqueSuffix();
        AdminFixtures.provisionSenderFor(otherAdmin.user(), foreignShortCode);
    }

    @When("I open the Senders page")
    public void iOpenTheSendersPage() {
        page().open();
    }

    @When("I open the new sender form")
    public void iOpenTheNewSenderForm() {
        page().openNewSenderForm();
    }

    @When("I cancel the new sender form")
    public void iCancelTheNewSenderForm() {
        page().closeNewSenderForm();
    }

    @When("I create a new sender with a unique short code")
    public void iCreateANewSenderWithAUniqueShortCode() {
        shortCode = "QAUI" + RandomData.uniqueSuffix();
        SendersPage page = page();
        page.openNewSenderForm();
        page.fillShortCode(shortCode);
        page.submit();
    }

    @When("I try to create a new sender with no short code")
    public void iTryToCreateANewSenderWithNoShortCode() {
        SendersPage page = page();
        page.openNewSenderForm();
        page.submit();
    }

    @Then("the sender I just created should appear in the sender list")
    public void theSenderIJustCreatedShouldAppearInTheSenderList() {
        Assert.assertTrue("Expected to find sender: " + shortCode, page().hasSenderRow(shortCode));
    }

    @Then("its approval status should read {string}")
    public void itsApprovalStatusShouldRead(String expected) {
        Assert.assertEquals(expected, page().approvalStatusFor(shortCode));
    }

    @Then("the new sender form should still be open")
    public void theNewSenderFormShouldStillBeOpen() {
        Assert.assertTrue(page().isNewSenderFormOpen());
    }

    @Then("the new sender form should be closed")
    public void theNewSenderFormShouldBeClosed() {
        Assert.assertFalse(page().isNewSenderFormOpen());
    }

    @Then("the sender from the other client should not appear in the sender list")
    public void theSenderFromTheOtherClientShouldNotAppearInTheSenderList() {
        Assert.assertTrue("Did not expect to see foreign sender: " + foreignShortCode,
                page().neverShowsSenderRow(foreignShortCode));
    }
}
