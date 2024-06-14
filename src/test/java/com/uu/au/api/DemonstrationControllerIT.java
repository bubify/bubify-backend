package com.uu.au.api;
import com.uu.au.models.Json;
import com.uu.au.enums.Result;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.test.annotation.DirtiesContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class DemonstrationControllerIT {
    
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
    public void testRequestDemonstration(){
        // Setup achievement and user data
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        Long userIdTeacher = testHelper.getIdFromUserName("johnteacher");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");

        // Define demonstration data with an empty list of achievements
        Json.DemonstrationRequest demonstrationDataEmpty = Json.DemonstrationRequest.builder()
                .achievementIds(List.of())
                .ids(List.of(userIdTeacher))
                .zoomPassword("password")
                .physicalRoom("Room1")
                .build();
        
        // Perform demo request with an empty list of achievements
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.POST, "/demonstration/request", demonstrationDataEmpty, true);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("A_DEMONSTRATION_REQUEST_MUST_CONTAIN_AT_LEAST_ONE_ACHIEVEMENT"));

        // Define demonstration data with an invalid achievement id
        Json.DemonstrationRequest demonstrationDataInvalid = Json.DemonstrationRequest.builder()
                .achievementIds(List.of(10000L))
                .ids(List.of(userIdTeacher))
                .zoomPassword("password")
                .physicalRoom("Room1")
                .build();
        
        // Perform demo request with an invalid achievement id
        HttpClientErrorException exception2 = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.POST, "/demonstration/request", demonstrationDataInvalid, true);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception2.getStatusCode());
        assertTrue(exception2.getMessage().contains("ACHIEVEMENT_NOT_FOUND"));

        // Perform demo request with an invalid user id
        HttpClientErrorException exception3 = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.postDemoRequest(achievementId, 10000L);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception3.getStatusCode());
        assertTrue(exception3.getMessage().contains("USER_NOT_FOUND"));

        // Perform demo request with valid data, verify that the demonstration is created and the status is "CLAIMED"
        testHelper.postDemoRequest(achievementId, userIdTeacher);
        Long demonstrationId = testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdTeacher);
        assertTrue(demonstrationId != 0);
        assertEquals("SUBMITTED", testHelper.getStatusFromDemonstration(demonstrationId));

        // Perform demo request when user already has an active demonstration
        HttpClientErrorException exception4 = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.postDemoRequest(achievementId, userIdTeacher);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception4.getStatusCode());
        assertTrue(exception4.getMessage().contains("USER_IN_MULTIPLE_HELP_REQUEST"));       
    }

    @Test
    public void testRequestDemonstrationByStudents(){
        // Setup achievement and user data
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.postNewAchievement("Code2", "Name2", "GRADE_4", "ACHIEVEMENT", "http://example.com/Code2");
        testHelper.postNewUser("Jane", "Doe", "jane.doe@uu.se", "janestudent", "STUDENT");
        testHelper.postNewUser("James", "Smith", "james.smith@uu.se", "jamesstudent", "STUDENT");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        Long achievementId2 = testHelper.getIdFromAchievementCode("Code2");
        Long userIdJane = testHelper.getIdFromUserName("janestudent");
        Long userIdJames = testHelper.getIdFromUserName("jamesstudent");

        testHelper.updateToken("janestudent");
            // Perform demo request with a student account, multiple achievements and submitters
            testHelper.postDemoRequestMultiple(List.of(achievementId, achievementId2), List.of(userIdJane, userIdJames));
        
        testHelper.updateToken("johnteacher");

        // Verify that the student demonstrations are in the database
        assertTrue(testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdJane) != 0);
        assertTrue(testHelper.getDemoIdFromAchievementAndUser(achievementId2, userIdJames) != 0);
    } 

    // @Test
    // public void testRequestDemonstrationFailedCodeExam(){
    //     // Setup achievement and user data
    //     testHelper.postNewAchievement("CodeExam", "Name1", "GRADE_3", "CODE_EXAM", "http://example.com/Code1");
    //     testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("CodeExam", "Jane", "Doe", "janestudent");
    //     Long achievementId = testHelper.getIdFromAchievementCode("CodeExam");
    //     Long userIdJane = testHelper.getIdFromUserName("janestudent");

    //     // Mark demonstration as done but Code Exam achievement is Failed
    //     Long demonstrationId = testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdJane);
    //     testHelper.postDemoClaim(demonstrationId);
    //     testHelper.postDemoResult(demonstrationId, achievementId, userIdJane, Result.FAIL);
        
    //     testHelper.updateToken("janestudent");
    //         // Perform demo request on a failed Code Exam achievement, should fail
    //         HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
    //             testHelper.postDemoRequest(achievementId, userIdJane);
    //         });
    //         assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    //         assertTrue(exception.getMessage().contains("STUDENT_NOT_ALLOWED_TO_PERFORM_THAT_ACTION"));

    //         // FIXME: Test doesn't work, probably because time has not passed so the demoblocker is same date as the current date
    // }

    @Test
    public void testClaimDemonstration(){
        // Setup achievement and user data
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "James", "Smith", "jamesstudent");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        Long userIdJane = testHelper.getIdFromUserName("janestudent");
        Long userIdJames = testHelper.getIdFromUserName("jamesstudent");
        Long demonstrationIdJane = testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdJane);
        Long demonstrationIdJames = testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdJames);

        // Claim demonstration with invalid demonstration id
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.postDemoClaim(10000L);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("DEMONSTRATION_NOT_FOUND"));

        // Claim demonstration with a teacher account, verify that the demonstration is claimed and the examiner is correct
        testHelper.postDemoClaim(demonstrationIdJane);
        assertEquals("johnteacher", testHelper.getExaminerFromDemonstration(demonstrationIdJane));
        assertEquals("CLAIMED", testHelper.getStatusFromDemonstration(demonstrationIdJane));

        // Claim demonstration with a student account, should fail
        testHelper.updateToken("janestudent");
            HttpClientErrorException exception2 = assertThrows(HttpClientErrorException.class, () -> {
                testHelper.postDemoClaim(demonstrationIdJames);
            });
            assertEquals(HttpStatus.FORBIDDEN, exception2.getStatusCode());
        
        // Claim multiple demonstrations with a teacher account, verify that the demonstration is claimed and the examiner is correct
        testHelper.updateToken("johnteacher");
        testHelper.postDemoClaim(demonstrationIdJames);
        assertEquals("johnteacher", testHelper.getExaminerFromDemonstration(demonstrationIdJames));
        assertEquals("CLAIMED", testHelper.getStatusFromDemonstration(demonstrationIdJames));
    }

    @Test
    public void testClaimDemonstrationAlreadyClaimed(){
        // Setup achievement and user data
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        Long userIdJane = testHelper.getIdFromUserName("janestudent");
        Long demonstrationIdJane = testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdJane);

        testHelper.postDemoClaim(demonstrationIdJane);
        assertEquals("johnteacher", testHelper.getExaminerFromDemonstration(demonstrationIdJane));
        assertEquals("CLAIMED", testHelper.getStatusFromDemonstration(demonstrationIdJane));
        
        // Try to claim a demonstration (with another teacher account) that is already claimed
        testHelper.postNewUser("Adam", "Brown", "adam.brown@uu.se", "adamteacher", "TEACHER");
        testHelper.updateToken("adamteacher");
                testHelper.postDemoClaim(demonstrationIdJane);

        // Verify that the demonstration is still claimed by the first teacher (hence not claimed by the second teacher)
        assertEquals("johnteacher", testHelper.getExaminerFromDemonstration(demonstrationIdJane));
    }
    
    @Test
    public void testClaimDemonstrationAlreadyCompleted(){
        // Setup achievement and user data
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        Long userIdJane = testHelper.getIdFromUserName("janestudent");
        Long demonstrationIdJane = testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdJane);

        // Claim a demonstration and mark it as done, verify that the demonstration is removed from the list of active demonstrations
        testHelper.postDemoClaim(demonstrationIdJane);
        testHelper.postDemoResult(demonstrationIdJane, achievementId, userIdJane, Result.PASS);
        assertTrue(testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdJane) == 0);
        
        // Try to claim a demonstration that is already completed, verify that the demo is still not in the list of active demonstrations
        testHelper.postDemoClaim(demonstrationIdJane);        
        assertTrue(testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdJane) == 0);
    }

    @Test
    public void testUnclaimDemonstration(){
        // Setup achievement and user data
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        Long userIdJane = testHelper.getIdFromUserName("janestudent");
        Long demonstrationIdJane = testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdJane);

        // Unclaim demonstration with invalid demonstration id
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.postDemoUnclaim(10000L);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("DEMONSTRATION_NOT_FOUND"));

        // Unclaim a demonstration that is submitted, verify that the demonstration is still submitted
        testHelper.postDemoUnclaim(demonstrationIdJane);
        assertEquals("SUBMITTED", testHelper.getStatusFromDemonstration(demonstrationIdJane));
        
        // Unclaim a demonstration that is claimed by the teacher, verify that the demonstration is submitted
        testHelper.postDemoClaim(demonstrationIdJane);
        assertEquals("CLAIMED", testHelper.getStatusFromDemonstration(demonstrationIdJane));
        testHelper.postDemoUnclaim(demonstrationIdJane);
        assertEquals("SUBMITTED", testHelper.getStatusFromDemonstration(demonstrationIdJane));
        
        // Unclaim a demonstration that is claimed by another teacher
        testHelper.postDemoClaim(demonstrationIdJane);

        testHelper.postNewUser("Adam", "Brown", "adam.brown@uu.se", "adamteacher", "TEACHER");
        testHelper.updateToken("adamteacher");
                testHelper.postDemoUnclaim(demonstrationIdJane);
                assertEquals("SUBMITTED", testHelper.getStatusFromDemonstration(demonstrationIdJane));

        // Assert throws exception when current user is a student, hence not authorized
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student
        
        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.postDemoUnclaim(demonstrationIdJane);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testUnclaimDemonstrationAlreadyCompleted(){
        // Setup achievement and user data
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        Long userIdJane = testHelper.getIdFromUserName("janestudent");
        Long demonstrationIdJane = testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdJane);
        testHelper.postDemoClaim(demonstrationIdJane);
        testHelper.postDemoResult(demonstrationIdJane, achievementId, userIdJane, Result.PASS);
        
        // Try to unclaim a demonstration that is already completed, should not be possible
        testHelper.postDemoUnclaim(demonstrationIdJane);
        assertNotEquals("SUBMITTED", testHelper.getStatusFromDemonstration(demonstrationIdJane));
    }

    @Test
    public void testDoneDemonstration(){
        // Setup achievement and user data
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        Long userIdJane = testHelper.getIdFromUserName("janestudent");
        Long demonstrationIdJane = testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdJane);
        testHelper.postDemoClaim(demonstrationIdJane);

        // Done demonstration with invalid demonstration id
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.postDemoResult(10000L, achievementId, userIdJane, Result.PASS);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("DEMONSTRATION_NOT_FOUND"));
        
        // Done demonstration with invalid achievement id
        HttpClientErrorException exception2 = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.postDemoResult(demonstrationIdJane, 10000L, userIdJane, Result.PASS);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception2.getStatusCode());
        assertTrue(exception.getMessage().contains("DEMONSTRATION_NOT_FOUND"));

        // Done a demonstration that is claimed by the teacher, verify that the demonstration is completed
        testHelper.postDemoResult(demonstrationIdJane, achievementId, userIdJane, Result.PASS);
        
        // Perform GET request for /explore/progress to verify student has passed the achievement
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/explore/progress", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Get JSON object from response body and assert user is in the list
        try {
            JSONObject jsonObject = new JSONObject(responseEntity.getBody());
            
            JSONArray achievements = jsonObject.getJSONArray("achievements");
            assertEquals(1, achievements.length());
            assertEquals("Code1", achievements.getJSONObject(0).getString("code"));
            
            JSONArray userProgress = jsonObject.getJSONArray("userProgress");
            assertEquals(1, userProgress.length());
            JSONObject userProgressFirst = userProgress.getJSONObject(0);
            assertEquals("janestudent", userProgressFirst.getJSONObject("user").getString("userName"));
            assertEquals("[\"Pass\"]", userProgressFirst.getString("progress"));
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }
        
        // Assert throws exception when current user is a student, hence not authorized
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "James", "Smith", "jamesstudent");
        Long userIdJames = testHelper.getIdFromUserName("jamesstudent");
        Long demonstrationIdJames = testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdJames);
        testHelper.postDemoClaim(demonstrationIdJames);

        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student
        
        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.postDemoResult(demonstrationIdJames, achievementId, userIdJane, Result.PASS);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testDoneDemonstrationClaimedByOtherTeacher(){
        // Setup achievement and user data
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        Long userIdJane = testHelper.getIdFromUserName("janestudent");
        Long demonstrationIdJane = testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdJane);
        testHelper.postDemoClaim(demonstrationIdJane);

        // Mark a demonstration claimed by another teacher as done, should fail
        testHelper.postNewUser("Adam", "Brown", "adam.brown@uu.se", "adamteacher", "TEACHER");
        testHelper.updateToken("adamteacher");

            HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
                testHelper.postDemoResult(demonstrationIdJane, achievementId, userIdJane, Result.PASS);
            });
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    public void testDoneDemonstrationMultiple(){
        // Setup achievement and user data
        testHelper.setupStudentWithValidPic("Jane", "Doe", "janestudent");
        testHelper.setupStudentWithValidPic("James", "Smith", "jamesstudent");
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.postNewAchievement("Code2", "Name2", "GRADE_4", "ACHIEVEMENT", "http://example.com/Code2");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        Long achievementId2 = testHelper.getIdFromAchievementCode("Code2");
        Long userIdJane = testHelper.getIdFromUserName("janestudent");
        Long userIdJames = testHelper.getIdFromUserName("jamesstudent");
        
        testHelper.updateToken("janestudent");
            // Perform demo request with a student account, multiple achievements and submitters
            testHelper.postDemoRequestMultiple(List.of(achievementId, achievementId2), List.of(userIdJane, userIdJames));
        
        testHelper.updateToken("johnteacher");

        // Done multiple demonstrations with a teacher account, verify that the demonstrations are completed
        Long demonstrationId = testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdJane);
        testHelper.postDemoClaim(demonstrationId);

        Json.AchievementId_UserId_Result resultJane = Json.AchievementId_UserId_Result.builder()
                .achievementId(achievementId)
                .id(userIdJane)
                .result(Result.PASS)
                .build();
        Json.AchievementId_UserId_Result resultJane2 = Json.AchievementId_UserId_Result.builder()
                .achievementId(achievementId2)
                .id(userIdJane)
                .result(Result.FAIL)
                .build();
        Json.AchievementId_UserId_Result resultJames = Json.AchievementId_UserId_Result.builder()
                .achievementId(achievementId)
                .id(userIdJames)
                .result(Result.PUSHBACK)
                .build();
        Json.AchievementId_UserId_Result resultJames2 = Json.AchievementId_UserId_Result.builder()
                .achievementId(achievementId2)
                .id(userIdJames)
                .result(Result.PASS)
                .build();
        
        testHelper.postDemoResultsMultiple(demonstrationId, List.of(resultJane, resultJane2, resultJames, resultJames2));

        // Perform GET request for /explore/progress to verify students have the correct results
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/explore/progress", null, true);
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
            assertEquals("[\"Pass\",\"Fail\"]", userProgressFirst.getString("progress"));
            JSONObject userProgressSecond = userProgress.getJSONObject(1);
            assertEquals("jamesstudent", userProgressSecond.getJSONObject("user").getString("userName"));
            assertEquals("[\"Pushback\",\"Pass\"]", userProgressSecond.getString("progress"));
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }
    }

    @Test
    public void testDoneDemonstrationNoProfilePic(){
        // Setup achievement and user data
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.postNewUser("Jane", "Doe", "jane.doe@uu.se", "janestudent", "STUDENT");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        Long userIdJane = testHelper.getIdFromUserName("janestudent");

        testHelper.updateToken("janestudent");
            testHelper.postDemoRequest(achievementId, userIdJane);

        testHelper.updateToken("johnteacher");        
        Long demonstrationIdJane = testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdJane);
        testHelper.postDemoClaim(demonstrationIdJane);
        
        // Try to mark a demonstration as done when the user has no profile picture, should fail
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.postDemoResult(demonstrationIdJane, achievementId, userIdJane, Result.PASS);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("GRADING_REQUIRES_VERIFIED_PROFILE_PIC"));
    }
    
    @Test
    public void testDoneDemonstrationUnlockPushedBackAchievement(){
        // Setup achievement and user data
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        Long userIdJane = testHelper.getIdFromUserName("janestudent");
        Long demonstrationIdJane = testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdJane);
        testHelper.postDemoClaim(demonstrationIdJane);
        testHelper.postDemoResult(demonstrationIdJane, achievementId, userIdJane, Result.PUSHBACK);

        // Make another demonstration request for the same achievement
        testHelper.updateToken("janestudent");
            testHelper.postDemoRequest(achievementId, userIdJane);
        
        testHelper.updateToken("johnteacher");
        Long demonstrationIdJane2 = testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdJane);
        testHelper.postDemoClaim(demonstrationIdJane2);

        // Try to mark a demonstration as done when the achievement is currently on push back, should fail
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.postDemoResult(demonstrationIdJane2, achievementId, userIdJane, Result.PASS);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("CANNOT_UNLOCK_ACHIEVEMENT_CURRENTLY_ON_PUSH_BACK"));
    }

    @Test
    public void testActiveAndSubmittedOrPickedUp() {
        // Perform GET request for /demonstrations/activeAndSubmittedOrPickedUp with no active demonstrations
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/demonstrations/activeAndSubmittedOrPickedUp", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());

        // Setup achievement and user data
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        Long userIdJane = testHelper.getIdFromUserName("janestudent");
        Long demonstrationIdJane = testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdJane);
        testHelper.postDemoClaim(demonstrationIdJane);

        // Perform GET request for /demonstrations/activeAndSubmittedOrPickedUp to verify that the demonstration is active
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/demonstrations/activeAndSubmittedOrPickedUp", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Get JSON object from response body and assert the demonstration is active
        try {
            JSONArray jsonArray = new JSONArray(responseEntity.getBody());
            assertEquals(1, jsonArray.length());

            JSONObject jsonObject = jsonArray.getJSONObject(0);
            assertEquals("janestudent", jsonObject.getJSONArray("submitters").getJSONObject(0).getString("userName"));
            assertEquals("Code1", jsonObject.getJSONArray("achievements").getJSONObject(0).getString("code"));
            assertEquals("CLAIMED", jsonObject.getString("status"));
            assertEquals("johnteacher", jsonObject.getJSONObject("examiner").getString("userName"));
            assertTrue(jsonObject.isNull("zoomRoom"));
            assertEquals("Room1", jsonObject.getString("physicalRoom"));
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        // Add 2 more students with active demonstrations of 2 different achievements
        testHelper.setupStudentWithValidPic("James", "Smith", "jamesstudent");
        testHelper.setupStudentWithValidPic("Adam", "Brown", "adamstudent");
        testHelper.postNewAchievement("Code2", "Name2", "GRADE_4", "ACHIEVEMENT", "http://example.com/Code2");
        Long achievementId2 = testHelper.getIdFromAchievementCode("Code2");
        Long userIdJames = testHelper.getIdFromUserName("jamesstudent");
        Long userIdAdam = testHelper.getIdFromUserName("adamstudent");
        testHelper.updateToken("jamesstudent");
            testHelper.postDemoRequestMultiple(List.of(achievementId, achievementId2), List.of(userIdJames, userIdAdam));
        
        testHelper.updateToken("johnteacher");

        // Perform GET request for /demonstrations/activeAndSubmittedOrPickedUp to verify that the demonstrations are active
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/demonstrations/activeAndSubmittedOrPickedUp", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Get JSON object from response body and assert 2 demonstrations are active

        try {
            JSONArray jsonArray = new JSONArray(responseEntity.getBody());
            assertEquals(2, jsonArray.length());

            JSONObject jsonObject2 = jsonArray.getJSONObject(1);
            assertEquals("jamesstudent", jsonObject2.getJSONArray("submitters").getJSONObject(0).getString("userName"));
            assertEquals("adamstudent", jsonObject2.getJSONArray("submitters").getJSONObject(1).getString("userName"));
            assertEquals("Code1", jsonObject2.getJSONArray("achievements").getJSONObject(0).getString("code"));
            assertEquals("Code2", jsonObject2.getJSONArray("achievements").getJSONObject(1).getString("code"));
            assertEquals("SUBMITTED", jsonObject2.getString("status"));
            assertTrue(jsonObject2.isNull("examiner"));
            assertTrue(jsonObject2.isNull("zoomRoom"));
            assertEquals("Room1", jsonObject2.getString("physicalRoom"));
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }
    }

    @Test
    public void testMatchMaking(){
        // Perform GET request for /demonstration/matchMaking with students or achievements in DB
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/demonstration/matchMaking", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());

        // Setup 2 students and 1 achievements in DB
        testHelper.setupStudentWithValidPic("Jane", "Doe", "janestudent");
        testHelper.setupStudentWithValidPic("James", "Smith", "jamesstudent");
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        Long userIdJane = testHelper.getIdFromUserName("janestudent");
        Long userIdJames = testHelper.getIdFromUserName("jamesstudent");

        // Perform GET request for /demonstration/matchMaking to verify that the 2 students are matched with the achievement
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/demonstration/matchMaking", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        try {
            JSONArray jsonArray = new JSONArray(responseEntity.getBody());
            assertEquals(2, jsonArray.length());

            JSONObject jsonObject = jsonArray.getJSONObject(0);
            Long firstUserId = jsonObject.getJSONObject("first").getLong("id");
            JSONObject jsonObject2 = jsonArray.getJSONObject(1);
            Long secondUserId = jsonObject2.getJSONObject("first").getLong("id");
            assertTrue((firstUserId == userIdJane && secondUserId == userIdJames) || (firstUserId == userIdJames && secondUserId == userIdJane));

            JSONArray jsonArrayAchievements = jsonObject.getJSONArray("second");
            assertEquals(1, jsonArrayAchievements.length());
            assertEquals("Code1", jsonArrayAchievements.getJSONObject(0).getString("code"));
            JSONArray jsonArrayAchievements2 = jsonObject.getJSONArray("second");
            assertEquals(1, jsonArrayAchievements2.length());
            assertEquals("Code1", jsonArrayAchievements2.getJSONObject(0).getString("code"));
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        // Perform POST request for /grade/group_users to grade Jane on Code1 achievement
        testHelper.postGradeGroupUsers(List.of(userIdJane), List.of("Code1"));

        // Perform GET request for /demonstration/matchMaking to verify that only James is in the list
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/demonstration/matchMaking", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        try {
            JSONArray jsonArray = new JSONArray(responseEntity.getBody());
            assertEquals(1, jsonArray.length());

            JSONObject jsonObject = jsonArray.getJSONObject(0);
            assertEquals(userIdJames, jsonObject.getJSONObject("first").getLong("id"));
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        // Setup another student and achievement in DB
        testHelper.setupStudentWithValidPic("Adam", "Brown", "adamstudent");
        testHelper.postNewAchievement("Code2", "Name2", "GRADE_4", "ACHIEVEMENT", "http://example.com/Code2");
        Long userIdAdam = testHelper.getIdFromUserName("adamstudent");

        // Perform POST request for /grade/group_users to grade Jane and James on Code2 achievement
        testHelper.postGradeGroupUsers(List.of(userIdJane, userIdJames), List.of("Code2"));

        // Perform GET request for /demonstration/matchMaking to verify that only Adam and James are in the list with 2 vs 1 achievement(s)
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/demonstration/matchMaking", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        try {
            JSONArray jsonArray = new JSONArray(responseEntity.getBody());
            assertEquals(2, jsonArray.length());

            JSONObject jsonObject = jsonArray.getJSONObject(0);
            Long firstUserId = jsonObject.getJSONObject("first").getLong("id");
            JSONObject jsonObject2 = jsonArray.getJSONObject(1);
            Long secondUserId = jsonObject2.getJSONObject("first").getLong("id");
            assertTrue((firstUserId == userIdAdam && secondUserId == userIdJames) || (firstUserId == userIdJames && secondUserId == userIdAdam));

            int aLength = jsonObject.getJSONArray("second").length();
            int aLength2 = jsonObject2.getJSONArray("second").length();
            assertTrue(aLength == 1 && aLength2 == 2 || aLength == 2 && aLength2 == 1);
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        // Assert throws exception when current user is a student, hence not authorized
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student
        
        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.GET, "/demonstration/matchMaking", null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }
    
    @Test
    public void testClearAllActiveDemonstrationsUser(){
        // Perform GET request for /demonstration/clearList to clear an empty list of active demos
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/demonstration/clearList", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(testHelper.getIdsOfActiveDemonstrations().length == 0);

        // Add 1 student with an active demonstration, verify that the list of active demos has 1 element
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        assertTrue(testHelper.getIdsOfActiveDemonstrations().length == 1);
        
        // Perform GET request for /demonstration/clearList to clear the list of active demonstrations
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/demonstration/clearList", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(testHelper.getIdsOfActiveDemonstrations().length == 0);
        
        // Add 2 students with active demonstrations, verify that the list of active demos has 2 elements
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Adam", "Brown", "adamstudent");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "James", "Smith", "jamesstudent");
        assertTrue(testHelper.getIdsOfActiveDemonstrations().length == 2);

        // Perform GET request for /demonstration/clearList to clear the list of active demonstrations
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/demonstration/clearList", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(testHelper.getIdsOfActiveDemonstrations().length == 0);

        // Assert throws exception when current user is a student, hence not authorized
        testHelper.postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        testHelper.updateToken("somestudent"); // Authenticate as student

        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.GET, "/demonstration/clearList", null, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }
    
    @Test
    public void testCancelDemonstration(){
        // Cancel demonstration with invalid demonstration id
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            testHelper.makeRequest(HttpMethod.GET, "/demonstration/cancel/1000L", null, true);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        // Setup achievement and 3 users with active demonstrations
        testHelper.postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        Long achievementId = testHelper.getIdFromAchievementCode("Code1");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "James", "Smith", "jamesstudent");
        testHelper.setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Adam", "Brown", "adamstudent");
        Long userIdJane = testHelper.getIdFromUserName("janestudent");
        Long userIdJames = testHelper.getIdFromUserName("jamesstudent");
        Long userIdAdam = testHelper.getIdFromUserName("adamstudent");
        Long demonstrationIdJane = testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdJane);
        Long demonstrationIdJames = testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdJames);
        Long demonstrationIdAdam = testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdAdam);

        // Teacher cancels a demonstration that a student submitted, verify that it is removed from the list of active demonstrations
        ResponseEntity<String> responseEntity = testHelper.makeRequest(HttpMethod.GET, "/demonstration/cancel/" + demonstrationIdJane, null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdJane) == 0);
        
        // Student cancels a claimed demonstration that it has submitted, verify that it is removed from the list of active demonstrations
        testHelper.postDemoClaim(demonstrationIdJames);
        testHelper.updateToken("jamesstudent");
            responseEntity = testHelper.makeRequest(HttpMethod.GET, "/demonstration/cancel/" + demonstrationIdJames, null, true);
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        testHelper.updateToken("johnteacher");
        assertTrue(testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdJames) == 0);
        
        // Student tries to cancel a demonstration from another student, should fail, verify that the demonstration is still active
        testHelper.updateToken("jamesstudent");
            HttpClientErrorException exception2 = assertThrows(HttpClientErrorException.class, () -> {
                testHelper.makeRequest(HttpMethod.GET, "/demonstration/cancel/" + demonstrationIdAdam, null, true);
            });
            assertEquals(HttpStatus.BAD_REQUEST, exception2.getStatusCode());
        testHelper.updateToken("johnteacher");
        assertTrue(testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdAdam) != 0);

        // Teacher cancels an already cancelled demonstration
        responseEntity = testHelper.makeRequest(HttpMethod.GET, "/demonstration/cancel/" + demonstrationIdJames, null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(testHelper.getDemoIdFromAchievementAndUser(achievementId, userIdJames) == 0);
    }
}