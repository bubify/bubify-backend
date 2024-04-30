package com.uu.au.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
public class ExperimentalIT {

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
    
    private ResponseEntity<String> postJpgFile(String endpoint, String filepath, String filename) {
        Path path = Paths.get(filepath);
        String contentType = "image/jpeg";
        byte[] content = null;
        try {
            content = Files.readAllBytes(path);
        } catch (final IOException e) {
            e.printStackTrace();
            fail("Failed to read file: " + e.getMessage());
        }
        MultipartFile file = new MockMultipartFile(filename, filename, contentType, content);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());
        // body.add("file_name", filename); // Probably not needed or applicable
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("token", token);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();

        String url = "http://localhost:8900" + endpoint;
        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
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
            fail("Failed to parse JSON object/array: " + e.getMessage());
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
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }
        return achievementId;
    }

    @Test
    public void experimentalPostProfilePic() {
        String filepath = "src/test/resources/profile_pic_1.jpg";
        String filename = "profile_pic_1.jpg";
        ResponseEntity<String> responseEntity = postJpgFile("/user/upload-profile-pic", filepath, filename);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void experimentalAchievementAllRemaining() {
        // Define and POST achievement data, assert status code
        String achievementData = "Code1;Name1;GRADE_3;ACHIEVEMENT;http://example.com/name1";
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.POST, "/admin/add-achievement", achievementData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Define and POST student data, assert status code
        String studentData = "Jane;Doe;jane.doe@uu.se;janedoe;STUDENT";
        responseEntity = makeRequest(HttpMethod.POST, "/admin/add-user", studentData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Get IDs for achievement and users
        int achievementId = getIdFromAchievementCode("Code1");
        int userId = getIdFromUserName("janedoe");
        int userId2 = getIdFromUserName("jdoe"); // Current authenticated user

        // Define and POST a demonstration request, assert status code
        String demonstrationData = "{\"achievementIds\":[" + achievementId + "],\"ids\":[" + userId + "," + userId2 +"],\"zoomPassword\":\"string\",\"physicalRoom\":\"string\"}";
        responseEntity = makeRequest(HttpMethod.POST, "/demonstration/request", demonstrationData, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Perform GET request for /demonstrations/activeAndSubmittedOrPickedUp, assert status code
        responseEntity = makeRequest(HttpMethod.GET, "/demonstrations/activeAndSubmittedOrPickedUp", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        String responseBody = responseEntity.getBody();
        
        // Get first demonstration ID from JSON array
        int demonstrationId = 0;
        try {
            JSONArray jsonArray = new JSONArray(responseBody);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            demonstrationId = jsonObject.getInt("id");
        }
        catch (JSONException e) {
            e.printStackTrace();
            fail("Failed to parse JSON object/array: " + e.getMessage());
        }

        // Upload profile pictures
        String filepath = "src/test/resources/profile_pic_1.jpg";
        String filename = "profile_pic_1.jpg";
        responseEntity = postJpgFile("/user/upload-profile-pic", filepath, filename);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Verify profile pictures
        responseEntity = makeRequest(HttpMethod.PUT, "/user/profile-pic/" + userId + "/verified", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        responseEntity = makeRequest(HttpMethod.PUT, "/user/profile-pic/" + userId2 + "/verified", null, true);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        // Define and POST achievement result data, assert status code
        String achievementResultData = "{\"demoId\":" + demonstrationId + ",\"results\":[{\"achievementId\":" + achievementId + ",\"id\":" + userId + ",\"result\":\"Pass\"}]}";
        responseEntity = makeRequest(HttpMethod.POST, "/demonstration/done", achievementResultData, true);

        // Perform GET request for /achievement/all-remaining/{code} with 1 achievement and 1 student in system but NOT in remaining
        responseEntity = makeRequest(HttpMethod.GET, "/achievement/all-remaining/Code1", null, true);

        // Assert status code and response body
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());
    }
}