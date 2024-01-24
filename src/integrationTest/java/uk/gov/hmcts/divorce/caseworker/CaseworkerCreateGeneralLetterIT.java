package uk.gov.hmcts.divorce.caseworker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetter;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetterDetails;
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;
import uk.gov.hmcts.divorce.testutil.ClockTestUtil;
import uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.Clock;
import java.time.LocalDate;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGeneralLetter.CASEWORKER_CREATE_GENERAL_LETTER;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.GENERAL_LETTER;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssemblyUnauthorized;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssemblyWith;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.CASEWORKER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.SYSTEM_USER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamToken;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.buildCaseDataWithGeneralLetter;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
@ContextConfiguration(initializers = {
    DocAssemblyWireMock.PropertiesInitializer.class,
    IdamWireMock.PropertiesInitializer.class
})
public class CaseworkerCreateGeneralLetterIT {

    private static final LocalDate DATE = LocalDate.of(2021, 6, 17);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private DocumentIdProvider documentIdProvider;

    @MockBean
    private BulkPrintService bulkPrintService;

    @MockBean
    private LetterPrinter letterPrinter;

    @MockBean
    private Clock clock;

    @BeforeAll
    static void setUp() {
        DocAssemblyWireMock.start();
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        DocAssemblyWireMock.stopAndReset();
        IdamWireMock.stopAndReset();
    }

    @BeforeEach
    void setClock() {
        ClockTestUtil.setMockClock(clock, DATE);
    }

    @Test
    public void shouldProcessGeneralLetterDocumentsForApplicantAndUpdateCaseDataWhenAddressedToApplicant() throws Exception {
        final CaseData caseData = buildCaseDataWithGeneralLetter(APPLICANT);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("123456789");

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", "NFD_General_Letter.docx");

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            CASEWORKER_CREATE_GENERAL_LETTER)
                    )
                )
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .isEqualTo(json(expectedResponse("classpath:caseworker-general-letter-response.json")));
    }

    @Test
    public void shouldProcessGeneralLetterDocumentsForConfidentialApplicantAndUpdateCaseDataWhenAddressedToApplicant() throws Exception {
        final CaseData caseData = buildCaseDataWithGeneralLetter(APPLICANT);
        caseData.getApplicant1().setContactDetailsType(ContactDetailsType.PRIVATE);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("123456789");

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", "NFD_General_Letter.docx");

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            CASEWORKER_CREATE_GENERAL_LETTER)
                    )
                )
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .isEqualTo(json(expectedResponse("classpath:caseworker-general-letter-confidential-response.json")));

    }

    @Test
    public void shouldSendProcessedGeneralLetterDocumentsForApplicant() throws Exception {
        final CaseData caseData = buildCaseDataWithGeneralLetter(APPLICANT);

        final ListValue<GeneralLetterDetails> doc1 = ListValue.<GeneralLetterDetails>builder()
                .value(GeneralLetterDetails.builder()
                    .generalLetterLink(Document.builder().build())
                    .generalLetterParties(APPLICANT)
                    .build())
                .build();

        caseData.setGeneralLetters(Lists.newArrayList(doc1));

        ListValue<DivorceDocument> generalLetterDivorceDocument = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(GENERAL_LETTER)
                .build())
            .build();

        caseData.setDocuments(CaseDocuments.builder()
            .documentsGenerated(Lists.newArrayList(generalLetterDivorceDocument))
            .build());

        caseData.setGeneralLetter(GeneralLetter
            .builder()
            .generalLetterParties(APPLICANT)
            .build()
        );

        String response = mockMvc.perform(post(SUBMITTED_URL)
                        .contentType(APPLICATION_JSON)
                        .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                        .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                        .content(objectMapper.writeValueAsString(
                                        callbackRequest(
                                                caseData,
                                                CASEWORKER_CREATE_GENERAL_LETTER)
                                )
                        )
                        .accept(APPLICATION_JSON))
                .andExpect(
                        status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(letterPrinter).sendLetters(
            any(CaseData.class),
            anyLong(),
            any(Applicant.class),
            any(DocumentPackInfo.class),
            any(String.class));
    }

    @Test
    void shouldReturn401UnauthorizedWhenAboutToSubmitCallbackIsInvokedAndAuthorizationFailsForDocAssembly() throws Exception {
        final CaseData caseData = buildCaseDataWithGeneralLetter(APPLICANT);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        stubForDocAssemblyUnauthorized();

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            CASEWORKER_CREATE_GENERAL_LETTER)
                    )
                )
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isUnauthorized()
            );
    }
}
