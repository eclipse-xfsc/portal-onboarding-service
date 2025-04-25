package tsystems.gaiax.onboarding.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import tsystems.gaiax.onboarding.dto.VerifiedCredentials;
import tsystems.gaiax.onboarding.model.*;
import tsystems.gaiax.onboarding.service.OnboardingService;
import tsystems.gaiax.onboarding.util.ProxyCall;
import tsystems.gaiax.onboarding.util.ValidationUtil;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Log4j2
@RequestMapping("/api/onboarding/register/organization")
@Api(tags = "Organization Registration Service")
public class OrganizationRegistrationRest {

  @Autowired
  @Qualifier("ocmSrv")
  private WebClient ocmSrv;

  @Autowired
  private OnboardingService onboardingService;

  @Value("${services.portal.uri.external}")
  private String portalURIExt;

  @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<?> addOrganizationRequest(
          @RequestParam String name,
          @RequestParam String email,
          @RequestParam boolean aisbl,
          @RequestPart("documents") List<MultipartFile> documents
  ) {
    log.info("validating email");
    boolean validEmail = ValidationUtil.validateEmail(email);
    if (!validEmail) {
      return ResponseEntity.badRequest().body(
              new ErrorDto(
                      "api/onboarding/register/organization",
                      "not valid email"
              )
      );
    }
    final OrganizationDetailsRq data = new OrganizationDetailsRq(name, email, aisbl);

    log.info("check if pending request with such email exists");
    if (onboardingService.isAnyOnboardingPendingRequestExist(data.getEmail())) {
      return ResponseEntity.badRequest().body(
              new ErrorDto(
                      "api/onboarding/register/organization",
                      "request with such email already exists"
              )
      );
    }

    log.info("storing request to DB and inform the user atomically");
    try {
      onboardingService.createVCPPRNotConfirmedRequest(data, documents);

      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(new ErrorDto("api/onboarding/register/organization",
              e.getMessage()));
    }
  }

  @PostMapping("/confirm_email/{id}")
  @ApiOperation("Confirm email address using link from email (P.21 in documentation)")
  public ResponseEntity<?> confirmEmail(@PathVariable("id") String uniqueId) {
    log.debug("get email from uniqueID");
    final String email = onboardingService.getEmailFromUniqueId(uniqueId);
    log.debug("Check that email was already confirmed during current onboarding, email: {}", email);
    if (onboardingService.isEmailForVCPPRRequestAlreadyConfirmed(email)) {
      log.info("Email already confirmed, set redirection");
      return ResponseEntity.status(HttpStatus.FOUND).location(
              URI.create(portalURIExt + "/onboarding/email_already_confirmed")
      ).build();
    }

    log.info("Convert pending request to real request");
    try {
      onboardingService.createVCPPRConfirmedRequest(email);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(
              new ErrorDto(
                      "/api/onboarding/register/organization/confirm_email/{id}",
                      e.getMessage()
              )
      );
    }

    Map<String, String> res = new HashMap<>();
    res.put("id", uniqueId);
    return ResponseEntity.ok(res);
  }

  @GetMapping("/vc")
  @ApiOperation("Provider VC Details")
  public ResponseEntity requestVC(
          HttpServletRequest request
  ) {
    log.info("we should already have JWT because user scanned QR code and was authorized");
    final String jwt = request.getHeader("Authorization");
    if (jwt == null || jwt.isBlank()) {
      log.info("no JWT provided");
      return ResponseEntity
              .status(HttpStatus.UNAUTHORIZED)
              .body(
                      new ErrorDto(
                              "/api/onboarding/register/organization/vc",
                              "Not authorized")
              );
    }

    log.info("Requesting OCM");

    try {
      final ResponseEntity<Map<String, ?>> extRespEntity = ProxyCall.<Map<String, ?>>doGet(ocmSrv, request);
      final Map<String, ?> raw = extRespEntity.getBody();

      if ((Boolean) raw.get("first_did")) {
        return ResponseEntity.ok().body(
                new OrganizationWithVCDTO(
                        OrganizationRegistrationVC.from((Map<String, ?>) raw.get("vc")),
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
                              "/api/onboarding/register/organization/vc",
                              String.format("Exception during external call: %s", e.getMessage())
                      )
              );
    }
  }

  @PostMapping("/pr")
  @ApiOperation("PR request creation")
  public ResponseEntity<?> pr(HttpServletRequest request, @RequestParam("email") String email) {
    final String jwt = request.getHeader("Authorization");
    if (jwt == null || jwt.isBlank()) {
      log.error("Not authorized call");
      return ResponseEntity
              .status(HttpStatus.UNAUTHORIZED)
              .body(
                      new ErrorDto(
                              "/api/onboarding/register/organization/pr",
                              "Not authorized"
                      )
              );
    }
    log.info("validating email, email: {}", email);
    boolean validEmail = ValidationUtil.validateEmail(email);
    if (!validEmail) {
      return ResponseEntity.badRequest().body(
              new ErrorDto(
                      request.getContextPath(), "not valid email"));
    }

    try {
      // We are requesting VC data for PPR because user was already authorized for onboarding,
      // i.e. user has special kind of JWT
      log.info("Get VC for organization");
      final ResponseEntity<OrganizationWithVCDTO> extRespEntity =
              ocmSrv.get()
                    .uri(
                            builder ->
                                    builder.path("/api/onboarding/register/organization/vc").build()
                    )
                    .header("Authorization", jwt)
                    .retrieve()
                    .toEntity(OrganizationWithVCDTO.class)
                    .block();
      onboardingService.createPRRequest(extRespEntity.getBody(), email);

      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest()
                           .body(
                                   new ErrorDto(
                                           "/api/onboarding/register/organization/pr",
                                           e.getMessage()
                                   )
                           );
    }
  }
}
