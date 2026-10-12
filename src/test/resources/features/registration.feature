Feature: Company registration and approval

  Anyone can register a company on /register, prepaid (on a price band) or postpaid (invoiced daily, weekly, every two weeks or
  monthly). The company waits for a Super Administrator, and nobody in it can sign in until it is approved.

  @C4180
  Scenario: A company registers on the Register page and is told it is waiting for approval
    When I register a company on the Register page as prepaid on the "Starter" price band
    Then I should be told that my company is registered and waiting for approval

  @C4181
  Scenario: A registered company's administrator can't sign in until it is approved
    Given a company has registered and is waiting for approval
    When the registered company's administrator tries to sign in
    Then the sign-in should be refused, saying "waiting for approval"

  @C4182
  Scenario: The Super Administrator approves a registration, and the company's administrator can then sign in
    Given a company has registered and is waiting for approval
    And I am logged in as the super admin
    When I open the clients waiting for approval
    And I approve that company
    Then a success message should say "is approved"
    And that company should be listed as "Active"
    When I log out
    Then the registered company's administrator can sign in

  @C4183
  Scenario: Rejecting a registration needs a reason, and the company's administrator is told it
    Given a company has registered and is waiting for approval
    And I am logged in as the super admin
    When I open the clients waiting for approval
    And I reject that company because "Not a registered business"
    Then that company should be listed as "Rejected"
    When I log out
    And the registered company's administrator tries to sign in
    Then the sign-in should be refused, saying "Not a registered business"

  @C4184
  Scenario: A postpaid registration can't be approved without a cost per message
    Given a postpaid company has registered and is waiting for approval
    And I am logged in as the super admin
    When I open the clients waiting for approval
    And I start approving that company
    Then the Approve button should be disabled until a cost per message is set

  @C4185
  Scenario: A postpaid company approved at a cost per message can sign in
    Given a postpaid company has registered and is waiting for approval
    And I am logged in as the super admin
    When I open the clients waiting for approval
    And I approve that company at KES "0.40" per SMS
    Then a success message should say "is approved"
    When I log out
    Then the registered company's administrator can sign in
