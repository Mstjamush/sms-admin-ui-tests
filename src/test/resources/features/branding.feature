Feature: Jambopro brand - name, logo, titles, colours and icons

  @C4170
  Scenario: The login page carries the Jambopro brand
    Given I am on the login page
    Then the page title should be "Sign in · Jambopro"
    And the Jambopro logo should be shown
    And the login page should say "Reach. Engage. Grow."

  @C4171
  Scenario: Brand colours and typeface are applied
    Given I am on the login page
    Then the sign-in button should be brand green with navy text
    And the page should use the Poppins typeface

  @C4172
  Scenario: Every screen is titled after itself and Jambopro
    Given I am logged in as a newly provisioned client administrator
    When I open the "/campaigns" page as a signed-in user
    Then the page title should be "Campaigns · Jambopro"
    When I open the "/lists" page as a signed-in user
    Then the page title should be "Broadcast lists · Jambopro"

  @C4173
  Scenario: The sidebar logo returns to the dashboard
    Given I am logged in as a newly provisioned client administrator
    When I open the "/reports" page as a signed-in user
    Then the Jambopro logo should be shown
    When I click the sidebar logo
    Then the page title should be "Dashboard · Jambopro"

  @C4174
  Scenario: The Jambopro icons are served
    Given I am on the login page
    Then "/favicon.svg" should be served as "image/svg+xml"
    And "/favicon.ico" should be served as "image/x-icon"
    And "/apple-touch-icon.png" should be served as "image/png"
