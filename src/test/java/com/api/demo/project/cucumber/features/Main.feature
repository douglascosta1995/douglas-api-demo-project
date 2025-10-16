Feature: Main.feature

  Scenario: Creating a new User
    Given I get the token with payload "auth.json"
    And I prepare the request payload "newUser.json"
    When I send the request to the create a new user with name "Pedro" and email "pedro@test.com"
    Then the response status code should be 201
    And the response body should contain "Pedro"

  Scenario: Listing all users
    Given I get the token with payload "auth.json"
    When I send the GET request
    Then the response status code should be 200
    And all existing users are listed

  Scenario: Delete an user by ID
    Given I get the token with payload "auth.json"
    When I send the DELETE request with newest User ID
    Then the response status code should be 200
    And the user with newest ID is not present anymore

  Scenario: Get an user by ID
    Given I get the token with payload "auth.json"
    When I send the GET request with User ID "26"
    Then the response status code should be 200
    And the user with ID "26" is displayed

  Scenario: Update an user by ID
    Given I get the token with payload "auth.json"
    And I prepare the request payload "updateUser.json"
    When I send the PUT request with User ID "26"
    Then the response status code should be 200
    And the user is updated successfully