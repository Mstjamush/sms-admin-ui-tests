package smsadminui.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"smsadminui.stepdefinitions", "smsadminui.hooks"},
        plugin = {"pretty", "html:target/cucumber-html-report", "json:target/cucumber.json"},
        monochrome = true,
        tags = "not @ignore"
)
public class TestRunner {
}
