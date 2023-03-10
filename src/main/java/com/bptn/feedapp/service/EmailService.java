package com.bptn.feedapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.bptn.feedapp.jpa.User;
import com.bptn.feedapp.provider.ResourceProvider;
import com.bptn.feedapp.security.JwtService;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${spring.mail.username}")
	private String emailFrom;

	@Autowired
	JwtService jwtService;

	@Autowired
	ResourceProvider provider;

	@Autowired
	TemplateEngine templateEngine;

	@Autowired
	JavaMailSender javaMailSender;

	private void sendEmail(User user, String clientParam, String templateName, String emailSubject, long expiration) {

		try {

			/* Collect Data for the Email HTML generation */
			Context context = new Context();
			context.setVariable("user", user);
			context.setVariable("client", this.provider.getClientUrl());
			context.setVariable("param", clientParam);
			context.setVariable("token", this.jwtService.generateJwtToken(user.getUsername(), expiration));

			/* Process Email HTML Template */
			String process = this.templateEngine.process(templateName, context);

			MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

			/* Set Email Information */
			helper.setFrom(this.emailFrom, "FeedApp - Obsidi Academy");
			helper.setSubject(emailSubject);
			helper.setText(process, true);
			helper.setTo(user.getEmailId());

			/* Send Email */
			this.javaMailSender.send(mimeMessage);

			this.logger.debug("Email Sent, {} ", user.getEmailId());

		} catch (Exception ex) {

			this.logger.error("Error while Sending Email, Username: " + user.getUsername(), ex);
		}
	}

	@Async
	public void sendVerificationEmail(User user) {

		this.sendEmail(user, this.provider.getClientVerifyParam(), "verify_email",
				String.format("Welcome %s %s", user.getFirstName(), user.getLastName()),
				this.provider.getClientVerifyExpiration());
	}
}
