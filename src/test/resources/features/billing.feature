Feature: Billing - prepaid tokens, buying tokens, price bands, and postpaid usage and invoices

  Every client is prepaid (it buys tokens: 1 token sends 1 SMS page) or postpaid (it is invoiced). A Client Administrator has
  Billing (and Invoices when postpaid); a Super Administrator has Clients, Payments, Invoices, Price bands and M-Pesa.
  (Paying by M-Pesa calls Safaricom, so it isn't driven here.)

  @C4140
  Scenario: A prepaid client administrator sees their token balance on the Billing page and in the top bar
    Given I am logged in as a newly provisioned client administrator
    When I open the "/billing" page
    Then the page heading should read "Billing"
    And the "Token balance" tile should read "1,000"
    And the "Price per token" tile should read "KES 1.00"
    And the balance chip should show "1,000 tokens"

  @C4141
  Scenario: A client administrator asks to buy tokens by cash, bank or cheque, and the request waits
    Given I am logged in as a newly provisioned client administrator
    When I ask to buy KES "500" of tokens by cash, bank or cheque from the Billing page
    Then a success message should say "Request sent"
    And my purchases should include a waiting request for "KES 500.00"
    And the "Token balance" tile should read "1,000"

  @C4142
  Scenario: The Super Administrator adds tokens to a client from its page
    Given I am logged in as the super admin with a newly provisioned client
    When I open that client's page
    And I add "250" tokens to that client with the reason "Goodwill credit"
    Then a success message should say "added 250 tokens"
    And the "Token balance" tile should read "1,250"

  @C4143
  Scenario: The Super Administrator confirms a client's request to pay, and the tokens arrive
    Given I am logged in as the super admin with a newly provisioned client
    And that client has asked to pay KES "300" for tokens
    When I confirm that client's request to pay on the Payments page
    Then a success message should say "300 tokens added"
    When I open that client's page
    Then the "Token balance" tile should read "1,300"

  @C4144
  Scenario: The Super Administrator adds a price band and archives it again
    Given no price band from an earlier run is still open
    And I am logged in as the super admin
    When I add the price band "QA UI band" from "1" to "99" tokens at KES "2" per token
    Then that price band should be listed as active
    When I archive that price band
    Then a success message should say "archived"

  @C4175
  Scenario: A postpaid client administrator sees its bill, and has Invoices as well as Billing
    Given I am logged in as a newly provisioned postpaid client administrator
    Then the nav bar should show a "Billing" link
    And the nav bar should show a "Invoices" link
    When I open the "/billing" page
    Then the "Cost per message" tile should read "KES 0.50"
    And the "This period so far" tile should read "KES 0.00"
    And the balance chip should show "this period"

  @C4176
  Scenario: The Super Administrator sets a user's own cost per message from a postpaid client's page
    Given I am logged in as the super admin with a newly provisioned postpaid client
    And that postpaid client has sent a billed SMS
    When I open that client's page
    And I set the cost per message of that client's administrator to KES "0.80"
    Then a success message should say "cost per message saved"
    And the usage by user should show that administrator at KES "0.80" on their own rate

  @C4177
  Scenario: The Invoices page shows what is outstanding, overdue and paid
    Given I am logged in as the super admin
    When I open the "/invoices" page
    Then the page heading should read "Invoices"
    And the Invoices page should show the outstanding, overdue and paid tiles

  @C4178
  Scenario: The Super Administrator marks a postpaid client's invoice paid
    Given the database used by the API can be reached to age usage
    And I am logged in as the super admin with a newly provisioned postpaid client
    And an invoice has been issued to that postpaid client
    When I open the Invoices page for that client
    And I change the status of its first invoice to "Paid"
    Then a success message should say "is now paid"
    And the first invoice should be "Paid"

  @C4179
  Scenario: The M-Pesa page shows how M-Pesa is set up
    Given I am logged in as the super admin
    When I open the "/mpesa" page
    Then the page heading should read "M-Pesa"
    And the page should say "Callback URLs"
