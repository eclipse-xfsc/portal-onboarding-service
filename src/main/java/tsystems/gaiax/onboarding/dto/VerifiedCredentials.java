package tsystems.gaiax.onboarding.dto;

import lombok.ToString;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;

@ToString
@Value(staticConstructor = "of")
public class VerifiedCredentials {
    String email;
    String name;
    String phoneNumber;
    String streetAndNumber;
    String zip;
    String city;

  public static VerifiedCredentials from(Map<String, String> m) {
    return of(m.get("email"), m.get("name"), m.get("phoneNumber"), m.get("streetAndNumber"), m.get("zip"), m.get("city"));
  }

  public Map<String, String> map() {
    final Map<String, String> m = new HashMap<>();
    m.put("email", email);
    m.put("name", name);
    m.put("phoneNumber", phoneNumber);
    m.put("streetAndNumber", streetAndNumber);
    m.put("zip", zip);
    m.put("city", city);
    return m;
  }
}
