package smsadminui.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import smsadminui.pages.UiKit;
import smsadminui.utils.AdminFixtures;
import smsadminui.utils.RandomData;
import smsadminui.utils.TestData;
import smsadminui.utils.WebDriverFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MessagingSteps {

    private WebDriver driver() {
        return WebDriverFactory.getDriver();
    }

    private UiKit ui() {
        return new UiKit(driver());
    }

    private WebDriverWait waitUpTo(int seconds) {
        return new WebDriverWait(driver(), Duration.ofSeconds(seconds));
    }

    // ------------------------------------------------------------ broadcast lists

    @When("I upload {string} as a broadcast list through the Broadcast lists page")
    public void iUploadAsABroadcastList(String file) {
        String name = "QA UI list " + RandomData.uniqueSuffix();
        ScenarioData.put("listName", name);
        UiKit ui = ui();
        ui.open("/lists");
        ui.clickButton("Upload list");
        ui.chooseFileInModal(TestData.path(file));
        ui.typeIntoModal("Name", name);
        ui.clickModalButton("Import");
        ui.waitForModalToClose();
    }

    @Then("the import report should show {int} added, {int} duplicates skipped and {int} invalid")
    public void theImportReportShouldShow(int added, int duplicates, int invalid) {
        Assert.assertEquals(String.valueOf(added), figure("added"));
        Assert.assertEquals(String.valueOf(duplicates), figure("duplicates skipped"));
        Assert.assertEquals(String.valueOf(invalid), figure("invalid"));
    }

    private String figure(String caption) {
        return ui().texts(By.xpath("//div[@class='figures']/div[span[normalize-space(.)='" + caption + "']]/strong")).get(0);
    }

    @Then("the duplicates section should list {string} as {string}")
    public void theDuplicatesSectionShouldList(String number, String reason) {
        driver().findElement(By.xpath("//summary[starts-with(normalize-space(.), 'Duplicate numbers')]")).click();
        List<String> rows = ui().texts(By.xpath("//summary[starts-with(normalize-space(.), 'Duplicate numbers')]/following-sibling::table//tbody/tr"));
        Assert.assertTrue("No duplicate row " + number + " / " + reason + " in " + rows,
                rows.stream().anyMatch(r -> r.contains(number) && r.contains(reason)));
    }

    @Then("the new list should appear in the broadcast lists table with placeholders {string}")
    public void theNewListShouldAppear(String placeholders) {
        String name = ScenarioData.get("listName");
        String row = waitUpTo(10).until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//table//tr[.//a[normalize-space(.)='" + name + "']]"))).getText();
        for (String p : placeholders.split(",\\s*")) {
            Assert.assertTrue(row + " lacks placeholder " + p, row.contains(p));
        }
    }

    @When("I open the new list")
    public void iOpenTheNewList() {
        String name = ScenarioData.get("listName");
        waitUpTo(10).until(ExpectedConditions.elementToBeClickable(By.xpath("//table//a[normalize-space(.)='" + name + "']"))).click();
        ui().heading();
    }

    @Then("its import history should show the file with {int} duplicates")
    public void itsImportHistoryShouldShow(int duplicates) {
        String row = waitUpTo(10).until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h3[normalize-space(.)='Import history']/following-sibling::div//tbody/tr[1]"))).getText();
        Assert.assertTrue("History row: " + row, row.contains(".csv") && row.contains(String.valueOf(duplicates)));
    }

    // ------------------------------------------------------------ Bulk Messages upload

    @When("I upload {string} on the Upload file tab as campaign {string}")
    public void iUploadOnTheUploadFileTab(String file, String campaign) {
        String name = campaign + " " + RandomData.uniqueSuffix();
        ScenarioData.put("campaignName", name);
        UiKit ui = ui();
        ui.chooseFile(TestData.path(file));
        ui.typeInto("Campaign name", name);
        driver().findElement(By.xpath("//form//button[@type='submit']")).click();
        waitUpTo(15).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".detected .badge")));
    }

    @Then("the detected format should be {string}")
    public void theDetectedFormatShouldBe(String format) {
        Assert.assertEquals(format, driver().findElement(By.cssSelector(".detected .badge")).getText().trim());
    }

    @When("I write the upload message {string}")
    public void iWriteTheUploadMessage(String message) {
        ui().typeInto("Message", message);
    }

    @Then("the upload preview should show {string}")
    public void theUploadPreviewShouldShow(String text) {
        waitUpTo(10).until(d -> d.findElements(By.cssSelector(".previews .bubble")).stream()
                .anyMatch(b -> b.getText().equals(text)));
    }

    @When("I send the upload")
    public void iSendTheUpload() {
        waitUpTo(10).until(ExpectedConditions.elementToBeClickable(By.cssSelector(".compose .form-actions button"))).click();
    }

    @Then("a success message should confirm the campaign")
    public void aSuccessMessageShouldConfirmTheCampaign() {
        String toast = ui().successToast();
        Assert.assertTrue(toast, toast.contains(ScenarioData.<String>get("campaignName")));
    }

    // ------------------------------------------------------------ campaign wizard

    @Given("a broadcast list {string} has been uploaded for my client")
    public void aBroadcastListHasBeenUploaded(String file) {
        String name = "QA UI list " + RandomData.uniqueSuffix();
        int id = AdminFixtures.provisionListFor(TestContext.clientAdmin().user(), name, file);
        ScenarioData.put("listId", id);
        ScenarioData.put("listName", name);
    }

    @When("I start a campaign to that list with message {string}")
    public void iStartACampaignToThatList(String message) {
        String name = "QA UI campaign " + RandomData.uniqueSuffix();
        ScenarioData.put("campaignName", name);
        driver().get(smsadminui.config.Config.get("ui.base.url") + "/campaigns/new?list=" + ScenarioData.get("listId"));
        UiKit ui = ui();
        ui.heading();
        waitUpTo(10).until(ExpectedConditions.attributeToBeNotEmpty(
                driver().findElement(By.xpath("//label[contains(normalize-space(.), 'Campaign name')]/input")), "value"));
        ui.typeInto("Campaign name", name);
        ui.clickButton("Continue");
        ui.typeInto("Message", message);
    }

    @Then("the wizard preview should show {string}")
    public void theWizardPreviewShouldShow(String text) {
        waitUpTo(10).until(d -> d.findElements(By.cssSelector(".bubble")).stream().anyMatch(b -> b.getText().equals(text)));
    }

    @When("I schedule it for tomorrow and confirm")
    public void iScheduleItForTomorrow() {
        UiKit ui = ui();
        ui.clickButton("Continue");
        driver().findElement(By.xpath("//label[contains(@class,'option')][.//strong[normalize-space(.)='Schedule for later']]")).click();
        String tomorrow = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        ((JavascriptExecutor) driver()).executeScript(
                "const el = document.querySelector('input[type=datetime-local]'); el.value = arguments[0];"
                        + "el.dispatchEvent(new Event('input', { bubbles: true }));", tomorrow);
        ui.clickButton("Continue");
        waitForEstimate();
        ui.clickButton("Schedule campaign");
    }

    @When("I send it now and confirm")
    public void iSendItNow() {
        UiKit ui = ui();
        ui.clickButton("Continue");
        ui.clickButton("Continue");
        waitForEstimate();
        ui.clickButton("Send campaign now");
    }

    private void waitForEstimate() {
        String cost = waitUpTo(10).until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h3[normalize-space(.)='Estimated cost']/following-sibling::div[contains(@class,'big')]"))).getText();
        ScenarioData.put("estimate", cost);
    }

    @Then("the review step should have shown an estimated cost of {string}")
    public void theReviewStepShouldHaveShown(String cost) {
        Assert.assertEquals(cost, ScenarioData.get("estimate"));
    }

    @Then("the campaign report should show the status {string}")
    public void theCampaignReportShouldShowTheStatus(String status) {
        waitUpTo(15).until(ExpectedConditions.urlMatches(".*/campaigns/\\d+$"));
        waitUpTo(15).until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".page-header .badge"), status));
    }

    @When("I cancel the campaign from its report")
    public void iCancelTheCampaign() {
        UiKit ui = ui();
        ui.clickButton("Cancel campaign");
        driver().findElement(By.cssSelector("[role=dialog] button.danger")).click();
    }

    @Then("the campaign should be listed on the Campaigns page")
    public void theCampaignShouldBeListed() {
        ui().open("/campaigns");
        waitUpTo(10).until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//table//a[normalize-space(.)='" + ScenarioData.get("campaignName") + "']")));
    }

    // ------------------------------------------------------------ templates

    @When("I create a template {string} with message {string}")
    public void iCreateATemplate(String name, String body) {
        String unique = name + " " + RandomData.uniqueSuffix();
        ScenarioData.put("templateName", unique);
        saveTemplate(unique, body);
    }

    @When("I create another template with the same name")
    public void iCreateAnotherTemplateWithTheSameName() {
        saveTemplate(ScenarioData.get("templateName"), "Another body");
    }

    private void saveTemplate(String name, String body) {
        UiKit ui = ui();
        ui.open("/templates");
        ui.clickButton("New template");
        ui.typeIntoModal("Name", name);
        ui.typeIntoModal("Message", body);
        ui.clickModalButton("Save template");
    }

    @Then("the template should appear in the templates table")
    public void theTemplateShouldAppear() {
        new UiKit(driver()).waitForModalToClose();
        waitUpTo(10).until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//table//strong[normalize-space(.)='" + ScenarioData.get("templateName") + "']")));
    }

    @Then("the template dialog should say {string}")
    public void theTemplateDialogShouldSay(String text) {
        String error = ui().modalError();
        Assert.assertTrue(error, error.contains(text));
    }
}
