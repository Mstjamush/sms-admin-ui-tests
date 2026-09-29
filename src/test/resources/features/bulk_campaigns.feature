Feature: Bulk campaigns (Client Administrator, scoped to their own client)

  @C4050
  Scenario: The bulk campaigns page prompts for a sender first, when none exists yet
    Given I am logged in as a newly provisioned client administrator
    When I open the Bulk Messages page
    Then I should be prompted to create a sender first

  # Creating a campaign queues a real outbound_messages row + sms.outbound
  # publish per recipient (see bulksms-api's create_bulk_campaign), so this
  # needs RabbitMQ reachable the same way single-SMS sending does.
  @C4051 @requires-rabbitmq
  Scenario: A client administrator sends a bulk campaign against their own sender
    Given I am logged in as a newly provisioned client administrator
    And a sender has been provisioned for my client
    When I open the Bulk Messages page
    And I send a bulk campaign with a unique name to "254712345678, 254798765432"
    Then the campaign I just created should appear in the campaign list

  @C4052
  Scenario: Switching to the Upload file tab shows the file upload form
    Given I am logged in as a newly provisioned client administrator
    And a sender has been provisioned for my client
    When I open the Bulk Messages page
    And I switch to the Upload file tab
    Then a recipients file input should be shown

  @C4053
  Scenario: The insert form blocks submission with recipients left empty
    Given I am logged in as a newly provisioned client administrator
    And a sender has been provisioned for my client
    When I open the Bulk Messages page
    And I try to send a campaign with no recipients
    Then no campaign should have been created
