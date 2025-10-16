package com.api.demo.project.cucumber.steps;

import com.api.demo.project.helpers.PayloadBuilder;
import com.api.demo.project.storage.MainResponseStorage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;

public class MainSteps {

    @Autowired
    private PayloadBuilder payloadBuilder;

    @Autowired
    private MainResponseStorage mainResponseStorage;

    @Value("${URL}")
    private String baseUrl;

    @Value("${USERS}")
    private String USERS_ENDPOINT;

    @Value("${TOKEN}")
    private String TOKEN_ENDPOINT;

    @Value("${USERS_BY_ID}")
    private String USERS_BY_ID_ENDPOINT;


    @Given("I get the token with payload {string}")
    public void given_iGetTheTokenWithPayload(String fileName) {
        String payload = payloadBuilder.prepareRequestPayload(fileName);
        Response response = RestAssured
                .given()
                .baseUri(baseUrl) // baseUrl injected or read from properties
                .header("Content-Type", "application/json")
                .body(payload)
                .when()
                .post(TOKEN_ENDPOINT);

        mainResponseStorage.setBearerToken(response.jsonPath().getString("token"));
    }

    @And("I prepare the request payload {string}")
    public void and_IPrepareTheRequestPayload(String fileName) {
        String payload = payloadBuilder.prepareRequestPayload(fileName);
        mainResponseStorage.setPayload(payload);
    }

    @When("I send the request to the create a new user with name {string} and email {string}")
    public void when_iSendTheRequestToTheCreateANewUser(String name, String email) throws JsonProcessingException {
        String payload = mainResponseStorage.getPayload();

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = (ObjectNode) objectMapper.readTree(payload);

        objectNode.put("name", name);
        objectNode.put("email", email);

        String updatedPayload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
        Response response = RestAssured
                .given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + mainResponseStorage.getBearerToken())
                .contentType(ContentType.JSON)
                .body(updatedPayload)
                .when()
                .post(USERS_ENDPOINT);

        //System.out.println(response.jsonPath().getInt("id"));
        mainResponseStorage.setNewestCreatedUser(response.jsonPath().getString("id"));
        mainResponseStorage.setResponseFull(response);
        //mainResponseStorage.setResponseCode(response.getStatusCode());
    }

    @Then("the response status code should be {int}")
    public void then_theResponseStatusCodeShouldBe(int expected_code) {
        Assert.assertEquals(expected_code, mainResponseStorage.getResponseFull().getStatusCode());
    }

    @And("the response body should contain {string}")
    public void and_theResponseBodyShouldContain(String expected_name) {
        String actual_name = mainResponseStorage.getResponseFull().jsonPath().getString("name");
        Assert.assertEquals(expected_name, actual_name);
    }

    @When("I send the GET request")
    public void when_iSendTheGetRequest() {

        Response response = RestAssured
                .given()
                .baseUri(baseUrl) // baseUrl injected or read from properties
                .header("Authorization", "Bearer " + mainResponseStorage.getBearerToken())
                .when()
                .get(USERS_ENDPOINT);

        mainResponseStorage.setResponseFull(response);
        System.out.println(response.asString());
    }

    @Then("all existing users are listed")
    public void then_allExistingUsersAreListed() {
        List<Map<String, Object>> users = mainResponseStorage.getResponseFull().jsonPath().getList("$");

        Assert.assertFalse(users.isEmpty());

        int nameCount = mainResponseStorage.getResponseFull().jsonPath().getList("name").size();
        System.out.println("Number of names: " + nameCount);
        Assert.assertTrue(nameCount>0);
    }

    @When("I send the DELETE request with newest User ID")
    public void when_ISendTheDeleteRequestWithNewestUserId() {
        String id = mainResponseStorage.getNewestCreatedUser();
        String userByID = USERS_BY_ID_ENDPOINT.replace("<user_id>", id);
        Response response = RestAssured
                .given()
                .baseUri(baseUrl) // baseUrl injected or read from properties
                .header("Authorization", "Bearer " + mainResponseStorage.getBearerToken())
                .when()
                .delete(userByID);

        mainResponseStorage.setResponseFull(response);
        System.out.println(response.asString());
    }

    @Then("the user with newest ID is not present anymore")
    public void then_theUserWithNewestIdIsNotPresentAnymore() {
        String id = mainResponseStorage.getNewestCreatedUser();
        String userByID = USERS_BY_ID_ENDPOINT.replace("<user_id>", id);
        Response response = RestAssured
                .given()
                .baseUri(baseUrl) // baseUrl injected or read from properties
                .header("Authorization", "Bearer " + mainResponseStorage.getBearerToken())
                .when()
                .delete(userByID);

        mainResponseStorage.setResponseFull(response);
        Assert.assertEquals(404, response.getStatusCode());
    }

    @When("I send the GET request with User ID {string}")
    public void when_ISendTheGetRequestWithUserId(String string) {
        String userByID = USERS_BY_ID_ENDPOINT.replace("<user_id>", string);
        System.out.println(userByID);
        Response response = RestAssured
                .given()
                .baseUri(baseUrl) // baseUrl injected or read from properties
                .header("Authorization", "Bearer " + mainResponseStorage.getBearerToken())
                .when()
                .get(userByID);

        mainResponseStorage.setResponseFull(response);
        System.out.println(response.asString());
    }

    @Then("the user with ID {string} is displayed")
    public void then_TheUserWithIdIsDisplayed(String id) {
        int actual_id = mainResponseStorage.getResponseFull().jsonPath().getInt("id");
        Assert.assertEquals(actual_id,Integer.parseInt(id));
    }

    @When("I send the PUT request with User ID {string}")
    public void when_ISendThePutRequestWithUserId(String id) {
        String userByID = USERS_BY_ID_ENDPOINT.replace("<user_id>", id);
        String payload = mainResponseStorage.getPayload();
        Response response = RestAssured
                .given()
                .baseUri(baseUrl) // baseUrl injected or read from properties
                .header("Authorization", "Bearer " + mainResponseStorage.getBearerToken())
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .put(userByID);

        mainResponseStorage.setResponseFull(response);
        System.out.println(response.asString());
    }

    @And("the user is updated successfully")
    public void theUserIsUpdatedSuccessfully() {
        String message = mainResponseStorage.getResponseFull().jsonPath().getString("message");

        Assert.assertEquals("User updated successfully", message);


    }
}