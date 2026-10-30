package smsadminui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DashboardPage extends BasePage {

    private static final By HEADING = By.cssSelector(".page h1");
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

    /** A client's dashboard says good morning, afternoon or evening, and the first name. */
    public boolean hasGreeting() {
        return heading().matches("Good (morning|afternoon|evening)(, .+)?");
    }

    /** Whether a figure in the strip under the greeting ("Delivery rate", "Scheduled", ...) is on the page. */
    public boolean hasFigure(String label) {
        try {
            wait.until(d -> isPresent(figure(label)));
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    private static By figure(String label) {
        return By.xpath("//div[contains(@class,'tile')]/div[@class='label' and normalize-space(.)='" + label + "']");
    }

    public boolean hasClientsCard() {
        return isPresent(CLIENTS_CARD);
    }
}
