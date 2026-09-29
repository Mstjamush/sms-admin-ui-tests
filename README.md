# sms-admin-ui – Selenium BDD Tests

Automated UI test suite for [`sms-admin-ui`](https://github.com/Mstjamush/sms-admin-ui) 

Two ways to run it against **TestRail**:
1. **`mvn test`** — runs everything, and reports each `@C<id>`-tagged scenario's result (with a screenshot attached on failure) to a TestRail run you created manually.
2. **`TestRailDrivenRunner`** — the fuller pipeline: **reads** the case list straight from your TestRail project, **creates** a run for exactly those cases, **executes** only the scenarios tagged for them, and **updates** TestRail with each result as it finishes.

---

## Prerequisites

| Tool | Minimum version |
|------|------------------|
| Java JDK | 17 |
| Apache Maven | 3.8+ |
| Google Chrome | any recent version (Selenium Manager resolves a matching chromedriver automatically) |
| Node.js | `sms-admin-ui`'s own requirement (see its `package.json` `engines`) |
| `bulksms-api` | running, with MySQL **and RabbitMQ** reachable (see its own README) |
| `sms-admin-ui` | running (`npm run dev`) |
| TestRail account | with API access enabled |

---

## Setup

### 1. Start bulksms-api

In `bulksms-api/` (see its README for full detail):

```bash
python -m app.migrate                   # builds a fresh DB too; re-run whenever bulksms-api changes
docker compose up -d rabbitmq           # optional - see "Run without RabbitMQ"
source .venv/bin/activate && pip install -r requirements.txt
cp .env.example .env
BOOTSTRAP_ADMIN_PASSWORD='ChangeMe123!' python seed_admin.py   # once - the Super Administrator this suite signs in as
uvicorn app.main:app --port 8000        # the API
python -m app.scheduler                 # scheduled campaigns (a second terminal)
```

### 2. Start sms-admin-ui

In `sms-admin-ui/`:

```bash
npm install
cp .env.example .env   # VITE_API_BASE_URL, defaults to http://localhost:8000
npm run dev            # Vite dev server on http://localhost:5173
```

### 3. Configure `testrail.properties`

Edit `src/test/resources/testrail.properties`:

```properties
testrail.url=https://YOUR_INSTANCE.testrail.io
testrail.username=your.email@company.com
testrail.api_key=your_testrail_api_key_here   # TestRail -> My Settings -> API Keys
testrail.project_id=1
testrail.suite_id=                            # only if your project uses multiple suites
testrail.run_id=                              # only needed for the "mvn test" flow, see below

ui.base.url=http://localhost:5173
api.base.url=http://localhost:8000

admin.super_admin.email=admin@bulksms-platform.com
admin.super_admin.password=ChangeMe123!

browser.headless=true                         # false to watch it run while debugging
```

> Don't commit real TestRail API keys if this project ends up on a shared remote. Every key above can also be set via an environment variable instead (dots → underscores, upper-cased: `testrail.api_key` → `TESTRAIL_API_KEY`) - see [`Config.java`](src/test/java/smsadminui/config/Config.java).

### 4. Install dependencies

```bash
mvn dependency:resolve
```

---

## Coverage

| Feature file | Cases | Covers |
|---|---|---|
| `login.feature`, `dashboard_nav.feature` | C4001-C4017 | login, role-based navigation and guards, logout, dashboard |
| `branding.feature` | C4170-C4174 | the **Jambopro** brand: login page logo and headline, page titles ("Campaigns · Jambopro"), brand green button with navy text, Poppins loaded, sidebar logo returns to the dashboard, favicon / ICO / Apple touch icon served |
| `clients.feature`, `users.feature`, `senders.feature` | C4020-C4046 | the original account screens |
| `bulk_campaigns.feature` | C4050-C4053 | quick send to typed numbers, the upload tab |
| `bulk_upload_formats.feature` | C4110-C4112 | uploading the three file shapes: detected format, template with placeholders and live preview (defaults for missing values), each row's own message, numbers only |
| `broadcast_lists.feature` | C4100-C4103 | list upload, import report, **duplicates listed with reason**, import history, `.xls` |
| `file_formats.feature` | C4104-C4107, C4113, C4160-C4163 | `.ods` / `.numbers` / `.xlsb` list uploads; every file picker accepts all seven formats; a personalised `.xlsx` detected and previewed on the Upload file tab; the **Export** menu offers all seven formats; exporting a list's numbers (`.xlsx`), the daily report (`.numbers`) and the statement (`.ods`) |
| `campaigns.feature` | C4120-C4122 | campaign builder: preview, estimated cost, schedule, cancel from the report, send now |
| `templates.feature` | C4130-C4131 | create, unique names |
| `credits_billing.feature` | C4140-C4144 | Credits page and balance chip, bundle request; Super Admin top-up, bundle approval, plans |
| `reports_api_access.feature` | C4150-C4154 | Reports page, API credentials, navigation, the signing guide documents HMAC-SHA256 (not X-Api-Key), changing your password from the top bar |

Every provisioned client starts with KES 1000 of credit (billing is enforced). Upload fixtures are in `src/test/resources/testdata/` and are attached with Selenium's `sendKeys` on the file input. The spreadsheet fixtures are copies of bulksms-api-tests' (see its `tools/make_fixtures.py`).

Export scenarios capture the download in the page: the app fetches the file exactly as it does for a user, but the step inspects the blob (name, size, type) instead of saving it, so no browser download directory is needed.

## Mapping scenarios to TestRail cases

Every scenario worth reporting is tagged `@C<id>`. The ids currently in the feature files (`C4001`–`C4003`, `C4010`–`C4012`, `C4020`–`C4022`, `C4030`–`C4031`, `C4040`–`C4041`, `C4050`–`C4051`) are **placeholders** — they won't mean anything until they match real case ids in your TestRail project. (Chosen in the `C4xxx` range specifically so they don't collide with `bulksms-api-tests`' `C3xxx` ids if both projects end up reported to the same TestRail project.)

Two ways to align them:

- **Create the cases in TestRail first** with these exact titles (see each `.feature` file's `Scenario:` lines), then find-and-replace the placeholder tag with the real id TestRail assigns, e.g. `@C4020` → `@C512`.
- **Run `TestRailDrivenRunner --dry-run`** (below) to print every case TestRail already has in the configured project/suite, then re-tag scenarios to match the ones you want this suite to own.

---

## Running the tests

### Run everything

```bash
mvn test
```

### Run one case / one area

```bash
mvn test -Dcucumber.filter.tags="@C4020"
mvn test -Dcucumber.filter.tags="@C4020 or @C4021 or @C4022"
```

### Run without RabbitMQ (e.g. local dev on this machine - see Known limitations)

```bash
mvn test -Dcucumber.filter.tags="not @ignore and not @requires-rabbitmq"
```

### Watch it run instead of headless

```bash
mvn test -Dbrowser.headless=false -Dcucumber.filter.tags="@C4020"
```

### Skip TestRail reporting (dry run, no TestRail side-effects)

```bash
mvn test -Dtestrail.disabled=true
```

For plain `mvn test` runs, results are posted against the fixed `testrail.run_id` in `testrail.properties` — create that run in TestRail first (include the cases you tagged above), copy its numeric id from the run's URL (`/runs/view/456` → `456`).

---

## The TestRail-driven pipeline

The "read cases from TestRail, execute them, update TestRail" flow end to end, with no manually-created run needed:

```bash
# Preview only - lists the cases TestRail returns and the tag filter that
# would be used, without creating a run or executing anything:
mvn test-compile exec:java -Dexec.mainClass=smsadminui.testrail.TestRailDrivenRunner -Dexec.args="--dry-run"

# The real thing:
mvn test-compile exec:java -Dexec.mainClass=smsadminui.testrail.TestRailDrivenRunner
```

What it does ([`TestRailDrivenRunner`](src/test/java/smsadminui/testrail/TestRailDrivenRunner.java)):

1. **Read** — calls TestRail's `get_cases` for `testrail.project_id` (+ `testrail.suite_id`), paging through all of them.
2. **Run** — opens a new TestRail run scoped to exactly those case ids, builds a Cucumber tag filter (`@C<id1> or @C<id2> or ...`) from them, and runs [`TestRunner`](src/test/java/smsadminui/runners/TestRunner.java) (the same suite `mvn test` runs) filtered to just those tags.
3. **Update** — as each scenario finishes, [`Hooks`](src/test/java/smsadminui/hooks/Hooks.java) posts its result (Passed/Failed, with elapsed time) to the run created in step 2, via [`TestRailClient`](src/test/java/smsadminui/testrail/TestRailClient.java) — with a screenshot attached to the result if the scenario failed.

If 0 scenarios match, it's almost always because the feature files' `@C<id>` tags don't line up with real case ids yet — see "Mapping scenarios to TestRail cases" above.

---

## Screenshots on failure

Every failed scenario's final browser state is captured twice ([`Hooks`](src/test/java/smsadminui/hooks/Hooks.java)):

- Embedded in the Cucumber HTML report (`target/cucumber-html-report/index.html`), inline with that scenario.
- Saved to `target/screenshots/` and, if `@C<id>`-tagged and a TestRail run is configured, attached to that scenario's TestRail result.

---

## Logs

Code calls **`org.jboss.logging.Logger`** throughout — the same facade `bulksms-api-tests` and the sibling `bulksms` Quarkus consumer use. It auto-detects SLF4J on the classpath and delegates to it, so `slf4j-api` + `logback-classic` do the actual work (console + rotating file, [`logback-test.xml`](src/test/resources/logback-test.xml)). Every HTTP call this suite makes (to `bulksms-api`'s admin API for fixtures, and to TestRail) is logged automatically via `ApiLoggingFilter`; Selenium's own driver chatter is left at WARN.

```bash
mvn test -Dsmsadminui.http.log.level=DEBUG   # request/response bodies for fixture + TestRail calls
mvn test -Dsmsadminui.log.level=DEBUG        # this suite's own step/hook logging
```

Logs land in `target/logs/sms-admin-ui-tests.log` (rotated by day, capped at 200MB total) alongside the console.

---

## Skipping scenarios

Tag any scenario `@ignore` to exclude it from `mvn test`'s default `not @ignore` filter:

```gherkin
@ignore
Scenario: Work in progress
  ...
```

---

## Known limitations

- **`@C4051` (sending a bulk campaign) needs RabbitMQ reachable**, tagged `@requires-rabbitmq` — same constraint as `bulksms-api-tests`' single-SMS/bulk-campaign scenarios (creating a campaign queues a real send per recipient). Exclude locally without it: `mvn test -Dcucumber.filter.tags="not @ignore and not @requires-rabbitmq"`.
- **No CI pipeline yet.** `bulksms-api-tests` has a GitHub Actions workflow (MySQL + RabbitMQ service containers, a live backend); this project would additionally need a Node/Vite dev server stage and a headless-Chrome-capable runner (GitHub's `ubuntu-latest` has Chrome preinstalled, so this is mostly wiring, not a blocker) - ask for it if/when wanted.
- **Bulk campaign upload mode (file upload, as opposed to typed-in recipients) isn't covered.** Only the "Insert recipients" tab is exercised.
- **No coverage of the Users screen's Super-Admin-side table contents** (only that the "+ New user" button is absent for that role) or of viewing another client's data as Super Admin.
- **Locators rely on this app's consistent `<label>Field name <input/></label>` markup** (see `BasePage.fieldByLabel`) rather than `data-testid` attributes, since the app has none yet - resilient to styling changes but would need updating if a field's label text changes.
