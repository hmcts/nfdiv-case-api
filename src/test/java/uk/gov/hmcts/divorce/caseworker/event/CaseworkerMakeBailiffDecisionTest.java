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
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.notification.ServiceApplicationNotification;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceOutcome;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.BailiffApprovedOrderContent;
import uk.gov.hmcts.divorce.document.content.BailiffNotApprovedOrderContent;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerMakeBailiffDecision.CASEWORKER_BAILIFF_DECISION;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.BAILIFF;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingBailiffService;
import static uk.gov.hmcts.divorce.document.DocumentConstants.BAILIFF_APPLICATION_APPROVED_FILE_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.BAILIFF_APPLICATION_APPROVED_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.BAILIFF_APPLICATION_NOT_APPROVED_FILE_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.BAILIFF_APPLICATION_NOT_APPROVED_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.BAILIFF_SERVICE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.BAILIFF_SERVICE_REFUSED;
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
    private BailiffNotApprovedOrderContent bailiffNotApprovedOrderContent;

    @Mock
    private ServiceApplicationNotification serviceApplicationNotification;

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

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartOrSubmitResponse =
            makeBailiffDecision.aboutToSubmit(details, details);

        assertThat(aboutToStartOrSubmitResponse.getState()).isEqualTo(AwaitingBailiffService);
        assertThat(aboutToStartOrSubmitResponse.getData().getAlternativeService().getServiceApplicationDecisionDate())
            .isEqualTo(getExpectedLocalDate());

        verify(serviceApplicationNotification, never()).sendToApplicant1(any(CaseData.class), anyLong());
    }

    @Test
    void shouldChangeCaseStateToAwaitingAosAndSetDecisionDateWhenServiceApplicationIsNotGranted() {
        setMockClock(clock);

        final CaseData caseData = caseData();
        caseData.getAlternativeService().setServiceApplicationGranted(NO);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartOrSubmitResponse =
            makeBailiffDecision.aboutToSubmit(details, details);

        assertThat(aboutToStartOrSubmitResponse.getState()).isEqualTo(AwaitingAos);

        ListValue<AlternativeServiceOutcome> listValue = aboutToStartOrSubmitResponse.getData().getAlternativeServiceOutcomes().get(0);
        assertThat(listValue.getValue().getServiceApplicationDecisionDate()).isEqualTo(getExpectedLocalDate());

        verify(serviceApplicationNotification).sendToApplicant1(any(CaseData.class), eq(TEST_CASE_ID));
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

        verify(serviceApplicationNotification, never()).sendToApplicant1(any(CaseData.class), anyLong());
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

        verify(serviceApplicationNotification, never()).sendToApplicant1(any(CaseData.class), anyLong());
    }

    @Test
    void shouldGenerateBailiffRefusalOrderWithDivorceContent() {
        setMockClock(clock);

        final CaseData caseData = caseData();
        caseData.getAlternativeService().setServiceApplicationGranted(NO);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        Map<String, Object> templateContent = new HashMap<>();

        when(bailiffNotApprovedOrderContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);

        makeBailiffDecision.aboutToSubmit(details, details);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                any(CaseData.class),
                eq(BAILIFF_SERVICE_REFUSED),
                eq(templateContent),
                eq(TEST_CASE_ID),
                eq(BAILIFF_APPLICATION_NOT_APPROVED_ID),
                eq(ENGLISH),
                eq(BAILIFF_APPLICATION_NOT_APPROVED_FILE_NAME)
            );

        verify(serviceApplicationNotification).sendToApplicant1(any(CaseData.class), eq(TEST_CASE_ID));
    }

    @Test
    void shouldGenerateBailiffRefusalOrderWithDissolutionContent() {
        setMockClock(clock);

        final CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.getAlternativeService().setServiceApplicationGranted(NO);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        Map<String, Object> templateContent = new HashMap<>();

        when(bailiffNotApprovedOrderContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);

        makeBailiffDecision.aboutToSubmit(details, details);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                any(CaseData.class),
                eq(BAILIFF_SERVICE_REFUSED),
                eq(templateContent),
                eq(TEST_CASE_ID),
                eq(BAILIFF_APPLICATION_NOT_APPROVED_ID),
                eq(ENGLISH),
                eq(BAILIFF_APPLICATION_NOT_APPROVED_FILE_NAME)
            );

        verify(serviceApplicationNotification).sendToApplicant1(any(CaseData.class), eq(TEST_CASE_ID));
    }
}
