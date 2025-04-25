package tsystems.gaiax.onboarding;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tsystems.gaiax.onboarding.rest.OrganizationRegistrationRest;
import tsystems.gaiax.onboarding.service.OnboardingService;
import tsystems.gaiax.onboarding.util.ValidationUtil;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.jpa.database: POSTGRESQL"
})
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
public class OrganizationRegistrationRestTest {
    @MockBean
    private OrganizationRegistrationRest service;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private OnboardingService onboardingService;

    @Test
    public void addOrganizationRequest() throws Exception {
        ResponseEntity rs = ResponseEntity.ok().build();
        MockMultipartFile firstFile = new MockMultipartFile("file", "filename.txt", "text/plain", "some xml".getBytes());
        when(service.addOrganizationRequest(anyString(), anyString(), anyBoolean(), any())).thenReturn(rs);
        mockMvc.perform(
                        MockMvcRequestBuilders
                                .multipart("/api/onboarding/register/organization?name=aaa&email=aaa@aaa.aa&aisbl=false")
                                .file(firstFile))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void confirmEmail() throws Exception {
        ResponseEntity rs = ResponseEntity.ok().build();
        when(service.confirmEmail(anyString())).thenReturn(rs);
        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post("/api/onboarding/register/organization/confirm_email/12"))
                .andExpect(status().isOk());
    }

    @Test
    public void requestVC() throws Exception {
        ResponseEntity rs = ResponseEntity.ok().build();
        when(service.requestVC(any())).thenReturn(rs);
        mockMvc.perform(
                        MockMvcRequestBuilders
                                .get("/api/onboarding/register/organization/vc"))
                .andExpect(status().isOk());
    }

    @Test
    public void pr() throws Exception {
        ResponseEntity rs = ResponseEntity.ok().build();
        when(service.pr(any(), anyString())).thenReturn(rs);
        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post("/api/onboarding/register/organization/pr?email=aaa@aa.aa"))
                .andExpect(status().isOk());
    }

    @Test
    public void emailValidationPositiveTest() {
        Assertions.assertTrue(ValidationUtil.validateEmail("aaa@aaa.com"));
    }

    @Test
    public void emailValidationNegativeTest1() {
        Assertions.assertFalse(ValidationUtil.validateEmail("asdf@"));
    }

    @Test
    public void emailValidationNegativeTest2() {
        Assertions.assertFalse(ValidationUtil.validateEmail("asdf@asdf"));
    }

    @Test
    public void emailValidationNegativeTest3() {
        Assertions.assertFalse(ValidationUtil.validateEmail("asdf@asdf."));
    }
}
