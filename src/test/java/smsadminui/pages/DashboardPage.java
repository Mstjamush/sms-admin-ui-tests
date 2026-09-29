package smsadminui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DashboardPage extends BasePage {

    private static final By HEADING = By.cssSelector(".page h1");
    private static final By SUBTITLE = By.cssSelector(".page p.muted");
    private static final By CLIENTS_CARD = By.xpath("//a[contains(@class,'link-card')][.//h2[text()='Clients']]");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    public DashboardPage open() {
        navigateTo("/dashboard");
        find(HEADING);
        return this;
    }

    public String heading() {
        return find(HEADING).getText();
    }

    /** "Signed in as <role> [for <client>]" - the client part only renders for non-Super-Admins. */
    public String subtitleText() {
        return find(SUBTITLE).getText();
    }

    public boolean hasClientsCard() {
        return isPresent(CLIENTS_CARD);
    }
}
