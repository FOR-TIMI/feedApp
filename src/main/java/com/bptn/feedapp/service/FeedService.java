package com.bptn.feedapp.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

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
import com.bptn.feedapp.exception.domain.FeedNotUserException;
import com.bptn.feedapp.exception.domain.LikeExistException;
import com.bptn.feedapp.exception.domain.UserNotFoundException;
import com.bptn.feedapp.jpa.Feed;
import com.bptn.feedapp.jpa.FeedMetaData;
import com.bptn.feedapp.jpa.User;
import com.bptn.feedapp.repository.FeedMetaDataRepository;
import com.bptn.feedapp.repository.FeedRepository;
import com.bptn.feedapp.repository.UserRepository;

@Service
public class FeedService {
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	UserRepository userRepository;

	@Autowired
	FeedRepository feedRepository;
	
	@Autowired
	FeedMetaDataRepository feedMetaDataRepository;

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

		Page<Feed> paged = this.feedRepository.findByUser(user,
				PageRequest.of(pageNum, pageSize, Sort.by("feedId").descending()));

		return new PageResponse<Feed>(paged);
	}

	/* Get all feeds not related to the signed in user */
	public PageResponse<Feed> getOtherUsersFeeds(int pageNum, int pageSize) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = this.userRepository.findByUsername(username)
				.orElseThrow(() -> new UserNotFoundException(String.format("Username doesn't exist, %s", username)));

		Page<Feed> paged = this.feedRepository.findByUserNot(user,
				PageRequest.of(pageNum, pageSize, Sort.by("feedId").descending()));
		
		return new PageResponse<Feed>(paged);
	}
	
	/* To add comment or like */
	public FeedMetaData createFeedMetaData(int feedId, FeedMetaData meta) {
		
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
			
		User user = this.userRepository.findByUsername(username)
					             .orElseThrow(()-> new UserNotFoundException(String.format("Username doesn't exist, %s", username)));
			
		Feed feed = this.feedRepository.findById(feedId)
					             .orElseThrow(()-> new FeedNotFoundException(String.format("Feed doesn't exist, %d", feedId)));

		FeedMetaData newMeta = new FeedMetaData();
			
		newMeta.setIsLike(false);
		newMeta.setUser(user);
		newMeta.setFeed(feed);
		newMeta.setCreatedOn(Timestamp.from(Instant.now()));
			
	    if (Optional.ofNullable(meta.getIsLike()).isPresent()) {
	        	
	        newMeta.setIsLike( meta.getIsLike() );
	            
	        if (meta.getIsLike()) {
	        		
	            feed.getFeedMetaData().stream()
	                      .filter(m -> m.getUser().getUsername().equals(username))
	      	              .filter(m -> m.getIsLike().equals(true)).findAny()
	      	              .ifPresent(m -> {throw new LikeExistException(String.format("Feed already liked, feedId: %d, username: %s", feedId, username));});
	            	
	            newMeta.setComment("");
	        }
	    } 
	        
	    if (!newMeta.getIsLike()) {
	        newMeta.setComment(meta.getComment());
	    }
	        
		return this.feedMetaDataRepository.save(newMeta);
	}

	/* To delete a Feed */
	public void deleteFeed(int feedId) {
		
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		
		Feed feed = this.feedRepository.findById(feedId)			
		             .orElseThrow(()-> new FeedNotFoundException(String.format("Feed doesn't exist, %d", feedId)));

		Optional.of(feed).filter(f -> f.getUser().getUsername().equals(username))
			         .orElseThrow(()-> new FeedNotUserException(String.format("Feed doesn't belong to current User, feedId: %d, username: %s", feedId, username)));
			
		this.feedRepository.delete(feed);
	}
}
