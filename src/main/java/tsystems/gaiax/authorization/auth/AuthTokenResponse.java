package tsystems.gaiax.authorization.auth;

import lombok.Value;
import tsystems.gaiax.onboarding.common.CallStatus;

import java.util.Map;

@SuppressWarnings("unused")
@Value(staticConstructor = "of")
public class AuthTokenResponse {
  String state;
  String accessToken;
  CallStatus status;

  public static AuthTokenResponse from(Map<String, String> m) {
    return of(m.get("state"), m.get("accessToken"), CallStatus.valueOf(m.get("status")));
  }
}
