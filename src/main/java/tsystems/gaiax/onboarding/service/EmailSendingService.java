package tsystems.gaiax.onboarding.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import tsystems.gaiax.onboarding.dto.RegisterResult;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
public class EmailSendingService {

  private final JavaMailSender javaMailSender;
  @Value("${support.email}")
  private String senderMail;

  @Value("${services.portal.uri.external}")
  private String portalExtLink;
  @Value("${send.email:true}")
  private boolean sendEmail;

  @Autowired
  public EmailSendingService(JavaMailSender javaMailSender) {
    this.javaMailSender = javaMailSender;
  }

  public RegisterResult sendEmailRegistrationMessage(String email, String routing) {
    try {
      String uniqueId = createUniqueId(email);
      String emailContent = prepareEmailContent(routing, uniqueId);
      MimeMessage message = prepareMessage(email, routing, uniqueId, emailContent);
      log.info("Email sending. Identifier: {}; email: {}; emailContent: {}", uniqueId, email, emailContent);
      if (sendEmail) javaMailSender.send(message);
      else log.info("Email sending was switched off");
      return new RegisterResult(false);
    } catch (MessagingException e) {
      return new RegisterResult(true, e.getMessage() + "; cause: " + e.getLocalizedMessage());
    }
  }

  private String createUniqueId(String email) {
    String encoded = Base64.getEncoder().encodeToString(email.getBytes());
    char[] chars = encoded.toCharArray();
    int eqCount = 0;
    StringBuilder sb = new StringBuilder();
    for (char ch : chars) {
      if (ch == '=') eqCount++;
      else sb.append(ch);
    }
    return sb.append("-")
             .append(eqCount)
             .append(UUID.randomUUID().toString().replaceAll("-", ""))
             .toString();
  }

  private MimeMessage prepareMessage(String email, String routing, String uniqueId, String emailContent) throws MessagingException {
    MimeMessage message = javaMailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    helper.setSubject("Gaia-X: email confirmation");
    helper.setFrom(senderMail);
    helper.setTo(email);
    helper.setText(emailContent, true);
    return message;
  }

  private String prepareEmailContent(final String routing, final String uniqueId) {
    final String link = String.format("%s/confirmation/%s/%s", portalExtLink, routing, uniqueId);
    String text = String.format(
            "<h1>Welcome to Gaia-X</h1>" +
                    "<p>Please click on the following link to confirm your email address: " +
                    "<a href=\"%s\">%s</a>", link, link);
    return text;
  }
}
