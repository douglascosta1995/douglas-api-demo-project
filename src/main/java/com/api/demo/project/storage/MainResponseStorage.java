package com.api.demo.project.storage;

import com.api.demo.project.annotation.LazyComponent;
import io.restassured.response.Response;
import lombok.Data;

/**
 * Singleton storage component used to temporarily hold data
 * The data stored here will be available during the entire run
 **/
@Data
@LazyComponent
public class MainResponseStorage {

    // For example, store here the bearer token, it will then be available for all the scenarios
    // You can then use it like "mainResponseStorage.getBearerToken()"
    private String bearerToken;
    private String payload;
    private String userId;
    private Response responseFull;
    private String newestCreatedUser;

}