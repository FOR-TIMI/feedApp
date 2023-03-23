package com.bptn.feedapp.service;

import java.sql.Timestamp;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.bptn.feedapp.domain.PageResponse;
import com.bptn.feedapp.exception.domain.FeedNotFoundException;
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

		User user = this.userRepository.findByUsername(username)
				.orElseThrow(() -> new UserNotFoundException(String.format("Username doesn't exist, %s", username)));

		feed.setUser(user);
		feed.setCreatedOn(Timestamp.from(Instant.now()));

		return this.feedRepository.save(feed);

	}

	/* To get one feed by it's id */
	public Feed getFeedbyId(int feedId) {
		return this.feedRepository.findById(feedId)
				.orElseThrow(() -> new FeedNotFoundException((String.format("Feed doesn't exost, %d", feedId))));
	}

	/* Get all feed related to the signedIn user */
	public PageResponse<Feed> getUserFeeds(int pageNum, int pageSize) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		/* Check if the user exists */
		User user = this.userRepository.findByUsername(username)
				.orElseThrow(() -> new UserNotFoundException(String.format("Username doesn't exist, %s", username)));
		
		Page<Feed> paged = this.feedRepository.findByUser(user, PageRequest.of(pageNum, pageSize, Sort.by("feedId").descending()));
		
		return new PageResponse<Feed>(paged);
	}
}
