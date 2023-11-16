package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SuperuserRemoveErroredCases.SUPERUSER_REMOVE_ERRORED_CASES;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createBulkActionConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class SuperuserRemoveErroredCasesTest {

    @InjectMocks
    private SuperuserRemoveErroredCases superuserRemoveErroredCases;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<BulkActionCaseData, BulkActionState, UserRole> configBuilder = createBulkActionConfigBuilder();

        superuserRemoveErroredCases.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SUPERUSER_REMOVE_ERRORED_CASES);
    }

    @Test
    void shouldRemoveCasesInAboutToSubmit() {
        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder().build();
        bulkActionCaseData.setProcessedCaseDetails(new ArrayList<>());

        final var caseDetails = new CaseDetails<BulkActionCaseData, BulkActionState>();
        caseDetails.setData(bulkActionCaseData);

        final var response = superuserRemoveErroredCases.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getErroredCaseDetails()).isNull();
    }

}
