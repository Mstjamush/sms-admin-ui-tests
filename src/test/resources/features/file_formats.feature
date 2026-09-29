Feature: Spreadsheet formats - upload any of seven, export in any of seven

  Uploads take .csv, .xlsx, .xlsm, .xlsb, .xls, .ods and .numbers; every
  Export menu offers the same seven.

  Background:
    Given I am logged in as a newly provisioned client administrator

  @C4104
  Scenario: An OpenDocument .ods file can be uploaded as a broadcast list
    When I upload "personalised_list.ods" as a broadcast list through the Broadcast lists page
    Then the import report should show 3 added, 0 duplicates skipped and 0 invalid
    And the new list should appear in the broadcast lists table with placeholders "name, amount"

  @C4105
  Scenario: An Apple Numbers file can be uploaded as a broadcast list
    When I upload "personalised_list.numbers" as a broadcast list through the Broadcast lists page
    Then the import report should show 3 added, 0 duplicates skipped and 0 invalid
    And the new list should appear in the broadcast lists table with placeholders "name, amount"

  @C4106
  Scenario: A binary Excel .xlsb file can be uploaded as a broadcast list
    When I upload "personalised_list.xlsb" as a broadcast list through the Broadcast lists page
    Then the import report should show 3 added, 0 duplicates skipped and 0 invalid

  @C4107
  Scenario: The Upload list dialog accepts every supported format
    When I open the Upload list dialog
    Then the dialog's file picker should accept "csv, xlsx, xlsm, xlsb, xls, ods, numbers"

  @C4113
  Scenario: A personalised .xlsx file is detected and previewed on the Upload file tab
    Given a sender has been provisioned for my client
    When I open the Bulk Messages page
    And I switch to the Upload file tab
    Then the file picker should accept "csv, xlsx, xlsm, xlsb, xls, ods, numbers"
    When I upload "personalised_list.xlsx" on the Upload file tab as campaign "QA xlsx loans"
    Then the detected format should be "Personalised columns found"
    When I write the upload message "Hi {{name}}, Please pay your loan of Kes.{{amount|the balance}}"
    Then the upload preview should show "Hi Wanjiku, Please pay your loan of Kes.1500"

  @C4160
  Scenario: A list's Export numbers menu offers all seven formats
    When I upload "personalised_list.csv" as a broadcast list through the Broadcast lists page
    And I open the new list
    And I open the "Export numbers" export menu
    Then the export menu should offer "csv, xlsx, xlsm, xlsb, xls, ods, numbers"

  @C4161
  Scenario: Exporting a list's numbers as .xlsx downloads the workbook
    When I upload "personalised_list.csv" as a broadcast list through the Broadcast lists page
    And I open the new list
    And I open the "Export numbers" export menu
    And I export as "xlsx"
    Then a file ending "-members.xlsx" should have been downloaded

  @C4162
  Scenario: The daily report exports as Apple Numbers from the Reports page
    When I open the "/reports" page
    And I open the "Export" export menu
    And I export as "numbers"
    Then a file ending "daily-report-30d.numbers" should have been downloaded

  @C4163
  Scenario: The statement exports as .ods from the Credits page
    When I open the "/credits" page
    And I open the "Export" export menu
    And I export as "ods"
    Then a file ending ".ods" should have been downloaded
