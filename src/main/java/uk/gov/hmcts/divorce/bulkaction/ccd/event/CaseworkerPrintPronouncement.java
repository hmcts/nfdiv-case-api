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

import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerPrintPronouncement implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {
    public static final String CASEWORKER_PRINT_PRONOUNCEMENT = "caseworker-print-for-pronouncement";

    @Autowired
    private ScheduleCaseService scheduleCaseService;

    @Autowired
    private PronouncementListDocService pronouncementListDocService;

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {

        new BulkActionPageBuilder(configBuilder
            .event(CASEWORKER_PRINT_PRONOUNCEMENT)
            .forState(Listed)
            .name("Print for pronouncement")
            .description("Print for pronouncement")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .aboutToStartCallback(this::aboutToStart)
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SYSTEMUPDATE)
            .grantHistoryOnly(LEGAL_ADVISOR))
            .page("printPronouncement")
            .pageLabel("Print Cases for Pronouncement")
            .mandatory(BulkActionCaseData::getPronouncementJudge, null, "District Judge");
    }

    public SubmittedCallbackResponse submitted(
        CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails,
        CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails
    ) {
        scheduleCaseService.updatePronouncementJudgeDetailsForCasesInBulk(bulkCaseDetails);
        return SubmittedCallbackResponse.builder().build();
    }

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> aboutToSubmit(
        CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails,
        CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetailsBefore) {

        log.info("{} about to submit callback invoked for Case Id: {}", CASEWORKER_PRINT_PRONOUNCEMENT, bulkCaseDetails.getId());

        final BulkActionCaseData caseData = bulkCaseDetails.getData();

        pronouncementListDocService.generateDocument(bulkCaseDetails);

        return AboutToStartOrSubmitResponse.<BulkActionCaseData, BulkActionState>builder()
            .data(caseData)
            .build();
    }


    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> aboutToStart(
        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails
    ) {

        log.info("{} about to start callback invoked for Case Id: {}", CASEWORKER_PRINT_PRONOUNCEMENT, bulkCaseDetails.getId());

        final BulkActionCaseData caseData = bulkCaseDetails.getData();

        if (null == caseData.getPronouncementJudge()) {
            caseData.setPronouncementJudge("District Judge");
        }

        return AboutToStartOrSubmitResponse.<BulkActionCaseData, BulkActionState>builder()
            .data(caseData)
            .build();
    }
}
