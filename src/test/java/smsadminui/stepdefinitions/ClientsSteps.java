package smsadminui.stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import smsadminui.pages.ClientsPage;
import smsadminui.utils.RandomData;
import smsadminui.utils.WebDriverFactory;

public class ClientsSteps {

    private String clientName;

    private ClientsPage page() {
        return new ClientsPage(WebDriverFactory.getDriver());
    }

    @When("I open the Clients page")
    public void iOpenTheClientsPage() {
        page().open();
    }

    @When("I open the new client form")
    public void iOpenTheNewClientForm() {
        page().openNewClientForm();
    }

    @When("I cancel the new client form")
    public void iCancelTheNewClientForm() {
        page().closeNewClientForm();
    }

    @When("I create a new client with a unique name")
    public void iCreateANewClientWithAUniqueName() {
        String suffix = RandomData.uniqueSuffix();
        clientName = "QA UI Client " + suffix;

        ClientsPage page = page();
        page.openNewClientForm();
        page.fillNewClientForm(clientName, "qa.ui.client." + suffix + "@example.com");
        page.submitNewClientForm();
    }

    @When("I try to create a new client with no name")
    public void iTryToCreateANewClientWithNoName() {
        ClientsPage page = page();
        page.openNewClientForm();
        page.fillNewClientForm("", "qa.ui.client." + RandomData.uniqueSuffix() + "@example.com");
        page.submitNewClientForm();
    }

    /** .test is a syntactically-valid-enough TLD for the browser's native
     * type=email check to accept, but bulksms-api's email-validator rejects it
     * as a reserved TLD (RFC 2606) - see ClientCreateRequest.client_email -
     * so this exercises the backend's own validation via the UI, not just the
     * browser's client-side format check (already covered by the empty-name test). */
    @When("I try to create a new client with an email the backend will reject")
    public void iTryToCreateANewClientWithAnEmailTheBackendWillReject() {
        String suffix = RandomData.uniqueSuffix();
        ClientsPage page = page();
        page.openNewClientForm();
        page.fillNewClientForm("QA UI Client " + suffix, "qa.ui.client." + suffix + "@example.test");
        page.submitNewClientForm();
    }

    @When("I create a Client Administrator for the client I just created")
    public void iCreateAClientAdministratorForTheClientIJustCreated() {
        String suffix = RandomData.uniqueSuffix();
        ClientsPage page = page();
        page.openClientAdminFormFor(clientName);
        page.fillClientAdminForm(
                "QA UI Admin", "qa.ui.admin." + suffix + "@example.com", "QaPass123!", RandomData.uniqueMsisdn());
        page.submitClientAdminForm();
    }

    @When("I try to create a Client Administrator with a short password")
    public void iTryToCreateAClientAdministratorWithAShortPassword() {
        ClientsPage page = page();
        page.openClientAdminFormFor(clientName);
        page.fillClientAdminForm("QA UI Admin", "qa.ui.admin." + RandomData.uniqueSuffix() + "@example.com",
                "abc12", RandomData.uniqueMsisdn());
        page.submitClientAdminForm();
    }

    @Then("the client I just created should appear in the client list")
    public void theClientIJustCreatedShouldAppearInTheClientList() {
        Assert.assertTrue("Expected to find client: " + clientName, page().hasClientRow(clientName));
    }

    @Then("the new client form should still be open")
    public void theNewClientFormShouldStillBeOpen() {
        Assert.assertTrue(page().isNewClientFormOpen());
    }

    @Then("the new client form should be closed")
    public void theNewClientFormShouldBeClosed() {
        Assert.assertFalse(page().isNewClientFormOpen());
    }

    @Then("the Client Administrator form should still be open")
    public void theClientAdministratorFormShouldStillBeOpen() {
        Assert.assertTrue(page().isClientAdminFormOpen());
    }
}
