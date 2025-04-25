package tsystems.gaiax.onboarding.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class OrganizationDetailsRq {
  @JsonProperty(value = "name", required = true)
  @ApiModelProperty(value = "Name", example = "Google Inc.", required = true)
  private String name;
  @JsonProperty(value = "email", required = true)
  @ApiModelProperty(value = "E-Mail", example = "email@company.com", required = true)
  private String email;
  @JsonProperty(value = "aisbl", required = true)
  @ApiModelProperty(value = "aisbl", example = "true", required = true)
  private boolean aisbl;
}
