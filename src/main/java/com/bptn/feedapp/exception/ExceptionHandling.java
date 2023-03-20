package com.bptn.feedapp.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.bptn.feedapp.domain.HttpResponse;
import com.bptn.feedapp.exception.domain.EmailExistException;
import com.bptn.feedapp.exception.domain.EmailNotFoundException;
import com.bptn.feedapp.exception.domain.EmailNotVerifiedException;
import com.bptn.feedapp.exception.domain.FeedNotFoundException;
import com.bptn.feedapp.exception.domain.FeedNotUserException;
import com.bptn.feedapp.exception.domain.LikeExistException;
import com.bptn.feedapp.exception.domain.UserNotFoundException;
import com.bptn.feedapp.exception.domain.UsernameExistException;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import jakarta.persistence.NoResultException;

@RestController
@RestControllerAdvice
public class ExceptionHandling implements ErrorController {
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String TOKEN_DECODE_ERROR = "Token Decode Error";
	private static final String TOKEN_EXPIRED_ERROR = "Token has Expired";
	private static final String ACCOUNT_LOCKED = "Your account has been locked. Please contact administration";
	private static final String METHOD_IS_NOT_ALLOWED = "This request method is not allowed on this endpoint. Please send a '%s' request";
	private static final String INTERNAL_SERVER_ERROR_MSG = "An error occurred while processing the request";
	private static final String INCORRECT_CREDENTIALS = "Username or Password is Incorrect. Please try again";
	private static final String ACCOUNT_DISABLED = "Your account has been disabled. If this is an error, please contact administration";
	private static final String NOT_ENOUGH_PERMISSION = "You do not have enough permission";
	private static final String NOT_AUTHENTICATED = "You need to log in to access this URL";
	private static final String NO_MAPPING_EXIST_URL = "There is no mapping for this URL";
	private static final String ERROR_PATH = "/error";

	private ResponseEntity<HttpResponse> createHttpResponse(HttpStatus httpStatus, String message) {
		return new ResponseEntity<>(
				new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
				httpStatus);
	}

	@ExceptionHandler(JWTDecodeException.class)
	public ResponseEntity<HttpResponse> tokenDecodeException() {
		return this.createHttpResponse(BAD_REQUEST, TOKEN_DECODE_ERROR);
	}

	@ExceptionHandler(DisabledException.class)
	public ResponseEntity<HttpResponse> accountDisabledException() {
		return this.createHttpResponse(BAD_REQUEST, ACCOUNT_DISABLED);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<HttpResponse> badCredentialsException() {
		return this.createHttpResponse(BAD_REQUEST, INCORRECT_CREDENTIALS);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<HttpResponse> accessDeniedException() {
		return this.createHttpResponse(FORBIDDEN, NOT_ENOUGH_PERMISSION);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<HttpResponse> authenticationException() {
		return this.createHttpResponse(FORBIDDEN, NOT_AUTHENTICATED);
	}

	@ExceptionHandler(LockedException.class)
	public ResponseEntity<HttpResponse> lockedException() {
		return this.createHttpResponse(UNAUTHORIZED, ACCOUNT_LOCKED);
	}

	@ExceptionHandler(TokenExpiredException.class)
	public ResponseEntity<HttpResponse> tokenExpiredException(TokenExpiredException ex) {
		return this.createHttpResponse(UNAUTHORIZED, TOKEN_EXPIRED_ERROR);
	}

	@ExceptionHandler(EmailExistException.class)
	public ResponseEntity<HttpResponse> emailExistException(EmailExistException ex) {
		return this.createHttpResponse(BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(UsernameExistException.class)
	public ResponseEntity<HttpResponse> usernameExistException(UsernameExistException ex) {
		return this.createHttpResponse(BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(LikeExistException.class)
	public ResponseEntity<HttpResponse> likeExistException(LikeExistException ex) {
		return this.createHttpResponse(BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(EmailNotFoundException.class)
	public ResponseEntity<HttpResponse> emailNotFoundException(EmailNotFoundException ex) {
		return this.createHttpResponse(BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(EmailNotVerifiedException.class)
	public ResponseEntity<HttpResponse> emailNotVerifiedException(EmailNotVerifiedException ex) {
		return this.createHttpResponse(BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<HttpResponse> userNotFoundException(UserNotFoundException ex) {
		return this.createHttpResponse(BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(FeedNotFoundException.class)
	public ResponseEntity<HttpResponse> feedNotFoundException(FeedNotFoundException ex) {
		return this.createHttpResponse(BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(FeedNotUserException.class)
	public ResponseEntity<HttpResponse> feedNotUserException(FeedNotUserException ex) {
		return this.createHttpResponse(BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<HttpResponse> methodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
		HttpMethod supportedMethod = Objects.requireNonNull(ex.getSupportedHttpMethods()).iterator().next();
		return this.createHttpResponse(METHOD_NOT_ALLOWED, String.format(METHOD_IS_NOT_ALLOWED, supportedMethod));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<HttpResponse> internalServerErrorException(Exception exception) {
		logger.error(exception.getMessage(), exception);
		return this.createHttpResponse(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG);
	}

	@ExceptionHandler(NoResultException.class)
	public ResponseEntity<HttpResponse> notFoundException(NoResultException exception) {
		logger.error(exception.getMessage());
		return this.createHttpResponse(NOT_FOUND, exception.getMessage());
	}

	@GetMapping(ERROR_PATH)
	public ResponseEntity<HttpResponse> notFound404() throws Exception {
		return this.createHttpResponse(NOT_FOUND, NO_MAPPING_EXIST_URL);
	}
}
