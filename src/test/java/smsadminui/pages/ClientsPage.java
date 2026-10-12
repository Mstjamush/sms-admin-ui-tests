package smsadminui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ClientsPage extends BasePage {

    private static final By NEW_CLIENT_TOGGLE = By.xpath("//div[@class='page-header']/button");
    // "New client" is a dialog (title in its header, buttons in its footer) with the billing settings in it.
    private static final By NEW_CLIENT_FORM = By.xpath("//*[@role='dialog'][.//h2[normalize-space(.)='New client']]");
    private static final By NEW_CLIENT_CANCEL = By.xpath("//*[@role='dialog'][.//h2[normalize-space(.)='New client']]//button[normalize-space(.)='Cancel']");
    private static final By CREATE_CLIENT_SUBMIT = By.xpath("//*[@role='dialog'][.//h2[normalize-space(.)='New client']]//button[normalize-space(.)='Create client']");
    private static final By CLIENT_SEARCH = By.cssSelector("input[aria-label='Search clients']");
    private static final By CREATE_CLIENT_ADMIN_SUBMIT = By.xpath("//button[contains(text(),'Create Client Administrator')]");
    private static final By CLIENT_ADMIN_FORM = By.xpath("//form[.//button[contains(text(),'Create Client Administrator')]]");

    public ClientsPage(WebDriver driver) {
        super(driver);
    }

    public ClientsPage open() {
        navigateTo("/clients");
        find(By.cssSelector(".page h1"));
        return this;
    }

    public void openNewClientForm() {
        click(NEW_CLIENT_TOGGLE);
        find(NEW_CLIENT_FORM);
    }

    public void closeNewClientForm() {
        click(NEW_CLIENT_CANCEL);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(NEW_CLIENT_FORM));
    }

    /** Fills the company details, and the billing a client needs to be created: a prepaid client on its own price of KES 1.00 a token. */
    public void fillNewClientForm(String name, String email) {
        type(fieldByLabel("Client name"), name);
        type(fieldByLabel("Client email"), email);
        type(fieldByLabel("Own price per token"), "1.00");
    }

    /** Switches the dialog to the Postpaid plan, which asks for a cost per message instead of a band or a price. */
    public void choosePostpaid() {
        new UiKit(driver).selectInModal("Plan", "Postpaid");
    }

    public void typeCostPerMessage(String rate) {
        new UiKit(driver).typeIntoModal("Cost per message", rate);
    }

    public boolean createClientButtonEnabled() {
        return find(CREATE_CLIENT_SUBMIT).isEnabled();
    }

    public void submitNewClientForm() {
        click(CREATE_CLIENT_SUBMIT);
    }

    public boolean isNewClientFormOpen() {
        return isPresent(NEW_CLIENT_FORM);
    }

    /**
     * Pending registrations are listed first, 20 to a page, so a new client may not be on the first page: search for it.
     */
    public boolean hasClientRow(String clientName) {
        type(CLIENT_SEARCH, clientName);
        return waitForTableRowContaining(clientName);
    }

    public void openClientAdminFormFor(String clientName) {
        hasClientRow(clientName);
        rowFor(clientName).findElement(By.xpath(".//button[normalize-space(.)='+ Admin']")).click();
    }

    /** The plan and position cells of a client's row, e.g. "Postpaid daily · KES 0.45 / SMS". */
    public String rowTextFor(String clientName) {
        hasClientRow(clientName);
        return rowFor(clientName).getText();
    }

    public void fillClientAdminForm(String fullNames, String email, String password, String msisdn) {
        type(fieldByLabel("Full names"), fullNames);
        type(fieldByLabel("Email"), email);
        type(fieldByLabel("Password"), password);
        type(fieldByLabel("Phone"), msisdn);
    }

    public void submitClientAdminForm() {
        click(CREATE_CLIENT_ADMIN_SUBMIT);
    }

    public boolean isClientAdminFormOpen() {
        return isPresent(CLIENT_ADMIN_FORM);
    }

    private WebElement rowFor(String clientName) {
        return tableRows().stream()
                .filter(row -> row.getText().contains(clientName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No client row found containing: " + clientName));
    }
}
