package com.bptn.feedapp.filter;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bptn.feedapp.provider.ResourceProvider;
import com.bptn.feedapp.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.HandlerExceptionResolver;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.OPTIONS;
import org.springframework.security.core.context.SecurityContextHolder;
import com.auth0.jwt.exceptions.JWTVerificationException;

import java.util.Optional;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

	// TODO Auto-generated method stub
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	JwtService jwtService;

	@Autowired
	ResourceProvider provider;

	@Autowired
	@Qualifier("handlerExceptionResolver")
	HandlerExceptionResolver resolver;


	private Authentication getAuthentication(String username, HttpServletRequest req) {
		UsernamePasswordAuthenticationToken userPasswordAuthToken = new UsernamePasswordAuthenticationToken(username,
				null, null);
		userPasswordAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
		return userPasswordAuthToken;
	}
	
	private boolean isJwtPrefixValid(String header) {
    	
		logger.debug("Authorization Header: {}", Optional.ofNullable(header).orElse("Not Present"));
		    	
		return Optional.ofNullable(header)
		    			     .filter(h -> h.startsWith(this.provider.getJwtPrefix()))
		    			     .isPresent();
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain) throws ServletException, IOException {

	    	logger.debug("Running Jwt Filter, URL: {}, Method: {}", req.getRequestURL(), req.getMethod());
	    	   	    			
	    	try {    				
	    		
	    		/* Requests with method = OPTIONS are not validated. */
	    		if ( !req.getMethod().equalsIgnoreCase(OPTIONS.name()) ) {
	    			
	    			String header = req.getHeader(AUTHORIZATION); /* Get the Header from the request.*/
	    			
	    			/* Check if the JWT is present in the Header and starts with the JWT Prefix. */
	    			if ( this.isJwtPrefixValid(header) ){ 
	    				/* Validate the JWT and get the username from it. */
	    				String username = this.jwtService.getSubject(header.substring(7)); 
	    				
	    				/* Set the username in the Spring Security context for later use. */
	    		SecurityContextHolder.getContext().setAuthentication(this.getAuthentication(username, req));			
	    				logger.debug("User Authorized: {}", username);
	    			}
	    		}
	    		
	    		filterChain.doFilter(req, res);
	    	} 
	    	catch(JWTVerificationException ex) {
	    		logger.debug("Token cannot be verified, Reason: {}", ex.getMessage());
	    				
	    		this.resolver.resolveException(req, res, null, ex);
	    	}       	
	}

}
