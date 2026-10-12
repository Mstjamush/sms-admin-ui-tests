package smsadminui.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.junit.Assume;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import smsadminui.config.Config;
import smsadminui.pages.LoginPage;
import smsadminui.pages.UiKit;
import smsadminui.utils.AdminFixtures;
import smsadminui.utils.Db;
import smsadminui.utils.RandomData;
import smsadminui.utils.WebDriverFactory;

import java.time.Duration;

/**
 * Billing through the screens. A prepaid client buys tokens (1 token = 1 SMS page) on Billing and asks to pay by cash, bank
 * transfer or cheque; a Super Administrator confirms it on Payments, adjusts tokens on the client's page, and keeps the price
 * bands. A postpaid client sees its bill and, once an invoice is issued, Invoices (where a Super Administrator changes the
 * status). M-Pesa itself is not driven: it calls Safaricom.
 */
public class BillingSteps {

    private WebDriver driver() {
        return WebDriverFactory.getDriver();
    }

    private UiKit ui() {
        return new UiKit(driver());
    }

    /** Tables re-render as data arrives, so a row read mid-update is stale: keep polling instead of failing. */
    private FluentWait<WebDriver> waitUpTo(int seconds) {
        return new WebDriverWait(driver(), Duration.ofSeconds(seconds)).ignoring(StaleElementReferenceException.class);
    }

    private AdminFixtures.ProvisionedClientAdmin company() {
        return TestContext.clientAdmin();
    }

    // ---------------------------------------------------------------- who is signed in

    @Given("I am logged in as a newly provisioned postpaid client administrator")
    public void iAmLoggedInAsAPostpaidClientAdministrator() {
        AdminFixtures.ProvisionedClientAdmin admin = AdminFixtures.provisionPostpaidClientAdministrator();
        TestContext.setClientAdmin(admin);
        signIn(admin.user().email(), admin.user().password());
    }

    @Given("I am logged in as the super admin with a newly provisioned postpaid client")
    public void iAmLoggedInAsTheSuperAdminWithAPostpaidClient() {
        TestContext.setClientAdmin(AdminFixtures.provisionPostpaidClientAdministrator());
        signInAsSuperAdmin();
    }

    @Given("I am logged in as the super admin with a newly provisioned client")
    public void iAmLoggedInAsTheSuperAdminWithANewClient() {
        TestContext.setClientAdmin(AdminFixtures.provisionClientAdministrator());
        signInAsSuperAdmin();
    }

    @Given("I am logged in as the super admin")
    public void iAmLoggedInAsTheSuperAdmin() {
        signInAsSuperAdmin();
    }

    private void signInAsSuperAdmin() {
        signIn(Config.get("admin.super_admin.email"), Config.get("admin.super_admin.password"));
    }

    private void signIn(String email, String password) {
        LoginPage login = new LoginPage(driver()).open();
        login.loginAs(email, password);
        login.waitForUrlToContain("/dashboard");
    }

    // ---------------------------------------------------------------- a client buys tokens

    @When("I ask to buy KES {string} of tokens by cash, bank or cheque from the Billing page")
    public void iAskToBuyTokensOffline(String amount) {
        UiKit ui = ui();
        ui.open("/billing");
        ui.clickButton("Buy tokens");
        // With M-Pesa set up on the API the dialog offers tabs and starts on the phone payment: pick the offline one.
        By offlineTab = By.xpath("//*[@role='dialog']//button[@role='tab'][normalize-space(.)='Cash, bank or cheque']");
        if (!driver().findElements(offlineTab).isEmpty()) driver().findElement(offlineTab).click();
        ui.typeIntoModal("Amount to pay", amount);
        ui.clickModalButton("Send request");
        ui.waitForModalToClose();
    }

    @Then("my purchases should include a waiting request for {string}")
    public void myPurchasesShouldIncludeAWaitingRequest(String amount) {
        By firstRow = By.xpath("//h2[normalize-space(.)='Your purchases']/ancestor::div[contains(concat(' ', normalize-space(@class), ' '), ' card ')][1]//tbody/tr[1]");
        waitUpTo(10).until(d -> {
            for (WebElement row : d.findElements(firstRow)) {
                if (row.getText().contains(amount) && row.getText().contains("Waiting")) return true;
            }
            return false;
        });
    }

    @When("I look at the Billing page")
    public void iLookAtTheBillingPage() {
        ui().open("/billing");
    }

    // ---------------------------------------------------------------- a Super Administrator and a client's tokens

    @Given("that client has asked to pay KES {string} for tokens")
    public void thatClientHasAskedToPay(String amount) {
        AdminFixtures.requestTokensAs(company().user(), amount);
    }

    @When("I open that client's page")
    public void iOpenThatClientsPage() {
        ui().open("/clients/" + company().client().clientId());
        waitUpTo(10).until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".page h1"), company().client().clientName()));
    }

    @When("I add {string} tokens to that client with the reason {string}")
    public void iAddTokensToThatClient(String tokens, String reason) {
        UiKit ui = ui();
        ui.clickButton("Adjust tokens");
        ui.typeIntoModal("Tokens", tokens);
        ui.typeIntoModal("Reason", reason);
        ui.clickModalButton("Save");
        ui.waitForModalToClose();
    }

    @When("I confirm that client's request to pay on the Payments page")
    public void iConfirmThatClientsRequest() {
        UiKit ui = ui();
        ui.open("/payments");
        String client = company().client().clientName();
        By confirm = By.xpath("//tr[.//td[contains(normalize-space(.), '" + client + "')]]//button[normalize-space(.)='Confirm received']");
        waitUpTo(15).until(ExpectedConditions.elementToBeClickable(confirm)).click();
        ui.clickModalButton("Money received: add tokens");
        ui.waitForModalToClose();
    }

    @When("I reject that client's request to pay on the Payments page")
    public void iRejectThatClientsRequest() {
        UiKit ui = ui();
        ui.open("/payments");
        String client = company().client().clientName();
        By reject = By.xpath("//tr[.//td[contains(normalize-space(.), '" + client + "')]]//button[normalize-space(.)='Reject']");
        waitUpTo(15).until(ExpectedConditions.elementToBeClickable(reject)).click();
    }

    @Then("the Payments page should list no waiting request from that client")
    public void thePaymentsPageShouldListNoWaitingRequest() {
        String client = company().client().clientName();
        ui().open("/payments");
        waitUpTo(10).until(d -> d.findElements(By.xpath("//tr[.//td[contains(normalize-space(.), '" + client + "')]]//button[normalize-space(.)='Confirm received']")).isEmpty());
    }

    // ---------------------------------------------------------------- price bands

    @Given("no price band from an earlier run is still open")
    public void noPriceBandFromAnEarlierRunIsStillOpen() {
        AdminFixtures.archiveLeftoverQaBands();
    }

    @When("I add the price band {string} from {string} to {string} tokens at KES {string} per token")
    public void iAddThePriceBand(String name, String from, String to, String price) {
        String unique = name + " " + RandomData.uniqueSuffix();
        ScenarioData.put("bandName", unique);
        UiKit ui = ui();
        ui.open("/bands");
        ui.clickButton("New band");
        ui.typeIntoModal("Name", unique);
        ui.typeIntoModal("From (tokens)", from);
        ui.typeIntoModal("To (tokens)", to);
        ui.typeIntoModal("Price per token", price);
        ui.clickModalButton("Save");
        ui.waitForModalToClose();
    }

    @Then("that price band should be listed as active")
    public void thatPriceBandShouldBeListedAsActive() {
        String name = ScenarioData.get("bandName");
        waitUpTo(10).until(d -> {
            for (WebElement row : d.findElements(By.cssSelector("table tbody tr"))) {
                if (row.getText().contains(name) && row.getText().contains("Active")) return true;
            }
            return false;
        });
    }

    @When("I archive that price band")
    public void iArchiveThatPriceBand() {
        String name = ScenarioData.get("bandName");
        By archive = By.xpath("//tr[.//strong[normalize-space(.)='" + name + "']]//button[normalize-space(.)='Archive']");
        waitUpTo(10).until(ExpectedConditions.elementToBeClickable(archive)).click();
    }

    // ---------------------------------------------------------------- postpaid

    @Given("that postpaid client has sent a billed SMS")
    public void thatPostpaidClientHasSentABilledSms() {
        sendOneBilledSms();
    }

    private void sendOneBilledSms() {
        AdminFixtures.ProvisionedUser user = company().user();
        String sender = AdminFixtures.provisionSenderFor(user, "QA" + RandomData.uniqueSuffix().toUpperCase());
        AdminFixtures.sendBilledSms(AdminFixtures.apiClientIdOf(user), sender, "254712345678");
    }

    @When("I set the cost per message of that client's administrator to KES {string}")
    public void iSetTheCostPerMessageOfThatClientsAdministrator(String rate) {
        UiKit ui = ui();
        By setRate = By.xpath("//h2[normalize-space(.)='By user']/ancestor::div[contains(concat(' ', normalize-space(@class), ' '), ' card ')][1]//tbody/tr[1]//button[normalize-space(.)='Set rate']");
        waitUpTo(15).until(ExpectedConditions.elementToBeClickable(setRate)).click();
        ui.typeIntoModal("KES per SMS page", rate);
        ui.clickModalButton("Save");
        ui.waitForModalToClose();
    }

    @Then("the usage by user should show that administrator at KES {string} on their own rate")
    public void theUsageByUserShouldShowTheirOwnRate(String rate) {
        By row = By.xpath("//h2[normalize-space(.)='By user']/ancestor::div[contains(concat(' ', normalize-space(@class), ' '), ' card ')][1]//tbody/tr[1]");
        waitUpTo(10).until(d -> {
            for (WebElement r : d.findElements(row)) {
                if (r.getText().contains(rate) && r.getText().contains("their own rate")) return true;
            }
            return false;
        });
    }

    // ---------------------------------------------------------------- invoices

    @Given("the database used by the API can be reached to age usage")
    public void theDatabaseCanBeReached() {
        Assume.assumeTrue("The database (db.host/db.port/db.name/db.user/db.password) isn't reachable, so usage can't be aged "
                + "into a completed billing period: skipped", Db.reachable());
    }

    @Given("an invoice has been issued to that postpaid client")
    public void anInvoiceHasBeenIssuedToThatPostpaidClient() {
        sendOneBilledSms();
        Assert.assertTrue("Usage to age", Db.ageUsage(company().client().clientId(), 2) > 0);
        Assert.assertEquals("Invoices issued", 1, AdminFixtures.generateInvoices(company().client().clientId()));
    }

    @When("I open the Invoices page for that client")
    public void iOpenTheInvoicesPageForThatClient() {
        ui().open("/invoices?client_id=" + company().client().clientId());
        waitUpTo(10).until(d -> !d.findElements(By.xpath("//table//a[contains(normalize-space(.), 'INV-')]")).isEmpty());
    }

    @When("I change the status of its first invoice to {string}")
    public void iChangeTheStatusOfItsFirstInvoice(String status) {
        UiKit ui = ui();
        By change = By.xpath("//table//tbody/tr[1]//button[normalize-space(.)='Change status']");
        waitUpTo(10).until(ExpectedConditions.elementToBeClickable(change)).click();
        ui.selectInModal("New status", status);
        ui.clickModalButton("Save");
        ui.waitForModalToClose();
    }

    @Then("the first invoice should be {string}")
    public void theFirstInvoiceShouldBe(String status) {
        By badge = By.xpath("//table//tbody/tr[1]");
        waitUpTo(10).until(d -> d.findElements(badge).stream().anyMatch(r -> r.getText().contains(status)));
    }

    @Then("the Invoices page should show the outstanding, overdue and paid tiles")
    public void theInvoicesPageShouldShowTheTiles() {
        UiKit ui = ui();
        for (String label : new String[] {"Outstanding", "Overdue", "Paid"}) {
            String value = ui.tileValue(label);
            Assert.assertTrue(label + " tile: '" + value + "'", value.startsWith("KES"));
        }
    }

    @Then("the page heading should read {string}")
    public void thePageHeadingShouldRead(String heading) {
        waitUpTo(10).until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".page h1"), heading));
    }

    @Then("the page should say {string}")
    public void thePageShouldSay(String text) {
        waitUpTo(10).until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".page"), text));
    }
}
