package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.EntitlementGrantedConditionalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateCertificateOfEntitlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithCourtHearing.SYSTEM_UPDATE_CASE_COURT_HEARING;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseWithCourtHearing;

@ExtendWith(MockitoExtension.class)
class SystemUpdateCaseWithCourtHearingTest {

    @Mock
    private GenerateCertificateOfEntitlement generateCertificateOfEntitlement;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private EntitlementGrantedConditionalOrderNotification entitlementGrantedConditionalOrderNotification;

    @InjectMocks
    private SystemUpdateCaseWithCourtHearing systemUpdateCaseWithCourtHearing;

    @Test
    void shouldAddConfigurationToConfigBuilder() {

        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemUpdateCaseWithCourtHearing.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_UPDATE_CASE_COURT_HEARING);
    }

    @Test
    void shouldCallGenerateCertificateOfEntitlementOnAboutToSubmit() {

        final long caseId = 2L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> updatedDetails = new CaseDetails<>();

        final CaseData caseData = CaseData.builder().build();

        caseDetails.setId(caseId);
        caseDetails.setData(caseData);

        updatedDetails.setId(caseId);
        updatedDetails.setData(caseData);

        when(generateCertificateOfEntitlement.apply(caseDetails)).thenReturn(updatedDetails);
        doNothing().when(notificationDispatcher).send(entitlementGrantedConditionalOrderNotification, caseData, caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemUpdateCaseWithCourtHearing.aboutToSubmit(caseDetails, null);

        assertThat(response.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldCallNotificationDispatcherOnAboutToSubmit() {

        final long caseId = 2L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> updatedDetails = new CaseDetails<>();

        caseDetails.setId(caseId);
        caseDetails.setData(validCaseWithCourtHearing());

        updatedDetails.setId(caseId);
        updatedDetails.setData(validCaseWithCourtHearing());

        when(generateCertificateOfEntitlement.apply(caseDetails)).thenReturn(updatedDetails);

        systemUpdateCaseWithCourtHearing.aboutToSubmit(caseDetails, null);

        verify(notificationDispatcher).send(entitlementGrantedConditionalOrderNotification, caseDetails.getData(), caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
    }
}
