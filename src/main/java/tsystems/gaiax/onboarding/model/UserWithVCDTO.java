package tsystems.gaiax.onboarding.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserWithVCDTO implements VCDTO {
  UserRegistrationRequest vc;
  @JsonProperty("first_did")
  @NonNull Boolean firstDID;
}
