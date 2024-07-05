package uk.gov.hmcts.divorce.noticeofchange.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.type.ChangeOrganisationRequest;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicListItem;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseRoleID;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.LitigantGrantOfRepresentationConfirmationTemplateContent;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.noticeofchange.client.AssignCaseAccessClient;
import uk.gov.hmcts.divorce.noticeofchange.model.AcaRequest;
import uk.gov.hmcts.divorce.noticeofchange.service.ChangeOfRepresentativeService;
import uk.gov.hmcts.divorce.testutil.TestDataHelper;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOTICE_OF_CHANGE_CONFIRMATION_APP1_APP2_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOTICE_OF_CHANGE_CONFIRMATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.noticeofchange.event.SystemApplyNoticeOfChange.LETTER_TYPE_GRANT_OF_REPRESENTATION;
import static uk.gov.hmcts.divorce.noticeofchange.event.SystemApplyNoticeOfChange.NOTICE_OF_CHANGE_APPLIED;
import static uk.gov.hmcts.divorce.noticeofchange.model.ChangeOfRepresentationAuthor.SOLICITOR_NOTICE_OF_CHANGE;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;

@ExtendWith(MockitoExtension.class)
class SystemApplyNoticeOfChangeTest {

    private static final String TEST_ORGANISATION_NAME = "organisation_name";
    private static final String TEST_ORGANISATION_ID = "organisation_id";
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private IdamService idamService;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private AssignCaseAccessClient assignCaseAccessClient;

    @Mock
    private User systemUser;

    @Mock
    private LitigantGrantOfRepresentationConfirmationTemplateContent litigantConfirmationTemplateContent;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ChangeOfRepresentativeService changeOfRepresentativeService;

    @Mock
    private BulkPrintService bulkPrintService;

    @Captor
    ArgumentCaptor<Print> printCaptor;

    @InjectMocks
    private SystemApplyNoticeOfChange systemApplyNoticeOfChange;

    public void setup() {
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(systemUser.getAuthToken()).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
    }

    @Test
    void configure() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemApplyNoticeOfChange.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(NOTICE_OF_CHANGE_APPLIED);
    }

    @Test
    void shouldApplyNoticeOfChangeForApplicant1Solicitor() {
        setup();
        Applicant applicant = TestDataHelper.applicantRepresentedBySolicitor();
        final ChangeOrganisationRequest<CaseRoleID> changeOrganisationRequest = getChangeOrganisationRequestField("[APPONESOLICITOR]",
                "APPLICANT_1_SOLICITOR");

        CaseData caseData = CaseData.builder().applicant1(applicant).changeOrganisationRequestField(changeOrganisationRequest).build();

        var details =  CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData).build();
        AcaRequest acaRequest = AcaRequest.acaRequest(details);
        Map<String, Object> expectedData = expectedData(caseData);
        when(objectMapper.convertValue(expectedData, CaseData.class)).thenReturn(caseData);
        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse
                .builder().data(expectedData).build();
        when(assignCaseAccessClient.applyNoticeOfChange(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, acaRequest))
                .thenReturn(response);
        final Map<String, Object> templateContent = new HashMap<>();

        when(litigantConfirmationTemplateContent.getTemplateContent(caseData, TEST_CASE_ID, caseData.getApplicant1()))
                .thenReturn(templateContent);

        Document nocConfirmationDocument =
                Document.builder()
                        .url("testUrl")
                        .filename("testFileName")
                        .binaryUrl("binaryUrl")
                        .build();

        when(caseDataDocumentService.renderDocument(
                        templateContent,
                        TEST_CASE_ID,
                        NFD_NOTICE_OF_CHANGE_CONFIRMATION_APP1_APP2_TEMPLATE_ID,
                        LanguagePreference.ENGLISH,
                NFD_NOTICE_OF_CHANGE_CONFIRMATION_DOCUMENT_NAME))
                .thenReturn(nocConfirmationDocument);

        systemApplyNoticeOfChange.aboutToStart(details);

        final Print print = printCaptor.getValue();

        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo(LETTER_TYPE_GRANT_OF_REPRESENTATION);
        assertThat(print.getLetters()).hasSize(1);
        assertThat(print.getLetters().get(0).getDocument()).isSameAs(nocConfirmationDocument);
        verify(assignCaseAccessClient).applyNoticeOfChange(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, acaRequest);
        verify(changeOfRepresentativeService).buildChangeOfRepresentative(caseData, null, SOLICITOR_NOTICE_OF_CHANGE.getValue(), true);
        verify(caseDataDocumentService)
                .renderDocument(
                        templateContent,
                        TEST_CASE_ID,
                        NFD_NOTICE_OF_CHANGE_CONFIRMATION_APP1_APP2_TEMPLATE_ID,
                        ENGLISH, NFD_NOTICE_OF_CHANGE_CONFIRMATION_DOCUMENT_NAME);
    }

    @Test
    void shouldApplyNoticeOfChangeForApplicant2Solicitor() {
        setup();
        Applicant applicant = TestDataHelper.applicantRepresentedBySolicitor();
        final ChangeOrganisationRequest<CaseRoleID> changeOrganisationRequest = getChangeOrganisationRequestField("[APPTWOSOLICITOR]",
                "APPLICANT_2_SOLICITOR");
        CaseData caseData = CaseData.builder().applicant2(applicant).changeOrganisationRequestField(changeOrganisationRequest).build();
        var details =  CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData).build();
        AcaRequest acaRequest = AcaRequest.acaRequest(details);
        Map<String, Object> expectedData = expectedData(caseData);
        when(objectMapper.convertValue(expectedData, CaseData.class)).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse
                .builder().data(expectedData).build();
        when(assignCaseAccessClient.applyNoticeOfChange(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, acaRequest))
                .thenReturn(response);

        systemApplyNoticeOfChange.aboutToStart(details);

        verify(assignCaseAccessClient).applyNoticeOfChange(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, acaRequest);
        verify(changeOfRepresentativeService).buildChangeOfRepresentative(caseData, null, SOLICITOR_NOTICE_OF_CHANGE.getValue(), false);
    }

    @Test
    void shouldNotApplyNoticeOfChangeWhenErrorsThrown() {
        setup();
        Applicant applicant = TestDataHelper.applicantRepresentedBySolicitor();
        final ChangeOrganisationRequest<CaseRoleID> changeOrganisationRequest = getChangeOrganisationRequestField("[APPTWOSOLICITOR]",
                "APPLICANT_2_SOLICITOR");
        CaseData caseData = CaseData.builder().applicant2(applicant).changeOrganisationRequestField(changeOrganisationRequest).build();
        var details =  CaseDetails.<CaseData, State>builder().data(caseData).build();
        AcaRequest acaRequest = AcaRequest.acaRequest(details);

        List<String> errors = List.of("One of the org policies is missing for NoC");
        AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse
                .builder().errors(errors).build();
        when(assignCaseAccessClient.applyNoticeOfChange(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, acaRequest))
                .thenReturn(response);

        systemApplyNoticeOfChange.aboutToStart(details);

        verify(assignCaseAccessClient).applyNoticeOfChange(
                TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, acaRequest
        );

        verifyNoInteractions(objectMapper);
    }

    private Map<String, Object> expectedData(final CaseData caseData) {

        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper.convertValue(caseData, new TypeReference<>() {
        });
    }

    private ChangeOrganisationRequest<CaseRoleID> getChangeOrganisationRequestField(String role, String roleLabel) {
        DynamicListItem dynamicListItem = DynamicListItem.builder().label(
                roleLabel).code(role).build();
        List<DynamicListItem> dynamicListItemList = new ArrayList<>();
        dynamicListItemList.add(dynamicListItem);

        ChangeOrganisationRequest<CaseRoleID> changeOrganisationRequest = ChangeOrganisationRequest.<CaseRoleID>builder().build();
        changeOrganisationRequest.setCaseRoleId(CaseRoleID.builder().value(dynamicListItem).listItems(dynamicListItemList).build());
        changeOrganisationRequest.setCreatedBy(TEST_SOLICITOR_EMAIL);
        changeOrganisationRequest.setOrganisationToAdd(Organisation
                .builder().organisationId(TEST_ORGANISATION_ID).organisationName(TEST_ORG_NAME).build());
        changeOrganisationRequest.setOrganisationToRemove(Organisation
                .builder().organisationId(TEST_ORG_ID).organisationName(TEST_ORGANISATION_NAME).build());
        return changeOrganisationRequest;
    }

    private void setupMocks(Clock mockClock) {
        if (Objects.nonNull(mockClock)) {
            setMockClock(mockClock);
        }
    }
}
