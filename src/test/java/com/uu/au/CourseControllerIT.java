package com.uu.au;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.DirtiesContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.extension.ExtendWith;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(SpringExtension.class)
// @Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CourseControllerIT {

    private String token;

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

    @BeforeEach
    public void setupCourseAndUser() {
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
    public void testGetCourse() {
        // Perform GET request for /course with initial course data
        ResponseEntity<String> responseEntity = makeRequest(HttpMethod.GET, "/course", null, true);

        // Assert status code and response body
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        String responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("name\":\"Fun Course"));
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
    
        // Assert status code and response body of the captured exception
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getResponseBodyAsString().contains("COURSE_ALREADY_EXISTS"));
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
    }
}