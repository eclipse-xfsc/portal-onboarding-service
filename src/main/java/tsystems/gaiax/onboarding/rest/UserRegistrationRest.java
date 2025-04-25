package tsystems.gaiax.onboarding.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import tsystems.gaiax.onboarding.dto.VerifiedCredentials;
import tsystems.gaiax.onboarding.model.ErrorDto;
import tsystems.gaiax.onboarding.model.NoVCDTO;
import tsystems.gaiax.onboarding.model.UserRegistrationRequest;
import tsystems.gaiax.onboarding.model.UserWithVCDTO;
import tsystems.gaiax.onboarding.service.EmailSendingService;
import tsystems.gaiax.onboarding.service.OnboardingService;
import tsystems.gaiax.onboarding.util.ProxyCall;
import tsystems.gaiax.onboarding.util.ValidationUtil;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api/onboarding/register/user")
@Api(tags = "User Registration Service")
public class UserRegistrationRest {
  @Autowired
  @Qualifier("ocmSrv")
  private WebClient ocmSrv;

  @Value("${services.portal.uri.external}")
  private String portalURIExt;

  @Autowired
  private OnboardingService onboardingService;

  @PostMapping
  public ResponseEntity<?> addUserRequest(@RequestBody UserRegistrationRequest rq) {
    log.info("validating email");
    boolean validEmail = ValidationUtil.validateEmail(rq.getEmail());
    if (!validEmail) {
      return ResponseEntity.badRequest().body(
              new ErrorDto(
                      "api/onboarding/register/user",
                      "not valid email"
              )
      );
    }

    log.info("check if pending request with such email exists");
    if (onboardingService.isAnyOnboardingPendingRequestExist(rq.getEmail())) {
      return ResponseEntity.badRequest().body(
              new ErrorDto(
                      "api/onboarding/register/user",
                      "request with such email already exists"
              )
      );
    }

    log.info("storing request to DB and inform the user atomically");
    try {
      onboardingService.createVCNPNotConfirmedRequest(rq);

      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(
              new ErrorDto(
                      "api/onboarding/register/user",
                      e.getMessage()
              )
      );
    }
  }

  @PostMapping("/confirm_email/{id}")
  @ApiOperation("Confirm email address using link from email (P.21 in documentation)")
  public ResponseEntity<?> confirmEmail(@PathVariable("id") String uniqueId) {
    log.debug("get email from uniqueID");
    final String email = onboardingService.getEmailFromUniqueId(uniqueId);
    log.debug("Check that email was already confirmed during current onboarding, email: {}", email);
    if (onboardingService.isEmailForVCNPRequestAlreadyConfirmed(email)) {
      log.info("Email already confirmed, set redirection");
      return ResponseEntity.status(HttpStatus.FOUND)
                           .location(
                                   URI.create(portalURIExt + "/onboarding/email_already_confirmed")
                           ).build();
    }

    log.info("Convert pending request to real request");
    try {
      onboardingService.createVCNPConfirmedRequest(email);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(
              new ErrorDto(
                      "/api/onboarding/register/user/confirm_email/{id}",
                      e.getMessage()
              )
      );
    }

    Map<String, String> res = new HashMap<>();
    res.put("id", uniqueId);
    return ResponseEntity.ok().body(res);
  }

  @GetMapping(value = "/vc")
  @ApiOperation("Customer VC Details")
  public ResponseEntity<?> requestVC(HttpServletRequest request) {
    log.info("We should already have JWT because user scanned QR code and was authorized");
    final String jwt = request.getHeader("Authorization");
    if (jwt == null || jwt.isBlank()) {
      return ResponseEntity
              .status(HttpStatus.UNAUTHORIZED)
              .body(
                      new ErrorDto(
                              "/api/onboarding/register/user/vc",
                              "Not authorized")
              );
    }

    log.info("Requesting OCM");

    try {
      final ResponseEntity<Map<String, ?>> extRespEntity = ProxyCall.<Map<String, ?>>doGet(ocmSrv, request);
      final Map<String, ?> raw = extRespEntity.getBody();

      if ((Boolean) raw.get("first_did")) {
        return ResponseEntity.ok().body(
                new UserWithVCDTO(
                        UserRegistrationRequest.from((Map<String, String>) raw.get("vc")),
                        true
                )
        );
      } else {
        return ResponseEntity.ok().body(new NoVCDTO());
      }
    } catch (Exception e) {
      return ResponseEntity
              .badRequest()
              .body(
                      new ErrorDto(
                              "/api/onboarding/register/user/vc",
                              String.format("Exception during external call: %s", e.getMessage())
                      )
              );
    }
  }

  @PostMapping("/vc/request/{uniqueId}")
  public ResponseEntity<?> createVCRequest(@PathVariable String uniqueId) {
    // Indeed, VC request for FR approval was already created when email was confirmed
    // That is because we can't ensure stable network connection and the following might happen:
    // user clicked email link for email confirmation, but for some reason can't click Submit
    // button on next screen. The problem is that email was already confirmed and next attempt
    // to click the same link in email will fail.
    log.info("In /vc/request/{}, nothing to do", uniqueId);

    return ResponseEntity
            .status(HttpStatus.OK)
            .build();
  }
}
