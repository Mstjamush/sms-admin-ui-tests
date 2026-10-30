Feature: The landing and pricing pages, and the language switch

  Anyone can open / and /pricing without signing in. A signed-in person is taken to their dashboard instead. The language
  switch (English / French) is in the corner of those pages, of sign-in and of the top bar. These scenarios have no TestRail
  case id yet: add an @C tag to each once its case exists.

  Scenario: The landing page says what the product does and where to go next
    When I open the landing page
    Then the page headline should read "Send once. Know who got it."
    And the page should offer "Pricing"
    And the page should offer "Sign in"
    And the page should offer "Request access"
    And the example campaign should show "9,200"

  Scenario: The landing page can be read in French, and back in English
    When I open the landing page
    And I switch the language to "FR"
    Then the page headline should read "Envoyez une fois. Sachez qui l'a reçu."
    And the page should offer "Se connecter"
    When I switch the language to "EN"
    Then the page headline should read "Send once. Know who got it."

  Scenario: The pricing page explains how a client pays
    When I open the landing page
    And I follow the "Pricing" link
    Then the page URL should contain "/pricing"
    And the page headline should read "Simple, honest pricing"

  Scenario: Request access leads to the registration form
    When I open the landing page
    And I follow the "Request access" link
    Then the page URL should contain "/register"

  Scenario: A signed-in person is taken from the landing page to their dashboard
    Given I am logged in as a newly provisioned client administrator
    When I visit the landing page directly
    Then I should be on the dashboard
