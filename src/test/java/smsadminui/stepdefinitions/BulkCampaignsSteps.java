package smsadminui.stepdefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import smsadminui.pages.BulkCampaignsPage;
import smsadminui.utils.AdminFixtures;
import smsadminui.utils.RandomData;
import smsadminui.utils.WebDriverFactory;

public class BulkCampaignsSteps {

    private String campaignName;

    private BulkCampaignsPage page() {
        return new BulkCampaignsPage(WebDriverFactory.getDriver());
    }

    /** Creates the sender via the admin API directly (not the UI) - the Senders
     * screen already has its own coverage; this scenario is about campaigns. */
    @And("a sender has been provisioned for my client")
    public void aSenderHasBeenProvisionedForMyClient() {
        String shortCode = "QAUI" + RandomData.uniqueSuffix();
        AdminFixtures.provisionSenderFor(TestContext.clientAdmin().user(), shortCode);
    }

    @When("I open the Bulk Messages page")
    public void iOpenTheBulkMessagesPage() {
        page().open();
    }

    @Then("I should be prompted to create a sender first")
    public void iShouldBePromptedToCreateASenderFirst() {
        Assert.assertTrue(page().needsSenderMessageShown());
    }

    @When("I send a bulk campaign with a unique name to {string}")
    public void iSendABulkCampaignWithAUniqueNameTo(String recipients) {
        String suffix = RandomData.uniqueSuffix();
        campaignName = "QA UI Campaign " + suffix;

        BulkCampaignsPage page = page();
        page.fillInsertForm(campaignName, "Hello from the automated UI suite", recipients);
        page.submitInsertForm();
    }

    @When("I try to send a campaign with no recipients")
    public void iTryToSendACampaignWithNoRecipients() {
        campaignName = "QA UI Campaign " + RandomData.uniqueSuffix();
        BulkCampaignsPage page = page();
        // Name + message filled, recipients left blank - native `required` on
        // that textarea should block submission before Vue's handler runs.
        page.fillInsertForm(campaignName, "Hello from the automated UI suite", "");
        page.submitInsertForm();
    }

    @When("I switch to the Upload file tab")
    public void iSwitchToTheUploadFileTab() {
        page().switchToUploadTab();
    }

    @Then("the campaign I just created should appear in the campaign list")
    public void theCampaignIJustCreatedShouldAppearInTheCampaignList() {
        Assert.assertTrue("Expected to find campaign: " + campaignName, page().hasCampaignRow(campaignName));
    }

    @Then("no campaign should have been created")
    public void noCampaignShouldHaveBeenCreated() {
        Assert.assertTrue("Did not expect a campaign to be created: " + campaignName,
                page().neverShowsCampaignRow(campaignName));
    }

    @Then("a recipients file input should be shown")
    public void aRecipientsFileInputShouldBeShown() {
        Assert.assertTrue(page().hasFileInput());
    }
}
