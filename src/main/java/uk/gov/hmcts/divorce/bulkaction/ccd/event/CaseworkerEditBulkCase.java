package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionPageBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.service.PronouncementListDocService;
import uk.gov.hmcts.divorce.bulkaction.service.ScheduleCaseService;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Created;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerEditBulkCase implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {

    public static final String CASEWORKER_EDIT_BULK_CASE = "caseworker-edit-bulk-case";

    @Autowired
    private ScheduleCaseService scheduleCaseService;

    @Autowired
    private PronouncementListDocService pronouncementListDocService;

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        new BulkActionPageBuilder(configBuilder
            .event(CASEWORKER_EDIT_BULK_CASE)
            .forStates(Created, Listed)
            .name("Edit bulk case")
            .description("Edit bulk case")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SYSTEMUPDATE)
            .grantHistoryOnly(LEGAL_ADVISOR, JUDGE))
            .page("editBulkCase")
            .pageLabel("Edit bulk case")
            .mandatory(BulkActionCaseData::getCourt)
            .mandatory(BulkActionCaseData::getDateAndTimeOfHearing);
    }

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> aboutToSubmit(
        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails,
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails
    ) {

        log.info("{} about to submit callback invoked for Case Id: {}", CASEWORKER_EDIT_BULK_CASE, bulkCaseDetails.getId());

        if (bulkCaseDetails.getData().getDateAndTimeOfHearing().isBefore(LocalDateTime.now())) {
            return AboutToStartOrSubmitResponse
                .<BulkActionCaseData, BulkActionState>builder()
                .errors(List.of("Please enter a hearing date and time in the future"))
                .data(bulkCaseDetails.getData())
                .build();
        }

        if (bulkCaseDetails.getData().getPronouncementListDocument() != null) {
            log.info("Regenerating Pronouncement List document for bulk case {}", bulkCaseDetails.getId());
            pronouncementListDocService.generateDocument(bulkCaseDetails);
        }

        return AboutToStartOrSubmitResponse
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkCaseDetails.getData())
            .build();
    }

    public SubmittedCallbackResponse submitted(
        CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails,
        CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails
    ) {

        log.info("{} submitted callback invoked for Case Id: {}", CASEWORKER_EDIT_BULK_CASE, bulkCaseDetails.getId());

        scheduleCaseService.updateCourtHearingDetailsForCasesInBulk(bulkCaseDetails);
        return SubmittedCallbackResponse.builder().build();
    }
}
