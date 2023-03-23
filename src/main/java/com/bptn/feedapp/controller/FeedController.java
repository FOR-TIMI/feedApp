package com.bptn.feedapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bptn.feedapp.jpa.Feed;
import com.bptn.feedapp.service.FeedService;

@CrossOrigin
@RestController
@RequestMapping("/feeds")
public class FeedController {
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	FeedService feedService;

	@PostMapping
	public Feed createFeed(@RequestBody Feed feed) {
		logger.debug("Creating Feed");

		return this.feedService.createFeed(feed);
	}
	
	@GetMapping("/{feedId}")
	public Feed getFeed(@PathVariable int feedId) {
		logger.debug("Getting Feed, feedId: {}", feedId);
		
		return this.feedService.getFeedbyId(feedId);
	}

}
