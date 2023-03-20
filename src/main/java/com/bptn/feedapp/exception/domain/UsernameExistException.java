package com.bptn.feedapp.exception.domain;

public class UsernameExistException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UsernameExistException(String message) {
        super(message);
    }
}
