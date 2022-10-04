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
import uk.gov.hmcts.divorce.bulkaction.service.ScheduleCaseService;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Created;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerScheduleCase implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {
    public static final String CASEWORKER_SCHEDULE_CASE = "caseworker-schedule-case";

    @Autowired
    private ScheduleCaseService scheduleCaseService;

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        new BulkActionPageBuilder(configBuilder
            .event(CASEWORKER_SCHEDULE_CASE)
            .forStateTransition(Created, Listed)
            .name("Schedule cases for listing")
            .description("Schedule cases for listing")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SYSTEMUPDATE))
            .page("scheduleForListing")
            .pageLabel("Schedule cases for listing")
            .mandatory(BulkActionCaseData::getCourt)
            .mandatory(BulkActionCaseData::getDateAndTimeOfHearing)
            .mandatoryNoSummary(BulkActionCaseData::getBulkListCaseDetails);
    }

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> aboutToSubmit(
        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails,
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails
    ) {

        log.info("{} about to submit callback invoked for Case Id: {}", CASEWORKER_SCHEDULE_CASE, bulkCaseDetails.getId());

        if (bulkCaseDetails.getData().getDateAndTimeOfHearing().isBefore(LocalDateTime.now())) {
            return AboutToStartOrSubmitResponse
                .<BulkActionCaseData, BulkActionState>builder()
                .errors(List.of("Please enter a hearing date and time in the future"))
                .data(bulkCaseDetails.getData())
                .build();
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

        log.info("{} submitted callback invoked for Case Id: {}", CASEWORKER_SCHEDULE_CASE, bulkCaseDetails.getId());

        scheduleCaseService.updateCourtHearingDetailsForCasesInBulk(bulkCaseDetails);
        return SubmittedCallbackResponse.builder().build();
    }
}
