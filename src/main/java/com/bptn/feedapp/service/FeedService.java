package com.bptn.feedapp.service;

import java.sql.Timestamp;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.bptn.feedapp.exception.domain.UserNotFoundException;
import com.bptn.feedapp.jpa.Feed;
import com.bptn.feedapp.jpa.User;
import com.bptn.feedapp.repository.FeedRepository;
import com.bptn.feedapp.repository.UserRepository;

@Service
public class FeedService {
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	FeedRepository feedRepository;
	
	/* To create a new Feed */
	public Feed createFeed(Feed feed) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		
		User user = this.userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(String.format("Username doesn't exist, %s", username)));
		
		feed.setUser(user);
		feed.setCreatedOn(Timestamp.from(Instant.now()));
		
		return this.feedRepository.save(feed);
		
	}


}
