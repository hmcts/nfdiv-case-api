package uk.gov.hmcts.divorce.solicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.ClaimsCostFrom;
import uk.gov.hmcts.divorce.common.model.Court;
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock;
import uk.gov.hmcts.divorce.testutil.DocManagementStoreWireMock;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.Set;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorUpdate.SOLICITOR_UPDATE;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssembly;
import static uk.gov.hmcts.divorce.testutil.DocManagementStoreWireMock.stubForDocumentManagement;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.SOLICITOR_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SOLICITOR_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationContactInformation;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
    DocAssemblyWireMock.PropertiesInitializer.class,
    IdamWireMock.PropertiesInitializer.class,
    DocManagementStoreWireMock.PropertiesInitializer.class})
public class SolicitorUpdateTest {

    private static final String SOLICITOR_UPDATE_ABOUT_TO_SUBMIT = "classpath:solicitor-update-about-to-submit-response.json";
    private static final String DOCUMENT_URL =
        "http://dm-store-aat.service.core-compute-aat.internal/documents/8d2bd0f2-80e9-4b0f-b38d-2c138b243e27";

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

    @BeforeAll
    static void setUp() {
        DocAssemblyWireMock.start();
        IdamWireMock.start();
        DocManagementStoreWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        DocAssemblyWireMock.stopAndReset();
        IdamWireMock.stopAndReset();
        DocManagementStoreWireMock.stopAndReset();
    }

    @Test
    void givenValidCaseDataWithNoDraftDocumentWhenAboutToSubmitCallbackIsInvokedCaseDataIsSetCorrectly() throws Exception {

        when(serviceTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(documentIdProvider.documentId()).thenReturn("V2");

        stubForDocAssembly();

        final var jsonStringResponse = mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseDataWithNoDocument(), SOLICITOR_UPDATE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertEquals(jsonStringResponse, expectedResponse(SOLICITOR_UPDATE_ABOUT_TO_SUBMIT), STRICT);
    }

    @Test
    void givenValidCaseDataWithDraftDocumentWhenAboutToSubmitCallbackIsInvokedCaseDataIsSetCorrectly() throws Exception {

        when(serviceTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(documentIdProvider.documentId()).thenReturn("V2");

        stubForDocAssembly();
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, SOLICITOR_USER_ID, SOLICITOR_ROLE);

        final var documentUuid = FilenameUtils.getName(DOCUMENT_URL);
        stubForDocumentManagement(documentUuid, OK);

        when(serviceTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        final var jsonStringResponse = mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseDataWithDocument(), SOLICITOR_UPDATE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertEquals(jsonStringResponse, expectedResponse(SOLICITOR_UPDATE_ABOUT_TO_SUBMIT), STRICT);
    }

    private CaseData caseDataWithNoDocument() {
        return CaseData
            .builder()
            .applicant1FirstName(TEST_FIRST_NAME)
            .applicant1LastName(TEST_LAST_NAME)
            .applicant1Email(TEST_USER_EMAIL)
            .divorceOrDissolution(DIVORCE)
            .divorceCostsClaim(YES)
            .financialOrder(NO)
            .languagePreferenceWelsh(NO)
            .divorceClaimFrom(Set.of(ClaimsCostFrom.APPLICANT_2))
            .divorceUnit(Court.SERVICE_CENTRE)
            .selectedDivorceCentreSiteId("AA07")
            .applicant2OrgContactInformation(organisationContactInformation())
            .build();
    }

    private CaseData caseDataWithDocument() {
        final CaseData caseDate = caseDataWithNoDocument();

        Document ccdDocument = new Document(
            "http://dm-store-aat.service.core-compute-aat.internal/documents/8d2bd0f2-80e9-4b0f-b38d-2c138b243e27",
            "draft-mini-application-1616591401473378.pdf",
            "http://dm-store-aat.service.core-compute-aat.internal/documents/8d2bd0f2-80e9-4b0f-b38d-2c138b243e27/binary"
        );

        DivorceDocument divorceDocument = DivorceDocument
            .builder()
            .documentLink(ccdDocument)
            .documentFileName("draft-mini-application-1616591401473378.pdf")
            .documentType(DIVORCE_APPLICATION)
            .build();

        ListValue<DivorceDocument> divorceDocumentListValue = ListValue
            .<DivorceDocument>builder()
            .id(DIVORCE_APPLICATION.getLabel())
            .value(divorceDocument)
            .build();

        caseDate.setDocumentsGenerated(singletonList(divorceDocumentListValue));
        return caseDate;
    }
}
