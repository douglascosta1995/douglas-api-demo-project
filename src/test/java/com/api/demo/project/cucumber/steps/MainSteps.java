package com.api.demo.project.cucumber.steps;

import com.api.demo.project.helpers.PayloadBuilder;
import com.api.demo.project.storage.MainResponseStorage;
import com.api.demo.project.storage.Requests;
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
    private Requests requests;

    @Value("${URL}")
    private String baseUrl;

    @Value("${USERS}")
    private String USERS_ENDPOINT;

    @Value("${TOKEN}")
    private String TOKEN_ENDPOINT;

    @Value("${USERS_BY_ID}")
    private String USERS_BY_ID_ENDPOINT;

    @Given("I get the token")
    public void given_iGetTheToken() {
        requests.getToken();
    }

    @And("I prepare the request payload {string}")
    public void and_IPrepareTheRequestPayload(String fileName) {
        String payload = payloadBuilder.prepareRequestPayload(fileName);
        requests.setPayload(payload);
    }

    @When("I send the request to the create a new user with name {string} and email {string}")
    public void when_iSendTheRequestToTheCreateANewUser(String name, String email) throws JsonProcessingException {
        requests.createUser(name,email);
    }

    @Then("the response status code should be {int}")
    public void then_theResponseStatusCodeShouldBe(int expected_code) {
        int actual_code = requests.getStatusCode();
        Assert.assertEquals(expected_code, actual_code);
    }

    @And("the response body should contain {string}")
    public void and_theResponseBodyShouldContain(String expected_name) {
        String actual_name = requests.getResponseBodyString("name");
        Assert.assertEquals(expected_name, actual_name);
    }

    @When("I send the GET request")
    public void when_iSendTheGetRequest() {
        requests.getUsers();
    }

    @Then("all existing users are listed")
    public void then_allExistingUsersAreListed() {
        List<Map<String, Object>> users = requests.getFullListResponseBody();
        int nameCount = requests.getFullListResponseBodyString("name").size();
        Assert.assertFalse(users.isEmpty());
        Assert.assertTrue(nameCount>0);
    }

    @When("I send the DELETE request with newest User ID")
    public void when_ISendTheDeleteRequestWithNewestUserId() {
        requests.deleteNewestUser();
    }

    @Then("the user with newest ID is not present anymore")
    public void then_theUserWithNewestIdIsNotPresentAnymore() {
        requests.deleteNewestUser();

        int expected_response = 404;
        int actual_response = requests.getStatusCode();
        Assert.assertEquals(expected_response, actual_response);
    }

    @When("I send the GET request with User ID {string}")
    public void when_ISendTheGetRequestWithUserId(String id) {
        String userIDEndpoint = USERS_BY_ID_ENDPOINT.replace("<user_id>", id);
        requests.getUser(userIDEndpoint);
    }

    @Then("the user with ID {string} is displayed")
    public void then_TheUserWithIdIsDisplayed(String id) {
        int actual_id = requests.getResponseBodyInt("id");
        Assert.assertEquals(actual_id,Integer.parseInt(id));
    }

    @When("I send the PUT request with User ID {string}")
    public void when_ISendThePutRequestWithUserId(String id) {
        String userByID = USERS_BY_ID_ENDPOINT.replace("<user_id>", id);
        String payload = requests.getPayload();
        requests.updateUser(userByID,payload);
    }

    @And("the user is updated successfully")
    public void theUserIsUpdatedSuccessfully() {
        String message = requests.getResponseBodyString("message");
        Assert.assertEquals("User updated successfully", message);
    }
}