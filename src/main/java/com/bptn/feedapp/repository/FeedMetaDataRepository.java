package com.bptn.feedapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bptn.feedapp.jpa.FeedMetaData;

public interface FeedMetaDataRepository extends JpaRepository<FeedMetaData, Integer> {
//	The body for the interface is empty since no additional methods are defined.
}
