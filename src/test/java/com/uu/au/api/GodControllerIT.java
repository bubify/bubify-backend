package com.uu.au.api;
import com.uu.au.models.Json;
import com.uu.au.enums.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.test.annotation.DirtiesContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

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
        // Helper method to define and POST student data, assert status code, 
        String studentData = first + ";" + last + ";" + email + ";" + username + ";" + role;
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/admin/add-user", studentData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    private void postNewAchievement(String code, String name, String grade, String type, String url) {
        // Helper method to define and POST achievement data, assert status code
        String achievementData = code + ";" + name + ";" + grade + ";" + type + ";" + url;
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/admin/add-achievement", achievementData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    private void postDemoRequest(Long achievementId, Long userId) {
        // Helper method to define and POST demonstration request, assert status code
        Json.DemonstrationRequest demonstrationData = Json.DemonstrationRequest.builder()
                .achievementIds(List.of(achievementId))
                .ids(List.of(userId))
                .zoomPassword("password")
                .physicalRoom("Room1")
                .build();
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/demonstration/request", demonstrationData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
    
    private void postClaimDemo(Long demonstrationId) {
        // Helper method to define and POST demonstration claim data, assert status code
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/demonstration/claim", Long.toString(demonstrationId), true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    private void postDemoResult(Long demonstrationId, Long achievementId, Long userId, Result result) {
        // Helper method to define and POST demonstration result data, assert status code
        Json.AchievementId_UserId_Result resultData = Json.AchievementId_UserId_Result.builder()
                .achievementId(achievementId)
                .id(userId)
                .result(result)
                .build();
    
        Json.DemoResult demoResultData = Json.DemoResult.builder()
                .demoId(demonstrationId)
                .results(List.of(resultData))
                .build();
        
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/demonstration/done", demoResultData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    private void postHelpRequest(Long userId) {
        // Helper method to define and POST help request, assert status code
        Json.HelpRequest helpRequestData = Json.HelpRequest.builder()
                .ids(List.of(userId))
                .message("Help me!")
                .zoomPassword("password")
                .physicalRoom("Room1")
                .build();

        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/askForHelp", helpRequestData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    private void postOfferHelp(Long helpRequestId) {
        // Helper method to define and POST help offer data, assert status code
        Json.HelpRequestId helpRequestData = Json.HelpRequestId.builder()
                .helpRequestId(helpRequestId)
                .build();
    
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/offerHelp", helpRequestData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
    
    private void postHelpDone(Long helpRequestId) {
        // Helper method to define and POST help done data, assert status code
        Json.HelpRequestId helpRequestData = Json.HelpRequestId.builder()
                .helpRequestId(helpRequestId)
                .build();
        
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/markAsDone", helpRequestData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    private void postProfilePic() {
        // Helper method to POST a profile picture for the current authenticated user

        // Mock file by reading a jpg file from resources (probably not needed, can mock file directly?)
        Path path = Paths.get("src/test/resources/profile_pic.jpg");
        String contentType = "image/jpeg";
        byte[] content = null;
        try {
            content = Files.readAllBytes(path);
        } catch (final IOException e) {
            fail("Failed to read file: " + e.getMessage());
        }
        MultipartFile file = new MockMultipartFile("profile_pic.jpg", "profile_pic.jpg", contentType, content);

        // Alternative way to mock file directly:
        // MultipartFile file = new MockMultipartFile("profile_pic.jpg", "profile_pic.jpg", "image/jpeg", new byte[0]);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("token", token);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();

        String url = "http://localhost:8900/user/upload-profile-pic";
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    private Long getIdFromUserName(String userName) {
        // Helper method to get user ID from username by GET request to /admin/user?username={username}
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/admin/user?username=" + userName, null, true);
        String responseBody = responseEntity.getBody();
        assertNotNull(responseBody);

        Long userId = 0L;
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            userId = jsonObject.getLong("id");
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }
        return userId;
    }

    private Long getIdFromAchievementCode(String code) {
        // Helper method to get achievement ID from code by GET request to /achievement/code-to-id/{code}
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/achievement/code-to-id/" + code, null, true);
        String responseBody = responseEntity.getBody();
        assertNotNull(responseBody);

        Long achievementId = 0L;
        try {
            achievementId = Long.parseLong(responseBody);
        }
        catch (NumberFormatException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }
        return achievementId;
    }

    private Long[] getIdsOfActiveDemonstrations() {
        // Helper method to get IDs of active demonstrations by GET request to /demonstrations/activeAndSubmittedOrPickedUp
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/demonstrations/activeAndSubmittedOrPickedUp", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        try {
            JSONArray jsonArray = new JSONArray(responseEntity.getBody());
            Long[] demonstrationIds = new Long[jsonArray.length()];
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                demonstrationIds[i] = jsonObject.getLong("id");
            }
            return demonstrationIds;
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        return new Long[0]; // No active demonstrations found
    }

    private Long getDemoIdFromAchievementAndUser(Long achievementId, Long userId) {
        // Helper method to get demonstration ID from achievement and user ID by GET request to /demonstrations/activeAndSubmittedOrPickedUp
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/demonstrations/activeAndSubmittedOrPickedUp", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        try {
            JSONArray jsonArray = new JSONArray(responseEntity.getBody());
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject demonstration = jsonArray.getJSONObject(i);                
                JSONArray achievements = demonstration.getJSONArray("achievements");
                
                for (int j = 0; j < achievements.length(); j++) {

                    if (achievements.getJSONObject(j).getInt("id") == achievementId) {
                        JSONArray submitters = demonstration.getJSONArray("submitters");
                        
                        for (int k = 0; k < submitters.length(); k++) {
                            if (submitters.getJSONObject(k).getInt("id") == userId) {
                                return demonstration.getLong("id");
                            }
                        }
                    }
                }
            }
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        return 0L; // No demonstration found
    }

    private Long getHelpRequestIdFromUser(Long userId) {
        // Helper method to get help request ID from user ID by GET request to /helpRequests/active
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/helpRequests/active", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        try {
            JSONArray jsonArray = new JSONArray(responseEntity.getBody());
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject helpRequest = jsonArray.getJSONObject(i);
                JSONArray submitters = helpRequest.getJSONArray("submitters");
                        
                    for (int k = 0; k < submitters.length(); k++) {
                        if (submitters.getJSONObject(k).getInt("id") == userId) {
                            return helpRequest.getLong("id");
                        }
                    }
            }
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        return 0L; // No help request found
    }

    private void setupStudentWithValidPicAndActiveDemoAndHelpRequest(String code, String firstName, String lastName, String userName) {
        // Helper method to setup a student with a valid profile picture, active demonstration and help request

        // Create student, get IDs for achievement and student
        postNewUser(firstName, lastName, firstName.toLowerCase() + "." + lastName.toLowerCase() + "@uu.se", userName, "STUDENT");
        Long userId = getIdFromUserName(userName);
        Long achievementId = getIdFromAchievementCode(code);

        updateToken(userName); // Authenticate as student
    
            postProfilePic();
            postDemoRequest(achievementId, userId);
            postHelpRequest(userId);
            
        updateToken("johnteacher"); // Authenticate as teacher again
                
        // Verify profile picture and assert status code
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.PUT, "/user/profile-pic/" + userId + "/verified", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    // ************************************************************
    // ******************** TEST SECTION BELOW ********************
    // ************************************************************

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
    public void testAchievementAllRemaining() {
        // Perform GET request for /achievement/all-remaining/{code} with no achievements
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            makeRequest(HttpMethod.GET, "/achievement/all-remaining/Code1", null, true);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");

        // Perform GET request for /achievement/all-remaining/{code} with 1 achievement but no students
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/achievement/all-remaining/Code1", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());

        postNewUser("Jane", "Doe", "jane.doe@uu.se", "janestudent", "STUDENT");
        
        // Perform GET request for /achievement/all-remaining/{code} with 1 achievement and 1 student
        responseEntity = makeRequest(HttpMethod.GET, "/achievement/all-remaining/Code1", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[\"Jane Doe <jane.doe@uu.se>\"]", responseEntity.getBody());

        postNewUser("James", "Smith", "james.smith@uu.se", "jamesstudent", "STUDENT");

        // Perform GET request for /achievement/all-remaining/{code} with 1 achievement and 2 students
        responseEntity = makeRequest(HttpMethod.GET, "/achievement/all-remaining/Code1", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[\"Jane Doe <jane.doe@uu.se>\",\"James Smith <james.smith@uu.se>\"]", responseEntity.getBody());

        // Assert throws exception when current user is a student, hence not authorized
        postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        updateToken("somestudent"); // Authenticate as student
        
        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            makeRequest(HttpMethod.GET, "/achievement/all-remaining/Code1", null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testAchievementAllRemainingDemonstrated() {
        // Setup and get IDs for achievement, student and demonstration
        postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = getIdFromAchievementCode("Code1");
        Long userId = getIdFromUserName("janestudent");
        Long demonstrationId = getDemoIdFromAchievementAndUser(achievementId, userId);
        
        postDemoResult(demonstrationId, achievementId, userId, Result.PASS);

        // Perform GET request for /achievement/all-remaining/{code} with 1 achievement and 1 student in system but NOT in remaining
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/achievement/all-remaining/Code1", null, true);

        // Assert status code and response body
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());
    }

    @Test
    public void testAchivementCodeToId() {
        // Perform GET request for /achievement/code-to-id/{code} with non-existent achievement code
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/achievement/code-to-id/Code1", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Achivement Code1 not found", responseEntity.getBody());

        postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        
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
        
        // Assert throws exception when current user is a student, hence not authorized
        postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            makeRequest(HttpMethod.GET, "/achievement/all-remaining/Code1", null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testResetCodeExamBlocker() {
        // Perform GET request for /admin/resetCodeExamBlocker with no students
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/admin/resetCodeExamBlocker", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().startsWith("Ignoring all failed code exam demonstration attempts earlier than"));

        // Setup and get IDs for achievement, student and demonstration
        postNewAchievement("CodeExam1", "Name1", "GRADE_3", "CODE_EXAM", "http://example.com/Code1");
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("CodeExam1", "Jane", "Doe", "janestudent");
        Long achievementId = getIdFromAchievementCode("CodeExam1");
        Long userId = getIdFromUserName("janestudent");
        Long demonstrationId = getDemoIdFromAchievementAndUser(achievementId, userId);
        
        postDemoResult(demonstrationId, achievementId, userId, Result.FAIL);
        
        // Perform GET request for /admin/resetCodeExamBlocker again
        responseEntity = makeRequest(HttpMethod.GET, "/admin/resetCodeExamBlocker", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // TODO: Assert an API dependent on the codeExamDemonstrationBlocker (HOW? Must move forward in time to check blocker)
    }

    @Test
    public void testClearAllRequests() {
        // Perform GET request for /clearLists with no requests
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/clearLists", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody()); // Endpoint has no return value
        
        // Verify queues are empty
        responseEntity = makeRequest(HttpMethod.GET, "/demonstrations/activeAndSubmittedOrPickedUp", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());
        
        responseEntity = makeRequest(HttpMethod.GET, "/helpRequests/active", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());

        // Assert throws exception when current user is a student, hence not authorized
        postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        updateToken("somestudent"); // Authenticate as student
        
        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            makeRequest(HttpMethod.GET, "/clearLists", null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }
    
    @Test
    public void testClearAllRequestsWithActiveRequests() {
        // Verify queues are empty
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/demonstrations/activeAndSubmittedOrPickedUp", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());
        
        responseEntity = makeRequest(HttpMethod.GET, "/helpRequests/active", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());

        // Setup achievement, student and demonstration
        postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");

        // Verify queues are NOT empty
        responseEntity = makeRequest(HttpMethod.GET, "/demonstrations/activeAndSubmittedOrPickedUp", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotEquals("[]", responseEntity.getBody());
        
        responseEntity = makeRequest(HttpMethod.GET, "/helpRequests/active", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotEquals("[]", responseEntity.getBody());

        // Perform GET request for /clearLists with active requests in the system, assert status code
        responseEntity = makeRequest(HttpMethod.GET, "/clearLists", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        // Verify queues are empty AGAIN
        responseEntity = makeRequest(HttpMethod.GET, "/demonstrations/activeAndSubmittedOrPickedUp", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());
        
        responseEntity = makeRequest(HttpMethod.GET, "/helpRequests/active", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());
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
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }
    }

    @Test
    public void testPostCourse() {
        // Define course data
        Json.CourseInfo courseData = Json.CourseInfo.builder()
                .name("Course 1")
                .courseWebURL("http://example1.com")
                .githubBaseURL("http://github.com/example1")
                .startDate("2024-04-05")
                .helpModule(true)
                .demoModule(true)
                .statisticsModule(true)
                .burndownModule(true)
                .examMode(true)
                .onlyIntroductionTasks(true)
                .clearQueuesUsingCron(true)
                .profilePictures(true)
                .build();

        // Asserts COURSE_ALREADY_EXISTS error when attempting to create duplicate course
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            makeRequest(HttpMethod.POST, "/course", courseData, true);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getResponseBodyAsString().contains("COURSE_ALREADY_EXISTS"));

        // TODO: Assert SUCCESS of post with no course data (How? Course data is needed to get token!)

        // Assert throws exception when current user is a student, hence not authorized
        postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        updateToken("somestudent"); // Authenticate as student
        
        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            makeRequest(HttpMethod.POST, "/course", courseData, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testPutCourse() {
        // Define updated course data
        Json.CourseInfo updatedCourseData = Json.CourseInfo.builder()
                .name("Course 2")
                .courseWebURL("http://example2.com")
                .githubBaseURL("http://github.com/example2")
                .startDate("2024-04-06")
                .helpModule(false)
                .demoModule(false)
                .statisticsModule(false)
                .burndownModule(false)
                .examMode(false)
                .onlyIntroductionTasks(false)
                .clearQueuesUsingCron(false)
                .profilePictures(false)
                .build();

        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.PUT, "/course", updatedCourseData, true);

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

        // Assert throws exception when current user is a student, hence not authorized
        postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        updateToken("somestudent"); // Authenticate as student
        
        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            makeRequest(HttpMethod.PUT, "/course", updatedCourseData, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testExploreAchievementNoAchievement() {
        // Perform GET request for /explore/achievement/{achievementId} with non-existent achievement ID
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/explore/achievement/100", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        // Get JSON object from response body and assert no users in the lists
        try {
            JSONObject jsonObject = new JSONObject(responseEntity.getBody());
            String unlocked = jsonObject.getJSONArray("unlocked").toString();
            assertEquals("[]", unlocked);
            
            String struggling = jsonObject.getJSONArray("struggling").toString();
            assertEquals("[]", struggling);
            
            // Achievement ID 100 does not exist, thus no users should be in the list or exception thrown
            String remaining = jsonObject.getJSONArray("remaining").toString();
            assertEquals("[]", remaining);
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }
    }
    
    @Test
    public void testExploreAchievement() {
        postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        
        // Perform GET request for /explore/achievement/{achievementId} with 1 achievement in DB but no unlocked/pushed back users
        Long achievementId = getIdFromAchievementCode("Code1");
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/explore/achievement/" + achievementId, null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        // Get JSON object from response body and assert user is in the remaining list
        try {
            JSONObject jsonObject = new JSONObject(responseEntity.getBody());
            
            String unlocked = jsonObject.getJSONArray("unlocked").toString();
            assertEquals("[]", unlocked);
            
            String struggling = jsonObject.getJSONArray("struggling").toString();
            assertEquals("[]", struggling);
            
            JSONObject remainingFirst = jsonObject.getJSONArray("remaining").getJSONObject(0);
            assertEquals("johnteacher", remainingFirst.getString("userName"));
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }
        
        // Assert throws exception when current user is a student, hence not authorized
        postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        updateToken("somestudent"); // Authenticate as student
        
        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            makeRequest(HttpMethod.GET, "/explore/achievement/" + achievementId, null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testExploreAchievementDemonstrated() {
        // Setup and get IDs for achievement, student and demonstration
        postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = getIdFromAchievementCode("Code1");
        Long userId = getIdFromUserName("janestudent");
        Long demonstrationId = getDemoIdFromAchievementAndUser(achievementId, userId);

        postDemoResult(demonstrationId, achievementId, userId, Result.PASS);
        
        // Perform GET request for /explore/achievement/{achievementId} with 1 achievement in DB unlocked by the student
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/explore/achievement/" + achievementId, null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        // Get JSON object from response body and assert user is in the remaining list
        try {            
            JSONObject jsonObject = new JSONObject(responseEntity.getBody());
            
            JSONObject unlockedFirst = jsonObject.getJSONArray("unlocked").getJSONObject(0);
            assertEquals("janestudent", unlockedFirst.getString("userName"));
            
            String struggling = jsonObject.getJSONArray("struggling").toString();
            assertEquals("[]", struggling);
            
            String remaining = jsonObject.getJSONArray("remaining").toString();
            assertFalse(remaining.contains("janestudent"));
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }
    }

    @Test
    public void testExploreAchievementPushedBack() {
        // Setup and get IDs for achievement, student and demonstration
        postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = getIdFromAchievementCode("Code1");
        Long userId = getIdFromUserName("janestudent");
        Long demonstrationId = getDemoIdFromAchievementAndUser(achievementId, userId);

        postDemoResult(demonstrationId, achievementId, userId, Result.PUSHBACK);
        
        // Perform GET request for /explore/achievement/{achievementId} with 1 achievement in DB pushed back for the student
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/explore/achievement/" + achievementId, null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        // Get JSON object from response body and assert user is in the remaining list
        try {
            JSONObject jsonObject = new JSONObject(responseEntity.getBody());
            
            String unlocked = jsonObject.getJSONArray("unlocked").toString();
            assertEquals("[]", unlocked);
            
            JSONObject strugglingFirst = jsonObject.getJSONArray("struggling").getJSONObject(0);
            assertEquals("janestudent", strugglingFirst.getString("userName"));
            
            String remaining = jsonObject.getJSONArray("remaining").toString();
            assertTrue(remaining.contains("janestudent")); // A pushed-back achievement is still in the remaining list
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }
    }

    @Test
    public void testExploreProgress() {
        // Perform GET request for /explore/progress with no students
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/explore/progress", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("{\"achievements\":[],\"userProgress\":[]}", responseEntity.getBody());
        
        postNewUser("Jane", "Doe", "jane.doe@uu.se", "janestudent", "STUDENT");
        
        // Perform GET request for /explore/progress with 1 student in DB
        responseEntity = makeRequest(HttpMethod.GET, "/explore/progress", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        // Get JSON object from response body and assert user is in the list
        try {
            JSONObject jsonObject = new JSONObject(responseEntity.getBody());
            
            String achievements = jsonObject.getJSONArray("achievements").toString();
            assertEquals("[]", achievements);
            
            JSONArray userProgress = jsonObject.getJSONArray("userProgress");
            assertEquals(1, userProgress.length());
            JSONObject userProgressFirst = jsonObject.getJSONArray("userProgress").getJSONObject(0);
            assertEquals("janestudent", userProgressFirst.getJSONObject("user").getString("userName"));
            assertEquals("[]", userProgressFirst.getString("progress"));
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        // Assert throws exception when current user is a student, hence not authorized
        postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        updateToken("somestudent"); // Authenticate as student
        
        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            makeRequest(HttpMethod.GET, "/explore/progress", null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testExploreProgressUnlocked() {
        // Setup and get IDs for achievement, student and demonstration
        postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = getIdFromAchievementCode("Code1");
        Long userId = getIdFromUserName("janestudent");
        Long demonstrationId = getDemoIdFromAchievementAndUser(achievementId, userId);

        postDemoResult(demonstrationId, achievementId, userId, Result.PASS);

        // Perform GET request for /explore/progress with 1 student and 1 unlocked achievement in DB
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/explore/progress", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Get JSON object from response body and assert user is in the list
        try {
            JSONObject jsonObject = new JSONObject(responseEntity.getBody());
            
            JSONArray achievements = jsonObject.getJSONArray("achievements");
            assertEquals(1, achievements.length());
            assertEquals("Code1", achievements.getJSONObject(0).getString("code"));
            
            JSONObject userProgressFirst = jsonObject.getJSONArray("userProgress").getJSONObject(0);
            assertEquals("janestudent", userProgressFirst.getJSONObject("user").getString("userName"));
            assertEquals("[\"Pass\"]", userProgressFirst.getString("progress"));
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        postNewAchievement("Code2", "Name2", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code2");
        postNewUser("James", "Smith", "james.smith@uu.se", "jamesstudent", "STUDENT");

        // Perform GET request for /explore/progress with 2 students and 2 achievement in DB, 1 unlocked by 1 student
        responseEntity = makeRequest(HttpMethod.GET, "/explore/progress", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Get JSON object from response body and assert user is in the list
        try {
            JSONObject jsonObject = new JSONObject(responseEntity.getBody());
            
            JSONArray achievements = jsonObject.getJSONArray("achievements");
            assertEquals(2, achievements.length());
            assertEquals("Code1", achievements.getJSONObject(0).getString("code"));
            assertEquals("Code2", achievements.getJSONObject(1).getString("code"));
            
            JSONArray userProgress = jsonObject.getJSONArray("userProgress");
            assertEquals(2, userProgress.length());
            JSONObject userProgressFirst = jsonObject.getJSONArray("userProgress").getJSONObject(0);
            assertEquals("janestudent", userProgressFirst.getJSONObject("user").getString("userName"));
            assertEquals("[\"Pass\",\"Fail\"]", userProgressFirst.getString("progress"));
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }
    }

    @Test
    public void testExploreStudent() {
        // Perform GET request for /explore/student/{userId} with non-existent user ID
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            makeRequest(HttpMethod.GET, "/explore/student/100", null, true);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        postNewUser("Jane", "Doe", "jane.doe@uu.se", "janestudent", "STUDENT");

        // Perform GET request for /explore/student/{userId} with 1 student in DB
        Long userId = getIdFromUserName("janestudent");
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/explore/student/" + userId, null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Get JSON object from response body and assert user data is in the returned object
        try {
            JSONObject jsonObject = new JSONObject(responseEntity.getBody());
            assertEquals("janestudent", jsonObject.getJSONObject("user").getString("userName"));
            assertEquals("Fun Course", jsonObject.getJSONObject("courseInstance").getString("name"));
            assertEquals(0, jsonObject.getJSONArray("unlocked").length());
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }
        
        // Assert throws exception when current user is a student, hence not authorized
        postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        updateToken("somestudent"); // Authenticate as student
        
        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            makeRequest(HttpMethod.GET, "/explore/student/" + userId, null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testGradeGroup() {
        // Perform POST request for /grade/group with neither student nor achievement in DB
        Json.GroupGradingCurl gradingData = Json.GroupGradingCurl.builder()
                .username("janestudent")
                .achievements(List.of("Code1"))
                .build();
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/grade/group", gradingData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Something went wrong in group grading", responseEntity.getBody());

        // Setup and get IDs for achievement, student and demonstration
        postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");

        // Perform POST request for /grade/group with 1 student and 1 achievement in DB but with request for non-existent achievement
        gradingData = Json.GroupGradingCurl.builder()
                .username("janestudent")
                .achievements(List.of("Code2"))
                .build();
        responseEntity = makeRequest(HttpMethod.POST, "/grade/group", gradingData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("User or achievement not found", responseEntity.getBody());

        // Perform POST request for /grade/group with 1 student and 1 achievement in DB
        gradingData = Json.GroupGradingCurl.builder()
                .username("janestudent")
                .achievements(List.of("Code1"))
                .build();
        responseEntity = makeRequest(HttpMethod.POST, "/grade/group", gradingData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("SUCCESS", responseEntity.getBody());

        // Perform GET request for /explore/progress to verify student has passed the achievement
        responseEntity = makeRequest(HttpMethod.GET, "/explore/progress", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Get JSON object from response body and assert user is in the list
        try {
            JSONObject jsonObject = new JSONObject(responseEntity.getBody());

            JSONArray achievements = jsonObject.getJSONArray("achievements");
            assertEquals(1, achievements.length());
            assertEquals("Code1", achievements.getJSONObject(0).getString("code"));

            JSONObject userProgressFirst = jsonObject.getJSONArray("userProgress").getJSONObject(0);
            assertEquals("janestudent", userProgressFirst.getJSONObject("user").getString("userName"));
            assertEquals("[\"Pass\"]", userProgressFirst.getString("progress"));
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        // Assert throws exception when current user is a student, hence not authorized
        postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            Json.GroupGradingCurl gradingDataForbidden = Json.GroupGradingCurl.builder()
                    .username("janestudent")
                    .achievements(List.of("Code1"))
                    .build();
            makeRequest(HttpMethod.POST, "/grade/group", gradingDataForbidden, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }
    
    @Test
    public void testGradeGroupUsers() {
        // Perform POST request for /grade/group_users with neither student nor achievement in DB
        Json.GroupGradingUsers requestBody = Json.GroupGradingUsers.builder()
                .userIds(List.of(1000L))
                .achievements(List.of("Code1"))
                .build();
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/grade/group_users", requestBody, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Something went wrong in group grading", responseEntity.getBody());

        // Setup and get IDs for achievement, student and demonstration
        postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long userId = getIdFromUserName("janestudent");

        // Perform POST request for /grade/group_users with 1 student and 1 achievement in DB but with request for non-existent achievement
        requestBody = Json.GroupGradingUsers.builder()
                .userIds(List.of(userId))
                .achievements(List.of("Code2"))
                .build();
        responseEntity = makeRequest(HttpMethod.POST, "/grade/group_users", requestBody, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("User or achievement not found", responseEntity.getBody());

        // Perform POST request for /grade/group_users with 1 student and 1 achievement in DB
        requestBody = Json.GroupGradingUsers.builder()
                .userIds(List.of(userId))
                .achievements(List.of("Code1"))
                .build();
        responseEntity = makeRequest(HttpMethod.POST, "/grade/group_users", requestBody, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("SUCCESS", responseEntity.getBody());

        // Another setup and get IDs for achievement, student and demonstration
        postNewAchievement("Code2", "Name2", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code2");
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "James", "Smith", "jamesstudent");
        Long userId2 = getIdFromUserName("jamesstudent");

        // Perform POST request for /grade/group_users with 2 students and 2 achievements in DB
        requestBody = Json.GroupGradingUsers.builder()
                .userIds(List.of(userId, userId2))
                .achievements(List.of("Code1", "Code2"))
                .build();
        responseEntity = makeRequest(HttpMethod.POST, "/grade/group_users", requestBody, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("SUCCESS", responseEntity.getBody());

        // Perform GET request for /explore/progress to verify students has passed both achievements
        responseEntity = makeRequest(HttpMethod.GET, "/explore/progress", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Get JSON object from response body and assert user is in the list
        try {
            JSONObject jsonObject = new JSONObject(responseEntity.getBody());

            JSONArray achievements = jsonObject.getJSONArray("achievements");
            assertEquals(2, achievements.length());
            assertEquals("Code1", achievements.getJSONObject(0).getString("code"));
            assertEquals("Code2", achievements.getJSONObject(1).getString("code"));
            
            JSONArray userProgress = jsonObject.getJSONArray("userProgress");
            assertEquals(2, userProgress.length());
            JSONObject userProgressFirst = userProgress.getJSONObject(0);
            assertEquals("janestudent", userProgressFirst.getJSONObject("user").getString("userName"));
            assertEquals("[\"Pass\",\"Pass\"]", userProgressFirst.getString("progress"));
            JSONObject userProgressSecond = userProgress.getJSONObject(1);
            assertEquals("jamesstudent", userProgressSecond.getJSONObject("user").getString("userName"));
            assertEquals("[\"Pass\",\"Pass\"]", userProgressSecond.getString("progress"));
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        // Assert throws exception when current user is a student, hence not authorized
        postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            Json.GroupGradingUsers gradingDataForbidden = Json.GroupGradingUsers.builder()
                    .userIds(List.of(1000L))
                    .achievements(List.of("Code1"))
                    .build();
            makeRequest(HttpMethod.POST, "/grade/group_users", gradingDataForbidden, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testRecentDemo() {
        // Setup and get IDs for achievement, student and demonstration
        postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = getIdFromAchievementCode("Code1");
        Long userId = getIdFromUserName("janestudent");
        Long demonstrationId = getDemoIdFromAchievementAndUser(achievementId, userId);

        // Perform GET request for /recent/demo with 1 active demonstration but not claimed by current user (johnteacher)
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/recent/demo", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());

        // Report demonstration as completed
        postClaimDemo(demonstrationId);

        // Perform GET request for /recent/demo with 1 active demonstration claimed by current user (johnteacher)
        responseEntity = makeRequest(HttpMethod.GET, "/recent/demo", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        

        // Get JSON object from response body and assert demo is in the list
        try {
            JSONArray jsonArray = new JSONArray(responseEntity.getBody());
            assertEquals(1, jsonArray.length());
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            assertEquals(demonstrationId, jsonObject.getInt("id"));
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        // Add 5 more students with active and claimed demonstrations
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "James", "Smith", "jamesstudent");
        Long userId2 = getIdFromUserName("jamesstudent");
        Long demonstrationId2 = getDemoIdFromAchievementAndUser(achievementId, userId2);
        postClaimDemo(demonstrationId2);

        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Kieran", "Smith", "kieranstudent");
        Long userId3 = getIdFromUserName("kieranstudent");
        Long demonstrationId3 = getDemoIdFromAchievementAndUser(achievementId, userId3);
        postClaimDemo(demonstrationId3);

        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Liam", "Smith", "liamstudent");
        Long userId4 = getIdFromUserName("liamstudent");
        Long demonstrationId4 = getDemoIdFromAchievementAndUser(achievementId, userId4);
        postClaimDemo(demonstrationId4);

        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Mason", "Smith", "masonstudent");
        Long userId5 = getIdFromUserName("masonstudent");
        Long demonstrationId5 = getDemoIdFromAchievementAndUser(achievementId, userId5);
        postClaimDemo(demonstrationId5);

        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Noah", "Smith", "noahstudent");
        Long userId6 = getIdFromUserName("noahstudent");
        Long demonstrationId6 = getDemoIdFromAchievementAndUser(achievementId, userId6);
        postClaimDemo(demonstrationId6);

        // Perform GET request for /recent/demo with 6 active demonstrations claimed by current user (johnteacher)
        responseEntity = makeRequest(HttpMethod.GET, "/recent/demo", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Get JSON object from response body and assert only 5 most recent demonstrations are returned and most recent is first
        try {
            JSONArray jsonArray = new JSONArray(responseEntity.getBody());
            assertEquals(5, jsonArray.length());
            assertEquals(demonstrationId6, jsonArray.getJSONObject(0).getInt("id"));
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        // Assert throws exception when current user is a student, hence not authorized
        postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            makeRequest(HttpMethod.GET, "/recent/demo", null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testRecentHelp() {
        // Setup and get IDs for achievement, student and help request
        postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long userId = getIdFromUserName("janestudent");
        Long helpRequestId = getHelpRequestIdFromUser(userId);

        // Perform GET request for /recent/help with 1 active help request but not claimed by current user (johnteacher)
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/recent/help", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());

        // Report help request as completed
        postOfferHelp(helpRequestId);

        // Perform GET request for /recent/help with 1 active help request claimed by current user (johnteacher)
        responseEntity = makeRequest(HttpMethod.GET, "/recent/help", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Get JSON object from response body and assert help request is in the list
        try {
            JSONArray jsonArray = new JSONArray(responseEntity.getBody());
            assertEquals(1, jsonArray.length());
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            assertEquals(helpRequestId, jsonObject.getInt("id"));
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        // Add 5 more students with active and claimed help requests
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "James", "Smith", "jamesstudent");
        Long userId2 = getIdFromUserName("jamesstudent");
        Long helpRequestId2 = getHelpRequestIdFromUser(userId2);
        postOfferHelp(helpRequestId2);

        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Kieran", "Smith", "kieranstudent");
        Long userId3 = getIdFromUserName("kieranstudent");
        Long helpRequestId3 = getHelpRequestIdFromUser(userId3);
        postOfferHelp(helpRequestId3);
        
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Liam", "Smith", "liamstudent");
        Long userId4 = getIdFromUserName("liamstudent");
        Long helpRequestId4 = getHelpRequestIdFromUser(userId4);
        postOfferHelp(helpRequestId4);

        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Mason", "Smith", "masonstudent");
        Long userId5 = getIdFromUserName("masonstudent");
        Long helpRequestId5 = getHelpRequestIdFromUser(userId5);
        postOfferHelp(helpRequestId5);

        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Noah", "Smith", "noahstudent");
        Long userId6 = getIdFromUserName("noahstudent");
        Long helpRequestId6 = getHelpRequestIdFromUser(userId6);
        postOfferHelp(helpRequestId6);

        // Perform GET request for /recent/help with 6 active help requests claimed by current user (johnteacher)
        responseEntity = makeRequest(HttpMethod.GET, "/recent/help", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Get JSON object from response body and assert only 5 most recent help requests are returned and most recent is first
        try {
            JSONArray jsonArray = new JSONArray(responseEntity.getBody());
            assertEquals(5, jsonArray.length());
            assertEquals(helpRequestId6, jsonArray.getJSONObject(0).getInt("id"));
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        // Assert throws exception when current user is a student, hence not authorized
        postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            makeRequest(HttpMethod.GET, "/recent/help", null, true);
        });
        assertEquals(HttpStatus.UNAUTHORIZED, notAuthException.getStatusCode());
    }

    @Test
    public void testRecentStudentDemo() {
        // Setup and get IDs for achievement, student and demonstration
        postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = getIdFromAchievementCode("Code1");
        Long userId = getIdFromUserName("janestudent");
        Long demonstrationId = getDemoIdFromAchievementAndUser(achievementId, userId);
        
        // Perform GET request for /recent/student/demo as a teacher with no active demonstrations
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/recent/student/demo", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());
        
        updateToken("janestudent"); // Authenticate as student

            // Perform GET request for /recent/student/demo with 1 active demonstration
            responseEntity = makeRequest(HttpMethod.GET, "/recent/student/demo", null, true);
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

            // Get JSON object from response body and assert user is in the list
            try {
                JSONArray jsonArray = new JSONArray(responseEntity.getBody());
                assertEquals(1, jsonArray.length());
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                assertEquals(demonstrationId, jsonObject.getInt("id"));
            }
            catch (JSONException e) {
                fail("Failed to parse JSON object/array: " + e.getMessage());
            }
        
        updateToken("johnteacher"); // Authenticate as teacher

        postDemoResult(demonstrationId, achievementId, userId, Result.PASS);
        
        // Create a new achievement and demonstration
        postNewAchievement("Code2", "Name2", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code2");
        Long achievementId2 = getIdFromAchievementCode("Code2");

        updateToken("janestudent"); // Authenticate as student
            
            postDemoRequest(achievementId2, userId);

            // Perform GET request for /recent/student/demo with 1 active and 1 passed demonstration
            responseEntity = makeRequest(HttpMethod.GET, "/recent/student/demo", null, true);
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

            // Get JSON object from response body and assert demos is in the list
            try {
                JSONArray jsonArray = new JSONArray(responseEntity.getBody());
                assertEquals(2, jsonArray.length());
            }
            catch (JSONException e) {
                fail("Failed to parse JSON object/array: " + e.getMessage());
            }
    }
    
    @Test
    public void testRecentStudentHelp() {
        // Setup and get IDs for achievement, student and help request
        postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long userId = getIdFromUserName("janestudent");
        Long helpRequestId = getHelpRequestIdFromUser(userId);
        
        // Perform GET request for /recent/student/help as a teacher with no active help requests
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/recent/student/help", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());
        
        updateToken("janestudent"); // Authenticate as student

            // Perform GET request for /recent/student/help with 1 active help request
            responseEntity = makeRequest(HttpMethod.GET, "/recent/student/help", null, true);
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

            // Get JSON object from response body and assert help request is in the list
            try {
                JSONArray jsonArray = new JSONArray(responseEntity.getBody());
                assertEquals(1, jsonArray.length());
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                assertEquals(helpRequestId, jsonObject.getInt("id"));
            }
            catch (JSONException e) {
                fail("Failed to parse JSON object/array: " + e.getMessage());
            }
        
        updateToken("johnteacher"); // Authenticate as teacher
        
        postOfferHelp(helpRequestId);
        postHelpDone(helpRequestId);

        updateToken("janestudent"); // Authenticate as student

            postHelpRequest(userId);

            // Perform GET request for /recent/student/help with 1 active and 1 completed help request
            responseEntity = makeRequest(HttpMethod.GET, "/recent/student/help", null, true);
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

            // Get JSON object from response body and assert help requests are in the list
            try {
                JSONArray jsonArray = new JSONArray(responseEntity.getBody());
                assertEquals(2, jsonArray.length());
            }
            catch (JSONException e) {
                fail("Failed to parse JSON object/array: " + e.getMessage());
            }
    }

    @Test
    public void testReportFinished() {
        // Perform GET request for /report/finished with no students
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/report/finished/", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().startsWith("%PDF"));
        
        // Setup and get IDs for achievement, student and demonstration
        postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        postNewAchievement("Code2", "Name2", "GRADE_4", "ACHIEVEMENT", "http://example.com/Code2");
        postNewAchievement("Code3", "Name3", "GRADE_5", "ACHIEVEMENT", "http://example.com/Code3");
        
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = getIdFromAchievementCode("Code1");
        Long userId = getIdFromUserName("janestudent");
        Long demonstrationId = getDemoIdFromAchievementAndUser(achievementId, userId);
        postClaimDemo(demonstrationId);
        postDemoResult(demonstrationId, achievementId, userId, Result.PASS);
        
        // Perform GET request for /report/finished with 1 student and 3 achievements in DB (GRADE_3, GRADE_4, GRADE_5)
        responseEntity = makeRequest(HttpMethod.GET, "/report/finished/", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().startsWith("%PDF"));
        
        // TODO: Assert content of PDF file to contain Jane Doe and GRADE_3

        // Assert throws exception when current user is a student, hence not authorized
        postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            makeRequest(HttpMethod.GET, "/report/finished/", null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testReportPartial() {
        // NOTE: /report/partial/ returns a list of students without a final grade
        // Perform GET request for /report/partial with no students
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/report/partial/", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());

        // Add 3 achievements and 2 students with active demonstrations (GRADE_3 and GRADE_4 respectively)
        postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        postNewAchievement("Code2", "Name2", "GRADE_4", "ACHIEVEMENT", "http://example.com/Code2");
        postNewAchievement("Code3", "Name3", "GRADE_5", "ACHIEVEMENT", "http://example.com/Code3");
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");        
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code2", "James", "Smith", "jamesstudent");

        // Perform GET request for /report/partial with 2 students without a final grade
        responseEntity = makeRequest(HttpMethod.GET, "/report/partial/", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        try {
            JSONArray jsonArray = new JSONArray(responseEntity.getBody());
            assertEquals(2, jsonArray.length());
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        // Make first student pass the GRADE_3 achievement, thus no longer in the list
        Long achievementId = getIdFromAchievementCode("Code1");
        Long userId = getIdFromUserName("janestudent");
        Long demonstrationId = getDemoIdFromAchievementAndUser(achievementId, userId);
        postClaimDemo(demonstrationId);
        postDemoResult(demonstrationId, achievementId, userId, Result.PASS);
        
        // Make second student pass the GRADE_4 achievement and still in the list
        Long achievementId2 = getIdFromAchievementCode("Code2");
        Long userId2 = getIdFromUserName("jamesstudent");
        Long demonstrationId2 = getDemoIdFromAchievementAndUser(achievementId2, userId2);
        postClaimDemo(demonstrationId2);
        postDemoResult(demonstrationId2, achievementId2, userId2, Result.PASS);

        // Perform GET request for /report/partial with 1 student without a final grade but 1 passed achievement
        responseEntity = makeRequest(HttpMethod.GET, "/report/partial/", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        try {
            JSONArray jsonArray = new JSONArray(responseEntity.getBody());
            assertEquals(1, jsonArray.length());

            JSONObject jsonObject = jsonArray.getJSONObject(0);
            assertEquals("jamesstudent", jsonObject.getString("username"));

            JSONArray passedAchievementsArray = jsonObject.getJSONArray("passedAchievements");
            assertEquals(1, passedAchievementsArray.length());

            String first = passedAchievementsArray.getString(0);
            assertEquals("Code2", first);
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        // Assert throws exception when current user is a student, hence not authorized
        postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            makeRequest(HttpMethod.GET, "/report/partial/", null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testPartialHp() {
        // Perform GET request for /report/partial with no students
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/report/partial/hp", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());
        
        // Add 15 achievements of "GRADE_3" and "ACHIEVEMENT" type + 2 of "ASSIGNMENT" type to DB
        for (int i = 1; i <= 15; i++) {
            postNewAchievement("Code" + i, "Name" + i, "GRADE_3", "ACHIEVEMENT", "http://example.com/Code" + i);
        }
        postNewAchievement("Code16", "Name16", "GRADE_3", "ASSIGNMENT", "http://example.com/Code16");
        postNewAchievement("Code17", "Name17", "GRADE_3", "ASSIGNMENT", "http://example.com/Code17");
        
        // Add 1 student with active demonstration with 1 passed achievement
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = getIdFromAchievementCode("Code1");
        Long userId = getIdFromUserName("janestudent");
        Long demonstrationId = getDemoIdFromAchievementAndUser(achievementId, userId);
        postClaimDemo(demonstrationId);
        postDemoResult(demonstrationId, achievementId, userId, Result.PASS);
        
        // Perform GET request for /report/partial with 1 student without any HP
        responseEntity = makeRequest(HttpMethod.GET, "/report/partial/hp", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());

        // Make student pass the remaining 14+2 GRADE_3 achievements
        List<String> achievements = new ArrayList<>();
        for (int i = 2; i <= 17; i++) {
            achievements.add("Code" + i);
        }

        Json.GroupGradingCurl gradingData = Json.GroupGradingCurl.builder()
                .username("janestudent")
                .achievements(achievements)
                .build();
        responseEntity = makeRequest(HttpMethod.POST, "/grade/group", gradingData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Perform GET request for /report/partial/hp with 1 student with enough achievements passed for HP
        responseEntity = makeRequest(HttpMethod.GET, "/report/partial/hp", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        try {
            JSONArray jsonArray = new JSONArray(responseEntity.getBody());
            assertEquals(1, jsonArray.length());

            JSONObject jsonObject = jsonArray.getJSONObject(0);
            assertEquals("janestudent", jsonObject.getString("username"));

            JSONArray creditsArray = jsonObject.getJSONArray("credits");
            assertEquals(1, creditsArray.length());

            String first = creditsArray.getString(0);
            assertEquals("INLUPP1", first);
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        // Assert throws exception when current user is a student, hence not authorized
        postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            makeRequest(HttpMethod.GET, "/report/partial/hp", null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testPartialHpPDF() {
        // Perform GET request for /report/finished with no students
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/report/partial/hp/pdf", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().startsWith("%PDF"));
        
        // Add 15 achievements of "GRADE_3" and "ACHIEVEMENT" type + 2 of "ASSIGNMENT" type to DB
        for (int i = 1; i <= 15; i++) {
            postNewAchievement("Code" + i, "Name" + i, "GRADE_3", "ACHIEVEMENT", "http://example.com/Code" + i);
        }
        postNewAchievement("Code16", "Name16", "GRADE_3", "ASSIGNMENT", "http://example.com/Code16");
        postNewAchievement("Code17", "Name17", "GRADE_3", "ASSIGNMENT", "http://example.com/Code17");
        
        // Add 1 student with active demonstration with 1 passed achievement
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = getIdFromAchievementCode("Code1");
        Long userId = getIdFromUserName("janestudent");
        Long demonstrationId = getDemoIdFromAchievementAndUser(achievementId, userId);
        postClaimDemo(demonstrationId);
        postDemoResult(demonstrationId, achievementId, userId, Result.PASS);

        // Make student pass the remaining 14+2 GRADE_3 achievements
        List<String> achievements = new ArrayList<>();
        for (int i = 2; i <= 17; i++) {
            achievements.add("Code" + i);
        }

        Json.GroupGradingCurl gradingData = Json.GroupGradingCurl.builder()
                .username("janestudent")
                .achievements(achievements)
                .build();
        responseEntity = makeRequest(HttpMethod.POST, "/grade/group", gradingData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        // Perform GET request for /report/finished with 1 student and 3 achievements in DB (GRADE_3, GRADE_4, GRADE_5)
        responseEntity = makeRequest(HttpMethod.GET, "/report/partial/hp/pdf", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().startsWith("%PDF"));
        
        // TODO: Assert content of PDF file to contain Jane Doe and GRADE_3

        // Assert throws exception when current user is a student, hence not authorized
        postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            makeRequest(HttpMethod.GET, "/report/partial/hp/pdf", null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }
}