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
import smsadminui.pages.NavBar;
import smsadminui.pages.UiKit;
import smsadminui.utils.WebDriverFactory;

import java.time.Duration;

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

    @Then("the JamboPro logo should be shown")
    public void theJamboProLogoShouldBeShown() {
        WebElement logo = waitUpTo(10).until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[role=img][aria-label='JamboPro']")));
        Assert.assertTrue("Logo text: " + logo.getText(), logo.getText().replace("\n", "").contains("JamboPro"));
    }

    @Then("the login page should say {string}")
    public void theLoginPageShouldSay(String text) {
        // The headline is two lines ("Send once." / "Know who got it."): compare it as one sentence.
        String headline = driver().findElement(By.cssSelector(".panel .headline")).getText().replaceAll("\\s+", " ");
        Assert.assertEquals(text, headline);
    }

    @Then("the sign-in button should be deep green with white text")
    public void theSignInButtonShouldBeDeepGreenWithWhiteText() {
        WebElement button = driver().findElement(By.cssSelector("form.login-card button[type=submit]"));
        Assert.assertEquals("rgb(14, 77, 60)", button.getCssValue("background-color").replace("rgba", "rgb").replace(", 1)", ")"));
        Assert.assertEquals("rgb(255, 255, 255)", button.getCssValue("color").replace("rgba", "rgb").replace(", 1)", ")"));
    }

    @Then("the page should use the Inter typefaces")
    public void thePageShouldUseTheInterTypefaces() {
        // Text is Inter, headings are Inter Tight; both are bundled with the app, so nothing is fetched from a font host.
        Boolean loaded = (Boolean) ((JavascriptExecutor) driver()).executeAsyncScript(
                "const done = arguments[arguments.length - 1];"
                + "document.fonts.ready.then(() => done(document.fonts.check('600 16px \"Inter Variable\"')"
                + " && document.fonts.check('800 16px \"Inter Tight Variable\"')));");
        String body = driver().findElement(By.tagName("body")).getCssValue("font-family").replace("\"", "");
        String heading = driver().findElement(By.cssSelector(".login-card h1")).getCssValue("font-family").replace("\"", "");
        Assert.assertTrue("body font-family " + body, body.startsWith("Inter Variable"));
        Assert.assertTrue("heading font-family " + heading, heading.startsWith("Inter Tight Variable"));
        Assert.assertTrue("The Inter fonts did not load", Boolean.TRUE.equals(loaded));
    }

    @When("I click the top bar logo")
    public void iClickTheTopBarLogo() {
        new NavBar(driver()).clickLogo();
    }

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
