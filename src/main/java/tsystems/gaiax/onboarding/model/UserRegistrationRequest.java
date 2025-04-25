package tsystems.gaiax.onboarding.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.Map;

/**
 * ...
 */
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserRegistrationRequest implements Serializable {
  @JsonProperty(value = "first_name")
  @NonNull String firstname;
  @JsonProperty(value = "last_name")
  @NonNull String lastname;
  @NonNull String email;
  @NonNull String phone;
  @NonNull String address;
  @JsonProperty(value = "zip_code")
  @NonNull String zip;
  @NonNull String city;
  @NonNull String country;

  public static UserRegistrationRequest from(final Map<String, String> raw) {
    return new UserRegistrationRequest(
            raw.get("first_name"),
            raw.get("last_name"),
            raw.get("email"),
            raw.get("phone"),
            raw.get("address"),
            raw.get("zip_code"),
            raw.get("city"),
            raw.get("country")

    );
  }

}
