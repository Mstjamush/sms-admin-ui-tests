Feature: Credits (client) and Billing (Super Administrator)

  @C4140
  Scenario: A client administrator sees their wallet on the Credits page and in the top bar
    Given I am logged in as a newly provisioned client administrator
    When I open the "/credits" page
    Then the "Wallet" tile should read "KES 1,000.00"
    And the balance chip should show "KES 1,000.00"

  @C4141
  Scenario: A client administrator can request a bundle
    Given I am logged in as a newly provisioned client administrator
    When I request a bundle of KES "500" from the Credits page
    Then my bundle requests should include a pending request for "500.00"

  @C4142
  Scenario: The Super Administrator tops up a client's wallet
    Given I am logged in as the super admin with a newly provisioned client
    When I open that client's billing
    And I top up that client with KES "250"
    Then a success message should say "Topped up KES 250.00"
    And the "Wallet" tile should read "KES 1,250.00"

  @C4143
  Scenario: The Super Administrator approves a bundle request at a negotiated rate
    Given I am logged in as the super admin with a newly provisioned client
    And that client has requested a bundle of KES "100"
    When I approve that client's bundle request at KES "0.50" per SMS
    Then a success message should say "Approved - bundle active"

  @C4144
  Scenario: The Super Administrator creates a plan
    Given I am logged in as the super admin with a newly provisioned client
    When I create a plan "QA Standard" at KES "0.80" per SMS with "100" SMS
    Then the plan should be listed
