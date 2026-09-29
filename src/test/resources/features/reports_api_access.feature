Feature: Reports and API access

  Background:
    Given I am logged in as a newly provisioned client administrator

  @C4150
  Scenario: The Reports page shows a row per day of the chosen period
    When I open the "/reports" page
    Then the daily breakdown should have 30 rows
    And the nav bar should show a "Reports" link

  @C4151
  Scenario: The API access page shows the client's credentials
    When I open the "/api-access" page
    Then my API client id should be shown and the token revealable

  @C4152
  Scenario: The navigation has the campaign and billing areas
    Then the nav bar should show a "Campaigns" link
    And the nav bar should show a "Broadcast lists" link
    And the nav bar should show a "Templates" link
    And the nav bar should show a "Credits" link

  @C4153
  Scenario: The API access page documents HMAC request signing
    When I open the "/api-access" page
    Then the signing guide should describe "HMAC-SHA256" and show an example using "hmac.new(API_TOKEN.encode()"

  @C4154
  Scenario: I can change my password from the top bar
    When I change my password through the Password link
    Then a success message should say "Password changed"
    And I can log out and sign in again with the new password

