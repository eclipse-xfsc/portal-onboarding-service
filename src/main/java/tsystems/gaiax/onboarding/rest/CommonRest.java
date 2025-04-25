package tsystems.gaiax.onboarding.rest;

import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import tsystems.gaiax.onboarding.model.IDP;
import tsystems.gaiax.onboarding.model.QrInfoResponse;
import tsystems.gaiax.onboarding.util.ProxyCall;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@Log4j2
@RequestMapping("/api/onboarding")
public class CommonRest {
  @Autowired
  @Qualifier("ocmSrv")
  private WebClient ocmSrv;

  @GetMapping("/idp")
  public List<IDP> idpList(HttpServletRequest request) {
    return ProxyCall.<List<IDP>>doGet(ocmSrv, request).getBody();
  }

  @GetMapping("/qr")
  public QrInfoResponse qrFlow(HttpServletRequest request) {
    return QrInfoResponse.from(ProxyCall.<Map<String, String>>doGet(ocmSrv, request).getBody());
  }

}
