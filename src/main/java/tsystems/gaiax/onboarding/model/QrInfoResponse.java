package tsystems.gaiax.onboarding.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class QrInfoResponse {
  @NonNull private String pollUrl;
  @NonNull private String qrCodePath;
  @NonNull private String walletLink;


  public static QrInfoResponse from(Map<String, String> m) {
    return new QrInfoResponse(
            m.get("pollUrl"),
            m.get("qrCodePath"),
            m.get("walletLink"));
  }

//  public Map<String, String> map() {
//    final Map<String, String> m = new HashMap<>();
//    m.put("qrCodePath", qrCodePath);
//    m.put("walletLink", walletLink);
//
//    return m;
//  }
}
