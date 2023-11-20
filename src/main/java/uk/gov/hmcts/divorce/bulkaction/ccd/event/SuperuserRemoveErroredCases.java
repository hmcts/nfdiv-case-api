package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionPageBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.Optional;

import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Created;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Pronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class SuperuserRemoveErroredCases implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {

    public static final String SUPERUSER_REMOVE_ERRORED_CASES = "superuser-remove-errored-cases";

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        new BulkActionPageBuilder(configBuilder
            .event(SUPERUSER_REMOVE_ERRORED_CASES)
            .forStates(Created, Listed, Pronounced)
            .name("Remove errored cases")
            .description("Remove errored cases")
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE_DELETE, SUPER_USER, SYSTEMUPDATE)
            .grantHistoryOnly(CASE_WORKER));
    }

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> aboutToSubmit(
        CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails,
        CaseDetails<BulkActionCaseData, BulkActionState> details) {

        var processed = Optional
            .ofNullable(details.getData().getProcessedCaseDetails())
            .orElse(Lists.newArrayList());

        var errored = Optional
            .ofNullable(details.getData().getErroredCaseDetails())
            .orElse(Lists.newArrayList());

        processed.addAll(errored);

        details.getData().setProcessedCaseDetails(processed);
        details.getData().setErroredCaseDetails(null);

        return AboutToStartOrSubmitResponse.<BulkActionCaseData, BulkActionState>builder()
            .data(details.getData())
            .build();
    }

}
