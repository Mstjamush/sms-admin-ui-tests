package smsadminui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * The top bar: five tabs (which five depends on the role) and a "More" menu holding every other link. A link in the menu is
 * in the page all the time, but can only be clicked once the menu is open, so {@link #clickLink} opens it when it has to.
 */
public class NavBar extends BasePage {

    private static final By ROLE_BADGE = By.cssSelector(".who .badge");
    private static final By ORGANISATION = By.cssSelector(".who .org");
    private static final By LOGOUT_BUTTON = By.xpath("//div[contains(@class,'who')]//button[text()='Log out']");
    private static final By MORE_MENU = By.cssSelector("nav details.more");
    private static final By MORE_SUMMARY = By.cssSelector("nav details.more > summary");
    private static final By LOGO_LINK = By.cssSelector(".mainbar a.brand");

    public NavBar(WebDriver driver) {
        super(driver);
    }

    private static By link(String text) {
        return By.xpath("//nav//a[text()='" + text + "']");
    }

    /** Whether the bar has a link with this text, as a tab or under More. */
    public boolean hasLink(String text) {
        return isPresent(link(text));
    }

    /** Clicks a link by its text, opening the More menu first when the link is in it. */
    public void clickLink(String text) {
        By locator = link(text);
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        // Decided by where the link is, not by whether the driver thinks it is visible: a closed menu's links still have a box.
        if (!el.findElements(By.xpath("ancestor::details[contains(@class,'more')]")).isEmpty()) {
            openMore();
        }
        click(locator);
    }

    private void openMore() {
        WebElement menu = wait.until(ExpectedConditions.presenceOfElementLocated(MORE_MENU));
        if (menu.getAttribute("open") == null) {
            click(MORE_SUMMARY);
        }
    }

    public boolean hasClientsLink() {
        return hasLink("Clients");
    }

    public boolean hasSendersLink() {
        return hasLink("Senders");
    }

    public boolean hasBulkMessagesLink() {
        return hasLink("Bulk Messages");
    }

    /** What a Client Administrator's people page is called; the Super Administrator's is "Users". */
    public boolean hasTeamLink() {
        return hasLink("Team");
    }

    public boolean hasUsersLink() {
        return hasLink("Users");
    }

    public String roleBadgeText() {
        return find(ROLE_BADGE).getText().trim();
    }

    /** The company the person is signed in for, in the line above the bar ("Amani Kalala · Owner · Coopérative Umoja"). */
    public String organisationText() {
        return find(ORGANISATION).getText().replace("·", "").trim();
    }

    public void goToClients() {
        clickLink("Clients");
    }

    public void goToSenders() {
        clickLink("Senders");
    }

    public void goToBulkMessages() {
        clickLink("Bulk Messages");
    }

    public void goToUsers() {
        clickLink("Users");
    }

    public void clickLogo() {
        click(LOGO_LINK);
    }

    public void logout() {
        click(LOGOUT_BUTTON);
    }
}
