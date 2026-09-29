Feature: Dashboard navigation, role-based guards, and logout

  @C4010
  Scenario: A Super Administrator sees the Clients nav link
    Given I am on the login page
    When I log in as the configured super admin
    Then I should be on the dashboard
    And the nav bar should show a Clients link
    And the nav bar role badge should read "Super Administrator"

  @C4011
  Scenario: A Client Administrator does not see the Clients nav link
    Given I am logged in as a newly provisioned client administrator
    Then the nav bar should not show a Clients link
    And the nav bar should show a Senders link
    And the nav bar should show a Bulk Messages link
    And the nav bar should show a Users link

  @C4012
  Scenario: Logging out returns to the login page and ends the session
    Given I am on the login page
    When I log in as the configured super admin
    Then I should be on the dashboard
    When I log out
    Then I should still be on the login page
    When I visit the dashboard directly
    Then I should still be on the login page

  @C4013
  Scenario: Clicking each nav link navigates to the right page
    Given I am on the login page
    When I log in as the configured super admin
    Then I should be on the dashboard
    When I click the Senders nav link
    Then the page URL should contain "/senders"
    When I click the Bulk Messages nav link
    Then the page URL should contain "/bulk-campaigns"
    When I click the Users nav link
    Then the page URL should contain "/users"
    When I click the Clients nav link
    Then the page URL should contain "/clients"

  @C4014
  Scenario: A Client Administrator is redirected away from the Clients page
    Given I am logged in as a newly provisioned client administrator
    When I visit the Clients page directly by URL
    Then I should be on the dashboard

  @C4015
  Scenario: Logging in from a redirected login preserves the originally requested page
    When I visit the Senders page directly by URL while logged out
    Then I should still be on the login page
    When I log in as the configured super admin
    Then I should be redirected to "/senders"

  @C4016
  Scenario: A Super Administrator sees the Clients card on the dashboard
    Given I am on the login page
    When I log in as the configured super admin
    Then I should be on the dashboard
    And the dashboard should show a Clients card

  @C4017
  Scenario: A Client Administrator does not see the Clients card on the dashboard
    Given I am logged in as a newly provisioned client administrator
    Then the dashboard should not show a Clients card
