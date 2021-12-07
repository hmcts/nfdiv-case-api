package uk.gov.hmcts.divorce;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.model.UserRole;
import uk.gov.hmcts.ccd.definition.store.rest.endpoint.UserRoleController;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class LibTest {

  @Autowired
  UserRoleController roleController;

  @Autowired
  MockMvc mockMvc;

  @MockBean
  IdamApi idamApi;

  // Mocking this out stops the s2s request interceptor getting added, bypassing service auth.
  @MockBean
  private WebMvcConfig webMvcConfig;

  @BeforeEach
  void setup() {
    when(idamApi.retrieveUserInfo(anyString())).thenReturn(UserInfo.builder()
        .uid("1")
        .givenName("A")
        .familyName("Person")
        .roles(List.of())
        .build());
  }

  @Test
  void contextLoads() throws Exception {
    createRoles(
        "caseworker-divorce-courtadmin_beta",
        "caseworker-divorce-superuser",
        "caseworker-divorce-courtadmin-la",
        "caseworker-divorce-courtadmin",
        "caseworker-divorce-solicitor",
        "caseworker-divorce-pcqextractor",
        "caseworker-divorce-systemupdate",
        "caseworker-divorce-bulkscan",
        "caseworker-caa",
        "citizen"
    );

    mockMvc.perform(multipart("/import").file(loadNFDivDef())
            .with(
                jwt().authorities(new SimpleGrantedAuthority("caseworker-divorce-solicitor")))
        )
        .andExpect(status().is2xxSuccessful());

    StartEventResponse
        startEventResponse = startEventForCreateCase();

    CaseDataContent caseDataContent = CaseDataContent.builder()
        .eventToken(startEventResponse.getToken())
        .event(Event.builder()
            .id("solicitor-create-application")
            .summary("Create draft case")
            .description("Create draft case for functional tests")
            .build())
        .data(Map.of(
            "applicant1SolicitorName", "functional test",
            "applicant1LanguagePreferenceWelsh", "NO",
            "divorceOrDissolution", "divorce",
            "applicant1FinancialOrder", "NO"
        ))
        .build();

    submitNewCase(caseDataContent);
  }

  private StartEventResponse startEventForCreateCase() throws Exception {
    MvcResult result = mockMvc.perform(
            get("/caseworkers/ham/jurisdictions/DIVORCE/case-types/NFD/event-triggers/solicitor-create-application/token")
                .with(jwt().authorities(new SimpleGrantedAuthority("caseworker-divorce-solicitor")))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful())
        .andReturn();
    return new ObjectMapper().readValue(result.getResponse().getContentAsString(), StartEventResponse.class);
  }

  private void submitNewCase(CaseDataContent caseDataContent) throws Exception {
    mockMvc.perform(
            post("/caseworkers/ham/jurisdictions/DIVORCE/case-types/NFD/cases")
                .with(jwt().authorities(new SimpleGrantedAuthority("caseworker-divorce-solicitor")))
                .content(new ObjectMapper().writeValueAsString(caseDataContent))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful())
        .andReturn();
  }

  void createRoles(String... roles) {
    for (String role : roles) {
      UserRole r = new UserRole();
      r.setRole(role);
      r.setSecurityClassification(SecurityClassification.PUBLIC);
      roleController.userRolePut(r);
    }
  }

  MockMultipartFile loadNFDivDef() throws IOException {
    return new MockMultipartFile(
        "file",
        "NFD-dev.xlsx",
        MediaType.MULTIPART_FORM_DATA_VALUE,
        getClass().getClassLoader().getResourceAsStream("NFD-dev.xlsx").readAllBytes()
    );
  }
}
