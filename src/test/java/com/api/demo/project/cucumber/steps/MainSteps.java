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
        mainResponseStorage.setResponseFull(response);
        //mainResponseStorage.setResponseCode(response.getStatusCode());
    }

    @Then("the response status code should be {int}")
    public void then_theResponseStatusCodeShouldBe(Integer int1) {
        Assert.assertEquals(201, mainResponseStorage.getResponseFull().getStatusCode());
    }
    @And("the response body should contain {string}")
    public void and_theResponseBodyShouldContain(String expected_name) {
        String actual_name = mainResponseStorage.getResponseFull().jsonPath().getString("name");
        Assert.assertEquals(expected_name, actual_name);
    }

}