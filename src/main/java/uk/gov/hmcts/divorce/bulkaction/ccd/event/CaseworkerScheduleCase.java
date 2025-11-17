package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionPageBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.BulkTriggerService;
import uk.gov.hmcts.divorce.bulkaction.service.ScheduleCaseService;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseCaseTaskFactory;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction.FailedBulkCaseRemover;
import uk.gov.hmcts.divorce.systemupdate.service.BulkCaseValidationService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.EnumSet;
import java.util.List;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Created;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemLinkWithBulkCase.SYSTEM_LINK_WITH_BULK_CASE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseworkerScheduleCase implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {
    public static final String CASEWORKER_SCHEDULE_CASE = "caseworker-schedule-case";
    private static final String SCHEDULE_CASES_FOR_LISTING = "Schedule cases for listing";

    private final ScheduleCaseService scheduleCaseService;
    private final BulkTriggerService bulkTriggerService;
    private final BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;
    private final HttpServletRequest request;
    private final FailedBulkCaseRemover failedBulkCaseRemover;
    private final BulkCaseValidationService bulkCaseValidationService;

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        new BulkActionPageBuilder(configBuilder
            .event(CASEWORKER_SCHEDULE_CASE)
            .forStateTransition(EnumSet.of(Created, Listed), Listed)
            .name(SCHEDULE_CASES_FOR_LISTING)
            .description(SCHEDULE_CASES_FOR_LISTING)
            .showSummary()
            .showEventNotes()
            .aboutToStartCallback(this::aboutToStart)
            .submittedCallback(this::submitted)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SYSTEMUPDATE))
            .page("scheduleForListing", this::midEvent)
            .pageLabel(SCHEDULE_CASES_FOR_LISTING)
            .mandatory(BulkActionCaseData::getCourt)
            .mandatory(BulkActionCaseData::getDateAndTimeOfHearing)
            .mandatoryNoSummary(BulkActionCaseData::getBulkListCaseDetails);
    }

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> aboutToStart(final CaseDetails<BulkActionCaseData,
        BulkActionState> bulkDetails) {

        log.info("{} aboutToStart-callback invoked for case id: {}", CASEWORKER_SCHEDULE_CASE, bulkDetails.getId());

        List<String> validationErrors = bulkCaseValidationService.validateBulkListErroredCases(bulkDetails);
        if (!isEmpty(validationErrors)) {
            return AboutToStartOrSubmitResponse
                .<BulkActionCaseData, BulkActionState>builder()
                .errors(validationErrors)
                .data(bulkDetails.getData())
                .build();
        }

        return AboutToStartOrSubmitResponse.<BulkActionCaseData, BulkActionState>builder()
            .data(bulkDetails.getData())
            .build();
    }

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> midEvent(
        CaseDetails<BulkActionCaseData, BulkActionState> bulkDetails,
        CaseDetails<BulkActionCaseData, BulkActionState> beforeBulkDetails
    ) {
        log.info("{} mid event callback invoked for Case Id: {}", CASEWORKER_SCHEDULE_CASE, bulkDetails.getId());

        final List<String> errors = bulkCaseValidationService.validateData(
            bulkDetails.getData(),
            beforeBulkDetails.getData(),
            bulkDetails.getId()
        );
        if (!errors.isEmpty()) {
            return AboutToStartOrSubmitResponse
                .<BulkActionCaseData, BulkActionState>builder()
                .errors(errors)
                .data(bulkDetails.getData())
                .build();
        }

        return AboutToStartOrSubmitResponse.<BulkActionCaseData, BulkActionState>builder().build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<BulkActionCaseData, BulkActionState> bulkDetails,
                                               CaseDetails<BulkActionCaseData, BulkActionState> beforeBulkDetails) {

        log.info("{} submitted callback invoked for Case Id: {}", CASEWORKER_SCHEDULE_CASE, bulkDetails.getId());

        User user = idamService.retrieveUser(request.getHeader(AUTHORIZATION));
        String serviceAuth = authTokenGenerator.generate();

        if (user.getUserDetails().getRoles().contains(CASE_WORKER.getRole())) {
            User systemUser = idamService.retrieveSystemUpdateUserDetails();

            final List<ListValue<BulkListCaseDetails>> failedAwaitingPronouncementCases = bulkTriggerService.bulkTrigger(
                    bulkDetails.getData().getBulkListCaseDetails(),
                    SYSTEM_LINK_WITH_BULK_CASE,
                    bulkCaseCaseTaskFactory.getCaseTask(bulkDetails, SYSTEM_LINK_WITH_BULK_CASE),
                    systemUser,
                    serviceAuth);

            failedBulkCaseRemover.removeFailedCasesFromBulkListCaseDetails(
                    failedAwaitingPronouncementCases,
                    bulkDetails,
                    systemUser,
                    serviceAuth
            );
        }

        scheduleCaseService.updateCourtHearingDetailsForCasesInBulk(bulkDetails);
        return SubmittedCallbackResponse.builder().build();
    }
}
