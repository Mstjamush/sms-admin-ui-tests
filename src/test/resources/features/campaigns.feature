Feature: Campaign builder - schedule, send, report, cancel

  Background:
    Given I am logged in as a newly provisioned client administrator
    And a sender has been provisioned for my client
    And a broadcast list "personalised_list.csv" has been uploaded for my client

  @C4120
  Scenario: Scheduling a personalised campaign shows the cost, then waits as Scheduled
    When I start a campaign to that list with message "Hi {{name}}, KES {{amount|0}} is due"
    Then the wizard preview should show "Hi Wanjiku, KES 1500 is due"
    When I schedule it for tomorrow and confirm
    Then the review step should have shown an estimated cost of "KES 3.00"
    And the campaign report should show the status "Scheduled"

  @C4121
  Scenario: A scheduled campaign can be cancelled from its report
    When I start a campaign to that list with message "Hello {{name}}"
    And I schedule it for tomorrow and confirm
    Then the campaign report should show the status "Scheduled"
    When I cancel the campaign from its report
    Then the campaign report should show the status "Cancelled"

  @C4122
  Scenario: A send-now campaign is dispatched and listed
    When I start a campaign to that list with message "Hello {{name}}"
    And I send it now and confirm
    Then the campaign report should show the status "Dispatched"
    And the campaign should be listed on the Campaigns page
