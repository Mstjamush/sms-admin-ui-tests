Feature: JamboPro brand - name, logo, titles, colours and icons

  @C4170
  Scenario: The login page carries the JamboPro brand
    Given I am on the login page
    Then the page title should be "Sign in · JamboPro"
    And the JamboPro logo should be shown
    And the login page should say "Send once. Know who got it."

  @C4171
  Scenario: Brand colours and typefaces are applied
    Given I am on the login page
    Then the sign-in button should be deep green with white text
    And the page should use the Inter typefaces

  @C4172
  Scenario: Every screen is titled after itself and JamboPro
    Given I am logged in as a newly provisioned client administrator
    When I open the "/campaigns" page as a signed-in user
    Then the page title should be "Campaigns · JamboPro"
    When I open the "/lists" page as a signed-in user
    Then the page title should be "Contacts · JamboPro"

  @C4173
  Scenario: The top bar logo returns to the dashboard
    Given I am logged in as a newly provisioned client administrator
    When I open the "/reports" page as a signed-in user
    Then the JamboPro logo should be shown
    When I click the top bar logo
    Then the page title should be "Dashboard · JamboPro"

  @C4174
  Scenario: The JamboPro icons are served
    Given I am on the login page
    Then "/favicon.svg" should be served as "image/svg+xml"
    And "/favicon.ico" should be served as "image/x-icon"
    And "/apple-touch-icon.png" should be served as "image/png"
