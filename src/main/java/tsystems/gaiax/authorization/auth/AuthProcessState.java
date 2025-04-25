package tsystems.gaiax.authorization.auth;

import lombok.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@SuppressWarnings("unused")
@Component
@SessionScope
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AuthProcessState {
  @Getter
  @Setter
  private String deviceCode;
}
