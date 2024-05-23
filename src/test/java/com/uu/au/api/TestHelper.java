package com.uu.au.api;
import com.uu.au.models.Json;
import com.uu.au.enums.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestHelper {
    private static String token;

    @Autowired
    private TestRestTemplate restTemplate;

    public <T> ResponseEntity<String> makeRequest(HttpMethod method, String endpoint, T data, Boolean useToken) {
        // Generic method to make GET/PUT/POST/DELETE request to endpoint with data (may be null) and token (if needed)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (useToken) { headers.set("token", token); }

        HttpEntity<T> requestEntity = new HttpEntity<>(data, headers);
        RestTemplate restTemplate = new RestTemplate();

        String url = "http://localhost:8900" + endpoint;
        return restTemplate.exchange(url, method, requestEntity, String.class);
    }

    public void updateToken(String user) {
        // Authenticate as user and update the token
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/su?username=" + user, null, false);
        token = responseEntity.getBody();
        assertNotNull(token);
    }

    public void postNewUser(String first, String last, String email, String username, String role) {
        // Helper method to define and POST user data, assert status code
        String userData = first + ";" + last + ";" + email + ";" + username + ";" + role;
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/admin/add-user", userData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    public void postNewAchievement(String code, String name, String grade, String type, String url) {
        // Helper method to define and POST achievement data, assert status code
        String achievementData = code + ";" + name + ";" + grade + ";" + type + ";" + url;
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/admin/add-achievement", achievementData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    public void postDemoRequest(Long achievementId, Long userId) {
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
    
    public void postDemoRequestMultiple(List<Long> achievementIds, List<Long> userIds) {
        // Helper method to define and POST demonstration request with multiple achievements/submitters, assert status code
        Json.DemonstrationRequest demonstrationData = Json.DemonstrationRequest.builder()
                .achievementIds(achievementIds)
                .ids(userIds)
                .zoomPassword("password")
                .physicalRoom("Room1")
                .build();
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/demonstration/request", demonstrationData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
    
    public void postDemoClaim(Long demonstrationId) {
        // Helper method to define and POST demonstration claim data, assert status code
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/demonstration/claim", Long.toString(demonstrationId), true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
    
    public void postDemoUnclaim(Long demonstrationId) {
        // Helper method to define and POST demonstration unclaim data, assert status code
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/demonstration/unclaim", Long.toString(demonstrationId), true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    public void postDemoResult(Long demonstrationId, Long achievementId, Long userId, Result result) {
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
    
    public void postDemoResultsMultiple(Long demonstrationId, List<Json.AchievementId_UserId_Result> resultDataList) {
        // Helper method to define and POST multiple demonstration results data, assert status code
        Json.DemoResult demoResultData = Json.DemoResult.builder()
                .demoId(demonstrationId)
                .results(resultDataList)
                .build();
        
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/demonstration/done", demoResultData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    public void postHelpRequest(Long userId) {
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

    public void postOfferHelp(Long helpRequestId) {
        // Helper method to define and POST help offer data, assert status code
        Json.HelpRequestId helpRequestData = Json.HelpRequestId.builder()
                .helpRequestId(helpRequestId)
                .build();
    
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/offerHelp", helpRequestData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
    
    public void postHelpDone(Long helpRequestId) {
        // Helper method to define and POST help done data, assert status code
        Json.HelpRequestId helpRequestData = Json.HelpRequestId.builder()
                .helpRequestId(helpRequestId)
                .build();
        
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/markAsDone", helpRequestData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
    
    public void postGradeGroupUsers(List<Long> userIds, List<String> achievementCodes) {
        // Helper method to define and POST group grading data, assert status code
        Json.GroupGradingUsers requestBody = Json.GroupGradingUsers.builder()
                .userIds(userIds)
                .achievements(achievementCodes)
                .build();
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/grade/group_users", requestBody, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("SUCCESS", responseEntity.getBody());
    }

    public void postProfilePic(String fileName) {
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
        MultipartFile file = new MockMultipartFile(fileName, fileName, contentType, content);

        
        // Alternative way to mock file directly:
        // MultipartFile file = new MockMultipartFile(fileName, fileName, "image/jpeg", new byte[0]);

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

    public void postImportPartial(String jsonArrayString) {
        // Helper method to perform POST request for /import/partial with JSON array
        byte [] bytes = jsonArrayString.getBytes();

        MultipartFile file = new MockMultipartFile("partial", "partial", "application/json", bytes);        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("token", token);
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        
        String url = "http://localhost:8900/import/partial";
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    public void putUserData(Long userId, String first, String last, String email, String username, String role, String gitHubHandle, String zoomRoom, String deadline, String gitHubRepoURL) {
        // Helper method to define and PUT user data, assert status code
        String userData = "{" +
            "\"id\":" + userId + "," +
            "\"firstName\":\"" + first + "\"," +
            "\"lastName\":\"" + last + "\"," +
            "\"userName\":\"" + username + "\"," +
            "\"email\":\"" + email + "\"," +
            "\"role\":\"" + role + "\"," +
            "\"gitHubHandle\":\"" + gitHubHandle + "\"," +
            "\"zoomRoom\":\"" + zoomRoom + "\"," +
            "\"deadline\":\"" + deadline + "\"," +
            "\"gitHubRepoURL\":\"" + gitHubRepoURL + "\"" +
            "}";
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.PUT, "/user", userData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    public Long getIdFromUserName(String userName) {
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

    public Long getIdFromAchievementCode(String code) {
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

    public Long[] getIdsOfActiveDemonstrations() {
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
    
    public String getStatusFromDemonstration(Long demonstrationId) {
        // Helper method to get status of demonstration by demonstration ID by GET request to /demonstrations/activeAndSubmittedOrPickedUp
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/demonstrations/activeAndSubmittedOrPickedUp", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        try {
            JSONArray jsonArray = new JSONArray(responseEntity.getBody());
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject demonstration = jsonArray.getJSONObject(i);                

                if (demonstration.getLong("id") == demonstrationId) {
                    return demonstration.getString("status");
                }
            }
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        return ""; // No demonstration found
    }

    public String getExaminerFromDemonstration(Long demonstrationId) {
        // Helper method to get examiner from demonstration by GET request to /demonstrations/activeAndSubmittedOrPickedUp
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/demonstrations/activeAndSubmittedOrPickedUp", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        try {
            JSONArray jsonArray = new JSONArray(responseEntity.getBody());
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject demonstration = jsonArray.getJSONObject(i);                

                if (demonstration.getLong("id") == demonstrationId) {
                    return demonstration.getJSONObject("examiner").getString("userName");
                }
            }
        }
        catch (JSONException e) {
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        return ""; // No demonstration found
    }

    public Long getDemoIdFromAchievementAndUser(Long achievementId, Long userId) {
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

    public Long getHelpRequestIdFromUser(Long userId) {
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

    public void setupStudentWithValidPic(String firstName, String lastName, String userName) {
        // Helper method to setup a student with a valid profile picture, active demonstration and help request

        // Create student, get IDs for achievement and student
        postNewUser(firstName, lastName, firstName.toLowerCase() + "." + lastName.toLowerCase() + "@uu.se", userName, "STUDENT");
        Long userId = getIdFromUserName(userName);

        updateToken(userName); // Authenticate as student
    
            postProfilePic("profile_pic.jpg");
            
        updateToken("johnteacher"); // Authenticate as teacher again
                
        // Verify profile picture and assert status code
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.PUT, "/user/profile-pic/" + userId + "/verified", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    public void setupStudentWithValidPicAndActiveDemoAndHelpRequest(String code, String firstName, String lastName, String userName) {
        // Helper method to setup a student with a valid profile picture, active demonstration and help request

        // Create student, get IDs for achievement and student
        postNewUser(firstName, lastName, firstName.toLowerCase() + "." + lastName.toLowerCase() + "@uu.se", userName, "STUDENT");
        Long userId = getIdFromUserName(userName);
        Long achievementId = getIdFromAchievementCode(code);

        updateToken(userName); // Authenticate as student
    
            postProfilePic("profile_pic.jpg");
            postDemoRequest(achievementId, userId);
            postHelpRequest(userId);
            
        updateToken("johnteacher"); // Authenticate as teacher again
                
        // Verify profile picture and assert status code
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.PUT, "/user/profile-pic/" + userId + "/verified", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}