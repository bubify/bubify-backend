package com.uu.au.api;
import com.uu.au.models.Json;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.annotation.DirtiesContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AchievementControllerIT {

    private static String token;

    @Autowired
    private TestRestTemplate restTemplate;

    private <T> ResponseEntity<String> makeRequest(HttpMethod method, String endpoint, T data, Boolean useToken) {
        // Generic method to make GET/PUT/POST/DELETE request to endpoint with data (may be null) and token (if needed)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (useToken) { headers.set("token", token); }

        HttpEntity<T> requestEntity = new HttpEntity<>(data, headers);
        RestTemplate restTemplate = new RestTemplate();

        String url = "http://localhost:8900" + endpoint;
        return restTemplate.exchange(url, method, requestEntity, String.class);
    }

    private void updateToken(String user) {
        // Authenticate as user and update the token
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/su?username=" + user, null, false);
        token = responseEntity.getBody();
        assertNotNull(token);
    }

    private void postNewUser(String first, String last, String email, String username, String role) {
        // Define and POST student data, assert status code
        String studentData = first + ";" + last + ";" + email + ";" + username + ";" + role;
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/admin/add-user", studentData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
    
    @BeforeEach
    public void setup() {
        // Define user and course data
        Json.CourseInfo courseData = Json.CourseInfo.builder()
                .name("Fun Course")
                .build();

        Json.CreateUser userData = Json.CreateUser.builder()
                .firstName("John")
                .lastName("Doe")
                .email("j.d@uu.se")
                .userName("johnteacher")
                .role("TEACHER")
                .build();

        // Create course and user
        makeRequest(HttpMethod.POST, "/internal/course", courseData, false);
        makeRequest(HttpMethod.POST, "/internal/user", userData, false);

        // Obtain token for the user
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/su?username=johnteacher", null, false);
        token = responseEntity.getBody();
        assertNotNull(token);
    }

    @Test
    public void testGetAchievements() {
        // Perform GET request for /achievements
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/achievements", null, true);
        
        // Assert status code and response body with 0 achievements
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        String responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals("[]", responseBody);
        
        // Define achievement data as a string from a CSV file (Code,Name,Level,Type,Url;Code...)
        String achievementData = "Code1;Name1;GRADE_3;ACHIEVEMENT;http://example.com/name1";

        // Post achievement data and assert status code
        responseEntity = makeRequest(HttpMethod.POST, "/admin/add-achievement", achievementData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Perform GET request for /achievements, assert status code and response body with 1 achievement
        responseEntity = makeRequest(HttpMethod.GET, "/achievements", null, true);
        
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("Code1"));
        
        // Add another achievement, post and assert status code
        achievementData = "Code2;Name2;GRADE_4;ASSIGNMENT;http://example.com/name2";
        responseEntity = makeRequest(HttpMethod.POST, "/admin/add-achievement", achievementData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Perform GET request for /achievements, assert status code and response body with 2 achievements
        responseEntity = makeRequest(HttpMethod.GET, "/achievements", null, true);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("Code1"));
        assertTrue(responseBody.contains("Code2"));
        
        // Assert length of the JSON array and headers of the first JSON object
        try {
            JSONArray jsonArray = new JSONArray(responseBody);
            assertEquals(2, jsonArray.length());

            JSONObject jsonObject = jsonArray.getJSONObject(0);
            assertTrue(jsonObject.has("id"));
            assertTrue(jsonObject.has("code"));
            assertTrue(jsonObject.has("name"));
            assertTrue(jsonObject.has("urlToDescription"));
            assertTrue(jsonObject.has("achievementType"));
            assertTrue(jsonObject.has("level"));
            assertTrue(jsonObject.has("createdDateTime"));
            assertTrue(jsonObject.has("updatedDateTime"));
            assertTrue(jsonObject.has("codeExam"));
            assertTrue(jsonObject.has("lab"));
            assertTrue(jsonObject.has("assignment"));
            assertTrue(jsonObject.has("introTask"));
        }
        catch (JSONException e) {
            e.printStackTrace();
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }
        
        // Perform GET request for /achievements, assert status code when authenticated as a student
        postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        updateToken("somestudent"); // Authenticate as student
        responseEntity = makeRequest(HttpMethod.GET, "/achievements", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}