package tsystems.gaiax.onboarding.model;

import lombok.*;
import org.springframework.web.bind.annotation.GetMapping;

@AllArgsConstructor
@Getter
@Setter
@ToString
@NoArgsConstructor
public class IDP {
  @NonNull private String logoUrl;
  @NonNull private String name;
  @NonNull private String link;
}
