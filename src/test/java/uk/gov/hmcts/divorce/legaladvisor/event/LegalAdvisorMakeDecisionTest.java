package uk.gov.hmcts.divorce.legaladvisor.event;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.LegalAdvisorDecision;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.ConditionalOrderRefusedForAmendmentContent;
import uk.gov.hmcts.divorce.document.content.ConditionalOrderRefusedForClarificationContent;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.legaladvisor.notification.LegalAdvisorMoreInfoDecisionNotification;
import uk.gov.hmcts.divorce.legaladvisor.notification.LegalAdvisorRejectedDecisionNotification;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ClarificationReason.JURISDICTION_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.model.ClarificationReason.OTHER;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralDecision.APPROVE;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralType.EXPEDITED_CASE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.ADMIN_ERROR;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.MORE_INFO;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.REJECT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAdminClarification;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAmendedApplication;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingClarification;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ExpeditedCase;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CLARIFICATION_REFUSAL_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REFUSAL_ORDER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REJECTED_REFUSAL_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL;
import static uk.gov.hmcts.divorce.legaladvisor.event.LegalAdvisorMakeDecision.LEGAL_ADVISOR_MAKE_DECISION;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.documentWithType;

@ExtendWith(MockitoExtension.class)
class LegalAdvisorMakeDecisionTest {

    private UserInfo userInfo;

    @Mock
    private HttpServletRequest request;

    @Mock
    private IdamService idamService;

    @Mock
    private LegalAdvisorRejectedDecisionNotification rejectedNotification;

    @Mock
    private LegalAdvisorMoreInfoDecisionNotification moreInfoDecisionNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private ConditionalOrderRefusedForAmendmentContent conditionalOrderRefusedForAmendmentContent;

    @Mock
    private ConditionalOrderRefusedForClarificationContent conditionalOrderRefusedForClarificationContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private LegalAdvisorMakeDecision legalAdvisorMakeDecision;

    void setup() {
        userInfo = UserInfo.builder().name("Judge").roles(List.of(JUDGE.getRole())).build();
        final User user = new User(TEST_AUTHORIZATION_TOKEN, userInfo);

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_SYSTEM_AUTHORISATION_TOKEN);
        when(idamService.retrieveUser(TEST_SYSTEM_AUTHORISATION_TOKEN)).thenReturn(user);
    }

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        legalAdvisorMakeDecision.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(LEGAL_ADVISOR_MAKE_DECISION);
    }

    @Test
    void shouldSetGrantedDateAndStateToAwaitingPronouncementIfConditionalOrderIsGranted() {

        setMockClock(clock);
        setup();

        final CaseData caseData = CaseData.builder()
            .generalReferrals(createGeneralReferrals())
            .conditionalOrder(ConditionalOrder.builder().granted(YES).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorMakeDecision.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getConditionalOrder().getDecisionDate()).isEqualTo(getExpectedLocalDate());
        assertThat(response.getState()).isEqualTo(AwaitingPronouncement);
    }

    @Test
    void shouldSetStateToAwaitingClarificationIfConditionalOrderIsNotGrantedAndRefusalIsDueToMoreInformationRequired() {

        setMockClock(clock);
        setup();

        final CaseData caseData = CaseData.builder()
            .generalReferrals(createGeneralReferrals())
            .conditionalOrder(ConditionalOrder.builder().granted(NO).refusalDecision(MORE_INFO).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final Map<String, Object> templateContent = new HashMap<>();
        when(conditionalOrderRefusedForClarificationContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        String documentUrl = "http://localhost:8080/4567";
        var refusalConditionalOrderDoc = new Document(
            documentUrl,
            REFUSAL_ORDER_DOCUMENT_NAME,
            documentUrl + "/binary"
        );

        when(
            caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                CLARIFICATION_REFUSAL_ORDER_TEMPLATE_ID,
                ENGLISH,
                REFUSAL_ORDER_DOCUMENT_NAME
            ))
            .thenReturn(refusalConditionalOrderDoc);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorMakeDecision.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getConditionalOrder().getDecisionDate()).isNull();
        assertThat(response.getState()).isEqualTo(AwaitingClarification);

    }

    @Test
    void shouldSetStateToAwaitingAdminClarificationIfConditionalOrderIsNotGrantedAndRefusalIsDueToAdminError() {

        setMockClock(clock);
        setup();

        final CaseData caseData = CaseData.builder()
            .generalReferrals(createGeneralReferrals())
            .conditionalOrder(ConditionalOrder.builder().granted(NO).refusalDecision(ADMIN_ERROR).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorMakeDecision.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getConditionalOrder().getDecisionDate()).isNull();
        assertThat(response.getState()).isEqualTo(AwaitingAdminClarification);
    }

    @Test
    void shouldSetStateToAwaitingAmendedApplicationIfConditionalOrderIsRejected() {

        setMockClock(clock);
        setup();

        final CaseData caseData = CaseData.builder()
            .generalReferrals(createGeneralReferrals())
            .conditionalOrder(ConditionalOrder.builder().granted(NO).refusalDecision(REJECT).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final Map<String, Object> templateContent = new HashMap<>();
        when(conditionalOrderRefusedForAmendmentContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        String documentUrl = "http://localhost:8080/4567";
        var refusalConditionalOrderDoc = new Document(
            documentUrl,
            REFUSAL_ORDER_DOCUMENT_NAME,
            documentUrl + "/binary"
        );

        when(
            caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                REJECTED_REFUSAL_ORDER_TEMPLATE_ID,
                ENGLISH,
                REFUSAL_ORDER_DOCUMENT_NAME
            ))
            .thenReturn(refusalConditionalOrderDoc);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorMakeDecision.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getConditionalOrder().getDecisionDate()).isNull();
        assertThat(response.getState()).isEqualTo(AwaitingAmendedApplication);

        verify(notificationDispatcher).send(rejectedNotification, caseData, TEST_CASE_ID);
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldSendEmailIfConditionalOrderIsRejectedForMoreInfoAndIsSolicitorApplication() {

        setMockClock(clock);
        setup();

        final CaseData caseData = CaseData.builder()
            .generalReferrals(createGeneralReferrals())
            .conditionalOrder(ConditionalOrder.builder().granted(NO).refusalDecision(MORE_INFO).build())
            .application(Application.builder().solSignStatementOfTruth(YES).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();
        when(conditionalOrderRefusedForClarificationContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        String documentUrl = "http://localhost:8080/4567";
        var refusalConditionalOrderDoc = new Document(
            documentUrl,
            REFUSAL_ORDER_DOCUMENT_NAME,
            documentUrl + "/binary"
        );

        when(
            caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                CLARIFICATION_REFUSAL_ORDER_TEMPLATE_ID,
                ENGLISH,
                REFUSAL_ORDER_DOCUMENT_NAME
            ))
            .thenReturn(refusalConditionalOrderDoc);

        legalAdvisorMakeDecision.aboutToSubmit(caseDetails, caseDetails);

        verify(notificationDispatcher).send(moreInfoDecisionNotification, caseData, TEST_CASE_ID);
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldNotSendEmailIfConditionalOrderIsRejectedForMoreInfoAndIsNotSolicitorApplication() {

        setMockClock(clock);
        setup();

        final CaseData caseData = CaseData.builder()
            .generalReferrals(createGeneralReferrals())
            .conditionalOrder(ConditionalOrder.builder().granted(NO).refusalDecision(REJECT).build())
            .application(Application.builder().solSignStatementOfTruth(NO).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final Map<String, Object> templateContent = new HashMap<>();
        when(conditionalOrderRefusedForAmendmentContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        String documentUrl = "http://localhost:8080/4567";
        var refusalConditionalOrderDoc = new Document(
            documentUrl,
            REFUSAL_ORDER_DOCUMENT_NAME,
            documentUrl + "/binary"
        );

        when(
            caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                REJECTED_REFUSAL_ORDER_TEMPLATE_ID,
                ENGLISH,
                REFUSAL_ORDER_DOCUMENT_NAME
            ))
            .thenReturn(refusalConditionalOrderDoc);

        legalAdvisorMakeDecision.aboutToSubmit(caseDetails, caseDetails);

        verifyNoInteractions(moreInfoDecisionNotification);
    }

    @Test
    void shouldCreateNewClarificationResponsesSubmittedListIfNotExist() {

        setMockClock(clock);
        setup();

        final CaseData caseData = CaseData.builder()
                .generalReferrals(createGeneralReferrals())
                .conditionalOrder(
                ConditionalOrder
                    .builder()
                    .granted(NO)
                    .refusalDecision(MORE_INFO)
                    .refusalClarificationAdditionalInfo("some info")
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final Map<String, Object> templateContent = new HashMap<>();

        String documentUrl = "http://localhost:8080/4567";
        var refusalConditionalOrderDoc = new Document(
            documentUrl,
            REFUSAL_ORDER_DOCUMENT_NAME,
            documentUrl + "/binary"
        );

        when(
            caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                CLARIFICATION_REFUSAL_ORDER_TEMPLATE_ID,
                ENGLISH,
                REFUSAL_ORDER_DOCUMENT_NAME
            ))
            .thenReturn(refusalConditionalOrderDoc);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorMakeDecision.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getConditionalOrder().getLegalAdvisorDecisions()).hasSize(1);
    }

    @Test
    void shouldAddClarificationResponseSubmittedToTopOfListIfExistsAlready() {

        setMockClock(clock);
        setup();

        final ListValue<LegalAdvisorDecision> listValue =
            ListValue.<LegalAdvisorDecision>builder()
                .value(LegalAdvisorDecision.builder().build())
                .build();
        final List<ListValue<LegalAdvisorDecision>> legalAdvisorDecisions = new ArrayList<>();
        legalAdvisorDecisions.add(listValue);

        final CaseData caseData = CaseData.builder()
                .generalReferrals(createGeneralReferrals())
                .conditionalOrder(
                ConditionalOrder
                    .builder()
                    .granted(NO)
                    .refusalDecision(MORE_INFO)
                    .refusalClarificationAdditionalInfo("some info")
                    .legalAdvisorDecisions(legalAdvisorDecisions)
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final Map<String, Object> templateContent = new HashMap<>();

        String documentUrl = "http://localhost:8080/4567";
        var refusalConditionalOrderDoc = new Document(
            documentUrl,
            REFUSAL_ORDER_DOCUMENT_NAME,
            documentUrl + "/binary"
        );

        when(
            caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                CLARIFICATION_REFUSAL_ORDER_TEMPLATE_ID,
                ENGLISH,
                REFUSAL_ORDER_DOCUMENT_NAME
            ))
            .thenReturn(refusalConditionalOrderDoc);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorMakeDecision.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getConditionalOrder().getLegalAdvisorDecisions()).hasSize(2);
    }

    @Test
    void shouldFilterFreeTextRefusalClarificationReasonFromAudit() {

        setMockClock(clock);
        setup();

        final List<ListValue<LegalAdvisorDecision>> legalAdvisorDecisions = new ArrayList<>();
        final CaseData caseData = CaseData.builder()
                .generalReferrals(createGeneralReferrals())
                .conditionalOrder(
                ConditionalOrder
                    .builder()
                    .granted(NO)
                    .refusalDecision(MORE_INFO)
                    .refusalClarificationReason(Set.of(OTHER, JURISDICTION_DETAILS))
                    .refusalClarificationAdditionalInfo("some info")
                    .legalAdvisorDecisions(legalAdvisorDecisions)
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final Map<String, Object> templateContent = new HashMap<>();

        String documentUrl = "http://localhost:8080/4567";
        var refusalConditionalOrderDoc = new Document(
            documentUrl,
            REFUSAL_ORDER_DOCUMENT_NAME,
            documentUrl + "/binary"
        );

        when(
            caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                CLARIFICATION_REFUSAL_ORDER_TEMPLATE_ID,
                ENGLISH,
                REFUSAL_ORDER_DOCUMENT_NAME
            ))
            .thenReturn(refusalConditionalOrderDoc);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorMakeDecision.aboutToSubmit(caseDetails, caseDetails);

        final List<ListValue<LegalAdvisorDecision>> decisions =
            response.getData().getConditionalOrder().getLegalAdvisorDecisions();

        assertThat(decisions).hasSize(1);
        assertThat(decisions.get(0).getValue().getRefusalClarificationReason()).hasSize(1);
        assertThat(decisions.get(0).getValue().getRefusalClarificationReason()).doesNotContain(OTHER);
    }

    @Test
    void shouldResetClarificationResponseFieldsUponDecision() {
        setMockClock(clock);
        setup();

        final ListValue<String> listValue1 =
            ListValue.<String>builder()
                .value("Clarification")
                .build();
        final List<ListValue<String>> clarifications = new ArrayList<>();
        clarifications.add(listValue1);

        final CaseData caseData = CaseData.builder()
            .generalReferrals(createGeneralReferrals())
            .conditionalOrder(
                ConditionalOrder
                    .builder()
                    .clarificationResponses(clarifications)
                    .cannotUploadClarificationDocuments(NO)
                    .clarificationUploadDocuments(List.of(documentWithType(CONDITIONAL_ORDER_REFUSAL)))
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorMakeDecision.aboutToSubmit(caseDetails, null);

        ConditionalOrder actualConditionalOrder = response.getData().getConditionalOrder();
        assertThat(actualConditionalOrder.getClarificationResponses()).hasSize(0);
        assertThat(actualConditionalOrder.getCannotUploadClarificationDocuments()).isNull();
        assertThat(actualConditionalOrder.getClarificationUploadDocuments()).hasSize(0);
    }

    @Test
    public void midEventShouldGenerateTheRefusalOrderDoc() {
        final CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder().granted(NO).refusalDecision(MORE_INFO).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final Map<String, Object> templateContent = new HashMap<>();
        when(conditionalOrderRefusedForClarificationContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        String documentUrl = "http://localhost:8080/4567";
        var refusalConditionalOrderDoc = new Document(
            documentUrl,
            REFUSAL_ORDER_DOCUMENT_NAME,
            documentUrl + "/binary"
        );

        when(
            caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                CLARIFICATION_REFUSAL_ORDER_TEMPLATE_ID,
                ENGLISH,
                REFUSAL_ORDER_DOCUMENT_NAME
            ))
            .thenReturn(refusalConditionalOrderDoc);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorMakeDecision.midEvent(caseDetails, null);

        assertThat(response.getData().getConditionalOrder().getRefusalOrderDocument()).isEqualTo(refusalConditionalOrderDoc);
    }

    @Test
    void shouldGenerateRefusalDocumentAndSendLettersIfConditionalOrderIsRejectedForAmendmentAndIsOfflineApplication() {

        setMockClock(clock);
        setup();

        final CaseData caseData = CaseData.builder()
                .generalReferrals(createGeneralReferrals())
                .conditionalOrder(ConditionalOrder.builder().granted(NO).refusalDecision(REJECT).build())
            .applicant1(Applicant.builder().offline(YES).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();
        when(conditionalOrderRefusedForAmendmentContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        String documentUrl = "http://localhost:8080/4567";
        var refusalConditionalOrderDoc = new Document(
            documentUrl,
            REFUSAL_ORDER_DOCUMENT_NAME,
            documentUrl + "/binary"
        );

        when(
            caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                REJECTED_REFUSAL_ORDER_TEMPLATE_ID,
                ENGLISH,
                REFUSAL_ORDER_DOCUMENT_NAME
            ))
            .thenReturn(refusalConditionalOrderDoc);

        legalAdvisorMakeDecision.aboutToSubmit(caseDetails, caseDetails);

        verify(notificationDispatcher).send(rejectedNotification, caseData, TEST_CASE_ID);
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldGenerateRefusalDocumentAndSendLettersIfConditionalOrderIsRejectedForMoreInfoAndIsOfflineApplication() {

        setMockClock(clock);
        setup();

        final CaseData caseData = CaseData.builder()
            .generalReferrals(createGeneralReferrals())
            .conditionalOrder(ConditionalOrder.builder().granted(NO).refusalDecision(MORE_INFO).build())
            .applicant1(Applicant.builder().offline(YES).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();
        when(conditionalOrderRefusedForClarificationContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        String documentUrl = "http://localhost:8080/4567";
        var refusalConditionalOrderDoc = new Document(
            documentUrl,
            REFUSAL_ORDER_DOCUMENT_NAME,
            documentUrl + "/binary"
        );

        when(
            caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                CLARIFICATION_REFUSAL_ORDER_TEMPLATE_ID,
                ENGLISH,
                REFUSAL_ORDER_DOCUMENT_NAME
            ))
            .thenReturn(refusalConditionalOrderDoc);

        legalAdvisorMakeDecision.aboutToSubmit(caseDetails, caseDetails);

        verify(notificationDispatcher).send(moreInfoDecisionNotification, caseData, TEST_CASE_ID);
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldUpdateStateToExpeditedCaseAndAddApplicationAddedDateToCaseDataWhenGeneralReferralFeeIsNotRequired() {

        setMockClock(clock);
        setup();

        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        caseData.setGeneralReferrals(createGeneralReferrals());
        details.setData(caseData);
        details.setState(ExpeditedCase);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmitResponse = legalAdvisorMakeDecision.aboutToSubmit(details, details);

        CaseData responseData = aboutToSubmitResponse.getData();

        assertThat(aboutToSubmitResponse.getState()).isEqualTo(ExpeditedCase);
        assertThat(responseData.getApplication().getPreviousState()).isEqualTo(ExpeditedCase);
        assertThat(responseData.getConditionalOrder().getGranted()).isEqualTo(YES);
        assertThat(responseData.getConditionalOrder().getPronouncementJudge()).isEqualTo(userInfo.getName());
    }

    private List<ListValue<GeneralReferral>> createGeneralReferrals() {
        final List<ListValue<GeneralReferral>> generalReferrals = new ArrayList<>();
        generalReferrals.add(
                ListValue.<GeneralReferral>builder()
                        .value(GeneralReferral.builder()
                                .generalReferralDecision(APPROVE)
                                .generalReferralDecisionReason("reason")
                                .generalReferralType(EXPEDITED_CASE)
                                .build())
                        .build());

        return generalReferrals;
    }
}
