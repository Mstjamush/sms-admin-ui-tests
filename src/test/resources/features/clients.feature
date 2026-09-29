Feature: Clients (Super Administrator only)

  Background:
    Given I am on the login page
    And I log in as the configured super admin
    And I should be on the dashboard

  @C4020
  Scenario: Creating a client shows it in the client list
    When I open the Clients page
    And I create a new client with a unique name
    Then the client I just created should appear in the client list

  @C4021
  Scenario: The new client form will not submit with the client name empty
    When I open the Clients page
    And I try to create a new client with no name
    Then the new client form should still be open

  @C4022
  Scenario: Creating a Client Administrator for a client succeeds
    When I open the Clients page
    And I create a new client with a unique name
    Then the client I just created should appear in the client list
    When I create a Client Administrator for the client I just created
    Then there should be no page error

  @C4023
  Scenario: Creating a client with an email the backend rejects shows a page error
    When I open the Clients page
    And I try to create a new client with an email the backend will reject
    Then there should be a page error

  @C4024
  Scenario: Cancel hides the new client form again
    When I open the Clients page
    And I open the new client form
    Then the new client form should still be open
    When I cancel the new client form
    Then the new client form should be closed

  @C4025
  Scenario: The Client Administrator form blocks a password shorter than 6 characters
    When I open the Clients page
    And I create a new client with a unique name
    Then the client I just created should appear in the client list
    When I try to create a Client Administrator with a short password
    Then the Client Administrator form should still be open
