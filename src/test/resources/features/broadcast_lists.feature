Feature: Broadcast lists - upload, duplicate reporting, import history

  Background:
    Given I am logged in as a newly provisioned client administrator

  @C4100
  Scenario: Uploading a personalised list shows the import report and its placeholders
    When I upload "personalised_list.csv" as a broadcast list through the Broadcast lists page
    Then the import report should show 3 added, 0 duplicates skipped and 0 invalid
    And the new list should appear in the broadcast lists table with placeholders "name, amount"

  @C4101
  Scenario: Duplicate numbers are skipped and listed with the reason
    When I upload "duplicates_list.csv" as a broadcast list through the Broadcast lists page
    Then the import report should show 2 added, 2 duplicates skipped and 1 invalid
    And the duplicates section should list "254712300101" as "repeated in this file"

  @C4102
  Scenario: A list's page shows its import history with duplicate counts
    When I upload "duplicates_list.csv" as a broadcast list through the Broadcast lists page
    And I open the new list
    Then its import history should show the file with 2 duplicates

  @C4103
  Scenario: A legacy Excel .xls file can be uploaded
    When I upload "loans.xls" as a broadcast list through the Broadcast lists page
    Then the import report should show 2 added, 0 duplicates skipped and 0 invalid
