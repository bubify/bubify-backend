package com.uu.au;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.http.*;

import org.springframework.test.web.servlet.MockMvc;

import com.uu.au.models.User;
import com.uu.au.enums.Role;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class NewIT {
    private static String token;

    @Autowired
    private MockMvc mockmvc;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeAll
    public static void setup() {
        // Define user and course data
        String courseData = "{\"name\":\"Fun Course\"}";
        String userData = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"j.d@uu.se\",\"userName\":\"jdoe\",\"role\":\"TEACHER\"}";

        // Create course
        makePostRequest("http://localhost:8900/internal/course", courseData);

        // Create user
        makePostRequest("http://localhost:8900/internal/user", userData);

        // Obtain token for the created user
        ResponseEntity<String> responseEntity = makeGetRequest("http://localhost:8900/su?username=jdoe");
        token = responseEntity.getBody();
    }

    private static ResponseEntity<String> makePostRequest(String url, String data) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(data, headers);
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForEntity(url, requestEntity, String.class);
    }

    private static ResponseEntity<String> makeGetRequest(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
    }
    
    @Test
    public void testGetCourse() {
        // Perform GET request for /course with token in header
        HttpHeaders getRequestHeaders = new HttpHeaders();
        getRequestHeaders.set("token", token); // Add token to headers
        HttpEntity<Void> getRequestEntity = new HttpEntity<>(getRequestHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "http://localhost:8900/course", HttpMethod.GET, getRequestEntity, String.class);

        // Assert status code
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        // Assert content type
        assertNotNull(responseEntity.getHeaders().getContentType());
    }

    // @Test
    // public void testPostCourse() {
    //     // Create request body
    //     String requestBody = "{\"name\":\"Course 1\"," +
    //                             "\"courseWebURL\":\"http://example1.com\"," +
    //                             "\"codeSpaceBaseURL\":null," +
    //                             "\"githubBaseURL\":\"http://github.com/example1\"," +
    //                             "\"startDate\":\"2024-04-05\"," +
    //                             "\"helpModule\":true," +
    //                             "\"demoModule\":true," +
    //                             "\"statisticsModule\":true," +
    //                             "\"burndownModule\":true," +
    //                             "\"examMode\":true," +
    //                             "\"onlyIntroductionTasks\":true," +
    //                             "\"roomSetting\":null," +
    //                             "\"clearQueuesUsingCron\":true," +
    //                             "\"profilePictures\":true}";

    //     // Create headers with token
    //     HttpHeaders postRequestHeaders = new HttpHeaders();
    //     postRequestHeaders.set("token", token);

    //     // Perform POST request for /course with token in header
    //     ResponseEntity<Void> responseEntity = restTemplate.exchange(
    //             "http://localhost:8900/course", HttpMethod.POST,
    //             new HttpEntity<>(requestBody, postRequestHeaders), Void.class);

    //     // Assert status code
    //     assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    // }

    // @Test
    // public void testPutCourse() {
    //     // Create request body
    //     String requestBody = "{\"name\":\"Course 2\"," +
    //                             "\"courseWebURL\":\"http://example2.com\"," +
    //                             "\"codeSpaceBaseURL\":null," +
    //                             "\"githubBaseURL\":\"http://github.com/example2\"," +
    //                             "\"startDate\":\"2024-04-06\"," +
    //                             "\"helpModule\":false," +
    //                             "\"demoModule\":false," +
    //                             "\"statisticsModule\":false," +
    //                             "\"burndownModule\":false," +
    //                             "\"examMode\":false," +
    //                             "\"onlyIntroductionTasks\":false," +
    //                             "\"roomSetting\":null," +
    //                             "\"clearQueuesUsingCron\":false," +
    //                             "\"profilePictures\":false}";

    //     // Create headers with token
    //     HttpHeaders putRequestHeaders = new HttpHeaders();
    //     putRequestHeaders.set("token", token);

    //     // Perform PUT request for /course with token in header
    //     ResponseEntity<Void> responseEntity = restTemplate.exchange(
    //             "http://localhost:8900/course", HttpMethod.PUT,
    //             new HttpEntity<>(requestBody, putRequestHeaders), Void.class);

    //     // Assert status code
    //     assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    // }

}