Feature: Login

  @C4001
  Scenario: Logging in with the correct credentials reaches the dashboard
    Given I am on the login page
    When I log in as the configured super admin
    Then I should be on the dashboard
    And the dashboard should welcome "Super Administrator"

  @C4002
  Scenario: Logging in with the wrong password shows an error and stays on the login page
    Given I am on the login page
    When I log in with email "admin@bulksms-platform.com" and password "definitely-wrong"
    Then I should see a login error
    And I should still be on the login page

  @C4003
  Scenario: The login form will not submit with empty fields
    Given I am on the login page
    When I submit the login form with both fields empty
    Then I should still be on the login page
    And I should not see a login error

  @C4004
  Scenario: Logging in with an email that has no account shows an error
    Given I am on the login page
    When I log in with email "no.such.admin.user@example.com" and password "whatever-password"
    Then I should see a login error
    And I should still be on the login page

  @C4005
  Scenario: The login form blocks a malformed email address natively
    Given I am on the login page
    When I log in with email "not-an-email" and password "whatever-password"
    Then I should still be on the login page
    And I should not see a login error

  @C4006
  Scenario: A Client Administrator logs in and sees their own client on the dashboard
    Given I am logged in as a newly provisioned client administrator
    Then the dashboard subtitle should mention my client
