package smsadminui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.time.Duration;

public class BulkCampaignsPage extends BasePage {

    private static final By NEEDS_SENDER_MESSAGE = By.xpath("//p[contains(text(),'You need at least one sender ID')]");
    private static final By SEND_CAMPAIGN_BUTTON = By.xpath("//button[contains(text(),'Send campaign') or contains(text(),'Sending')]");
    private static final By UPLOAD_TAB_BUTTON = By.xpath("//div[@class='tabs']/button[text()='Upload file']");
    private static final By FILE_INPUT = By.cssSelector("input[type=file]");

    public BulkCampaignsPage(WebDriver driver) {
        super(driver);
    }

    public BulkCampaignsPage open() {
        navigateTo("/bulk-campaigns");
        find(By.cssSelector(".page h1"));
        return this;
    }

    public boolean needsSenderMessageShown() {
        return isPresent(NEEDS_SENDER_MESSAGE);
    }

    public void switchToUploadTab() {
        click(UPLOAD_TAB_BUTTON);
    }

    public boolean hasFileInput() {
        return isPresent(FILE_INPUT);
    }

    public void fillInsertForm(String name, String message, String recipients) {
        type(fieldByLabel("Campaign name"), name);
        type(fieldByLabel("Message"), message);
        type(fieldByLabel("Recipients"), recipients);
    }

    public void submitInsertForm() {
        click(SEND_CAMPAIGN_BUTTON);
    }

    public boolean hasCampaignRow(String campaignName) {
        return waitForTableRowContaining(campaignName);
    }

    public boolean neverShowsCampaignRow(String campaignName) {
        return tableNeverGetsRowContaining(campaignName, Duration.ofSeconds(3));
    }
}
