package smsadminui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class NavBar extends BasePage {

    private static final By CLIENTS_LINK = By.xpath("//nav//a[text()='Clients']");
    private static final By SENDERS_LINK = By.xpath("//nav//a[text()='Senders']");
    private static final By BULK_MESSAGES_LINK = By.xpath("//nav//a[text()='Bulk Messages']");
    private static final By USERS_LINK = By.xpath("//nav//a[text()='Users']");
    private static final By ROLE_BADGE = By.cssSelector(".who .badge");
    private static final By LOGOUT_BUTTON = By.xpath("//div[contains(@class,'who')]//button[text()='Log out']");

    public NavBar(WebDriver driver) {
        super(driver);
    }

    public boolean hasClientsLink() {
        return isPresent(CLIENTS_LINK);
    }

    public boolean hasSendersLink() {
        return isPresent(SENDERS_LINK);
    }

    public boolean hasBulkMessagesLink() {
        return isPresent(BULK_MESSAGES_LINK);
    }

    public boolean hasUsersLink() {
        return isPresent(USERS_LINK);
    }

    public String roleBadgeText() {
        return find(ROLE_BADGE).getText();
    }

    public void goToClients() {
        click(CLIENTS_LINK);
    }

    public void goToSenders() {
        click(SENDERS_LINK);
    }

    public void goToBulkMessages() {
        click(BULK_MESSAGES_LINK);
    }

    public void goToUsers() {
        click(USERS_LINK);
    }

    public void logout() {
        click(LOGOUT_BUTTON);
    }
}
