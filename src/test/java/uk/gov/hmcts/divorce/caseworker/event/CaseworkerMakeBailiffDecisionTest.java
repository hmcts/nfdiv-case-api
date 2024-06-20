package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.notification.ServiceApplicationNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.BailiffApprovedOrderContent;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerMakeBailiffDecision.CASEWORKER_BAILIFF_DECISION;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.BAILIFF;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingBailiffService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.BailiffRefused;
import static uk.gov.hmcts.divorce.document.DocumentConstants.BAILIFF_APPLICATION_APPROVED_FILE_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.BAILIFF_APPLICATION_APPROVED_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.BAILIFF_SERVICE;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerMakeBailiffDecisionTest {

    @Mock
    private Clock clock;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private BailiffApprovedOrderContent bailiffApprovedOrderContent;

    @Mock
    private ServiceApplicationNotification serviceApplicationNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private CaseworkerMakeBailiffDecision makeBailiffDecision;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        makeBailiffDecision.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_BAILIFF_DECISION);
    }

    @Test
    void shouldChangeCaseStateToAwaitingBailiffServiceAndSetDecisionDateWhenServiceApplicationIsGrantedAndServiceTypeIsBailiff() {
        setMockClock(clock);

        final CaseData caseData = caseData();
        caseData.getAlternativeService().setServiceApplicationGranted(YES);
        caseData.getAlternativeService().setAlternativeServiceType(BAILIFF);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            makeBailiffDecision.aboutToSubmit(details, details);

        assertThat(response.getState()).isEqualTo(AwaitingBailiffService);
        assertThat(response.getData().getAlternativeService().getServiceApplicationDecisionDate())
            .isEqualTo(getExpectedLocalDate());

        verify(notificationDispatcher).send(serviceApplicationNotification, response.getData(), details.getId());
    }

    @Test
    void shouldChangeCaseStateToBailiffRefusedAndSetDecisionDateWhenServiceApplicationIsNotGranted() {
        setMockClock(clock);

        final CaseData caseData = caseData();
        caseData.getAlternativeService().setServiceApplicationGranted(NO);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            makeBailiffDecision.aboutToSubmit(details, details);

        assertThat(response.getState()).isEqualTo(BailiffRefused);

        assertThat(response.getData().getAlternativeService().getServiceApplicationDecisionDate())
            .isEqualTo(getExpectedLocalDate());

        verifyNoInteractions(notificationDispatcher);
    }

    @Test
    void shouldGenerateBailiffApprovalWithDivorceContent() {
        setMockClock(clock);

        final CaseData caseData = caseData();
        caseData.getAlternativeService().setServiceApplicationGranted(YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        Map<String, Object> templateContent = new HashMap<>();

        when(bailiffApprovedOrderContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            makeBailiffDecision.aboutToSubmit(details, details);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                BAILIFF_SERVICE,
                templateContent,
                TEST_CASE_ID,
                BAILIFF_APPLICATION_APPROVED_ID,
                ENGLISH,
                BAILIFF_APPLICATION_APPROVED_FILE_NAME
            );

        verify(notificationDispatcher).send(serviceApplicationNotification, response.getData(), details.getId());
    }

    @Test
    void shouldGenerateBailiffApprovalWithDissolutionContent() {
        setMockClock(clock);

        final CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.getAlternativeService().setServiceApplicationGranted(YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        Map<String, Object> templateContent = new HashMap<>();

        when(bailiffApprovedOrderContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            makeBailiffDecision.aboutToSubmit(details, details);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                BAILIFF_SERVICE,
                templateContent,
                TEST_CASE_ID,
                BAILIFF_APPLICATION_APPROVED_ID,
                ENGLISH,
                BAILIFF_APPLICATION_APPROVED_FILE_NAME
            );

        verify(notificationDispatcher).send(serviceApplicationNotification, response.getData(), details.getId());
    }
}
