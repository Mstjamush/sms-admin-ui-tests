Feature: Senders (Client Administrator, scoped to their own client)

  @C4030
  Scenario: Registering a sender shows it in the sender list, deactivated pending approval
    Given I am logged in as a newly provisioned client administrator
    When I open the Senders page
    And I create a new sender with a unique short code
    Then the sender I just created should appear in the sender list
    And its approval status should read "Sender ID Deactivated"

  @C4031
  Scenario: The new sender form will not submit with the short code empty
    Given I am logged in as a newly provisioned client administrator
    When I open the Senders page
    And I try to create a new sender with no short code
    Then the new sender form should still be open

  @C4032
  Scenario: A sender created by one client administrator is not visible to another
    Given a sender has been provisioned for a different client
    And I am logged in as a newly provisioned client administrator
    When I open the Senders page
    Then the sender from the other client should not appear in the sender list

  @C4033
  Scenario: Cancel hides the new sender form again
    Given I am logged in as a newly provisioned client administrator
    When I open the Senders page
    And I open the new sender form
    Then the new sender form should still be open
    When I cancel the new sender form
    Then the new sender form should be closed
