package smsadminui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ClientsPage extends BasePage {

    private static final By NEW_CLIENT_TOGGLE = By.xpath("//div[@class='page-header']/button");
    private static final By NEW_CLIENT_FORM = By.xpath("//form[.//h2[text()='New client']]");
    private static final By CREATE_CLIENT_SUBMIT = By.xpath("//form[.//h2[text()='New client']]//button[@type='submit']");
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

    /** Same toggle button acts as Cancel while the form is open. */
    public void closeNewClientForm() {
        click(NEW_CLIENT_TOGGLE);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(NEW_CLIENT_FORM));
    }

    public void fillNewClientForm(String name, String email) {
        type(fieldByLabel("Client name"), name);
        type(fieldByLabel("Client email"), email);
    }

    public void submitNewClientForm() {
        click(CREATE_CLIENT_SUBMIT);
    }

    /** A native `required`-blocked submit never reaches Vue's handler, so the
     * form simply stays open - the clearest signal available without a server round trip. */
    public boolean isNewClientFormOpen() {
        return isPresent(NEW_CLIENT_FORM);
    }

    public boolean hasClientRow(String clientName) {
        return waitForTableRowContaining(clientName);
    }

    public void openClientAdminFormFor(String clientName) {
        waitForTableRowContaining(clientName);
        rowFor(clientName).findElement(By.xpath(".//button[contains(text(),'Client Admin')]")).click();
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
