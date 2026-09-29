Feature: Bulk Messages - uploading the three file shapes

  Background:
    Given I am logged in as a newly provisioned client administrator
    And a sender has been provisioned for my client
    When I open the Bulk Messages page
    And I switch to the Upload file tab

  @C4110
  Scenario: A personalised file is sent with a template built from its columns
    When I upload "personalised_list.csv" on the Upload file tab as campaign "QA loans"
    Then the detected format should be "Personalised columns found"
    When I write the upload message "Hi {{name}}, Please pay your loan of Kes.{{amount|the balance}}"
    Then the upload preview should show "Hi Wanjiku, Please pay your loan of Kes.1500"
    And the upload preview should show "Hi Achieng, Please pay your loan of Kes.the balance"
    When I send the upload
    Then a success message should confirm the campaign

  @C4111
  Scenario: A file with a message column sends each row's own text
    When I upload "per_row_messages.csv" on the Upload file tab as campaign "QA own messages"
    Then the detected format should be "Each row has its own message"
    And the upload preview should show "Your parcel ABC123 is ready for collection"
    When I send the upload
    Then a success message should confirm the campaign

  @C4112
  Scenario: A numbers-only file sends one message to every number
    When I upload "numbers_only.csv" on the Upload file tab as campaign "QA broadcast"
    Then the detected format should be "Numbers only"
    When I write the upload message "Offices closed on Monday"
    And I send the upload
    Then a success message should confirm the campaign
