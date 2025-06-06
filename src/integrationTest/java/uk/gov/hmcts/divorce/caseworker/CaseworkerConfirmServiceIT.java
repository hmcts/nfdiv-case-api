package uk.gov.hmcts.divorce.caseworker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod;
import uk.gov.hmcts.divorce.divorcecase.model.SolicitorService;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerConfirmService.CASEWORKER_CONFIRM_SERVICE;
import static uk.gov.hmcts.divorce.common.service.ConfirmService.DOCUMENTS_NOT_UPLOADED_ERROR;
import static uk.gov.hmcts.divorce.document.model.DocumentType.OTHER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.PrdOrganisationWireMock.start;
import static uk.gov.hmcts.divorce.testutil.PrdOrganisationWireMock.stopAndReset;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.documentWithType;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CaseworkerConfirmServiceIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private Clock clock;

    @BeforeAll
    static void setUp() {
        start();
    }

    @AfterAll
    static void tearDown() {
        stopAndReset();
    }

    @Test
    void shouldSetDueDateTo14DaysFromToday() throws Exception {

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        setMockClock(clock);
        final LocalDate serviceDate = getExpectedLocalDate();

        final SolicitorService solicitorService = SolicitorService.builder()
            .dateOfService(serviceDate)
            .build();

        final CaseData caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YesOrNo.YES);
        caseData.getApplication().setServiceMethod(ServiceMethod.SOLICITOR_SERVICE);
        caseData.getApplication().setIssueDate(serviceDate);
        caseData.getApplication().setSolicitorService(solicitorService);
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/about-to-submit?page=CaseworkerConfirmService")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, CASEWORKER_CONFIRM_SERVICE)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            )
            .andExpect(jsonPath("$.data.dueDate").value(serviceDate.plusDays(16).toString()));
    }

    @Test
    void shouldSetDueDateTo14DaysFromTodayAndAddAttachmentsToDocumentsUploadedList() throws Exception {

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        setMockClock(clock);
        final LocalDate serviceDate = getExpectedLocalDate();

        final SolicitorService solicitorService = SolicitorService.builder()
            .dateOfService(serviceDate)
            .build();

        final CaseData caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YesOrNo.YES);
        caseData.getApplication().setServiceMethod(ServiceMethod.SOLICITOR_SERVICE);
        caseData.getApplication().setIssueDate(serviceDate);
        caseData.getApplication().setSolicitorService(solicitorService);
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);

        final ListValue<DivorceDocument> confirmServiceAttachments = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentLink(new Document("url", "filename.pdf", "url/binary"))
                .build())
            .build();

        caseData.setDocuments(CaseDocuments.builder()
            .documentsUploaded(new ArrayList<>())
            .build());

        caseData.getDocuments().setDocumentsUploadedOnConfirmService(Lists.newArrayList(confirmServiceAttachments));

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/about-to-submit?page=CaseworkerConfirmService")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, CASEWORKER_CONFIRM_SERVICE)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            )
            .andExpect(jsonPath("$.data.dueDate").value(serviceDate.plusDays(16).toString()))
            .andExpect(
                jsonPath("$.data.documentsUploaded[0].value.documentLink.document_url").value("url"))
            .andExpect(
                jsonPath("$.data.documentsUploaded[0].value.documentLink.document_filename").value("filename.pdf"))
            .andExpect(
                jsonPath("$.data.documentsUploaded[0].value.documentLink.document_binary_url").value("url/binary"));
    }

    @Test
    void shouldThrowErrorWhenServiceProcessedByProcessServerAndDocumentsNotAttached() throws Exception {

        setMockClock(clock);

        final LocalDate serviceDate = getExpectedLocalDate();

        final SolicitorService solicitorService = SolicitorService.builder()
            .dateOfService(serviceDate)
            .serviceProcessedByProcessServer(Set.of(SolicitorService.ServiceProcessedByProcessServer.CONFIRM))
            .build();

        final CaseData caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YesOrNo.YES);
        caseData.getApplication().setServiceMethod(ServiceMethod.SOLICITOR_SERVICE);
        caseData.getApplication().setIssueDate(serviceDate);
        caseData.getApplication().setSolicitorService(solicitorService);

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/mid-event?page=CaseworkerConfirmService")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, CASEWORKER_CONFIRM_SERVICE)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            )
            .andExpect(jsonPath("$.errors").value(DOCUMENTS_NOT_UPLOADED_ERROR));
    }

    @Test
    void shouldNotThrowErrorWhenServiceProcessedByProcessServerAndDocumentsAreAttached() throws Exception {

        setMockClock(clock);

        final LocalDate serviceDate = getExpectedLocalDate();

        final SolicitorService solicitorService = SolicitorService.builder()
            .dateOfService(serviceDate)
            .serviceProcessedByProcessServer(Set.of(SolicitorService.ServiceProcessedByProcessServer.CONFIRM))
            .build();

        final CaseData caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YesOrNo.YES);
        caseData.getApplication().setServiceMethod(ServiceMethod.SOLICITOR_SERVICE);
        caseData.getApplication().setIssueDate(serviceDate);
        caseData.getApplication().setSolicitorService(solicitorService);
        caseData.setDocuments(CaseDocuments.builder()
            .documentsUploadedOnConfirmService(List.of(documentWithType(OTHER)))
            .build());

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/mid-event?page=CaseworkerConfirmService")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, CASEWORKER_CONFIRM_SERVICE)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            )
            .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    void shouldNotThrowErrorWhenServiceNotProcessedByProcessServer() throws Exception {

        setMockClock(clock);

        final LocalDate serviceDate = getExpectedLocalDate();

        final SolicitorService solicitorService = SolicitorService.builder()
            .dateOfService(serviceDate)
            .serviceProcessedByProcessServer(null)
            .build();

        final CaseData caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YesOrNo.YES);
        caseData.getApplication().setServiceMethod(ServiceMethod.SOLICITOR_SERVICE);
        caseData.getApplication().setIssueDate(serviceDate);
        caseData.getApplication().setSolicitorService(solicitorService);
        caseData.setDocuments(CaseDocuments.builder()
            .documentsUploadedOnConfirmService(List.of(documentWithType(OTHER)))
            .build());

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/mid-event?page=CaseworkerConfirmService")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, CASEWORKER_CONFIRM_SERVICE)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            )
            .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    void shouldSetDueDateTo141DaysFromTodayAndStateSetToHoldingWhenServiceProcessedByProcessServer() throws Exception {

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        setMockClock(clock);
        final LocalDate serviceDate = getExpectedLocalDate();

        final SolicitorService solicitorService = SolicitorService.builder()
            .dateOfService(serviceDate)
            .serviceProcessedByProcessServer(Set.of(SolicitorService.ServiceProcessedByProcessServer.CONFIRM))
            .build();

        final CaseData caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YesOrNo.YES);
        caseData.getApplication().setServiceMethod(ServiceMethod.SOLICITOR_SERVICE);
        caseData.getApplication().setIssueDate(serviceDate);
        caseData.getApplication().setSolicitorService(solicitorService);
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);

        final ListValue<DivorceDocument> confirmServiceAttachments = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentLink(new Document("url", "filename.pdf", "url/binary"))
                .build())
            .build();

        caseData.setDocuments(CaseDocuments.builder()
            .documentsUploaded(new ArrayList<>())
            .build());

        caseData.getDocuments().setDocumentsUploadedOnConfirmService(Lists.newArrayList(confirmServiceAttachments));


        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/about-to-submit?page=CaseworkerConfirmService")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, CASEWORKER_CONFIRM_SERVICE)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            )
            .andExpect(jsonPath("$.data.dueDate").value(caseData.getApplication().getIssueDate().plusDays(141).toString()))
            .andExpect(jsonPath("$.state").value("Holding"));
    }

    @Test
    void shouldNotChangeDueDateOrStateWhenSoleApplicationAndAoSSubmitted() throws Exception {

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        setMockClock(clock);
        final LocalDate serviceDate = getExpectedLocalDate();

        final SolicitorService solicitorService = SolicitorService.builder()
            .dateOfService(serviceDate)
            .serviceProcessedByProcessServer(Set.of(SolicitorService.ServiceProcessedByProcessServer.CONFIRM))
            .build();

        final CaseData caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YesOrNo.YES);
        caseData.getApplication().setServiceMethod(ServiceMethod.SOLICITOR_SERVICE);
        caseData.getApplication().setIssueDate(serviceDate);
        caseData.getApplication().setSolicitorService(solicitorService);
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.setAcknowledgementOfService(AcknowledgementOfService.builder().dateAosSubmitted(LocalDateTime.now()).build());

        final ListValue<DivorceDocument> confirmServiceAttachments = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentLink(new Document("url", "filename.pdf", "url/binary"))
                .build())
            .build();

        caseData.setDocuments(CaseDocuments.builder()
            .documentsUploaded(new ArrayList<>())
            .build());

        caseData.getDocuments().setDocumentsUploadedOnConfirmService(Lists.newArrayList(confirmServiceAttachments));

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/about-to-submit?page=CaseworkerConfirmService")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, CASEWORKER_CONFIRM_SERVICE, State.Holding.toString())))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            )
            .andExpect(jsonPath("$.data.dueDate").doesNotExist())
            .andExpect(jsonPath("$.state").value("Holding"));
    }
}
