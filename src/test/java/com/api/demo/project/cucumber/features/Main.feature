Feature: Main.feature

  Scenario: Creating a new User

    Given I get the token with payload "auth.json"
    And I prepare the request payload "newUser.json"
    When I send the request to the create a new user with name "Paulo" and email "paulo@test.com"
    Then the response status code should be 201
    And the response body should contain "Paulo"

