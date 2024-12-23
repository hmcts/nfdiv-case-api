package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.type.ChangeOrganisationRequest;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRemoveFailedSolNoticeOfChange.CASEWORKER_REMOVE_FAILED_SOL_NOC_REQUEST;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRemoveFailedSolNoticeOfChange.NO_NOC_REQUEST_ERROR;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerRemoveFailedSolNoticeOfChangeTest {

    @InjectMocks
    private CaseworkerRemoveFailedSolNoticeOfChange caseworkerRemoveFailedSolNoticeOfChange;

    @Test
    void configure() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerRemoveFailedSolNoticeOfChange.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_REMOVE_FAILED_SOL_NOC_REQUEST);
    }

    @Test
    void shouldReturnValidationErrorWhenNocRequestFieldIsNull() {
        var details = getCaseDetails();

        var result = caseworkerRemoveFailedSolNoticeOfChange.aboutToSubmit(details, details);

        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors()).contains(NO_NOC_REQUEST_ERROR + TEST_CASE_ID);
    }

    @Test
    void shouldNotReturnValidationErrorAndShouldNullNocRequestField() {
        var details = getCaseDetails();
        details.getData().setChangeOrganisationRequestField(new ChangeOrganisationRequest<>());

        var result = caseworkerRemoveFailedSolNoticeOfChange.aboutToSubmit(details, details);

        assertThat(result.getErrors()).isNullOrEmpty();
        assertThat(result.getData().getChangeOrganisationRequestField()).isNull();
    }

    private CaseDetails<CaseData, State> getCaseDetails() {
        final var details = new CaseDetails<CaseData, State>();
        final var data = caseData();
        details.setData(data);
        details.setId(TEST_CASE_ID);

        return details;
    }
}
