package tsystems.gaiax.onboarding.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
public class NoVCDTO implements VCDTO {
  @JsonProperty("first_did")
  @NonNull boolean firstDID = false;
}