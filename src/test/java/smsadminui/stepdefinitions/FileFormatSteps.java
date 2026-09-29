package smsadminui.stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import smsadminui.pages.UiKit;
import smsadminui.utils.WebDriverFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class FileFormatSteps {

    static final String ALL_FORMATS = "csv, xlsx, xlsm, xlsb, xls, ods, numbers";

    private WebDriver driver() {
        return WebDriverFactory.getDriver();
    }

    private WebDriverWait waitUpTo(int seconds) {
        return new WebDriverWait(driver(), Duration.ofSeconds(seconds));
    }

    private static String expectedAccept(String formats) {
        return "." + String.join(",.", formats.split(",\\s*"));
    }

    @When("I open the Upload list dialog")
    public void iOpenTheUploadListDialog() {
        UiKit ui = new UiKit(driver());
        ui.open("/lists");
        ui.clickButton("Upload list");
    }

    @Then("the dialog's file picker should accept {string}")
    public void theDialogsFilePickerShouldAccept(String formats) {
        String accept = driver().findElement(By.cssSelector("[role=dialog] input[type=file]")).getAttribute("accept");
        Assert.assertEquals(expectedAccept(formats), accept);
    }

    @Then("the file picker should accept {string}")
    public void theFilePickerShouldAccept(String formats) {
        String accept = driver().findElement(By.cssSelector("input[type=file]")).getAttribute("accept");
        Assert.assertEquals(expectedAccept(formats), accept);
    }

    @When("I open the {string} export menu")
    public void iOpenTheExportMenu(String label) {
        waitUpTo(10).until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//div[contains(@class,'export-menu')]/button[normalize-space(.)='" + label + "']"))).click();
        waitUpTo(5).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[role=menu]")));
    }

    @Then("the export menu should offer {string}")
    public void theExportMenuShouldOffer(String formats) {
        List<String> offered = driver().findElements(By.cssSelector("[role=menu] [role=menuitem]")).stream()
                .map(e -> e.getAttribute("data-format")).toList();
        Assert.assertEquals(List.of(formats.split(",\\s*")), offered);
    }

    @When("I export as {string}")
    public void iExportAs(String format) {
        ((JavascriptExecutor) driver()).executeScript(
                "window.__qaDownloads = [];"
                + "const click = HTMLAnchorElement.prototype.click;"
                + "HTMLAnchorElement.prototype.click = function () {"
                + "  if (!this.download) return click.call(this);"
                + "  const name = this.download;"
                + "  window.__qaDownloads.push(fetch(this.href).then(r => r.blob())"
                + "    .then(b => ({ name, size: b.size, type: b.type })));"
                + "};");
        driver().findElement(By.cssSelector("[role=menu] [data-format='" + format + "']")).click();
    }

    @Then("a file ending {string} should have been downloaded")
    @SuppressWarnings("unchecked")
    public void aFileEndingShouldHaveBeenDownloaded(String suffix) {
        waitUpTo(30).until(d -> ((Number) ((JavascriptExecutor) d).executeScript(
                "return (window.__qaDownloads || []).length")).intValue() > 0);
        Map<String, Object> file = (Map<String, Object>) ((JavascriptExecutor) driver()).executeAsyncScript(
                "const done = arguments[arguments.length - 1];"
                + "window.__qaDownloads[0].then(done, e => done({ error: String(e) }));");
        Assert.assertNull("Download failed: " + file, file.get("error"));
        Assert.assertTrue("Downloaded " + file, String.valueOf(file.get("name")).endsWith(suffix));
        Assert.assertTrue("Empty download " + file, ((Number) file.get("size")).longValue() > 0);
    }
}
