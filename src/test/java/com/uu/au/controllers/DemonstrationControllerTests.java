// package com.uu.au.controllers;

// import com.uu.au.enums.DemonstrationStatus;
// import com.uu.au.models.Demonstration;
// import com.uu.au.models.User;
// import com.uu.au.repository.DemonstrationRepository;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;

// import java.time.LocalDateTime;
// import java.util.Arrays;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Set;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.mockito.Mockito.*;

// class DemonstrationControllerTests {

//     @Mock
//     private DemonstrationRepository demonstrationRepository;

//     @InjectMocks
//     private DemonstrationController demonstrationController;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.initMocks(this);

//         // Mocking the method that is called in the method under test
//         when(demonstrationController._requestDemonstration(any())).thenReturn(new Demonstration());
//     }

//     @Test
//     void testDemonstrationRequestsCurrentCourseInstance() {
//         LocalDateTime now = LocalDateTime.now();
//         Demonstration demonstration1 = Demonstration.builder().requestTime(now.minusDays(1)).build();
//         Demonstration demonstration2 = Demonstration.builder().requestTime(now.plusDays(1)).build();

//         when(demonstrationRepository.findAll()).thenReturn(Arrays.asList(demonstration1, demonstration2));

//         List<Demonstration> result = demonstrationController.demonstrationRequestsCurrentCourseInstance();

//         assertEquals(1, result.size());
//         assertEquals(demonstration2, result.get(0));
//     }

//     @Test
//     void testRequestDemonstration() {
//         // Provide necessary mocks
//         User user = User.builder().id(1L).build();
//         when(demonstrationController._requestDemonstration(any())).thenReturn(new Demonstration());

//         // Call method under test
//         Demonstration result = demonstrationController.requestDemonstration(new Json.DemonstrationRequest());

//         // Assertions
//         verify(demonstrationController, times(1))._requestDemonstration(any());
//     }

//     // More tests can be added for other methods in DemonstrationController
// }
