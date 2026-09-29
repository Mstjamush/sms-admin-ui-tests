Feature: Users

  @C4040
  Scenario: A client administrator creates a user for their client
    Given I am logged in as a newly provisioned client administrator
    When I open the Users page
    And I create a new user with role "Sales Agent"
    Then the user I just created should appear in the user list

  @C4041
  Scenario: A Super Administrator does not see the "+ New user" button
    Given I am on the login page
    And I log in as the configured super admin
    And I should be on the dashboard
    When I open the Users page
    Then there should be no new-user button on the page

  @C4042
  Scenario: The role dropdown for a Client Administrator does not offer the Client Administrator role
    Given I am logged in as a newly provisioned client administrator
    When I open the Users page
    And I open the new user form
    Then the role dropdown should not offer "Client Administrator"

  @C4043
  Scenario: A user created by one client administrator is not visible to another
    Given a user has been provisioned for a different client
    And I am logged in as a newly provisioned client administrator
    When I open the Users page
    Then the user from the other client should not appear in the user list

  @C4044
  Scenario: The user list shows a Client column for a Super Administrator
    Given I am on the login page
    And I log in as the configured super admin
    And I should be on the dashboard
    When I open the Users page
    Then the user list should show a Client column

  @C4045
  Scenario: The user list does not show a Client column for a Client Administrator
    Given I am logged in as a newly provisioned client administrator
    When I open the Users page
    Then the user list should not show a Client column

  @C4046
  Scenario: The new user form blocks a password shorter than 6 characters
    Given I am logged in as a newly provisioned client administrator
    When I open the Users page
    And I try to create a new user with a short password
    Then the new user form should still be open
