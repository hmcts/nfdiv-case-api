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
import uk.gov.hmcts.divorce.document.content.BailiffNotApprovedOrderContent;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerConfirmBailiffRefusal.CASEWORKER_CONFIRM_BAILIFF_REFUSAL;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.document.DocumentConstants.BAILIFF_APPLICATION_NOT_APPROVED_FILE_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.BAILIFF_APPLICATION_NOT_APPROVED_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.BAILIFF_SERVICE_REFUSED;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerConfirmBailiffRefusalTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private BailiffNotApprovedOrderContent bailiffNotApprovedOrderContent;

    @Mock
    private ServiceApplicationNotification serviceApplicationNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private CaseworkerConfirmBailiffRefusal confirmBailiffRefusal;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        confirmBailiffRefusal.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_CONFIRM_BAILIFF_REFUSAL);
    }

    @Test
    void shouldChangeCaseStateToAwaitingAosAndSetDecisionDateWhenServiceApplicationIsNotGranted() {
        final CaseData caseData = caseData();
        LocalDate serviceDecisionDate = LocalDate.of(2024, 1,1);
        caseData.getAlternativeService().setServiceApplicationGranted(NO);
        caseData.getAlternativeService().setServiceApplicationDecisionDate(serviceDecisionDate);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            confirmBailiffRefusal.aboutToSubmit(details, details);

        assertThat(response.getState()).isEqualTo(AwaitingAos);

        ListValue<AlternativeServiceOutcome> listValue = response.getData().getAlternativeServiceOutcomes().get(0);
        assertThat(listValue.getValue().getServiceApplicationDecisionDate()).isEqualTo(serviceDecisionDate);

        verify(notificationDispatcher).send(serviceApplicationNotification, response.getData(), details.getId());
    }

    @Test
    void shouldGenerateBailiffRefusalOrderWithDivorceContent() {
        final CaseData caseData = caseData();
        caseData.getAlternativeService().setServiceApplicationGranted(NO);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        Map<String, Object> templateContent = new HashMap<>();

        when(bailiffNotApprovedOrderContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            confirmBailiffRefusal.aboutToSubmit(details, details);

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

        verify(notificationDispatcher).send(serviceApplicationNotification, response.getData(), details.getId());
    }

    @Test
    void shouldGenerateBailiffRefusalOrderWithDissolutionContent() {
        final CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.getAlternativeService().setServiceApplicationGranted(NO);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        Map<String, Object> templateContent = new HashMap<>();

        when(bailiffNotApprovedOrderContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            confirmBailiffRefusal.aboutToSubmit(details, details);

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

        verify(notificationDispatcher).send(serviceApplicationNotification, response.getData(), details.getId());
    }
}
