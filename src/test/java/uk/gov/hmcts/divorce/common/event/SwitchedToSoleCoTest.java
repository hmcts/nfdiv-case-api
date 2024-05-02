package uk.gov.hmcts.divorce.common.event;

import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.divorce.citizen.notification.SwitchToSoleCoNotification;
import uk.gov.hmcts.divorce.citizen.service.SwitchToSoleService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;
import uk.gov.hmcts.divorce.document.print.documentpack.SwitchToSoleCODocumentPack;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.SwitchedToSoleCo.SWITCH_TO_SOLE_CO;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.OfflineDocumentReceived.CO_D84;
import static uk.gov.hmcts.divorce.divorcecase.model.OfflineApplicationType.SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.model.OfflineWhoApplying.APPLICANT_1;
import static uk.gov.hmcts.divorce.divorcecase.model.OfflineWhoApplying.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.State.JSAwaitingLA;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.SWITCH_TO_SOLE_CO_LETTER;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class SwitchedToSoleCoTest {

    private static final DocumentPackInfo TEST_DOCUMENT_PACK_INFO = new DocumentPackInfo(
        ImmutableMap.of(
            SWITCH_TO_SOLE_CO_LETTER, Optional.of(SWITCH_TO_SOLE_CO_LETTER_TEMPLATE_ID)
        ),
        ImmutableMap.of(
            SWITCH_TO_SOLE_CO_LETTER_TEMPLATE_ID, SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME
        )
    );
    public static final String THE_LETTER_ID = "the-letter-id";

    @Mock
    private SwitchToSoleCoNotification switchToSoleCoNotification;

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private SwitchToSoleService switchToSoleService;

    @Mock
    private LetterPrinter printer;

    @Mock
    private SwitchToSoleCODocumentPack switchToSoleConditionalOrderDocumentPack;

    @InjectMocks
    private SwitchedToSoleCo switchedToSoleCo;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        switchedToSoleCo.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SWITCH_TO_SOLE_CO);
    }

    @Test
    void shouldSetApplicationTypeToSoleAndSendNotificationToApplicant1() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .build();
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("app1-token");

        final AboutToStartOrSubmitResponse<CaseData, State> response = switchedToSoleCo.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getApplicationType()).isEqualTo(SOLE_APPLICATION);
        assertThat(response.getData().getApplication().getSwitchedToSoleCo()).isEqualTo(YES);
    }

    @Test
    void shouldSetApplicationTypeToSoleAndSwitchUserRolesAndDataWhenS2STriggeredByApplicant2() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .build();
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("app2-token");
        when(ccdAccessService.isApplicant2("app2-token", caseId)).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response = switchedToSoleCo.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getApplicationType()).isEqualTo(SOLE_APPLICATION);
        assertThat(response.getData().getApplication().getSwitchedToSoleCo()).isEqualTo(YES);
        assertThat(response.getData().getLabelContent().getApplicant2()).isEqualTo("respondent");
        assertThat(response.getData().getConditionalOrder().getSwitchedToSole()).isEqualTo(YES);

        verify(switchToSoleService).switchUserRoles(caseData, caseId);
        verify(switchToSoleService).switchApplicantData(caseData);
    }

    @Test
    void shouldSendNotificationButNoLetterWhenTriggeredOnlineByCitizen() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setSwitchedToSoleCo(YES);
        caseData.getLabelContent().setApplicant2("respondent");
        caseData.getConditionalOrder().setSwitchedToSole(YES);

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .build();

        switchedToSoleCo.submitted(caseDetails, caseDetails);

        verify(notificationDispatcher).send(switchToSoleCoNotification, caseData, caseId);
        verifyNoInteractions(printer);
    }

    @Test
    void shouldSwitchUserDataAndRolesIfApplicant2TriggeredD84SwitchToSole() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setDocuments(CaseDocuments.builder().typeOfDocumentAttached(CO_D84).build());
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .d84ApplicationType(SWITCH_TO_SOLE)
            .d84WhoApplying(APPLICANT_2)
            .build()
        );
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .build();

        switchedToSoleCo.aboutToSubmit(caseDetails, caseDetails);

        verify(switchToSoleService).switchUserRoles(caseData, caseId);
        verify(switchToSoleService).switchApplicantData(caseData);
    }

    @Test
    void shouldSendNotificationsAndLetterToRespondentIfD84SwitchToSole() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setDocuments(CaseDocuments.builder().typeOfDocumentAttached(CO_D84).build());
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .d84ApplicationType(SWITCH_TO_SOLE)
            .build()
        );
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .build();

        when(switchToSoleConditionalOrderDocumentPack.getDocumentPack(caseData, null)).thenReturn(TEST_DOCUMENT_PACK_INFO);
        when(switchToSoleConditionalOrderDocumentPack.getLetterId()).thenReturn(THE_LETTER_ID);

        switchedToSoleCo.submitted(caseDetails, caseDetails);

        verify(notificationDispatcher).send(switchToSoleCoNotification, caseData, caseId);
        verify(printer).sendLetters(caseData, caseId, caseData.getApplicant2(), TEST_DOCUMENT_PACK_INFO, THE_LETTER_ID);
        verifyNoMoreInteractions(printer);
    }

    @Test
    void shouldNotSwitchRolesIfApplicant2TriggeredD84SwitchToSoleAndIsNewPaperCase() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        caseData.getApplication().setNewPaperCase(YES);
        caseData.setDocuments(CaseDocuments.builder().typeOfDocumentAttached(CO_D84).build());
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .d84ApplicationType(SWITCH_TO_SOLE)
            .d84WhoApplying(APPLICANT_2)
            .build()
        );
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .build();

        switchedToSoleCo.aboutToSubmit(caseDetails, caseDetails);

        verify(switchToSoleService).switchApplicantData(caseData);
        verifyNoMoreInteractions(switchToSoleService);
    }

    @Test
    void shouldNotSwitchUserDataOrRolesIfApplicant1TriggeredD84SwitchToSole() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setDocuments(CaseDocuments.builder().typeOfDocumentAttached(CO_D84).build());
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .d84ApplicationType(SWITCH_TO_SOLE)
            .d84WhoApplying(APPLICANT_1)
            .build()
        );
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .build();

        switchedToSoleCo.aboutToSubmit(caseDetails, caseDetails);

        verifyNoInteractions(switchToSoleService);
    }

    @Test
    void shouldKeepSameStateIfInJSAwaitingLA() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .state(JSAwaitingLA)
            .build();

        var response = switchedToSoleCo.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getState()).isEqualTo(JSAwaitingLA);
    }

    @Test
    void shouldProgressStateIfNotInStateJSAwaitingLA() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .state(ConditionalOrderPending)
            .build();

        var response = switchedToSoleCo.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getState()).isEqualTo(AwaitingLegalAdvisorReferral);
    }
}
