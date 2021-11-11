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
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateCertificateOfEntitlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithCourtHearing.SYSTEM_UPDATE_CASE_COURT_HEARING;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class SystemUpdateCaseWithCourtHearingTest {

    @Mock
    private GenerateCertificateOfEntitlement generateCertificateOfEntitlement;

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

        final long caseId = 12345L;
        final CaseData caseData = CaseData.builder().build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(caseId);
        final CaseDetails<CaseData, State> updatedDetails = new CaseDetails<>();
        updatedDetails.setData(caseData);
        caseDetails.setId(caseId);

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        when(generateCertificateOfEntitlement.apply(caseDetails)).thenReturn(updatedDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemUpdateCaseWithCourtHearing.aboutToSubmit(caseDetails, beforeDetails);

        assertThat(response.getData()).isEqualTo(caseData);
    }
}