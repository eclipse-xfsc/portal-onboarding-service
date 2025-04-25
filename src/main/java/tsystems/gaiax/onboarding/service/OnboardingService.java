package tsystems.gaiax.onboarding.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tsystems.gaiax.onboarding.entities.*;
import tsystems.gaiax.onboarding.model.OrganizationDetailsRq;
import tsystems.gaiax.onboarding.model.OrganizationWithVCDTO;
import tsystems.gaiax.onboarding.model.UserRegistrationRequest;
import tsystems.gaiax.onboarding.repo.FrRequestAttachmentDao;
import tsystems.gaiax.onboarding.repo.FrRequestDao;
import tsystems.gaiax.onboarding.repo.FrRequestStatusDao;
import tsystems.gaiax.onboarding.repo.FrRequestTypeDao;
import tsystems.gaiax.onboarding.util.ValidationUtil;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service
public class OnboardingService {
  @Autowired
  private EmailSendingService emailSendingService;

  @Autowired
  private FrRequestDao frRequestDao;
  @Autowired
  private
  FrRequestAttachmentDao frRequestAttachmentDao;

  @Autowired
  private
  FrRequestTypeDao frRequestTypeDao;

  @Autowired
  private
  FrRequestStatusDao frRequestStatusDao;

  FrRequestTypeEntity VC_NP_NOT_CONFIRMED, VC_NP, VC_PPR_NOT_CONFIRMED,
          VC_PPR, PR_PPR, SD_SERVICE, SD_DATA, SD_NODE, SD_PPR;

  FrRequestStatusEntity PUBLISHED, NOTIFIED;

  @PostConstruct
  public void initConstants() {
    VC_NP_NOT_CONFIRMED = frRequestTypeDao.findByName(FrRequestType.VC_NP_NOT_CONFIRMED.name()).orElseThrow();
    VC_NP = frRequestTypeDao.findByName(FrRequestType.VC_NP.name()).orElseThrow();
    VC_PPR_NOT_CONFIRMED = frRequestTypeDao.findByName(FrRequestType.VC_PPR_NOT_CONFIRMED.name()).orElseThrow();
    VC_PPR = frRequestTypeDao.findByName(FrRequestType.VC_PPR.name()).orElseThrow();
    PR_PPR = frRequestTypeDao.findByName(FrRequestType.PR_PPR.name()).orElseThrow();
    SD_SERVICE = frRequestTypeDao.findByName(FrRequestType.SD_SERVICE.name()).orElseThrow();
    SD_DATA = frRequestTypeDao.findByName(FrRequestType.SD_DATA.name()).orElseThrow();
    SD_NODE = frRequestTypeDao.findByName(FrRequestType.SD_NODE.name()).orElseThrow();
    SD_PPR = frRequestTypeDao.findByName(FrRequestType.SD_PPR.name()).orElseThrow();
    PUBLISHED = frRequestStatusDao.findByName(FrRequestStatus.PUBLISHED.name()).orElseThrow();
    NOTIFIED = frRequestStatusDao.findByName(FrRequestStatus.NOTIFIED.name()).orElseThrow();
  }

  @Transactional
  public boolean isEmailForVCPPRRequestAlreadyConfirmed(final String email) {
    return frRequestDao.findByEmailAndRequestType(email, VC_PPR).isPresent();
  }

  @Transactional
  public boolean isEmailForVCNPRequestAlreadyConfirmed(final String email) {
    return frRequestDao.findByEmailAndRequestType(email, VC_NP).isPresent();
  }

  @Transactional
  public void createVCPPRConfirmedRequest(final String email) {
    final FrRequest frRequest =
            frRequestDao.findByEmailAndRequestType(email, VC_PPR_NOT_CONFIRMED)
                        .orElseThrow(() -> new RuntimeException("no such pending request found"));
    frRequest.setRequestType(VC_PPR);
    frRequestDao.save(frRequest);
  }

  @Transactional
  public void createVCNPConfirmedRequest(final String email) {
    final FrRequest frRequest =
            frRequestDao.findByEmailAndRequestType(email, VC_NP_NOT_CONFIRMED)
                        .orElseThrow(() -> new RuntimeException("no such pending request found"));
    frRequest.setRequestType(VC_NP);
    frRequestDao.save(frRequest);
  }


  @Transactional
  public void createVCPPRNotConfirmedRequest(
          OrganizationDetailsRq data,
          List<MultipartFile> documents
  ) {
    final FrRequest frRequest = new FrRequest();
    frRequest.setRequestDate(Date.valueOf(LocalDate.now()));
    frRequest.setRequestType(VC_PPR_NOT_CONFIRMED);
    frRequest.setEmail(data.getEmail());
    frRequest.setLocation("Unspecified");
    frRequest.setParticipantName(data.getName());

    final Map<String, String> detailsData = new HashMap<>();
    detailsData.put("prName", data.getName());
    detailsData.put("AISBL", String.valueOf(data.isAisbl()));
    final JSONDetails details = new JSONDetails(detailsData);
    frRequest.setDetails(details);

    final List<FrRequestAttachment> attachments = documents.stream().map(
            a -> {
              try {
                final FrRequestAttachment frA = new FrRequestAttachment();
                frA.setFileName(a.getOriginalFilename());
                frA.setFileData(a.getBytes());
                frA.setFrRequest(frRequest);
                return frA;
              } catch (IOException e) {
                throw new RuntimeException("Can't process attached file");
              }
            }
    ).collect(Collectors.toList());
    frRequest.setFrRequestAttachments(attachments);

    frRequestDao.save(frRequest);

    log.info("sending email for email address verification");
    try {
      emailSendingService.sendEmailRegistrationMessage(data.getEmail(), "organization");
    } catch (Exception e) {
      throw new RuntimeException("Can't send confirmation email, registration failed: " + e.getMessage());
    }
  }

  @Transactional
  public void createVCNPNotConfirmedRequest(
          final UserRegistrationRequest data
  ) {
    final FrRequest frRequest = new FrRequest();
    frRequest.setRequestDate(Date.valueOf(LocalDate.now()));
    frRequest.setRequestType(VC_NP_NOT_CONFIRMED);
    frRequest.setEmail(data.getEmail());
    frRequest.setLocation(data.getCountry());
    final String prName = String.format("%s %s", data.getFirstname(), data.getLastname());
    frRequest.setParticipantName(prName);

    final Map<String, String> detailsData = new HashMap<>();
    detailsData.put("prName", prName);
    detailsData.put("conf_email", data.getEmail());
    detailsData.put("phone", data.getPhone());
    detailsData.put("zip", data.getZip());
    detailsData.put("country", data.getCountry());
    detailsData.put("city", data.getCity());
    detailsData.put("address", data.getAddress());

    final JSONDetails details = new JSONDetails(detailsData);
    frRequest.setDetails(details);

    frRequestDao.save(frRequest);


    log.info("sending email for email address verification");
    try {
      emailSendingService.sendEmailRegistrationMessage(data.getEmail(), "user");
    } catch (Exception e) {
      throw new RuntimeException("Can't send confirmation email, registration failed: " + e.getMessage());
    }
  }

  public String getEmailFromUniqueId(String uniqueId) {
    return ValidationUtil.getEmailFromId(uniqueId);
  }

  public boolean isAnyOnboardingPendingRequestExist(final String email) {
    return frRequestDao.findByEmailAndRequestType(email, VC_PPR_NOT_CONFIRMED).isPresent() ||
            frRequestDao.findByEmailAndRequestType(email, VC_PPR).isPresent() ||
            isPRPPRPendingRequestExist(email) ||
            frRequestDao.findByEmailAndRequestType(email, VC_NP_NOT_CONFIRMED).isPresent() ||
            frRequestDao.findByEmailAndRequestType(email, VC_NP).isPresent();
  }

  private boolean isPRPPRPendingRequestExist(final String email) {
    return frRequestDao.findByEmailAndRequestType(email, PR_PPR).isPresent();
  }

  @Transactional
  public void createPRRequest(final OrganizationWithVCDTO pprVC, final String emailForUpdates) {
    log.info("Storing PR_PPR request");
    // Check pending PR PPR request
    // Probably a better way also do not allow onboarding
    // of already onboarded participant. But RS has nothing about it
    if (isPRPPRPendingRequestExist(pprVC.getVc().getEmail())) {
      throw new RuntimeException("request with such email already exists");
    }

    final FrRequest frRequest = new FrRequest();
    frRequest.setRequestDate(Date.valueOf(LocalDate.now()));
    frRequest.setRequestType(PR_PPR);
    frRequest.setEmail(pprVC.getVc().getEmail());
    frRequest.setLocation(pprVC.getVc().getCountry());
    frRequest.setParticipantName(pprVC.getVc().getName());

    final Map<String, String> detailsData = new HashMap<>();
    detailsData.put("prName", pprVC.getVc().getName());
    detailsData.put("conf_email", emailForUpdates);
    detailsData.put("phone_number", pprVC.getVc().getPhoneNumber());
    detailsData.put("street_number", pprVC.getVc().getStreetAndNumber());
    detailsData.put("zip", pprVC.getVc().getZip());
    detailsData.put("city", pprVC.getVc().getCity());
    detailsData.put("country", pprVC.getVc().getCountry());
    detailsData.put("email", pprVC.getVc().getEmail());
    final JSONDetails details = new JSONDetails(detailsData);
    frRequest.setDetails(details);

    frRequestDao.save(frRequest);
  }
}
