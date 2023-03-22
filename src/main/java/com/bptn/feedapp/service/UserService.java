package com.bptn.feedapp.service;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.bptn.feedapp.exception.domain.EmailExistException;
import com.bptn.feedapp.exception.domain.EmailNotVerifiedException;
import com.bptn.feedapp.exception.domain.UserNotFoundException;
import com.bptn.feedapp.exception.domain.UsernameExistException;
import com.bptn.feedapp.jpa.Profile;
import com.bptn.feedapp.jpa.User;
import com.bptn.feedapp.provider.ResourceProvider;
import com.bptn.feedapp.repository.UserRepository;
import com.bptn.feedapp.security.JwtService;

@Service
public class UserService {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	UserRepository userRepository;

	@Autowired
	EmailService emailService;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	JwtService jwtService;

	@Autowired
	ResourceProvider provider;

	/* To register a user */
	public User signup(User user) {

		user.setUsername(user.getUsername().toLowerCase());
		user.setEmailId(user.getEmailId().toLowerCase());

		this.validateUsernameAndEmail(user.getUsername(), user.getEmailId());

		user.setEmailVerified(false);
		user.setPassword(this.passwordEncoder.encode(user.getPassword()));
		user.setCreatedOn(Timestamp.from(Instant.now()));

		this.emailService.sendVerificationEmail(user);

		this.userRepository.save(user);
		return user;

	}

	public List<User> listUsers() {
		return this.userRepository.findAll();
	}

	/* To find a user by their user name if it exists in the database */
	public Optional<User> findByUsername(String username) {
		return this.userRepository.findByUsername(username);
	}

	public void createUser(User user) {
		this.userRepository.save(user);
	}

	/* To make sure a user name and email is unique */
	private void validateUsernameAndEmail(String username, String emailId) {

		this.userRepository.findByUsername(username).ifPresent(u -> {
			throw new UsernameExistException(String.format("Username already exists, %s", u.getUsername()));
		});

		this.userRepository.findByEmailId(emailId).ifPresent(u -> {
			throw new EmailExistException(String.format("Email already exists, %s", u.getEmailId()));
		});

	}

	/* To verify a user's email */
	public void verifyEmail() {

		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = this.userRepository.findByUsername(username)
				.orElseThrow(() -> new UserNotFoundException(String.format("Username doesn't exist, %s", username)));

		user.setEmailVerified(true);

		this.userRepository.save(user);
	}

	/* To check if a user's email is verified */
	private static User isEmailVerified(User user) {
		if (user.getEmailVerified().equals(false)) {
			throw new EmailNotVerifiedException(String.format("Email requires verification, %s", user.getEmailId()));
		}

		return user;
	}

	/* To authenticate user passed in as parameter */
	private Authentication authenticate(String username, String password) {
		return this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
	}

	/*
	 * To over load the above authenticate method and return the users that have
	 * verified their email or throw an error if the user has not verified their
	 * email
	 */
	public User authenticate(User user) {

		/* Spring security authentication */
		this.authenticate(user.getUsername(), user.getPassword());

		/* Get user from the DB */
		return this.userRepository.findByUsername(user.getUsername()).map(UserService::isEmailVerified).get();
	}

	/*
	 * generates a JWT token for a given user and returns the header with a
	 * generated token
	 */
	public HttpHeaders generateJwtHeader(String username) {
		HttpHeaders headers = new HttpHeaders();

		headers.add(AUTHORIZATION, this.jwtService.generateJwtToken(username, this.provider.getJwtExpiration()));

		return headers;
	}

	/* Send reset password email */
	public void sendResetPasswordEmail(String emailId) {
		Optional<User> opt = this.userRepository.findByEmailId(emailId);

		if (opt.isPresent()) {
			this.emailService.sendResetPasswordEmail(opt.get());
		} else {
			logger.debug("That email does not exist, {}", emailId);
		}
	}

	/* To reset a password */
	public void resetPassword(String password) {

		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = this.userRepository.findByUsername(username)
				.orElseThrow(() -> new UserNotFoundException(String.format("Username doesn't exist, %s", username)));

		user.setPassword(this.passwordEncoder.encode(password));

		this.userRepository.save(user);

	}

	/* Get the signed in user */
	public User getUser() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		return this.userRepository.findByUsername(username)
				.orElseThrow(() -> new UserNotFoundException(String.format("Username does not exist, %s", username)));
	}

	/* Helper Method to update a field in the User table */
	private void updateValue(Supplier<String> getter, Consumer<String> setter) {

		Optional.ofNullable(getter.get())
				// .filter(StringUtils::hasText)
				.map(String::trim).ifPresent(setter);
	}

	/* Helper Method to update password */
	private void updatePassword(Supplier<String> getter, Consumer<String> setter) {

		Optional.ofNullable(getter.get()).filter(StringUtils::hasText).map(this.passwordEncoder::encode)
				.ifPresent(setter);
	}

	/* Helper Method to update a user */
	private User updateUser(User user, User currentUser) {

		this.updateValue(user::getFirstName, currentUser::setFirstName);
		this.updateValue(user::getLastName, currentUser::setLastName);
		this.updateValue(user::getPhone, currentUser::setPhone);
		this.updateValue(user::getEmailId, currentUser::setEmailId);
		this.updatePassword(user::getPassword, currentUser::setPassword);

		return this.userRepository.save(currentUser);
	}

	/* Method to update user */
	public User updateUser(User user) {

		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		/* Validates the new email if provided */
		this.userRepository.findByEmailId(user.getEmailId()).filter(u -> !u.getUsername().equals(username))
				.ifPresent(u -> {
					throw new EmailExistException(String.format("Email already exists, %s", u.getEmailId()));
				});

		/* Get and Update User */
		return this.userRepository.findByUsername(username).map(currentUser -> this.updateUser(user, currentUser))
				.orElseThrow(() -> new UserNotFoundException(String.format("Username doesn't exist, %s", username)));
	}

	/* Helper method to update a user's profile */
	private User updateUserProfile(Profile profile, User user) {
		Profile currentProfile = user.getProfile();

		/* Update the profile if it exists */
		if (Optional.ofNullable(currentProfile).isPresent()) {
			this.updateValue(profile::getHeadline, currentProfile::setHeadline);
			this.updateValue(profile::getBio, currentProfile::setBio);
			this.updateValue(profile::getCity, currentProfile::setCity);
			this.updateValue(profile::getCountry, currentProfile::setCountry);
			this.updateValue(profile::getPicture, currentProfile::setPicture);
		} else { /* create a new profile and setProfile to the user */
			user.setProfile(profile);
			profile.setUser(user);
		}

		return this.userRepository.save(user);
	}

	public User updateUserProfile(Profile profile) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		/* Get and Update the user if the user exists */
		return this.userRepository.findByUsername(username).map(u -> this.updateUserProfile(profile, u))
				.orElseThrow(() -> new UserNotFoundException(String.format("Username does not exist, %s", username)));
	}

}
