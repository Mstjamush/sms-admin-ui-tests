package smsadminui.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.jboss.logging.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import smsadminui.config.Config;
import smsadminui.stepdefinitions.TestContext;
import smsadminui.testrail.TestRailClient;
import smsadminui.utils.WebDriverFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Drives the WebDriver lifecycle (one Chrome instance per scenario), embeds
 * a screenshot in the Cucumber report on failure, and reports every
 * @C<id>-tagged scenario's outcome (with that screenshot attached) to
 * TestRail. Used by both entry points:
 *  - "mvn test": reports against the fixed testrail.run_id from testrail.properties.
 *  - TestRailDrivenRunner: reports against the run it just created, passed
 *    via the "testrail.run.id" system property (takes precedence).
 * Scenarios with no @C<id> tag are silently skipped.
 */
public class Hooks {

    private static final Logger LOG = Logger.getLogger(Hooks.class);
    private static final TestRailClient testRail = new TestRailClient();

    private long startedAtMs;

    @Before
    public void beforeScenario(Scenario scenario) {
        TestContext.clear();
        smsadminui.stepdefinitions.ScenarioData.clear();
        startedAtMs = System.currentTimeMillis();
        LOG.infof("--- Starting: %s [%s] ---", scenario.getName(), String.join(" ", scenario.getSourceTagNames()));
        WebDriverFactory.getDriver(); // starts the browser for this scenario
    }

    @After
    public void afterScenario(Scenario scenario) {
        long elapsedSeconds = (System.currentTimeMillis() - startedAtMs) / 1000;
        LOG.infof("--- Finished: %s -> %s (%ds) ---", scenario.getName(), scenario.getStatus(), elapsedSeconds);

        File screenshotFile = scenario.isFailed() ? captureScreenshot(scenario) : null;

        WebDriverFactory.quitDriver();

        if (!Boolean.parseBoolean(System.getProperty("testrail.disabled", "false"))) {
            reportToTestRail(scenario, elapsedSeconds, screenshotFile);
        }
    }

    private File captureScreenshot(Scenario scenario) {
        WebDriver driver = WebDriverFactory.getDriver();
        try {
            byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            scenario.attach(bytes, "image/png", scenario.getName()); // embedded in the Cucumber HTML report

            Path dir = Path.of("target", "screenshots");
            Files.createDirectories(dir);
            String safeName = scenario.getName().replaceAll("[^a-zA-Z0-9]+", "_");
            File file = dir.resolve(safeName + "_" + System.currentTimeMillis() + ".png").toFile();
            Files.write(file.toPath(), bytes);
            return file;
        } catch (IOException | RuntimeException e) {
            LOG.error("Failed to capture/save failure screenshot", e);
            return null;
        }
    }

    private void reportToTestRail(Scenario scenario, long elapsedSeconds, File screenshotFile) {
        int caseId = extractTestRailCaseId(scenario);
        if (caseId == -1) return;

        Integer runId = resolveRunId();
        if (runId == null) {
            LOG.warnf("TestRail: no run configured (testrail.run.id system property or "
                    + "testrail.run_id in testrail.properties) - skipping report for case %d", caseId);
            return;
        }

        int statusId = scenario.isFailed() ? 5 : 1; // 1 = Passed, 5 = Failed
        String comment = scenario.isFailed()
                ? scenario.getName() + " FAILED"
                : scenario.getName() + " PASSED";

        int resultId = testRail.addResultForCase(runId, caseId, statusId, comment, elapsedSeconds);
        if (screenshotFile != null) {
            testRail.addAttachmentToResult(resultId, screenshotFile);
        }
    }

    private Integer resolveRunId() {
        String dynamic = System.getProperty("testrail.run.id");
        if (dynamic != null && !dynamic.isBlank()) return Integer.parseInt(dynamic.trim());

        String configured = Config.get("testrail.run_id");
        if (configured == null || configured.isBlank()) return null;
        return Integer.parseInt(configured.trim());
    }

    private int extractTestRailCaseId(Scenario scenario) {
        for (String tag : scenario.getSourceTagNames()) {
            if (tag.startsWith("@C")) {
                try {
                    return Integer.parseInt(tag.substring(2));
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }
}
