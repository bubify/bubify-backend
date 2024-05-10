package com.uu.au.api;

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
        // Generic method to make GET/PUT/POST/DELETE request to endpoint with data (may be null) and token (if needed)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (useToken) { headers.set("token", token); }

        HttpEntity<String> requestEntity = new HttpEntity<>(data, headers);
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

    private void postClaimDemo(int demoId) {
        // Helper method to define and POST demonstration claim data, assert status code
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/demonstration/claim", Integer.toString(demoId), true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    private void postDemoResult(int demoId, int achievementId, int userId, String result) {
        // Helper method to define and POST demonstration result data, assert status code
        String demoResultData = "{\"demoId\":" + demoId + ",\"results\":[{\"achievementId\":" + achievementId + ",\"id\":" + userId + ",\"result\":\"" + result + "\"}]}";
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/demonstration/done", demoResultData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    private void postOfferHelp(int helpRequestId) {
        // Helper method to define and POST help offer data, assert status code
        String helpOfferData = "{\"helpRequestId\":" + helpRequestId + "}";
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/offerHelp", helpOfferData, true);
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

    private int getIdFromUserName(String userName) {
        // Helper method to get user ID from username by GET request to /admin/user?username={username}
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/admin/user?username=" + userName, null, true);
        String responseBody = responseEntity.getBody();
        assertNotNull(responseBody);

        int userId = 0;
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            userId = jsonObject.getInt("id");
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }
        return userId;
    }

    private int getIdFromAchievementCode(String code) {
        // Helper method to get achievement ID from code by GET request to /achievement/code-to-id/{code}
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/achievement/code-to-id/" + code, null, true);
        String responseBody = responseEntity.getBody();
        assertNotNull(responseBody);

        int achievementId = 0;
        try {
            achievementId = Integer.parseInt(responseBody);
        }
        catch (NumberFormatException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }
        return achievementId;
    }

    private int[] getIdsOfActiveDemonstrations() {
        // Helper method to get IDs of active demonstrations by GET request to /demonstrations/activeAndSubmittedOrPickedUp
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/demonstrations/activeAndSubmittedOrPickedUp", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        try {
            JSONArray jsonArray = new JSONArray(responseEntity.getBody());
            int[] demonstrationIds = new int[jsonArray.length()];
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                demonstrationIds[i] = jsonObject.getInt("id");
            }
            return demonstrationIds;
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        return new int[0];
    }

    private int getDemoIdFromAchievementAndUser(int achievementId, int userId) {
        // Helper method to get demonstration ID from achievement and user ID by GET request to /demonstrations/activeAndSubmittedOrPickedUp
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/demonstrations/activeAndSubmittedOrPickedUp", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        try {
            JSONArray jsonArray = new JSONArray(responseEntity.getBody());
            
            for (int i = 0; i < jsonArray.length(); i++) {
                // Loop through active demonstrations, achievements and submitters
                JSONObject demonstration = jsonArray.getJSONObject(i);                
                JSONArray achievements = demonstration.getJSONArray("achievements");
                
                for (int j = 0; j < achievements.length(); j++) {

                    if (achievements.getJSONObject(j).getInt("id") == achievementId) {
                        JSONArray submitters = demonstration.getJSONArray("submitters");
                        
                        for (int k = 0; k < submitters.length(); k++) {
                            if (submitters.getJSONObject(k).getInt("id") == userId) {
                                return demonstration.getInt("id");
                            }
                        }
                    }
                }
            }
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        return 0; // No demonstration found
    }

    private int getHelpRequestIdFromUser(int userId) {
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
                            return helpRequest.getInt("id");
                        }
                    }
            }
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        return 0; // No help request found
    }

    private void setupStudentWithValidPicAndActiveDemoAndHelpRequest(String code, String firstName, String lastName, String userName) {
        // Helper method to setup a student with a valid profile picture, active demonstration and help request

        // Create student, get IDs for achievement and student
        postNewUser(firstName, lastName, firstName.toLowerCase() + "." + lastName.toLowerCase() + "@uu.se", userName, "STUDENT");
        int userId = getIdFromUserName(userName);
        int achievementId = getIdFromAchievementCode(code);

        updateToken(userName); // Authenticate as student
    
            postProfilePic();
            
            // Define and POST a demonstration request, assert status code
            String demonstrationData = "{\"achievementIds\":[" + achievementId + "],\"ids\":[" + userId +"],\"zoomPassword\":\"password\",\"physicalRoom\":\"Room1\"}";
            ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/demonstration/request", demonstrationData, true);
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

            // Define and POST a help request, assert status code
            String helpRequestData = "{\"ids\":[" + userId +"],\"message\":\"Help me!\",\"zoomPassword\":\"password\",\"physicalRoom\":\"Room1\"}";
            responseEntity = makeRequest(HttpMethod.POST, "/askForHelp", helpRequestData, true);
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            
        updateToken("johnteacher"); // Authenticate as teacher again
                
        // Verify profile picture and assert status code
        responseEntity = makeRequest(HttpMethod.PUT, "/user/profile-pic/" + userId + "/verified", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @BeforeEach
    public void setup() {
        // Define user and course data
        String courseData = "{\"name\":\"Fun Course\"}";
        String userData = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"j.d@uu.se\",\"userName\":\"johnteacher\",\"role\":\"TEACHER\"}";

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
        int achievementId = getIdFromAchievementCode("Code1");
        int userId = getIdFromUserName("janestudent");
        int demonstrationId = getDemoIdFromAchievementAndUser(achievementId, userId);
        
        postDemoResult(demonstrationId, achievementId, userId, "Pass");

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
        int achievementId = getIdFromAchievementCode("CodeExam1");
        int userId = getIdFromUserName("janestudent");
        int demonstrationId = getDemoIdFromAchievementAndUser(achievementId, userId);
        
        postDemoResult(demonstrationId, achievementId, userId, "Fail");
        
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

        // Assert throws exception when current user is a student, hence not authorized
        postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        updateToken("somestudent"); // Authenticate as student
        
        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            makeRequest(HttpMethod.POST, "/course", requestBody, true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
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

        // Assert throws exception when current user is a student, hence not authorized
        postNewUser("Some", "One", "some.one@uu.se", "somestudent", "STUDENT");
        updateToken("somestudent"); // Authenticate as student
        
        HttpClientErrorException notAuthException = assertThrows(HttpClientErrorException.class, () -> {
            makeRequest(HttpMethod.PUT, "/course", requestBody, true);
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
        int achievementId = getIdFromAchievementCode("Code1");
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
        int achievementId = getIdFromAchievementCode("Code1");
        int userId = getIdFromUserName("janestudent");
        int demonstrationId = getDemoIdFromAchievementAndUser(achievementId, userId);

        postDemoResult(demonstrationId, achievementId, userId, "Pass");
        
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
        int achievementId = getIdFromAchievementCode("Code1");
        int userId = getIdFromUserName("janestudent");
        int demonstrationId = getDemoIdFromAchievementAndUser(achievementId, userId);

        postDemoResult(demonstrationId, achievementId, userId, "Pushback");
        
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
        int achievementId = getIdFromAchievementCode("Code1");
        int userId = getIdFromUserName("janestudent");
        int demonstrationId = getDemoIdFromAchievementAndUser(achievementId, userId);

        postDemoResult(demonstrationId, achievementId, userId, "Pass");

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
        int userId = getIdFromUserName("janestudent");
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
        String requestBody = "{\"username\":\"janestudent\",\"achievements\":[\"Code1\"]}";
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/grade/group", requestBody, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Something went wrong in group grading", responseEntity.getBody());

        // Setup and get IDs for achievement, student and demonstration
        postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        
        // Perform POST request for /grade/group with 1 student and 1 achievement in DB but with request for non-existent achievement
        requestBody = "{\"username\":\"janestudent\",\"achievements\":[\"Code2\"]}";
        responseEntity = makeRequest(HttpMethod.POST, "/grade/group", requestBody, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("User or achievement not found", responseEntity.getBody());

        // Perform POST request for /grade/group with 1 student and 1 achievement in DB
        requestBody = "{\"username\":\"janestudent\",\"achievements\":[\"Code1\"]}";
        responseEntity = makeRequest(HttpMethod.POST, "/grade/group", requestBody, true);
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
            makeRequest(HttpMethod.POST, "/grade/group", "{\"username\":\"janestudent\",\"achievements\":[\"Code1\"]}", true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    
    }
    @Test
    public void testGradeGroupUsers() {
        // Perform POST request for /grade/group_users with neither student nor achievement in DB
        String requestBody = "{\"userIds\":[1000],\"achievements\":[\"Code1\"]}";
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/grade/group_users", requestBody, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Something went wrong in group grading", responseEntity.getBody());

        // Setup and get IDs for achievement, student and demonstration
        postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        int userId = getIdFromUserName("janestudent");
        
        // Perform POST request for /grade/group_users with 1 student and 1 achievement in DB but with request for non-existent achievement
        requestBody = "{\"userIds\":[" + userId + "],\"achievements\":[\"Code2\"]}";
        responseEntity = makeRequest(HttpMethod.POST, "/grade/group_users", requestBody, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("User or achievement not found", responseEntity.getBody());

        // Perform POST request for /grade/group_users with 1 student and 1 achievement in DB
        requestBody = "{\"userIds\":[" + userId + "],\"achievements\":[\"Code1\"]}";
        responseEntity = makeRequest(HttpMethod.POST, "/grade/group_users", requestBody, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("SUCCESS", responseEntity.getBody());

        // Another setup and get IDs for achievement, student and demonstration
        postNewAchievement("Code2", "Name2", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code2");
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "James", "Smith", "jamesstudent");
        int userId2 = getIdFromUserName("jamesstudent");

        // Perform POST request for /grade/group_users with 2 students and 2 achievements in DB
        requestBody = "{\"userIds\":[" + userId + "," + userId2 + "],\"achievements\":[\"Code1\",\"Code2\"]}";
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
            makeRequest(HttpMethod.POST, "/grade/group_users", "{\"userIds\":[1000],\"achievements\":[\"Code1\"]}", true);
        });
        assertEquals(HttpStatus.FORBIDDEN, notAuthException.getStatusCode());
    }

    @Test
    public void testRecentDemo() {
        // Setup and get IDs for achievement, student and demonstration
        postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        int achievementId = getIdFromAchievementCode("Code1");
        int userId = getIdFromUserName("janestudent");
        int demonstrationId = getDemoIdFromAchievementAndUser(achievementId, userId);

        // Perform GET request for /recent/demo with 1 active demonstration but not claimed by current user (johnteacher)
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/recent/demo", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());

        // Report demonstration as completed
        postClaimDemo(demonstrationId);

        // Perform GET request for /recent/demo with 1 active demonstration claimed by current user (johnteacher)
        responseEntity = makeRequest(HttpMethod.GET, "/recent/demo", null, true);
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

        // Add 5 more students with active and claimed demonstrations
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "James", "Smith", "jamesstudent");
        int userId2 = getIdFromUserName("jamesstudent");
        int demonstrationId2 = getDemoIdFromAchievementAndUser(achievementId, userId2);
        postClaimDemo(demonstrationId2);

        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Kieran", "Smith", "kieranstudent");
        int userId3 = getIdFromUserName("kieranstudent");
        int demonstrationId3 = getDemoIdFromAchievementAndUser(achievementId, userId3);
        postClaimDemo(demonstrationId3);

        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Liam", "Smith", "liamstudent");
        int userId4 = getIdFromUserName("liamstudent");
        int demonstrationId4 = getDemoIdFromAchievementAndUser(achievementId, userId4);
        postClaimDemo(demonstrationId4);

        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Mason", "Smith", "masonstudent");
        int userId5 = getIdFromUserName("masonstudent");
        int demonstrationId5 = getDemoIdFromAchievementAndUser(achievementId, userId5);
        postClaimDemo(demonstrationId5);

        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Noah", "Smith", "noahstudent");
        int userId6 = getIdFromUserName("noahstudent");
        int demonstrationId6 = getDemoIdFromAchievementAndUser(achievementId, userId6);
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
        // Setup and get IDs for achievement, student and demonstration
        postNewAchievement("Code1", "Name1", "GRADE_3", "ACHIEVEMENT", "http://example.com/Code1");
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Jane", "Doe", "janestudent");
        int userId = getIdFromUserName("janestudent");
        int helpRequestId = getHelpRequestIdFromUser(userId);

        // Perform GET request for /recent/help with 1 active help request but not claimed by current user (johnteacher)
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/recent/help", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());

        // Report help request as completed
        postOfferHelp(helpRequestId);

        // Perform GET request for /recent/help with 1 active help request claimed by current user (johnteacher)
        responseEntity = makeRequest(HttpMethod.GET, "/recent/help", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Get JSON object from response body and assert user is in the list
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
        int userId2 = getIdFromUserName("jamesstudent");
        int helpRequestId2 = getHelpRequestIdFromUser(userId2);
        postOfferHelp(helpRequestId2);

        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Kieran", "Smith", "kieranstudent");
        int userId3 = getIdFromUserName("kieranstudent");
        int helpRequestId3 = getHelpRequestIdFromUser(userId3);
        postOfferHelp(helpRequestId3);
        
        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Liam", "Smith", "liamstudent");
        int userId4 = getIdFromUserName("liamstudent");
        int helpRequestId4 = getHelpRequestIdFromUser(userId4);
        postOfferHelp(helpRequestId4);

        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Mason", "Smith", "masonstudent");
        int userId5 = getIdFromUserName("masonstudent");
        int helpRequestId5 = getHelpRequestIdFromUser(userId5);
        postOfferHelp(helpRequestId5);

        setupStudentWithValidPicAndActiveDemoAndHelpRequest("Code1", "Noah", "Smith", "noahstudent");
        int userId6 = getIdFromUserName("noahstudent");
        int helpRequestId6 = getHelpRequestIdFromUser(userId6);
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
}