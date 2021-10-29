package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionPageBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.service.ScheduleCaseService;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import javax.servlet.http.HttpServletRequest;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Created;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

public class CaseworkerEditBulkCase implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {

    public static final String CASEWORKER_EDIT_BULK_CASE = "caseworker-edit-bulk-case";

    @Autowired
    private ScheduleCaseService scheduleCaseService;

    @Autowired
    private HttpServletRequest request;

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        new BulkActionPageBuilder(configBuilder
            .event(CASEWORKER_EDIT_BULK_CASE)
            .forStates(Created, Listed)
            .name("Edit bulk case")
            .description("Edit bulk case")
            .showSummary()
            .showEventNotes()
            .submittedCallback(this::submitted)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SYSTEMUPDATE))
            .page("editBulkCase")
            .pageLabel("Edit bulk case")
            .mandatory(BulkActionCaseData::getCourtName)
            .mandatory(BulkActionCaseData::getDateAndTimeOfHearing);
    }

    public SubmittedCallbackResponse submitted(
        CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails,
        CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails
    ) {
        scheduleCaseService.updateCourtHearingDetailsForCasesInBulk(bulkCaseDetails, request.getHeader(AUTHORIZATION));
        return SubmittedCallbackResponse.builder().build();
    }
}
