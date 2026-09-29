package smsadminui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Locators for the shared UI building blocks (ModalDialog, ToastHost,
 * StatTile, buttons by visible text) so feature page objects don't each
 * re-derive them.
 */
public class UiKit extends BasePage {

    private static final By MODAL = By.cssSelector("[role=dialog]");
    private static final By TOAST_SUCCESS = By.cssSelector(".toast.success .text");

    public UiKit(WebDriver driver) {
        super(driver);
    }

    public void clickButton(String text) {
        click(By.xpath("//button[normalize-space(.)=" + literal(text) + "] | //a[contains(@class,'button')][normalize-space(.)=" + literal(text) + "]"));
    }

    public void clickModalButton(String text) {
        click(By.xpath("//*[@role='dialog']//button[normalize-space(.)=" + literal(text) + "]"));
    }

    public void typeInto(String label, String text) {
        type(fieldByLabel(label), text);
    }

    public void typeIntoModal(String label, String text) {
        WebElement el = find(By.xpath("//*[@role='dialog']//label[contains(normalize-space(.), " + literal(label)
                + ")]/*[self::input or self::select or self::textarea]"));
        el.clear();
        el.sendKeys(text);
    }

    public void chooseFile(String absolutePath) {
        driver.findElement(By.cssSelector("input[type=file]")).sendKeys(absolutePath);
    }

    public void chooseFileInModal(String absolutePath) {
        driver.findElement(By.cssSelector("[role=dialog] input[type=file]")).sendKeys(absolutePath);
    }

    public boolean modalOpen() {
        return isPresent(MODAL);
    }

    public void waitForModalToClose() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(MODAL));
    }

    public String modalError() {
        return find(By.cssSelector("[role=dialog] p.error")).getText();
    }

    public String successToast() {
        return find(TOAST_SUCCESS).getText();
    }

    /** A StatTile's value, by its label. */
    public String tileValue(String label) {
        return find(By.xpath("//div[contains(@class,'tile')][div[@class='label' and normalize-space(.)=" + literal(label)
                + "]]/div[@class='value']")).getText();
    }

    public String heading() {
        return find(By.cssSelector(".page h1")).getText();
    }

    public boolean waitForText(String text) {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[contains(normalize-space(text()), " + literal(text) + ")]")));
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    public List<String> texts(By locator) {
        return findAll(locator).stream().map(WebElement::getText).toList();
    }

    public void open(String path) {
        navigateTo(path);
        find(By.cssSelector(".page h1"));
    }

    static String literal(String value) {
        if (!value.contains("'")) return "'" + value + "'";
        return "\"" + value + "\"";
    }
}
