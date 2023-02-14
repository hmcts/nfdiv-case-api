package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateCertificateOfEntitlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemMigrateCaseWithCoEGeneration.SYSTEM_MIGRATE_CASE_WITH_COE_GENERATION;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(SpringExtension.class)
class SystemMigrateCaseWithCoEGenerationTest {

    @Mock
    private GenerateCertificateOfEntitlement generateCertificateOfEntitlement;

    @InjectMocks
    private SystemMigrateCaseWithCoEGeneration systemMigrateCaseWithCoEGeneration;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemMigrateCaseWithCoEGeneration.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_MIGRATE_CASE_WITH_COE_GENERATION);
    }

    @Test
    void shouldGenerateCertificateOfEntitlementOnAboutToSubmit() {

        final CaseData caseData = mock(CaseData.class);
        final CaseData responseCaseData = mock(CaseData.class);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(1L);
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> responseCaseDetails = new CaseDetails<>();
        responseCaseDetails.setId(1L);
        responseCaseDetails.setData(responseCaseData);

        when(generateCertificateOfEntitlement.apply(caseDetails)).thenReturn(responseCaseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemMigrateCaseWithCoEGeneration.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(responseCaseData);

        verify(generateCertificateOfEntitlement).apply(caseDetails);
    }
}