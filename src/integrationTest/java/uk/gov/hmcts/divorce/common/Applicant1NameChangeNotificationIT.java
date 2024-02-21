package uk.gov.hmcts.divorce.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateApplicantName.CASEWORKER_UPDATE_APPLICANT_NAME;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_NAME_UPDATED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.*;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
@ContextConfiguration(initializers = {
    IdamWireMock.PropertiesInitializer.class})
public class Applicant1NameChangeNotificationIT {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private WebMvcConfig webMvcConfig;

    private static CaseDetails<CaseData, State> caseData = new CaseDetails<>();

    @BeforeAll
    static void setUp() {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @Test
    public void shouldSendNotificationToApplicant1OnApplicantNameUpdateCompletion() throws Exception{

        CaseData data = validApplicant1CaseData();

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, CASEWORKER_UPDATE_APPLICANT_NAME)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(APPLICANT_NAME_UPDATED), anyMap(), eq(ENGLISH), anyLong());
        verifyNoMoreInteractions(notificationService);
    }
}
