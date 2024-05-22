package com.uu.au.api;
import com.uu.au.models.Json;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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

    @Autowired
    private TestHelper testHelper;

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

        // Create course, user and set token
        testHelper.makeRequest(HttpMethod.POST, "/internal/course", courseData, false);
        testHelper.makeRequest(HttpMethod.POST, "/internal/user", userData, false);
        testHelper.updateToken("johnteacher");
    }

    @Test
    public void testGetAchievements() {
        // Perform GET request for /achievements
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/achievements", null, true);
        
        // Assert status code and response body with 0 achievements
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        String responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals("[]", responseBody);
        
        // Define achievement data as a string from a CSV file (Code,Name,Level,Type,Url;Code...)
        String achievementData = "Code1;Name1;GRADE_3;ACHIEVEMENT;http://example.com/name1";

        // Post achievement data and assert status code
        responseEntity = testHelper.makeRequest(HttpMethod.POST, "/admin/add-achievement", achievementData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Perform GET request for /achievements, assert status code and response body with 1 achievement
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/achievements", null, true);
        
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("Code1"));
        
        // Add another achievement, post and assert status code
        achievementData = "Code2;Name2;GRADE_4;ASSIGNMENT;http://example.com/name2";
        responseEntity = testHelper.makeRequest(HttpMethod.POST, "/admin/add-achievement", achievementData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Perform GET request for /achievements, assert status code and response body with 2 achievements
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/achievements", null, true);

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
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/achievements", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}