package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAmendApplicationType.CASEWORKER_AMEND_APPLICATION_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class CaseworkerAmendApplicationTypeTest {
    @InjectMocks
    private CaseworkerAmendApplicationType caseworkerAmendApplicationType;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerAmendApplicationType.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_AMEND_APPLICATION_TYPE);
    }

    @Test
    void shouldSetApplicationTypeFromDivorceToDissolutionOnAboutToSubmit() {
        final CaseData caseData = CaseData.builder().applicationType(ApplicationType.JOINT_APPLICATION).build();
        caseData.setDivorceOrDissolution(DIVORCE);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerAmendApplicationType
            .aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getDivorceOrDissolution().equals(DISSOLUTION));
    }

    @Test
    void shouldSetApplicationTypeFromDissolutionToDivorceOnAboutToSubmit() {
        final CaseData caseData = CaseData.builder().build();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerAmendApplicationType
            .aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getDivorceOrDissolution().equals(DIVORCE));
    }

    @Test
    void shouldSetApplicationTypeToBlankOnAboutToSubmit() {
        final CaseData caseData = CaseData.builder().applicationType(ApplicationType.JOINT_APPLICATION).build();
        caseData.setDivorceOrDissolution(null);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerAmendApplicationType
            .aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getDivorceOrDissolution().equals(""));
    }
}
