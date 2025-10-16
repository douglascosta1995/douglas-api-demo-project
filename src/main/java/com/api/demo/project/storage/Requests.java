package com.api.demo.project.storage;

import com.api.demo.project.annotation.LazyComponent;
import com.api.demo.project.helpers.PayloadBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;

@LazyComponent
public class Requests {

    @Autowired
    private MainResponseStorage mainResponseStorage;

    @Autowired
    private PayloadBuilder payloadBuilder;

    @Value("${URL}")
    private String BASE_URL;

    @Value("${USERS}")
    private String USERS_ENDPOINT;

    @Value("${TOKEN}")
    private String TOKEN_ENDPOINT;

    @Value("${USERS_BY_ID}")
    private String USERS_BY_ID_ENDPOINT;

    private String TOKEN_PAYLOAD = "auth.json";

    public int getStatusCode(){
        return mainResponseStorage.getResponseFull().getStatusCode();
    }

    public String getResponseBodyString(String key){
        return mainResponseStorage.getResponseFull().jsonPath().getString(key);
    }

    public int getResponseBodyInt(String key){
        return mainResponseStorage.getResponseFull().jsonPath().getInt(key);
    }

    public List<Map<String, Object>> getFullListResponseBody(){
        return mainResponseStorage.getResponseFull().jsonPath().getList("$");
    }

    public List<Map<String, Object>> getFullListResponseBodyString(String key){
        return mainResponseStorage.getResponseFull().jsonPath().getList(key);
    }

    public void setPayload(String payload){
        mainResponseStorage.setPayload(payload);
    }

    public String getPayload(){
        return mainResponseStorage.getPayload();
    }

    public void getToken() {
        String payload = payloadBuilder.prepareRequestPayload(TOKEN_PAYLOAD);
        Response response = RestAssured
                .given()
                .baseUri(BASE_URL) // baseUrl injected or read from properties
                .header("Content-Type", "application/json")
                .body(payload)
                .when()
                .post(TOKEN_ENDPOINT);

        mainResponseStorage.setBearerToken(response.jsonPath().getString("token"));

    }

    public void getUsers() {
        Response response = RestAssured
                .given()
                .baseUri(BASE_URL) // baseUrl injected or read from properties
                .header("Authorization", "Bearer " + mainResponseStorage.getBearerToken())
                .when()
                .get(USERS_ENDPOINT);

        mainResponseStorage.setResponseFull(response);
    }

    public void getUser(String userIDEndpoint) {
        Response response = RestAssured
                .given()
                .baseUri(BASE_URL) // baseUrl injected or read from properties
                .header("Authorization", "Bearer " + mainResponseStorage.getBearerToken())
                .when()
                .get(userIDEndpoint);

        mainResponseStorage.setResponseFull(response);
    }


    public void createUser(String name, String email) throws JsonProcessingException {
        String payload = mainResponseStorage.getPayload();

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = (ObjectNode) objectMapper.readTree(payload);

        objectNode.put("name", name);
        objectNode.put("email", email);

        String updatedPayload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
        Response response = RestAssured
                .given()
                .baseUri(BASE_URL)
                .header("Authorization", "Bearer " + mainResponseStorage.getBearerToken())
                .contentType(ContentType.JSON)
                .body(updatedPayload)
                .when()
                .post(USERS_ENDPOINT);

        mainResponseStorage.setNewestCreatedUser(response.jsonPath().getString("id"));
        mainResponseStorage.setResponseFull(response);
    }


    public void updateUser(String userIDEndpoint, String payload) {
        Response response = RestAssured
                .given()
                .baseUri(BASE_URL) // baseUrl injected or read from properties
                .header("Authorization", "Bearer " + mainResponseStorage.getBearerToken())
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .put(userIDEndpoint);

        mainResponseStorage.setResponseFull(response);
    }


    public void deleteNewestUser() {
        String id = mainResponseStorage.getNewestCreatedUser();
        System.out.println(id);
        System.out.println(USERS_BY_ID_ENDPOINT);
        String userByID = USERS_BY_ID_ENDPOINT.replace("<user_id>", id);
        Response response = RestAssured
                .given()
                .baseUri(BASE_URL) // baseUrl injected or read from properties
                .header("Authorization", "Bearer " + mainResponseStorage.getBearerToken())
                .when()
                .delete(userByID);

        mainResponseStorage.setResponseFull(response);
    }

}
