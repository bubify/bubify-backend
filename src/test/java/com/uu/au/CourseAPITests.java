package com.uu.au;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.uu.au.models.User;
import com.uu.au.enums.Role;
import org.junit.jupiter.api.BeforeAll;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@SpringBootTest("server.port=8900")
@AutoConfigureMockMvc
public class CourseAPITests {
    private static String token;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    public static void setup() {
        // Code to create a user and obtain token
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUserName("jdoe");
        user.setEmail("j.d@uu.se");
        user.setRole(Role.TEACHER);

        // Get and store the token needed for the tests
        token = getTokenForSuperUser(user.getUserName());
    }

    @Test
    public void testGetCourse() throws Exception {
        mockMvc.perform(get("/course")
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    public void testPostCourse() throws Exception {
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

        mockMvc.perform(post("/course")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("token", token))
                .andExpect(status().isOk());
    }
    
    @Test
    public void testPutCourse() throws Exception {
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
        
        mockMvc.perform(put("/course")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("token", token))
                .andExpect(status().isOk());
    }   

    public static String getTokenForSuperUser(String username) {
        // Method using API endpoint to get the token
        RestTemplate restTemplate = new RestTemplate();

        String url = "http://localhost:8900/su?username=" + username;

        // Make a GET request to the /su endpoint
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);

        if (responseEntity.getStatusCodeValue() == 200) {
            // Extract the token from the response body
            String token = responseEntity.getBody();
            return token;
        } else {
            throw new RuntimeException("Failed to retrieve token. Status code: " + responseEntity.getStatusCodeValue());
        }
    }
}