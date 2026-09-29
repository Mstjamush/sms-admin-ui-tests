package smsadminui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;

public class UsersPage extends BasePage {

    private static final By NEW_USER_TOGGLE = By.xpath("//div[@class='page-header']/button");
    private static final By NEW_USER_FORM = By.xpath("//form[.//h2[contains(text(),'New user for')]]");
    private static final By SUBMIT = By.xpath("//button[contains(text(),'Create user') or contains(text(),'Saving')]");
    private static final By CLIENT_COLUMN_HEADER = By.xpath("//table[contains(@class,'card')]//th[text()='Client']");

    public UsersPage(WebDriver driver) {
        super(driver);
    }

    public UsersPage open() {
        navigateTo("/users");
        find(By.cssSelector(".page h1"));
        return this;
    }
    public boolean hasNewUserButton() {
        return isPresent(NEW_USER_TOGGLE);
    }

    public void openNewUserForm() {
        click(NEW_USER_TOGGLE);
        find(NEW_USER_FORM);
    }

    public boolean isNewUserFormOpen() {
        return isPresent(NEW_USER_FORM);
    }

    public List<String> availableRoleOptions() {
        WebElement select = find(fieldByLabel("Role"));
        return select.findElements(By.tagName("option")).stream().map(WebElement::getText).toList();
    }

    public void fillNewUserForm(String fullNames, String email, String password, String msisdn, String roleName) {
        type(fieldByLabel("Full names"), fullNames);
        type(fieldByLabel("Email"), email);
        type(fieldByLabel("Password"), password);
        type(fieldByLabel("Phone"), msisdn);
        new Select(find(fieldByLabel("Role"))).selectByVisibleText(roleName);
    }

    public void submitNewUserForm() {
        click(SUBMIT);
    }

    public boolean hasUserRow(String email) {
        return waitForTableRowContaining(email);
    }

    public boolean neverShowsUserRow(String email) {
        return tableNeverGetsRowContaining(email, Duration.ofSeconds(3));
    }

    public boolean hasClientColumn() {
        return isPresent(CLIENT_COLUMN_HEADER);
    }
}
