package smsadminui.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
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
import smsadminui.utils.RandomData;
import smsadminui.utils.WebDriverFactory;

import java.time.Duration;

/**
 * A company registers itself on /register and stays PENDING: nobody in it can sign in until a Super Administrator approves it
 * (Clients, "Waiting for approval"), who can also reject it with a reason the user then sees at sign-in.
 */
public class RegistrationSteps {

    private static final String PASSWORD = "A-long-QA-password-1";

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

    private AdminFixtures.RegisteredCompany company() {
        return ScenarioData.get("registered");
    }

    // ---------------------------------------------------------------- the Register page

    @When("I register a company on the Register page as prepaid on the {string} price band")
    public void iRegisterACompanyAsPrepaid(String band) {
        fillRegistration();
        By bandOption = By.xpath("//div[@aria-label='Price band']//label[.//b[contains(normalize-space(.), '" + band + "')]]");
        waitUpTo(10).until(ExpectedConditions.elementToBeClickable(bandOption)).click();
        submitRegistration();
    }

    @When("I register a company on the Register page as postpaid, invoiced {string}")
    public void iRegisterACompanyAsPostpaid(String period) {
        fillRegistration();
        waitUpTo(10).until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@aria-label='Billing plan']//label[.//b[normalize-space(.)='Postpaid']]"))).click();
        new org.openqa.selenium.support.ui.Select(waitUpTo(10).until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//label[contains(normalize-space(.), 'Invoice me')]/select")))).selectByVisibleText(period);
        submitRegistration();
    }

    private void fillRegistration() {
        String suffix = RandomData.uniqueSuffix();
        String name = "QA UI Registered " + suffix;
        String email = "qa.ui.registered." + suffix + "@example.com";
        ScenarioData.put("registered", new AdminFixtures.RegisteredCompany(0, name, "", email, PASSWORD));

        driver().get(Config.get("ui.base.url") + "/register");
        waitUpTo(10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[normalize-space(.)='Register your company']")));
        type("Company name", name);
        type("Company email", "qa.ui.company." + suffix + "@example.com");
        type("Full name", "QA UI Registrant");
        type("Email (you sign in with this)", email);
        type("Phone number", RandomData.uniqueMsisdn());
        type("Password", PASSWORD);
        type("Confirm password", PASSWORD);
    }

    private void type(String label, String text) {
        // "Password" is also contained in "Confirm password": match the label's own text first.
        WebElement field = waitUpTo(10).until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//form//label[starts-with(normalize-space(.), '" + label + "')]/input")));
        field.clear();
        field.sendKeys(text);
    }

    private void submitRegistration() {
        waitUpTo(10).until(ExpectedConditions.elementToBeClickable(By.xpath("//form//button[@type='submit'][normalize-space(.)='Register']"))).click();
    }

    @Then("I should be told that my company is registered and waiting for approval")
    public void iShouldBeToldThatMyCompanyIsRegistered() {
        waitUpTo(15).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[normalize-space(.)=\"You're registered\"]")));
        String page = driver().findElement(By.cssSelector("main")).getText();
        Assert.assertTrue("Page: " + page, page.contains("waiting for approval"));
        String reference = driver().findElement(By.cssSelector(".done strong.mono")).getText();
        Assert.assertTrue("Reference '" + reference + "'", reference.matches("JP\\d+"));
        AdminFixtures.RegisteredCompany c = company();
        ScenarioData.put("registered", new AdminFixtures.RegisteredCompany(Integer.parseInt(reference.substring(2)), c.clientName(), reference,
                c.adminEmail(), c.adminPassword()));
    }

    // ---------------------------------------------------------------- pending, approved, rejected

    @Given("a company has registered and is waiting for approval")
    public void aCompanyHasRegistered() {
        ScenarioData.put("registered", AdminFixtures.registerCompany(false));
    }

    @Given("a postpaid company has registered and is waiting for approval")
    public void aPostpaidCompanyHasRegistered() {
        ScenarioData.put("registered", AdminFixtures.registerCompany(true));
    }

    @When("the registered company's administrator tries to sign in")
    public void theRegisteredAdministratorTriesToSignIn() {
        AdminFixtures.RegisteredCompany c = company();
        new LoginPage(driver()).open().loginAs(c.adminEmail(), c.adminPassword());
    }

    @Then("the sign-in should be refused, saying {string}")
    public void theSignInShouldBeRefusedSaying(String text) {
        LoginPage login = new LoginPage(driver());
        Assert.assertTrue("Expected a sign-in error", login.hasError());
        String error = login.errorText();
        Assert.assertTrue("Sign-in error: '" + error + "'", error.toLowerCase().contains(text.toLowerCase()));
        login.waitForUrlToContain("/login");
    }

    @Then("the registered company's administrator can sign in")
    public void theRegisteredAdministratorCanSignIn() {
        AdminFixtures.RegisteredCompany c = company();
        LoginPage login = new LoginPage(driver()).open();
        login.loginAs(c.adminEmail(), c.adminPassword());
        login.waitForUrlToContain("/dashboard");
    }

    @When("I open the clients waiting for approval")
    public void iOpenTheClientsWaitingForApproval() {
        ui().open("/clients?status=PENDING");
        search(company().clientName());
    }

    private void search(String text) {
        WebElement box = waitUpTo(10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[aria-label='Search clients']")));
        box.clear();
        box.sendKeys(text);
        waitUpTo(10).until(d -> d.findElements(By.cssSelector("table.card tbody tr")).stream().anyMatch(r -> r.getText().contains(text)));
    }

    private By rowButton(String button) {
        return By.xpath("//table[contains(@class,'card')]//tr[.//strong[normalize-space(.)='" + company().clientName()
                + "']]//button[normalize-space(.)='" + button + "']");
    }

    @When("I approve that company")
    public void iApproveThatCompany() {
        waitUpTo(10).until(ExpectedConditions.elementToBeClickable(rowButton("Approve"))).click();
        ui().clickModalButton("Approve");
        ui().waitForModalToClose();
    }

    @When("I start approving that company")
    public void iStartApprovingThatCompany() {
        waitUpTo(10).until(ExpectedConditions.elementToBeClickable(rowButton("Approve"))).click();
    }

    @Then("the Approve button should be disabled until a cost per message is set")
    public void theApproveButtonShouldBeDisabledUntilACostPerMessageIsSet() {
        UiKit ui = ui();
        Assert.assertFalse("Approve is enabled with no cost per message", ui.modalButtonEnabled("Approve"));
        Assert.assertTrue("Dialog: " + ui.modalText(), ui.modalText().contains("Set the cost per message"));
        ui.typeIntoModal("Cost per message", "0.40");
        Assert.assertTrue("Approve is still disabled once a cost per message is set", ui.modalButtonEnabled("Approve"));
    }

    @When("I approve that company at KES {string} per SMS")
    public void iApproveThatCompanyAtRate(String rate) {
        waitUpTo(10).until(ExpectedConditions.elementToBeClickable(rowButton("Approve"))).click();
        ui().typeIntoModal("Cost per message", rate);
        ui().clickModalButton("Approve");
        ui().waitForModalToClose();
    }

    @When("I reject that company because {string}")
    public void iRejectThatCompanyBecause(String reason) {
        waitUpTo(10).until(ExpectedConditions.elementToBeClickable(rowButton("Reject"))).click();
        UiKit ui = ui();
        Assert.assertFalse("Reject is enabled with no reason", ui.modalButtonEnabled("Reject"));
        ui.typeIntoModal("Reason", reason);
        ui.clickModalButton("Reject");
        ui.waitForModalToClose();
    }

    @Then("that company should no longer be waiting for approval")
    public void thatCompanyShouldNoLongerBeWaiting() {
        waitUpTo(10).until(d -> d.findElements(rowButton("Approve")).isEmpty() || d.findElements(By.cssSelector("table.card tbody tr")).stream()
                .noneMatch(r -> r.getText().contains(company().clientName())));
    }

    @Then("that company should be listed as {string}")
    public void thatCompanyShouldBeListedAs(String status) {
        ui().open("/clients");
        search(company().clientName());
        waitUpTo(10).until(d -> d.findElements(By.cssSelector("table.card tbody tr")).stream()
                .anyMatch(r -> r.getText().contains(company().clientName()) && r.getText().contains(status)));
    }
}
