package tsystems.gaiax.onboarding.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.extern.log4j.Log4j2;

import java.io.Serializable;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Log4j2
@ToString
public class OrganizationRegistrationVC implements Serializable {

  @JsonProperty(value = "name", required = true)
  @ApiModelProperty(value = "Name", example = "Google Inc.", required = true)
  private String name;

  @JsonProperty(value = "email", required = true)
  @ApiModelProperty(value = "E-Mail", example = "google@google.com", required = true)
  private String email;

  @JsonProperty(value = "aisbl", required = true)
  @ApiModelProperty(value = "A flag \"Apply for AISBL membership\"", example = "true", required = true)
  private boolean aisbl;

  @JsonProperty(value = "phone_number", required = true)
  @ApiModelProperty(value = "Phone number", example = "11223344", required = true)
  private String phoneNumber;

  @JsonProperty(value = "street_number", required = true)
  @ApiModelProperty(value = "Street and Number", example = "Wall str. 12/3", required = true)
  private String streetAndNumber;

  @JsonProperty(value = "zip", required = true)
  @ApiModelProperty(value = "ZIP", example = "334455", required = true)
  private String zip;

  @JsonProperty(value = "city", required = true)
  @ApiModelProperty(value = "City", example = "Berlin", required = true)
  private String city;

  @JsonProperty(value = "country", required = true)
  @ApiModelProperty(value = "Country", example = "Germany", required = true)
  private String country;

  public static OrganizationRegistrationVC from(final Map<String, ?> raw) {
    return new OrganizationRegistrationVC(
            (String) raw.get("name"),
            (String) raw.get("email"),
            (Boolean) raw.get("aisbl"),
            (String) raw.get("phone_number"),
            (String) raw.get("street_number"),
            (String) raw.get("zip"),
            (String) raw.get("city"),
            (String) raw.get("country")

    );
  }


}
