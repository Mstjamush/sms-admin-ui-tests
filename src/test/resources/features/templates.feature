Feature: Message templates

  Background:
    Given I am logged in as a newly provisioned client administrator

  @C4130
  Scenario: Creating a template adds it to the templates table
    When I create a template "QA reminder" with message "Hi {{name|there}}, your payment is due"
    Then the template should appear in the templates table

  @C4131
  Scenario: Template names must be unique
    When I create a template "QA dup" with message "First"
    Then the template should appear in the templates table
    When I create another template with the same name
    Then the template dialog should say "already exists"
