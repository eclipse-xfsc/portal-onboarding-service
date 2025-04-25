package tsystems.gaiax.onboarding.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class OrganizationWithVCDTO implements VCDTO {
  OrganizationRegistrationVC vc;
  @JsonProperty("first_did")
  @NonNull Boolean firstDID;
}
