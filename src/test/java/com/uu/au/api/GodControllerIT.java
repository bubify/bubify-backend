package com.uu.au.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
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
public class GodControllerIT {

    private static String token;

    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<String> makeRequest(HttpMethod method, String endpoint, String data, Boolean useToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (useToken) { headers.set("token", token); }

        HttpEntity<String> requestEntity = new HttpEntity<>(data, headers);
        RestTemplate restTemplate = new RestTemplate();

        String url = "http://localhost:8900" + endpoint;
        return restTemplate.exchange(url, method, requestEntity, String.class);
    }

    private int getIdFromUserName(String userName) {
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/admin/user?username=" + userName, null, true);
        String responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        int userId = 0;
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            userId = jsonObject.getInt("id");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return userId;
    }

    private int getIdFromAchievementCode(String code) {
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/achievement/code-to-id/" + code, null, true);
        String responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        int achievementId = 0;
        try {
            achievementId = Integer.parseInt(responseBody);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return achievementId;
    }

    @BeforeEach
    public void setup() {
        // Define user and course data
        String courseData = "{\"name\":\"Fun Course\"}";
        String userData = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"j.d@uu.se\",\"userName\":\"jdoe\",\"role\":\"TEACHER\"}";

        // Create course and user
        makeRequest(HttpMethod.POST, "/internal/course", courseData, false);
        makeRequest(HttpMethod.POST, "/internal/user", userData, false);

        // Obtain token for the user
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/su?username=jdoe", null, false);
        token = responseEntity.getBody();
        assertNotNull(token);
    }

    @Test
    public void testAchievementAllRemaining() {
        // Perform GET request for /achievement/all-remaining/{code} with no achievements
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            makeRequest(HttpMethod.GET, "/achievement/all-remaining/Code1", null, true);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        // assertEquals("No such achievement 'Code1'", exception.getResponseBodyAsString());

        // Define and POST achievement data, assert status code
        String achievementData = "Code1;Name1;GRADE_3;ACHIEVEMENT;http://example.com/name1";
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/admin/add-achievement", achievementData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Perform GET request for /achievement/all-remaining/{code} with 1 achievement but no students
        responseEntity = makeRequest(HttpMethod.GET, "/achievement/all-remaining/Code1", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());

        // Define and POST student data, assert status code
        String studentData = "Jane;Doe;jane.doe@uu.se;janedoe;STUDENT";
        responseEntity = makeRequest(HttpMethod.POST, "/admin/add-user", studentData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        // Perform GET request for /achievement/all-remaining/{code} with 1 achievement and 1 student
        responseEntity = makeRequest(HttpMethod.GET, "/achievement/all-remaining/Code1", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[\"Jane Doe <jane.doe@uu.se>\"]", responseEntity.getBody());

        // Add another student, post and assert status code
        studentData = "John;Smith;john.smith@uu.se;johnsmith;STUDENT";
        responseEntity = makeRequest(HttpMethod.POST, "/admin/add-user", studentData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Perform GET request for /achievement/all-remaining/{code} with 1 achievement and 2 students
        responseEntity = makeRequest(HttpMethod.GET, "/achievement/all-remaining/Code1", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[\"Jane Doe <jane.doe@uu.se>\",\"John Smith <john.smith@uu.se>\"]", responseEntity.getBody());

        // TODO: Add test when user has demonstrated the achievement, thus removed from the list

        // TODO: Add test when current user is not authorized
    }

    @Test
    public void testAchivementCodeToId() {
        // Perform GET request for /achievement/code-to-id/{code} with non-existent achievement code
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/achievement/code-to-id/Code1", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Achivement Code1 not found", responseEntity.getBody());

        // Define and POST achievement data, assert status code
        String achievementData = "Code1;Name1;GRADE_3;ACHIEVEMENT;http://example.com/name1";
        responseEntity = makeRequest(HttpMethod.POST, "/admin/add-achievement", achievementData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        // Perform GET request for /achievement/code-to-id/{code} with updated achievement data
        responseEntity = makeRequest(HttpMethod.GET, "/achievement/code-to-id/Code1", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        String responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        
        try {
            Integer.parseInt(responseBody);
        } catch (NumberFormatException e) {
            fail("Response body is not an integer");
        }
        
        // TODO: Add test when current user is not authorized
    }

    @Test
    public void testResetCodeExamBlocker() {
        // Perform GET request for /admin/resetCodeExamBlocker with no students
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/admin/resetCodeExamBlocker", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().startsWith("Ignoring all failed code exam demonstration attempts earlier than"));

        // TODO: Test with students and failed code exam demonstration attempts
    }

    @Test
    public void testClearAllRequests() {
        // Perform GET request for /admin/clearAllRequests with no requests
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/clearLists", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody()); // Endpoint has no return value

        // TODO: Test with active requests in the system
    }

    @Test
    public void testGetCourse() {
        // Perform GET request for /course with initial course data
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/course", null, true);

        // TODO: Assert throws exception with no course data (How? Course data is needed to get token!)

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        String responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("Fun Course"));

        // Assert headers of the JSON object
        try {
            JSONObject jsonObject = new JSONObject(responseBody);

            assertTrue(jsonObject.has("startDate"));
            assertTrue(jsonObject.has("name"));
            assertTrue(jsonObject.has("gitHubOrgURL"));
            assertTrue(jsonObject.has("courseWebURL"));
            assertTrue(jsonObject.has("helpModule"));
            assertTrue(jsonObject.has("demoModule"));
            assertTrue(jsonObject.has("onlyIntroductionTasks"));
            assertTrue(jsonObject.has("burndownModule"));
            assertTrue(jsonObject.has("statisticsModule"));
            assertTrue(jsonObject.has("examMode"));
            assertTrue(jsonObject.has("profilePictures"));
            assertTrue(jsonObject.has("clearQueuesUsingCron"));
            assertTrue(jsonObject.has("roomSetting"));
            assertTrue(jsonObject.has("createdDateTime"));
            assertTrue(jsonObject.has("updatedDateTime"));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPostCourse() {
        // Perform POST request for /course with new course data
        String requestBody = "{\"name\":\"Course 1\"," +
                                "\"courseWebURL\":\"http://example1.com\"," +
                                "\"codeSpaceBaseURL\":null," +
                                "\"githubBaseURL\":\"http://github.com/example1\"," +
                                "\"startDate\":\"2024-04-05\"," +
                                "\"helpModule\":true," +
                                "\"demoModule\":true," +
                                "\"statisticsModule\":true," +
                                "\"burndownModule\":true," +
                                "\"examMode\":true," +
                                "\"onlyIntroductionTasks\":true," +
                                "\"roomSetting\":null," +
                                "\"clearQueuesUsingCron\":true," +
                                "\"profilePictures\":true}";

        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            makeRequest(HttpMethod.POST, "/course", requestBody, true);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getResponseBodyAsString().contains("COURSE_ALREADY_EXISTS"));

        // TODO: Assert SUCCESS of post with no course data (How? Course data is needed to get token!)
    }

    @Test
    public void testPutCourse() {
        // Perform PUT request for /course with updated course data
        String requestBody = "{\"name\":\"Course 2\"," +
                                "\"courseWebURL\":\"http://example2.com\"," +
                                "\"codeSpaceBaseURL\":null," +
                                "\"githubBaseURL\":\"http://github.com/example2\"," +
                                "\"startDate\":\"2024-04-06\"," +
                                "\"helpModule\":false," +
                                "\"demoModule\":false," +
                                "\"statisticsModule\":false," +
                                "\"burndownModule\":false," +
                                "\"examMode\":false," +
                                "\"onlyIntroductionTasks\":false," +
                                "\"roomSetting\":null," +
                                "\"clearQueuesUsingCron\":false," +
                                "\"profilePictures\":false}";

        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.PUT, "/course", requestBody, true);

        // Assert status code and response body
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("SUCCESS", responseEntity.getBody());

        // Perform GET request for /course to check updated course data
        responseEntity = makeRequest(HttpMethod.GET, "/course", null, true);

        // Assert status code and response body
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        String responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("Course 2"));
    }
 
    @Test
    public void testExploreAchievement() {
        // Perform GET request for /explore/achievement/{achievementId} with non-existent achievement ID
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/explore/achievement/1", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        // assertEquals("", responseEntity.getBody());
        // {"unlocked":[],"remaining":[{"id":2,"firstName":"John","lastName":"Doe","userName":"jdoe","email":"j.d@uu.se","role":"TEACHER","gitHubHandle":null,"gitHubFlowSuccessful":false,"zoomRoom":null,"verifiedProfilePic":false,"canClaimHelpRequests":false,"updatedDateTime":"2024-04-25T16:42:32.928234","lastLogin":"2024-04-25T16:42:32.92071","deadline":null,"needsZoomLink":true,"needsProfilePic":true,"needsGitHubHandle":true,"gitHubRepoURL":"nullj.d"}],"struggling":[]}

        // Define and POST achievement data, assert status code
        String achievementData = "Code1;Name1;GRADE_3;ACHIEVEMENT;http://example.com/name1";
        responseEntity = makeRequest(HttpMethod.POST, "/admin/add-achievement", achievementData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Perform GET request for /explore/achievement/{achievementId} with updated achievement data
        int achievementId = getIdFromAchievementCode("Code1");
        responseEntity = makeRequest(HttpMethod.GET, "/explore/achievement/" + achievementId, null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        // assertEquals("", responseEntity.getBody());
        // {"unlocked":[],"remaining":[{"id":2,"firstName":"John","lastName":"Doe","userName":"jdoe","email":"j.d@uu.se","role":"TEACHER","gitHubHandle":null,"gitHubFlowSuccessful":false,"zoomRoom":null,"verifiedProfilePic":false,"canClaimHelpRequests":false,"updatedDateTime":"2024-04-25T16:46:49.253468","lastLogin":"2024-04-25T16:46:49.243442","deadline":null,"needsZoomLink":true,"needsProfilePic":true,"needsGitHubHandle":true,"gitHubRepoURL":"nullj.d"}],"struggling":[]}
    }
}