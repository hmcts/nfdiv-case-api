package uk.gov.hmcts.divorce.common.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.print.SwitchToSoleCoPrinter;
import uk.gov.hmcts.divorce.citizen.notification.SwitchToSoleCoNotification;
import uk.gov.hmcts.divorce.citizen.service.SwitchToSoleService;
import uk.gov.hmcts.divorce.common.service.task.GenerateConditionalOrderAnswersDocument;
import uk.gov.hmcts.divorce.common.service.task.GenerateSwitchToSoleConditionalOrderLetter;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.SwitchedToSoleCo.SWITCH_TO_SOLE_CO;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.OfflineDocumentReceived.CO_D84;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.divorcecase.model.OfflineApplicationType.SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.model.OfflineWhoApplying.APPLICANT_1;
import static uk.gov.hmcts.divorce.divorcecase.model.OfflineWhoApplying.APPLICANT_2;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class SwitchedToSoleCoTest {

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
    private SwitchToSoleCoPrinter switchToSoleCoPrinter;

    @Mock
    private GenerateConditionalOrderAnswersDocument generateConditionalOrderAnswersDocument;

    @Mock
    private GenerateSwitchToSoleConditionalOrderLetter generateSwitchToSoleCoLetter;

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
        final long caseId = 1L;
        CaseData caseData = validJointApplicant1CaseData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .build();
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("app1-token");

        final AboutToStartOrSubmitResponse<CaseData, State> response = switchedToSoleCo.aboutToSubmit(caseDetails, caseDetails);

        verify(generateConditionalOrderAnswersDocument).apply(caseDetails, WELSH);
        assertThat(response.getData().getApplicationType()).isEqualTo(SOLE_APPLICATION);
        assertThat(response.getData().getApplication().getSwitchedToSoleCo()).isEqualTo(YES);
    }

    @Test
    void shouldSetApplicationTypeToSoleAndSendNotificationToApplicant2() {
        final long caseId = 1L;
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
        verify(generateConditionalOrderAnswersDocument).apply(caseDetails, ENGLISH);
    }

    @Test
    void shouldSwitchUserDataAndRolesIfApplicant2TriggeredD84SwitchToSole() {
        final long caseId = 1L;
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
        verify(generateSwitchToSoleCoLetter).apply(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2());
        verify(generateConditionalOrderAnswersDocument).apply(caseDetails, ENGLISH);
    }

    @Test
    void shouldNotSwitchRolesIfApplicant2TriggeredD84SwitchToSoleAndIsNewPaperCase() {
        final long caseId = 1L;
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

        verify(generateSwitchToSoleCoLetter).apply(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2());
        verify(switchToSoleService).switchApplicantData(caseData);
        verifyNoMoreInteractions(switchToSoleService);
    }

    @Test
    void shouldNotSwitchUserDataOrRolesIfApplicant1TriggeredD84SwitchToSole() {
        final long caseId = 1L;
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

        verify(generateSwitchToSoleCoLetter).apply(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2());
        verifyNoInteractions(switchToSoleService);
    }

    @Test
    void shouldTriggerSwitchToSoleEmailNotificationsInSubmittedCallback() {
        final long caseId = 1L;
        CaseData caseData = validJointApplicant1CaseData();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .build();

        switchedToSoleCo.submitted(caseDetails, caseDetails);

        verify(notificationDispatcher).send(switchToSoleCoNotification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldPrintSwitchToSoleCoLetterD84SwitchToSoleInSubmittedCallback() {
        final long caseId = 1L;
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

        switchedToSoleCo.submitted(caseDetails, caseDetails);

        verify(switchToSoleCoPrinter).print(caseData, caseId);
        verifyNoMoreInteractions(switchToSoleCoPrinter);
    }
}
