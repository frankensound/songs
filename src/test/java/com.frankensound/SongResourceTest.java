package com.frankensound;

import com.frankensound.resources.SongResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@TestHTTPEndpoint(SongResource.class)
public class SongResourceTest {

    @Test
    public void testGetByKey() {
        String key = "Lalala";
        given()
                .pathParam("key", key)
                .when().get("/{key}")
                .then()
                .statusCode(200)
                .body(containsString(key));
    }

    @Test
    public void testGetAll() {
        given()
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(200)
                .body("$.size()", is(1));
    }

}
