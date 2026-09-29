package smsadminui.stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import smsadminui.pages.UiKit;
import smsadminui.utils.WebDriverFactory;

import java.time.Duration;

/** The Jambopro brand: name, logo, page titles, brand colours and icons. */
public class BrandingSteps {

    private WebDriver driver() {
        return WebDriverFactory.getDriver();
    }

    private WebDriverWait waitUpTo(int seconds) {
        return new WebDriverWait(driver(), Duration.ofSeconds(seconds));
    }

    @Then("the page title should be {string}")
    public void thePageTitleShouldBe(String title) {
        waitUpTo(10).until(ExpectedConditions.titleIs(title));
    }

    @Then("the Jambopro logo should be shown")
    public void theJamboproLogoShouldBeShown() {
        WebElement logo = waitUpTo(10).until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[role=img][aria-label='Jambopro']")));
        Assert.assertTrue("Logo text: " + logo.getText(), logo.getText().replace("\n", "").contains("Jambopro"));
    }

    @Then("the login page should say {string}")
    public void theLoginPageShouldSay(String text) {
        String headline = driver().findElement(By.cssSelector(".brand-panel .headline")).getText();
        Assert.assertEquals(text, headline);
    }

    @Then("the sign-in button should be brand green with navy text")
    public void theSignInButtonShouldBeBrandGreenWithNavyText() {
        WebElement button = driver().findElement(By.cssSelector("form.login-card button[type=submit]"));
        Assert.assertEquals("rgb(0, 200, 83)", button.getCssValue("background-color").replace("rgba", "rgb").replace(", 1)", ")"));
        Assert.assertEquals("rgb(11, 31, 59)", button.getCssValue("color").replace("rgba", "rgb").replace(", 1)", ")"));
    }

    @Then("the page should use the Poppins typeface")
    public void thePageShouldUseThePoppinsTypeface() {
        Boolean loaded = (Boolean) ((JavascriptExecutor) driver()).executeAsyncScript(
                "const done = arguments[arguments.length - 1];"
                + "document.fonts.ready.then(() => done(document.fonts.check('600 16px Poppins')));");
        String family = driver().findElement(By.tagName("body")).getCssValue("font-family");
        Assert.assertTrue("body font-family " + family, family.startsWith("Poppins"));
        Assert.assertTrue("Poppins did not load (is fonts.googleapis.com reachable?)", Boolean.TRUE.equals(loaded));
    }

    @When("I click the sidebar logo")
    public void iClickTheSidebarLogo() {
        waitUpTo(10).until(ExpectedConditions.elementToBeClickable(By.cssSelector(".sidebar a.brand"))).click();
    }

    /** Fetched by the browser, as a visitor's browser fetches the icons. */
    @Then("{string} should be served as {string}")
    @SuppressWarnings("unchecked")
    public void shouldBeServedAs(String path, String contentType) {
        java.util.Map<String, Object> resp = (java.util.Map<String, Object>) ((JavascriptExecutor) driver()).executeAsyncScript(
                "const done = arguments[arguments.length - 1];"
                + "fetch(arguments[0], { cache: 'no-store' }).then(async r => done({ status: r.status,"
                + " type: r.headers.get('content-type') || '', size: (await r.arrayBuffer()).byteLength }),"
                + " e => done({ status: 0, type: String(e), size: 0 }));", path);
        Assert.assertEquals(path + " " + resp, 200L, ((Number) resp.get("status")).longValue());
        Assert.assertTrue(path + " " + resp, String.valueOf(resp.get("type")).startsWith(contentType));
        Assert.assertTrue(path + " is empty", ((Number) resp.get("size")).longValue() > 0);
    }

    @When("I open the {string} page as a signed-in user")
    public void iOpenThePageAsASignedInUser(String path) {
        new UiKit(driver()).open(path);
    }
}
