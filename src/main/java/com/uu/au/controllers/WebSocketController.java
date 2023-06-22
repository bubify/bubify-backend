package com.uu.au.controllers;

import com.uu.au.models.User;
import com.uu.au.repository.CourseRepository;

import com.uu.au.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
public class WebSocketController {
	public final String HELP_LISTENER = "/topic/helpRequest";
	public final String HELP_LISTENER_CLAIM = "/topic/helpRequestClaimed";
	public final String DEMO_LISTENER = "/topic/demoRequest";
	public final String CLAIM_HELP_LISTENER_PREFIX = "/topic/claimedHelp/";
	public final String CLAIM_DEMO_LISTENER_PREFIX = "/topic/claimedDemo/";
	public final String PROFILE_CHANGE_PREFIX = "/topic/user/";
	public final String DEMO_LIST_CLEARED_LISTENER = "/topic/demonstration/cleared";
	public final String HELP_LIST_CLEARED_LISTENER = "/topic/help/cleared";
	public final String COURSE_LISTENER = "/topic/course";
	public final String PROFILE_PICTURE_PREFIX = "/topic/profilePicture/";

	private final SimpMessagingTemplate webSocket;

	private final AtomicInteger demoCount = new AtomicInteger();
	private final AtomicInteger helpCount = new AtomicInteger();
	private final AtomicInteger claimCount = new AtomicInteger();

	private int lastDemoCount = 0;
	private int lastHelpCount = 0;
	private int lastClaimCount = 0;

	@Autowired
	CourseRepository courseRepository;

	@Autowired
	UserRepository userRepository;

	@Scheduled(fixedRate = 500)
	public void webSocketBacklog() {
    	var currentDemoCount = demoCount.get();
    	if (lastDemoCount < currentDemoCount) {
    		lastDemoCount = currentDemoCount;
			this.webSocket.convertAndSend(DEMO_LISTENER, "updated");
		}

		var currentHelpCount = helpCount.get();
		if (lastHelpCount < currentHelpCount) {
			lastHelpCount = currentHelpCount;
			this.webSocket.convertAndSend(HELP_LISTENER, "updated");
		}

		var currentClaimCount = claimCount.get();
		if (lastClaimCount < currentClaimCount) {
			lastClaimCount = currentClaimCount;
			this.webSocket.convertAndSend(HELP_LISTENER_CLAIM, "{\"message\": \"change me\"}");
		}
	}

	@Autowired
	public WebSocketController(SimpMessagingTemplate template) {
		this.webSocket = template;
	}

	public void notifyHelpListSubscribers() {
		helpCount.incrementAndGet();
	}

	public void notifyDemoListSubscribers() {
		demoCount.incrementAndGet();
	}

	public void notifyHelpListClaimSubscribers() {
		claimCount.incrementAndGet();
	}

	public void notifyNewCourse() {
		var currentCourseList = courseRepository.findAll();
		if (currentCourseList.size() != 1) return;
		this.webSocket.convertAndSend(COURSE_LISTENER, currentCourseList.get(0));
	}

	public void notifyProcessedProfilePicture(Long uid) {
		this.webSocket.convertAndSend(PROFILE_PICTURE_PREFIX + uid.toString(), "Bump!");
	}

	public void notifyProfileChange(User recipient, String endPoint) {
		this.webSocket.convertAndSend(PROFILE_CHANGE_PREFIX + recipient.getId() + endPoint, "Bump!");
	}

	public void notifyHelpListCleared() {
		this.webSocket.convertAndSend(HELP_LIST_CLEARED_LISTENER, "Bump!");
	}

	public void notifyDemoListCleared() {
		this.webSocket.convertAndSend(DEMO_LIST_CLEARED_LISTENER, "Bump!");
	}

	public void notifyDemoClaim(Set<User> recipients, String message) {
		recipients
				.forEach(u -> this.webSocket.convertAndSend(CLAIM_DEMO_LISTENER_PREFIX + u.getId(), message));
	}

	public void notifyHelpClaim(Set<User> recipients, String message) {
		recipients
				.forEach(u -> this.webSocket.convertAndSend(CLAIM_HELP_LISTENER_PREFIX + u.getId(), message));
	}
}