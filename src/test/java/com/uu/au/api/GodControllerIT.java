package com.uu.au.api;

import com.uu.au.models.Json;
import com.uu.au.enums.Result;
import com.uu.au.enums.SortKey;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;
import org.springframework.test.annotation.DirtiesContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class GodControllerIT {

    private TestHelper testHelper;

    @BeforeEach
    public void setup() {
        this.testHelper = new TestHelper(); // Sets up a TestHelper object to access helper methods

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
    public void testAchievementAllRemaining() {
        // Perform GET request for /achievement/all-remaining/{code} with no achievements
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.GET, "/achievement/all-remaining/Code1", null, true);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");

        // Perform GET request for /achievement/all-remaining/{code} with 1 achievement but no students
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/achievement/all-remaining/Code1", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());

        testHelper.postNewUser("Jane", "Doe", "jane.doe@uu.se", "janestudent", "STUDENT");
        
        // Perform GET request for /achievement/all-remaining/{code} with 1 achievement and 1 student
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/achievement/all-remaining/Code1", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[\"Jane Doe <jane.doe@uu.se>\"]", responseEntity.getBody());

        testHelper.postNewUser("James", "Smith", "james.smith@uu.se", "jamesstudent", "STUDENT");

        // Perform GET request for /achievement/all-remaining/{code} with 1 achievement and 2 students
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/achievement/all-remaining/Code1", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[\"Jane Doe <jane.doe@uu.se>\",\"James Smith <james.smith@uu.se>\"]", responseEntity.getBody());

        // Assert throws exception when current user is a student, hence not authorized
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student
        
        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.GET, "/achievement/all-remaining/Code1", null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testAchievementAllRemainingDemonstrated() {
        // Setup and get IDs for achievement, student and demonstration
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        Long userId = testHelper.getIdFromUserName("janestudent");
        Long demonstrationId = testHelper.getDemoIdFromAchievementAndUser(achievementId, userId);
        
        testHelper.postDemoResult(demonstrationId, achievementId, userId, Result.PASS);

        // Perform GET request for /achievement/all-remaining/{code} with 1 achievement and 1 student in system but NOT in remaining
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/achievement/all-remaining/Code1", null, true);

        // Assert status code and response body
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());
    }

    @Test
    public void testAchivementCodeToId() {
        // Perform GET request for /achievement/code-to-id/{code} with non-existent achievement code
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/achievement/code-to-id/Code1", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Achivement Code1 not found", responseEntity.getBody());

        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        
        // Perform GET request for /achievement/code-to-id/{code} with updated achievement data
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/achievement/code-to-id/Code1", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        String responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        
        try {
            Integer.parseInt(responseBody);
        } catch (NumberFormatException e) {
            fail("Response body is not an integer");
        }
        
        // Assert throws exception when current user is a student, hence not authorized
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.GET, "/achievement/all-remaining/Code1", null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testResetCodeExamBlocker() {
        // Perform GET request for /admin/resetCodeExamBlocker with no students
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/admin/resetCodeExamBlocker", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().startsWith("Ignoring all failed code exam demonstration attempts earlier than"));

        // Setup and get IDs for achievement, student and demonstration
        testHelper.postNewAchievement("CodeExam1", "Name1", "GRADE_3", "CODE_EXAM", "http://example.com/Code1");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("CodeExam1", "Jane", "Doe", "janestudent");
        Long achievementId = testHelper.getIdFromAchievementCode("CodeExam1");
        Long userId = testHelper.getIdFromUserName("janestudent");
        Long demonstrationId = testHelper.getDemoIdFromAchievementAndUser(achievementId, userId);
        
        testHelper.postDemoResult(demonstrationId, achievementId, userId, Result.FAIL);
        
        // Perform GET request for /admin/resetCodeExamBlocker again
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/admin/resetCodeExamBlocker", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // FIXME: Assert an API dependent on the codeExamDemonstrationBlocker (HOW? Must move forward in time to check blocker)
    }

    @Test
    public void testClearAllRequests() {
        // Perform GET request for /clearLists with no requests
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/clearLists", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody()); // Endpoint has no return value
        
        // Verify queues are empty
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/demonstrations/activeAndSubmittedOrPickedUp", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());
        
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/helpRequests/active", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());

        // Assert throws exception when current user is a student, hence not authorized
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student
        
        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.GET, "/clearLists", null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }
    
    @Test
    public void testClearAllRequestsWithActiveRequests() {
        // Verify queues are empty
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/demonstrations/activeAndSubmittedOrPickedUp", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());
        
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/helpRequests/active", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());

        // Setup achievement, student and demonstration
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");

        // Verify queues are NOT empty
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/demonstrations/activeAndSubmittedOrPickedUp", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotEquals("[]", responseEntity.getBody());
        
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/helpRequests/active", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotEquals("[]", responseEntity.getBody());

        // Perform GET request for /clearLists with active requests in the system, assert status code
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/clearLists", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        // Verify queues are empty AGAIN
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/demonstrations/activeAndSubmittedOrPickedUp", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());
        
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/helpRequests/active", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());
    }

    @Test
    public void testGetCourse() {
        // Perform GET request for /course with initial course data
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/course", null, true);

        // FIXME: Assert throws exception with no course data (How? Course data is needed to get token!)

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
            testHelper.makeRequest(HttpMethod.POST, "/course", courseData, true);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getResponseBodyAsString().contains("COURSE_ALREADY_EXISTS"));

        // FIXME: Assert SUCCESS of post with no course data (How? Course data is needed to get token!)

        // Assert throws exception when current user is a student, hence not authorized
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student
        
        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.POST, "/course", courseData, true);
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

        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.PUT, "/course", updatedCourseData, true);

        // Assert status code and response body
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("SUCCESS", responseEntity.getBody());

        // Perform GET request for /course to check updated course data
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/course", null, true);

        // Assert status code and response body
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        String responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("Course 2"));

        // Assert throws exception when current user is a student, hence not authorized
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student
        
        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.PUT, "/course", updatedCourseData, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testExploreAchievementNoAchievement() {
        // Perform GET request for /explore/achievement/{achievementId} with non-existent achievement ID
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/explore/achievement/100", null, true);
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
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        
        // Perform GET request for /explore/achievement/{achievementId} with 1 achievement in DB but no unlocked/pushed back users
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/explore/achievement/" + achievementId, null, true);
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
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student
        
        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.GET, "/explore/achievement/" + achievementId, null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testExploreAchievementDemonstrated() {
        // Setup and get IDs for achievement, student and demonstration
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        Long userId = testHelper.getIdFromUserName("janestudent");
        Long demonstrationId = testHelper.getDemoIdFromAchievementAndUser(achievementId, userId);

        testHelper.postDemoResult(demonstrationId, achievementId, userId, Result.PASS);
        
        // Perform GET request for /explore/achievement/{achievementId} with 1 achievement in DB unlocked by the student
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/explore/achievement/" + achievementId, null, true);
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
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        Long userId = testHelper.getIdFromUserName("janestudent");
        Long demonstrationId = testHelper.getDemoIdFromAchievementAndUser(achievementId, userId);

        testHelper.postDemoResult(demonstrationId, achievementId, userId, Result.PUSHBACK);
        
        // Perform GET request for /explore/achievement/{achievementId} with 1 achievement in DB pushed back for the student
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/explore/achievement/" + achievementId, null, true);
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
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/explore/progress", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("{\"achievements\":[],\"userProgress\":[]}", responseEntity.getBody());
        
        testHelper.postNewUser("Jane", "Doe", "jane.doe@uu.se", "janestudent", "STUDENT");
        
        // Perform GET request for /explore/progress with 1 student in DB
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/explore/progress", null, true);
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
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student
        
        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.GET, "/explore/progress", null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testExploreProgressUnlocked() {
        // Setup and get IDs for achievement, student and demonstration
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        Long userId = testHelper.getIdFromUserName("janestudent");
        Long demonstrationId = testHelper.getDemoIdFromAchievementAndUser(achievementId, userId);

        testHelper.postDemoResult(demonstrationId, achievementId, userId, Result.PASS);

        // Perform GET request for /explore/progress with 1 student and 1 unlocked achievement in DB
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/explore/progress", null, true);
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

        testHelper.postNewAchievement("Code2", "Name2", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code2");
        testHelper.postNewUser("James", "Smith", "james.smith@uu.se", "jamesstudent", "STUDENT");

        // Perform GET request for /explore/progress with 2 students and 2 achievement in DB, 1 unlocked by 1 student
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/explore/progress", null, true);
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
            testHelper.makeRequest(HttpMethod.GET, "/explore/student/100", null, true);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        testHelper.postNewUser("Jane", "Doe", "jane.doe@uu.se", "janestudent", "STUDENT");

        // Perform GET request for /explore/student/{userId} with 1 student in DB
        Long userId = testHelper.getIdFromUserName("janestudent");
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/explore/student/" + userId, null, true);
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
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student
        
        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.GET, "/explore/student/" + userId, null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testExploreVelocity() {
        Json.ExploreVelocity velocityData = Json.ExploreVelocity.builder()
                .velocity(1L)
                .sortBy(SortKey.VELOCITY_DEC)
                .build();

        // Perform POST request for /explore/velocity with no students
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.POST, "/explore/velocity", velocityData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // FIXME: Add more tests when logic is implemented

        // Assert throws exception when current user is a student, hence not authorized
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.POST, "/explore/velocity", velocityData, true);
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
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.POST, "/grade/group", gradingData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Something went wrong in group grading", responseEntity.getBody());

        // Setup and get IDs for achievement, student and demonstration
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");

        // Perform POST request for /grade/group with 1 student and 1 achievement in DB but with request for non-existent achievement
        gradingData = Json.GroupGradingCurl.builder()
                .username("janestudent")
                .achievements(List.of("Code2"))
                .build();
        responseEntity = testHelper.makeRequest(HttpMethod.POST, "/grade/group", gradingData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("User or achievement not found", responseEntity.getBody());

        // Perform POST request for /grade/group with 1 student and 1 achievement in DB
        gradingData = Json.GroupGradingCurl.builder()
                .username("janestudent")
                .achievements(List.of("Code1"))
                .build();
        responseEntity = testHelper.makeRequest(HttpMethod.POST, "/grade/group", gradingData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("SUCCESS", responseEntity.getBody());

        // Perform GET request for /explore/progress to verify student has passed the achievement
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/explore/progress", null, true);
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
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            Json.GroupGradingCurl gradingDataForbidden = Json.GroupGradingCurl.builder()
                    .username("janestudent")
                    .achievements(List.of("Code1"))
                    .build();
            testHelper.makeRequest(HttpMethod.POST, "/grade/group", gradingDataForbidden, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }
    
    @Test
    public void testGradeGroupUsers() {
        // Perform POST request for /grade/group_users with neither student nor achievement in DB
        Json.GroupGradingUsers requestBody = Json.GroupGradingUsers.builder()
                .userIds(List.of(10000L))
                .achievements(List.of("Code1"))
                .build();
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.POST, "/grade/group_users", requestBody, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Something went wrong in group grading", responseEntity.getBody());

        // Setup and get IDs for achievement, student and demonstration
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long userId = testHelper.getIdFromUserName("janestudent");

        // Perform POST request for /grade/group_users with 1 student and 1 achievement in DB but with request for non-existent achievement
        requestBody = Json.GroupGradingUsers.builder()
                .userIds(List.of(userId))
                .achievements(List.of("Code2"))
                .build();
        responseEntity = testHelper.makeRequest(HttpMethod.POST, "/grade/group_users", requestBody, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("User or achievement not found", responseEntity.getBody());

        // Perform POST request for /grade/group_users with 1 student and 1 achievement in DB
        requestBody = Json.GroupGradingUsers.builder()
                .userIds(List.of(userId))
                .achievements(List.of("Code1"))
                .build();
        responseEntity = testHelper.makeRequest(HttpMethod.POST, "/grade/group_users", requestBody, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("SUCCESS", responseEntity.getBody());

        // Another setup and get IDs for achievement, student and demonstration
        testHelper.postNewAchievement("Code2", "Name2", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code2");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "James", "Smith", "jamesstudent");
        Long userId2 = testHelper.getIdFromUserName("jamesstudent");

        // Perform POST request for /grade/group_users with 2 students and 2 achievements in DB
        requestBody = Json.GroupGradingUsers.builder()
                .userIds(List.of(userId, userId2))
                .achievements(List.of("Code1", "Code2"))
                .build();
        responseEntity = testHelper.makeRequest(HttpMethod.POST, "/grade/group_users", requestBody, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("SUCCESS", responseEntity.getBody());

        // Perform GET request for /explore/progress to verify students has passed both achievements
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/explore/progress", null, true);
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
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            Json.GroupGradingUsers gradingDataForbidden = Json.GroupGradingUsers.builder()
                    .userIds(List.of(10000L))
                    .achievements(List.of("Code1"))
                    .build();
            testHelper.makeRequest(HttpMethod.POST, "/grade/group_users", gradingDataForbidden, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testImportPartial() {
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.postNewAchievement("Code2", "Name2", "GRADE_4", "ACHIEVEMENT", "http://example.com/Code2");
        
        // Perform POST request for /import/partial with empty JSON array
        testHelper.postImportPartial("[]");
        
        // Perform POST request for /import/partial with JSON array containing 1 new student
        String jsonArrayOne = "[{\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"username\":\"janestudent\",\"email\":\"jane.doe@uu.se\",\"passedAchievements\":[\"Code1\",\"Code2\"],\"lastLogin\":\"" + LocalDateTime.now() + "\"}]";
        testHelper.postImportPartial(jsonArrayOne);

        // Perform GET request for /explore/progress to check if student is in the list and has passed achievements
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/explore/progress", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        try {
            JSONObject jsonObject = new JSONObject(responseEntity.getBody());
            
            JSONArray achievements = jsonObject.getJSONArray("achievements");
            assertEquals(2, achievements.length());
            assertEquals("Code1", achievements.getJSONObject(0).getString("code"));
            assertEquals("Code2", achievements.getJSONObject(1).getString("code"));
            
            JSONArray userProgress = jsonObject.getJSONArray("userProgress");
            assertEquals(1, userProgress.length());
            JSONObject userProgressFirst = jsonObject.getJSONArray("userProgress").getJSONObject(0);
            assertEquals("janestudent", userProgressFirst.getJSONObject("user").getString("userName"));
            assertEquals("[\"Pass\",\"Pass\"]", userProgressFirst.getString("progress"));
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        // Perform POST request for /import/partial with JSON array containing 1 existing and 2 new students (one with lastLogin set to null)
        String jsonArrayThree = "[{\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"username\":\"janestudent\",\"email\":\"jane.doe@uu.se\",\"passedAchievements\":[\"Code1\",\"Code2\"],\"lastLogin\":\"" + LocalDateTime.now() + "\"}," +
                                "{\"firstName\":\"James\",\"lastName\":\"Smith\",\"username\":\"jamesstudent\",\"email\":\"james.smith@uu.se\",\"passedAchievements\":[\"Code1\"],\"lastLogin\":null}," +
                                "{\"firstName\":\"Kieran\",\"lastName\":\"Smith\",\"username\":\"kieranstudent\",\"email\":\"kieran.smith@uu.se\",\"passedAchievements\":[\"Code2\"],\"lastLogin\":\"" + LocalDateTime.now() + "\"}]";
        testHelper.postImportPartial(jsonArrayThree);

        // Perform GET request for /explore/progress to check 1 existing and 1 new students are in the list the new have passed achievements
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/explore/progress", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        try {
            JSONObject jsonObject = new JSONObject(responseEntity.getBody());
            
            JSONArray userProgress = jsonObject.getJSONArray("userProgress");
            assertEquals(2, userProgress.length());

            JSONObject userProgressFirst = jsonObject.getJSONArray("userProgress").getJSONObject(0);
            assertEquals("janestudent", userProgressFirst.getJSONObject("user").getString("userName"));
            assertEquals("[\"Pass\",\"Pass\"]", userProgressFirst.getString("progress"));

            JSONObject userProgressSecond = jsonObject.getJSONArray("userProgress").getJSONObject(1);
            assertEquals("kieranstudent", userProgressSecond.getJSONObject("user").getString("userName"));
            assertEquals("[\"Fail\",\"Pass\"]", userProgressSecond.getString("progress"));
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        // Assert throws exception when current user is a student, hence not authorized
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.postImportPartial("[]");
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testQos() {
        // Perform GET request for /qos with no students
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/qos", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        // Check JSON object from response body
        try {
            JSONObject jsonObject = new JSONObject(responseEntity.getBody());
            assertEquals(0, jsonObject.getInt("helpRequestsPending"));
            assertEquals(0, jsonObject.getInt("helpRequestsPickupTime"));
            assertEquals(0, jsonObject.getInt("helpRequestsRoundtripTime"));
            assertEquals(0, jsonObject.getInt("demonstrationsPending"));
            assertEquals(0, jsonObject.getInt("demonstrationsPickupTime"));
            assertEquals(0, jsonObject.getInt("demonstrationsRoundtripTime"));
            assertEquals(0, jsonObject.getInt("procentEverLoggedIn"));
            assertEquals(0, jsonObject.getInt("procentLoggedInLastTwoWeeks"));
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        // Setup and 2 students with active demonstrations and help requests
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long userId = testHelper.getIdFromUserName("janestudent");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        Long demonstrationId = testHelper.getDemoIdFromAchievementAndUser(achievementId, userId);
        Long helpRequestId = testHelper.getHelpRequestIdFromUser(userId);

        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "James", "Smith", "jamesstudent");
        Long userId2 = testHelper.getIdFromUserName("jamesstudent");
        Long achievementId2 = testHelper.getIdFromAchievementCode("Code1");
        Long demonstrationId2 = testHelper.getDemoIdFromAchievementAndUser(achievementId2, userId2);
        Long helpRequestId2 = testHelper.getHelpRequestIdFromUser(userId2);

        // Create 2 more students, but without demos and help requests and never logged in
        testHelper.postNewUser("Kieran","Smith","kieran.smiths@uu.se","kieranstudent","STUDENT");
        testHelper.postNewUser("Liam","Smith","liam.smiths@uu.se","liamstudent","STUDENT");

        // Perform GET request for /qos with 2 active demonstrations and help requests and 2 students never logged in
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/qos", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Check JSON object from response body
        try {
            JSONObject jsonObject = new JSONObject(responseEntity.getBody());
            assertEquals(2, jsonObject.getInt("helpRequestsPending"));
            assertEquals(2, jsonObject.getInt("demonstrationsPending"));
            assertEquals(50, jsonObject.getInt("procentEverLoggedIn"));
            assertEquals(50, jsonObject.getInt("procentLoggedInLastTwoWeeks"));
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        // Claim and pass demonstration and help request for first student
        testHelper.postDemoClaim(demonstrationId);
        testHelper.postDemoResult(demonstrationId, achievementId, userId, Result.PASS);
        testHelper.postOfferHelp(helpRequestId);
        testHelper.postHelpDone(helpRequestId);

        // Perform GET request for /qos with 1 active demonstrations and help requests
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/qos", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Check JSON object from response body
        try {
            JSONObject jsonObject = new JSONObject(responseEntity.getBody());
            assertEquals(1, jsonObject.getInt("helpRequestsPending"));
            assertEquals(1, jsonObject.getInt("demonstrationsPending"));
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        // Claim and pass demonstration and help request for second student
        testHelper.postDemoClaim(demonstrationId2);
        testHelper.postDemoResult(demonstrationId2, achievementId2, userId2, Result.PASS);
        testHelper.postOfferHelp(helpRequestId2);
        testHelper.postHelpDone(helpRequestId2);

        // Perform GET request for /qos with 0 active demonstrations and help requests
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/qos", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Check JSON object from response body
        try {
            JSONObject jsonObject = new JSONObject(responseEntity.getBody());
            assertEquals(0, jsonObject.getInt("helpRequestsPending"));
            assertEquals(0, jsonObject.getInt("demonstrationsPending"));
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        // Assert throws exception when current user is a student, hence not authorized
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.GET, "/qos", null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testRecentDemo() {
        // Setup and get IDs for achievement, student and demonstration
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        Long userId = testHelper.getIdFromUserName("janestudent");
        Long demonstrationId = testHelper.getDemoIdFromAchievementAndUser(achievementId, userId);

        // Perform GET request for /recent/demo with 1 active demonstration but not claimed by current user (johnteacher)
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/recent/demo", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());

        // Report demonstration as completed
        testHelper.postDemoClaim(demonstrationId);

        // Perform GET request for /recent/demo with 1 active demonstration claimed by current user (johnteacher)
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/recent/demo", null, true);
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
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "James", "Smith", "jamesstudent");
        Long userId2 = testHelper.getIdFromUserName("jamesstudent");
        Long demonstrationId2 = testHelper.getDemoIdFromAchievementAndUser(achievementId, userId2);
        testHelper.postDemoClaim(demonstrationId2);

        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Kieran", "Smith", "kieranstudent");
        Long userId3 = testHelper.getIdFromUserName("kieranstudent");
        Long demonstrationId3 = testHelper.getDemoIdFromAchievementAndUser(achievementId, userId3);
        testHelper.postDemoClaim(demonstrationId3);

        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Liam", "Smith", "liamstudent");
        Long userId4 = testHelper.getIdFromUserName("liamstudent");
        Long demonstrationId4 = testHelper.getDemoIdFromAchievementAndUser(achievementId, userId4);
        testHelper.postDemoClaim(demonstrationId4);

        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Mason", "Smith", "masonstudent");
        Long userId5 = testHelper.getIdFromUserName("masonstudent");
        Long demonstrationId5 = testHelper.getDemoIdFromAchievementAndUser(achievementId, userId5);
        testHelper.postDemoClaim(demonstrationId5);

        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Noah", "Smith", "noahstudent");
        Long userId6 = testHelper.getIdFromUserName("noahstudent");
        Long demonstrationId6 = testHelper.getDemoIdFromAchievementAndUser(achievementId, userId6);
        testHelper.postDemoClaim(demonstrationId6);

        // Perform GET request for /recent/demo with 6 active demonstrations claimed by current user (johnteacher)
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/recent/demo", null, true);
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
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.GET, "/recent/demo", null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testRecentHelp() {
        // Setup and get IDs for achievement, student and help request
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long userId = testHelper.getIdFromUserName("janestudent");
        Long helpRequestId = testHelper.getHelpRequestIdFromUser(userId);

        // Perform GET request for /recent/help with 1 active help request but not claimed by current user (johnteacher)
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/recent/help", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());

        // Report help request as completed
        testHelper.postOfferHelp(helpRequestId);

        // Perform GET request for /recent/help with 1 active help request claimed by current user (johnteacher)
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/recent/help", null, true);
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
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "James", "Smith", "jamesstudent");
        Long userId2 = testHelper.getIdFromUserName("jamesstudent");
        Long helpRequestId2 = testHelper.getHelpRequestIdFromUser(userId2);
        testHelper.postOfferHelp(helpRequestId2);

        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Kieran", "Smith", "kieranstudent");
        Long userId3 = testHelper.getIdFromUserName("kieranstudent");
        Long helpRequestId3 = testHelper.getHelpRequestIdFromUser(userId3);
        testHelper.postOfferHelp(helpRequestId3);
        
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Liam", "Smith", "liamstudent");
        Long userId4 = testHelper.getIdFromUserName("liamstudent");
        Long helpRequestId4 = testHelper.getHelpRequestIdFromUser(userId4);
        testHelper.postOfferHelp(helpRequestId4);

        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Mason", "Smith", "masonstudent");
        Long userId5 = testHelper.getIdFromUserName("masonstudent");
        Long helpRequestId5 = testHelper.getHelpRequestIdFromUser(userId5);
        testHelper.postOfferHelp(helpRequestId5);

        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Noah", "Smith", "noahstudent");
        Long userId6 = testHelper.getIdFromUserName("noahstudent");
        Long helpRequestId6 = testHelper.getHelpRequestIdFromUser(userId6);
        testHelper.postOfferHelp(helpRequestId6);

        // Perform GET request for /recent/help with 6 active help requests claimed by current user (johnteacher)
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/recent/help", null, true);
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
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.GET, "/recent/help", null, true);
        });
        assertEquals(HttpStatus.UNAUTHORIZED, notAuthException.getStatusCode());
    }

    @Test
    public void testRecentStudentDemo() {
        // Setup and get IDs for achievement, student and demonstration
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        Long userId = testHelper.getIdFromUserName("janestudent");
        Long demonstrationId = testHelper.getDemoIdFromAchievementAndUser(achievementId, userId);
        
        // Perform GET request for /recent/student/demo as a teacher with no active demonstrations
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/recent/student/demo", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());
        
        testHelper.updateToken("janestudent"); // Authenticate as student

            // Perform GET request for /recent/student/demo with 1 active demonstration
            responseEntity = testHelper.makeRequest(HttpMethod.GET, "/recent/student/demo", null, true);
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
        
        testHelper.updateToken("johnteacher"); // Authenticate as teacher

        testHelper.postDemoResult(demonstrationId, achievementId, userId, Result.PASS);
        
        // Create a new achievement and demonstration
        testHelper.postNewAchievement("Code2", "Name2", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code2");
        Long achievementId2 = testHelper.getIdFromAchievementCode("Code2");

        testHelper.updateToken("janestudent"); // Authenticate as student
            
            testHelper.postDemoRequest(achievementId2, userId);

            // Perform GET request for /recent/student/demo with 1 active and 1 passed demonstration
            responseEntity = testHelper.makeRequest(HttpMethod.GET, "/recent/student/demo", null, true);
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
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long userId = testHelper.getIdFromUserName("janestudent");
        Long helpRequestId = testHelper.getHelpRequestIdFromUser(userId);
        
        // Perform GET request for /recent/student/help as a teacher with no active help requests
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/recent/student/help", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());
        
        testHelper.updateToken("janestudent"); // Authenticate as student

            // Perform GET request for /recent/student/help with 1 active help request
            responseEntity = testHelper.makeRequest(HttpMethod.GET, "/recent/student/help", null, true);
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
        
        testHelper.updateToken("johnteacher"); // Authenticate as teacher
        
        testHelper.postOfferHelp(helpRequestId);
        testHelper.postHelpDone(helpRequestId);

        testHelper.updateToken("janestudent"); // Authenticate as student

            testHelper.postHelpRequest(userId);

            // Perform GET request for /recent/student/help with 1 active and 1 completed help request
            responseEntity = testHelper.makeRequest(HttpMethod.GET, "/recent/student/help", null, true);
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
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/report/finished/", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().startsWith("%PDF"));
        
        // Setup and get IDs for achievement, student and demonstration
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.postNewAchievement("Code2", "Name2", "GRADE_4", "ACHIEVEMENT", "http://example.com/Code2");
        testHelper.postNewAchievement("Code3", "Name3", "GRADE_5", "ACHIEVEMENT", "http://example.com/Code3");
        
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        Long userId = testHelper.getIdFromUserName("janestudent");
        Long demonstrationId = testHelper.getDemoIdFromAchievementAndUser(achievementId, userId);
        testHelper.postDemoClaim(demonstrationId);
        testHelper.postDemoResult(demonstrationId, achievementId, userId, Result.PASS);
        
        // Perform GET request for /report/finished with 1 student and 3 achievements in DB (GRADE_3, GRADE_4, GRADE_5)
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/report/finished/", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().startsWith("%PDF"));
        
        // FIXME: Assert content of PDF file to contain Jane Doe and GRADE_3

        // Assert throws exception when current user is a student, hence not authorized
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.GET, "/report/finished/", null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testReportPartial() {
        // NOTE: /report/partial/ returns a list of students without a final grade
        // Perform GET request for /report/partial with no students
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/report/partial/", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());

        // Add 3 achievements and 2 students with active demonstrations (GRADE_3 and GRADE_4 respectively)
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.postNewAchievement("Code2", "Name2", "GRADE_4", "ACHIEVEMENT", "http://example.com/Code2");
        testHelper.postNewAchievement("Code3", "Name3", "GRADE_5", "ACHIEVEMENT", "http://example.com/Code3");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");        
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code2", "James", "Smith", "jamesstudent");

        // Perform GET request for /report/partial with 2 students without a final grade
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/report/partial/", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        try {
            JSONArray jsonArray = new JSONArray(responseEntity.getBody());
            assertEquals(2, jsonArray.length());
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        // Make first student pass the GRADE_3 achievement, thus no longer in the list
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        Long userId = testHelper.getIdFromUserName("janestudent");
        Long demonstrationId = testHelper.getDemoIdFromAchievementAndUser(achievementId, userId);
        testHelper.postDemoClaim(demonstrationId);
        testHelper.postDemoResult(demonstrationId, achievementId, userId, Result.PASS);
        
        // Make second student pass the GRADE_4 achievement and still in the list
        Long achievementId2 = testHelper.getIdFromAchievementCode("Code2");
        Long userId2 = testHelper.getIdFromUserName("jamesstudent");
        Long demonstrationId2 = testHelper.getDemoIdFromAchievementAndUser(achievementId2, userId2);
        testHelper.postDemoClaim(demonstrationId2);
        testHelper.postDemoResult(demonstrationId2, achievementId2, userId2, Result.PASS);

        // Perform GET request for /report/partial with 1 student without a final grade but 1 passed achievement
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/report/partial/", null, true);
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
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.GET, "/report/partial/", null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testPartialHp() {
        // Perform GET request for /report/partial with no students
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/report/partial/hp", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());
        
        // Add 15 achievements of "GRADE_3" and "ACHIEVEMENT" type + 2 of "ASSIGNMENT" type to DB
        for (int i = 1; i <= 15; i++) {
            testHelper.postNewAchievement("Code" + i, "Name" + i, "GRADE_3", "ACHIEVEMENT", "http://example.com/Code" + i);
        }
        testHelper.postNewAchievement("Code16", "Name16", "GRADE_3", "ASSIGNMENT", "http://example.com/Code16");
        testHelper.postNewAchievement("Code17", "Name17", "GRADE_3", "ASSIGNMENT", "http://example.com/Code17");
        
        // Add 1 student with active demonstration with 1 passed achievement
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        Long userId = testHelper.getIdFromUserName("janestudent");
        Long demonstrationId = testHelper.getDemoIdFromAchievementAndUser(achievementId, userId);
        testHelper.postDemoClaim(demonstrationId);
        testHelper.postDemoResult(demonstrationId, achievementId, userId, Result.PASS);
        
        // Perform GET request for /report/partial with 1 student without any HP
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/report/partial/hp", null, true);
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
        responseEntity = testHelper.makeRequest(HttpMethod.POST, "/grade/group", gradingData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Perform GET request for /report/partial/hp with 1 student with enough achievements passed for HP
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/report/partial/hp", null, true);
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
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.GET, "/report/partial/hp", null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testPartialHpPDF() {
        // Perform GET request for /report/finished with no students
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/report/partial/hp/pdf", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().startsWith("%PDF"));
        
        // Add 15 achievements of "GRADE_3" and "ACHIEVEMENT" type + 2 of "ASSIGNMENT" type to DB
        for (int i = 1; i <= 15; i++) {
            testHelper.postNewAchievement("Code" + i, "Name" + i, "GRADE_3", "ACHIEVEMENT", "http://example.com/Code" + i);
        }
        testHelper.postNewAchievement("Code16", "Name16", "GRADE_3", "ASSIGNMENT", "http://example.com/Code16");
        testHelper.postNewAchievement("Code17", "Name17", "GRADE_3", "ASSIGNMENT", "http://example.com/Code17");
        
        // Add 1 student with active demonstration with 1 passed achievement
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        Long userId = testHelper.getIdFromUserName("janestudent");
        Long demonstrationId = testHelper.getDemoIdFromAchievementAndUser(achievementId, userId);
        testHelper.postDemoClaim(demonstrationId);
        testHelper.postDemoResult(demonstrationId, achievementId, userId, Result.PASS);

        // Make student pass the remaining 14+2 GRADE_3 achievements
        List<String> achievements = new ArrayList<>();
        for (int i = 2; i <= 17; i++) {
            achievements.add("Code" + i);
        }

        Json.GroupGradingCurl gradingData = Json.GroupGradingCurl.builder()
                .username("janestudent")
                .achievements(achievements)
                .build();
        responseEntity = testHelper.makeRequest(HttpMethod.POST, "/grade/group", gradingData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        // Perform GET request for /report/finished with 1 student and 3 achievements in DB (GRADE_3, GRADE_4, GRADE_5)
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/report/partial/hp/pdf", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().startsWith("%PDF"));
        
        // FIXME: Assert content of PDF file to contain Jane Doe and INLUPP1

        // Assert throws exception when current user is a student, hence not authorized
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.GET, "/report/partial/hp/pdf", null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testStats() {
        // Setup and get IDs for achievements, student and demonstration
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.postNewAchievement("Code2", "Name2", "GRADE_4", "ACHIEVEMENT", "http://example.com/Code2");
        testHelper.postNewAchievement("Code3", "Name3", "GRADE_5", "ACHIEVEMENT", "http://example.com/Code3");
        
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        Long userId = testHelper.getIdFromUserName("janestudent");
        Long demonstrationId = testHelper.getDemoIdFromAchievementAndUser(achievementId, userId);

        testHelper.updateToken("janestudent");
            // Perform GET request for /stats with no passed achievements
            ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/stats", null, true);
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

            try {
                JSONObject jsonObject = new JSONObject(responseEntity.getBody());
                assertEquals(1, jsonObject.getInt("currentCourseWeek"));
                assertEquals(20, jsonObject.getInt("weeksNeeded"));
                assertEquals(20.0, jsonObject.getDouble("remainingWeeks"), 0.01); // Delta for floating point error
                assertEquals(1, jsonObject.getInt("remaining"));
                assertEquals(0.0, jsonObject.getDouble("averageVelocity"), 0.01); // Delta for floating point error
                assertEquals(0, jsonObject.getInt("currentVelocity"));
                assertEquals(0.05, jsonObject.getDouble("targetVelocity"), 0.01); // Delta for floating point error
                assertEquals(3, jsonObject.getInt("currentTarget"));

                JSONObject burnDown = jsonObject.getJSONObject("burnDown");
                JSONArray grade3Array = burnDown.getJSONArray("GRADE_3");
                assertEquals(1, grade3Array.getInt(0));
                JSONArray grade4Array = burnDown.getJSONArray("GRADE_4");
                assertEquals(2, grade4Array.getInt(0));
                JSONArray grade5Array = burnDown.getJSONArray("GRADE_5");
                assertEquals(3, grade5Array.getInt(0));

                JSONObject achievementsPerLevel = jsonObject.getJSONObject("achievementsPerLevel");
                assertEquals(1, achievementsPerLevel.getInt("GRADE_3"));
                assertEquals(2, achievementsPerLevel.getInt("GRADE_4"));
                assertEquals(3, achievementsPerLevel.getInt("GRADE_5"));
            }
            catch (JSONException e) {
                fail("Failed to parse JSON object/array: " + e.getMessage());
            }
        
        testHelper.updateToken("johnteacher");
        // Make student pass the GRADE_3 achievement
        testHelper.postDemoClaim(demonstrationId);
        testHelper.postDemoResult(demonstrationId, achievementId, userId, Result.PASS);

        testHelper.updateToken("janestudent");
            // Perform GET request for /stats with 1 passed GRADE_3 achievement
            responseEntity = testHelper.makeRequest(HttpMethod.GET, "/stats", null, true);
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

            try {
                JSONObject jsonObject = new JSONObject(responseEntity.getBody());
                assertEquals(0, jsonObject.getInt("remaining"));

                JSONObject burnDown = jsonObject.getJSONObject("burnDown");
                JSONArray grade3Array = burnDown.getJSONArray("GRADE_3");
                assertEquals(0, grade3Array.getInt(0));
                JSONArray grade4Array = burnDown.getJSONArray("GRADE_4");
                assertEquals(1, grade4Array.getInt(0));
                JSONArray grade5Array = burnDown.getJSONArray("GRADE_5");
                assertEquals(2, grade5Array.getInt(0));
            }
            catch (JSONException e) {
                fail("Failed to parse JSON object/array: " + e.getMessage());
            }

        testHelper.updateToken("johnteacher");

        // Make student pass the GRADE_4 achievement
        Json.GroupGradingCurl gradingData = Json.GroupGradingCurl.builder()
                .username("janestudent")
                .achievements(List.of("Code2"))
                .build();
        responseEntity = testHelper.makeRequest(HttpMethod.POST, "/grade/group", gradingData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        testHelper.updateToken("janestudent");
            // Perform GET request for /stats with 2 passed achievements (GRADE_3 and GRADE_4)
            responseEntity = testHelper.makeRequest(HttpMethod.GET, "/stats", null, true);
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

            try {
                JSONObject jsonObject = new JSONObject(responseEntity.getBody());
                assertEquals(0, jsonObject.getInt("remaining"));

                JSONObject burnDown = jsonObject.getJSONObject("burnDown");
                JSONArray grade3Array = burnDown.getJSONArray("GRADE_3");
                assertEquals(0, grade3Array.getInt(0));
                JSONArray grade4Array = burnDown.getJSONArray("GRADE_4");
                assertEquals(0, grade4Array.getInt(0));
                JSONArray grade5Array = burnDown.getJSONArray("GRADE_5");
                assertEquals(1, grade5Array.getInt(0));
            }
            catch (JSONException e) {
                fail("Failed to parse JSON object/array: " + e.getMessage());
            }
   }

    @Test
    public void testStatsUser() {
        // Limited testing of this method since it's the same method as /stats but with user as parameter

        // Setup achievements and 1 student
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.postNewAchievement("Code2", "Name2", "GRADE_4", "ACHIEVEMENT", "http://example.com/Code2");
        testHelper.postNewAchievement("Code3", "Name3", "GRADE_5", "ACHIEVEMENT", "http://example.com/Code3");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long userId = testHelper.getIdFromUserName("janestudent");

        // Perform GET request for /stats/{userid} with non-existing user
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.GET, "/stats/1000", null, true);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        // Perform GET request for /stats/{userid} for existing user
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/stats/" + userId, null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        try {
            JSONObject jsonObject = new JSONObject(responseEntity.getBody());
            assertEquals(1, jsonObject.getInt("remaining"));
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        // Assert throws exception when current user is a student, hence not authorized
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.GET, "/stats/" + userId, null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testIsProfilePicVerified() {
        // Setup 2 students, one with a profile pic and one without
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long userIdJane = testHelper.getIdFromUserName("janestudent");

        testHelper.postNewUser("James", "Smith", "james.smith@uu.se", "jamesstudent", "STUDENT");
        Long userIdJames = testHelper.getIdFromUserName("jamesstudent");

        // Sleep for 3 seconds to allow processThumbnails() to run
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            fail("Failed to sleep: " + e.getMessage());
        }

        // Perform GET request for /user/profile-pic/verified with non-existing user
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.GET, "/user/profile-pic/1000", null, true);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        // Perform GET request for /user/profile-pic/{id} for existing user with no profile pic
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/user/profile-pic/" + userIdJames, null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
        
        // Perform GET request for /user/profile-pic/{id} for existing user with profile pic
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/user/profile-pic/" + userIdJane, null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        // FIXME: Fails in GitHub Actions (probably skips the sleep above), but works in Docker
        // assertNotNull(responseEntity.getBody());

        testHelper.updateToken("jamesstudent"); // Authenticate as student
        
            // Perform GET request for /user/profile-pic/{id} for own student user
            responseEntity = testHelper.makeRequest(HttpMethod.GET, "/user/profile-pic/" + userIdJames, null, true);
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            
            // Perform GET request for /user/profile-pic/{id} for teacher when current user is a student
            Long userIdTeacher = testHelper.getIdFromUserName("johnteacher");
            responseEntity = testHelper.makeRequest(HttpMethod.GET, "/user/profile-pic/" + userIdTeacher, null, true);
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            
            // FIXME: Fails in GitHub Actions (probably skips the sleep above), but works in Docker
            // Assert throws exception when requesting profile pic for another student (with profile pic) and current user is a student
            // HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            //     testHelper.makeRequest(HttpMethod.GET, "/user/profile-pic/" + userIdJane, null, true);
            // });
            // assertEquals(HttpStatus.UNAUTHORIZED, notAuthException.getStatusCode());
    }

    @Test
    public void testSetAndGetProfilePicVerified() {
        // Perform PUT request for /user/profile-pic/{id}/verified for non-existing user
        HttpClientErrorException putException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.PUT, "/user/profile-pic/1000/verified", null, true);
        });
        assertEquals(HttpStatus.BAD_REQUEST, putException.getStatusCode());

        // Perform GET request for /user/profile-pic/{id}/verified for non-existing user
        HttpClientErrorException getException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.GET, "/user/profile-pic/1000/verified", null, true);
        });
        assertEquals(HttpStatus.BAD_REQUEST, getException.getStatusCode());

        // Setup and get ID for student with no profile pic
        testHelper.postNewUser("Jane", "Doe", "jane.doe@uu.se", "janestudent", "STUDENT");
        Long userId = testHelper.getIdFromUserName("janestudent");

        // Perform PUT request for /user/profile-pic/{id}/verified for existing user with no profile pic
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.PUT, "/user/profile-pic/" + userId + "/verified", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Perform GET request for /user/profile-pic/{id}/verified for existing user with no profile pic
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/user/profile-pic/" + userId + "/verified", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertFalse(Boolean.parseBoolean(responseEntity.getBody()));

        testHelper.updateToken("janestudent"); // Authenticate as student
    
            testHelper.postProfilePic("profile_pic.jpg");
            
        testHelper.updateToken("johnteacher"); // Authenticate as teacher again

        // Perform PUT request for /user/profile-pic/{id}/verified for existing user with profile pic
        responseEntity = testHelper.makeRequest(HttpMethod.PUT, "/user/profile-pic/" + userId + "/verified", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Perform GET request for /user/profile-pic/{id}/verified for existing user with profile pic
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/user/profile-pic/" + userId + "/verified", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(Boolean.parseBoolean(responseEntity.getBody()));

        // Assert throws exception for PUT request when current user is a student, hence not authorized
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.PUT, "/user/profile-pic/" + userId + "/verified", null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testRevokeProfilePic() {
        // // Perform DELETE request for /user/revoke-profile-pic/{id} for non-existing user
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.DELETE, "/user/revoke-profile-pic/1000", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode()); 

        // Setup and get ID for student with no profile pic
        testHelper.postNewUser("James", "Smith", "james.smith@uu.se", "jamesstudent", "STUDENT");
        Long userIdJames = testHelper.getIdFromUserName("jamesstudent");

        // Perform DELETE request for /user/revoke-profile-pic/{id} for existing user with no profile pic
        responseEntity = testHelper.makeRequest(HttpMethod.DELETE, "/user/revoke-profile-pic/" + userIdJames, null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Setup and get ID for student with profile pic        
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long userIdJane = testHelper.getIdFromUserName("janestudent");

        // Perform GET request for /user/profile-pic/{id}/verified to check profile pic is verified
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/user/profile-pic/" + userIdJane + "/verified", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(Boolean.parseBoolean(responseEntity.getBody())); // Profile pic is verified

        // Perform DELETE request for /user/revoke-profile-pic/{id} for existing user with profile pic
        responseEntity = testHelper.makeRequest(HttpMethod.DELETE, "/user/revoke-profile-pic/" + userIdJane, null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Perform GET request for /user/profile-pic/{id}/verified to check profile pic is revoked
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/user/profile-pic/" + userIdJane + "/verified", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertFalse(Boolean.parseBoolean(responseEntity.getBody())); 

        // Assert throws exception for DELETE request when current user is a student, hence not authorized
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.DELETE, "/user/revoke-profile-pic/" + userIdJane, null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testUploadProfilePic() {
        // Setup and get ID for student
        testHelper.postNewUser("Jane", "Doe", "jane.doe@uu.se", "janestudent", "STUDENT");
        Long userId = testHelper.getIdFromUserName("janestudent");

        testHelper.updateToken("janestudent"); // Authenticate as student

            // Try to upload a profile pic with invalid file name, should fail
            HttpServerErrorException exception = assertThrows(InternalServerError.class, () -> {
                testHelper.postProfilePic("invalid_name");
            });
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());

            // Perform POST request for /user/upload-profile-pic with valid file
            testHelper.postProfilePic("profile_pic.jpg");            
        
        testHelper.updateToken("johnteacher"); // Authenticate as teacher again
        
        // Verify profile picture and assert status code
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.PUT, "/user/profile-pic/" + userId + "/verified", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        // Sleep for 3 seconds to allow processThumbnails() to run
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            fail("Failed to sleep: " + e.getMessage());
        }       
        
        testHelper.updateToken("janestudent"); // Authenticate as student again
            // Perform POST request for /user/approveThumbnail
            responseEntity = testHelper.makeRequest(HttpMethod.POST, "/user/approveThumbnail", null, true);
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

            // Try to upload a new profile pic, should fail
            HttpClientErrorException exception2 = assertThrows(HttpClientErrorException.class, () -> {
                testHelper.postProfilePic("profile_pic.jpg");
            });
            assertEquals(HttpStatus.BAD_REQUEST, exception2.getStatusCode());
    }

    @Test
    public void testWebhookGithubAccept() {
        Json.WebhookGithubAccept githubDataNull = Json.WebhookGithubAccept.builder()
                .action("test_action")
                .membership(null)
                .build();

        // Perform POST request for /webhook/github/accept with no membership-data
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.POST, "/webhook/github/accept", githubDataNull, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Setup and get ID for student
        testHelper.postNewUser("Jane", "Doe", "jane.doe@uu.se", "janestudent", "STUDENT");
        Long userId = testHelper.getIdFromUserName("janestudent");

        Json.GithubMembership membershipData = Json.GithubMembership.builder()
                .state("test_state")
                .role("test_role")
                .user(Json.GithubUser.builder().login("janestudentgithub").build())
                .build();
        Json.WebhookGithubAccept githubData = Json.WebhookGithubAccept.builder()
                .action("test_action")
                .membership(membershipData)
                .build();
        
        // Perform POST request for /webhook/github/accept with valid data but no githubhandle set
        responseEntity = testHelper.makeRequest(HttpMethod.POST, "/webhook/github/accept", githubData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Update user with githubhandle
        testHelper.putUserData(userId, "Jane", "Doe", "jane.doe@uu.se", "janestudent", "STUDENT", "janestudentgithub", "Room1", LocalDate.now().plusWeeks(2).toString(), "http://github.com/janestudentgithub");
        // FIXME: testHelper.putUserData (through put request to the UserController) can't update githubhandle if it's null in the first place
        
        // Perform POST request for /webhook/github/accept with valid data and githubhandle
        responseEntity = testHelper.makeRequest(HttpMethod.POST, "/webhook/github/accept", githubData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}