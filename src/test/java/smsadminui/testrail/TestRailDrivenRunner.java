package smsadminui.testrail;

import org.jboss.logging.Logger;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import smsadminui.config.Config;
import smsadminui.runners.TestRunner;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestRailDrivenRunner {

    private static final Logger LOG = Logger.getLogger(TestRailDrivenRunner.class);

    public static void main(String[] args) {
        boolean dryRun = Arrays.asList(args).contains("--dry-run");

        int projectId = Config.getInt("testrail.project_id");
        String suiteIdRaw = Config.get("testrail.suite_id");
        Integer suiteId = (suiteIdRaw == null || suiteIdRaw.isBlank()) ? null : Integer.parseInt(suiteIdRaw.trim());

        TestRailClient client = new TestRailClient();

        List<TestRailCase> cases = client.getCases(projectId, suiteId);
        if (cases.isEmpty()) {
            LOG.error("No cases found for this project/suite - nothing to run. Check "
                    + "testrail.project_id/testrail.suite_id in testrail.properties.");
            System.exit(1);
        }

        List<Integer> caseIds = cases.stream().map(TestRailCase::id).collect(Collectors.toList());
        String tagExpression = caseIds.stream().map(id -> "@C" + id).collect(Collectors.joining(" or "));

        for (TestRailCase c : cases) {
            LOG.infof("  C%d - %s", c.id(), c.title());
        }

        if (dryRun) {
            LOG.info("--dry-run: not creating a run or executing anything.");
            LOG.infof("Tag filter that would be used: %s", tagExpression);
            LOG.info("Tip: `grep -rlo '@C[0-9]*' src/test/resources/features` shows which of the above ids "
                    + "your feature files actually cover today.");
            return;
        }

        String runName = "Automated UI run " + LocalDateTime.now();
        int runId = client.addRun(projectId, suiteId, runName, caseIds);

        System.setProperty("cucumber.filter.tags", tagExpression);
        System.setProperty("testrail.run.id", String.valueOf(runId));

        Result result = JUnitCore.runClasses(TestRunner.class);

        LOG.infof("Ran %d scenario(s), %d failed.", result.getRunCount(), result.getFailureCount());
        for (Failure failure : result.getFailures()) {
            LOG.infof(" - %s", failure.getTestHeader());
        }
        LOG.infof("Results pushed to TestRail: %s/index.php?/runs/view/%d", Config.get("testrail.url"), runId);

        if (result.getRunCount() == 0) {
            LOG.warn("0 scenarios matched. None of this suite's feature files currently have a @C<id> tag for "
                    + "any case id fetched above - see the README's \"Mapping scenarios to TestRail cases\" section.");
        }

        System.exit(result.wasSuccessful() ? 0 : 1);
    }
}
