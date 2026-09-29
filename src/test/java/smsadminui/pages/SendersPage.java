package smsadminui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

public class SendersPage extends BasePage {

    private static final By NEW_SENDER_TOGGLE = By.xpath("//div[@class='page-header']/button");
    private static final By NEW_SENDER_FORM = By.xpath("//form[.//h2[text()='New sender ID']]");
    private static final By SUBMIT = By.xpath("//form[.//h2[text()='New sender ID']]//button[@type='submit']");

    public SendersPage(WebDriver driver) {
        super(driver);
    }

    public SendersPage open() {
        navigateTo("/senders");
        find(By.cssSelector(".page h1"));
        return this;
    }

    public void openNewSenderForm() {
        click(NEW_SENDER_TOGGLE);
        find(NEW_SENDER_FORM);
    }

    public void closeNewSenderForm() {
        click(NEW_SENDER_TOGGLE);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(NEW_SENDER_FORM));
    }

    public void fillShortCode(String shortCode) {
        type(fieldByLabel("Short code"), shortCode);
    }

    public void submit() {
        click(SUBMIT);
    }

    public boolean isNewSenderFormOpen() {
        return isPresent(NEW_SENDER_FORM);
    }

    public boolean hasSenderRow(String shortCode) {
        return waitForTableRowContaining(shortCode);
    }

    public boolean neverShowsSenderRow(String shortCode) {
        return tableNeverGetsRowContaining(shortCode, Duration.ofSeconds(3));
    }

    public String approvalStatusFor(String shortCode) {
        waitForTableRowContaining(shortCode);
        return wait.until(driver -> {
            try {
                return tableRows().stream()
                        .filter(row -> row.getText().contains(shortCode))
                        .findFirst()
                        .map(row -> row.findElement(By.cssSelector("td:nth-child(3) .badge")).getText())
                        .orElse(null);
            } catch (StaleElementReferenceException e) {
                return null;
            }
        });
    }
}
