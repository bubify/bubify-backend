package com.uu.au.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uu.au.models.Json;
import com.uu.au.models.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uu.au.controllers.DemonstrationController;
import com.uu.au.models.Achievement;
import com.uu.au.models.Demonstration;
import com.uu.au.repository.DemonstrationRepository;
import com.uu.au.repository.UserRepository;
import com.uu.au.repository.AchievementRepository;

import lombok.Builder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties.Build;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
// @WebMvcTest(controllers = DemonstrationController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension.class)
public class DemonstrationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DemonstrationRepository demonstrationRepository;
    
    @MockBean
    private UserRepository userRepository;
    
    @MockBean
    private AchievementRepository achievementRepository;

    @Test
    public void testRequestDemonstration() throws Exception {
        // Prepare a sample JSON request body
        Json.DemonstrationRequest demonstrationRequest = Json.DemonstrationRequest.builder()
                .achievementIds(List.of(1L, 2L))
                .ids(List.of(1L, 2L)) // User ids
                .zoomPassword("password")
                .physicalRoom("Room 1")
                .build();

        // Add users and achievements to the database
        // This can be done using the userRepository and achievementRepository
        // For example, you can create User and Achievement objects and save them using the respective repositories
        User user1 = User.builder().id(1L).build();
        User user2 = User.builder().id(2L).build();
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(java.util.Optional.of(user2));
        Achievement achievement1 = Achievement.builder().id(1L).build();
        Achievement achievement2 = Achievement.builder().id(2L).build();
        when(achievementRepository.findById(1L)).thenReturn(java.util.Optional.of(achievement1));
        when(achievementRepository.findById(2L)).thenReturn(java.util.Optional.of(achievement2));

        // Convert demonstrationRequest to JSON string
        String requestBody = objectMapper.writeValueAsString(demonstrationRequest);

        // Mock the behavior of demonstrationRepository.save() method
        when(demonstrationRepository.save(any(Demonstration.class))).thenReturn(new Demonstration());
        
        // Perform POST request to /demonstration/request endpoint
        MvcResult mvcResult = mockMvc.perform(post("/demonstration/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();
        // ERROR: Code 400 - CURRENT_USER_NOT_IN_SUBMITTERS, is there any point on mocking the userRepository.findById() method?
        // Furthermore it depends on users.currentUser() which requires and authentication token

        // Optionally, assert the response content or perform additional verifications
        // For example, you can parse the response body JSON and verify certain fields

        // Example: Assert that a Demonstration object was created and persisted
        Long demonstrationId = Long.parseLong(mvcResult.getResponse().getContentAsString());
        Demonstration demonstration = demonstrationRepository.findById(demonstrationId).orElse(null);
        assertNotNull(demonstration); // Example assertion
    }
}
