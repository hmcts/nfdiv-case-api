package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction.FailedBulkCaseRemover;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
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
    public static final String ERROR_HEARING_DATE_IN_PAST = "Please enter a hearing date and time in the future";
    public static final String ERROR_CASE_IDS_DUPLICATED = "Case IDs duplicated in the list: ";
    public static final String ERROR_REMOVE_DUPLICATES = "Please remove duplicates and try again";
    public static final String ERROR_NO_NEW_CASES_ADDED = "Please add at least one new case to schedule for listing";
    public static final String ERROR_CASE_ID = "Case ID ";
    public static final String ERROR_ALREADY_LINKED_TO_BULK_CASE = " is already linked to bulk case ";

    private static final String SCHEDULE_CASES_FOR_LISTING = "Schedule cases for listing";

    private final ScheduleCaseService scheduleCaseService;
    private final BulkTriggerService bulkTriggerService;
    private final BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;
    private final HttpServletRequest request;
    private final FailedBulkCaseRemover failedBulkCaseRemover;
    private final CcdSearchService ccdSearchService;
    private final ObjectMapper objectMapper;

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        new BulkActionPageBuilder(configBuilder
            .event(CASEWORKER_SCHEDULE_CASE)
            .forStates(Created, Listed)
            .name(SCHEDULE_CASES_FOR_LISTING)
            .description(SCHEDULE_CASES_FOR_LISTING)
            .showSummary()
            .showEventNotes()
            .submittedCallback(this::submitted)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SYSTEMUPDATE))
            .page("scheduleForListing", this::midEvent)
            .pageLabel(SCHEDULE_CASES_FOR_LISTING)
            .mandatory(BulkActionCaseData::getCourt)
            .mandatory(BulkActionCaseData::getDateAndTimeOfHearing)
            .mandatoryNoSummary(BulkActionCaseData::getBulkListCaseDetails);
    }

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> midEvent(
        CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails,
        CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails
    ) {
        log.info("{} mid event callback invoked for Case Id: {}", CASEWORKER_SCHEDULE_CASE, bulkCaseDetails.getId());

        final List<String> errors = validateData(bulkCaseDetails.getData(), beforeDetails.getData());
        if (!errors.isEmpty()) {
            return AboutToStartOrSubmitResponse
                .<BulkActionCaseData, BulkActionState>builder()
                .errors(errors)
                .data(bulkCaseDetails.getData())
                .build();
        }

        return AboutToStartOrSubmitResponse
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkCaseDetails.getData())
            .state(Listed)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails,
                                               CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails) {

        log.info("{} submitted callback invoked for Case Id: {}", CASEWORKER_SCHEDULE_CASE, bulkCaseDetails.getId());

        User user = idamService.retrieveUser(request.getHeader(AUTHORIZATION));
        String serviceAuth = authTokenGenerator.generate();

        if (user.getUserDetails().getRoles().contains(CASE_WORKER.getRole())) {
            User systemUser = idamService.retrieveSystemUpdateUserDetails();

            final List<ListValue<BulkListCaseDetails>> failedAwaitingPronouncementCases = bulkTriggerService.bulkTrigger(
                    bulkCaseDetails.getData().getBulkListCaseDetails(),
                    SYSTEM_LINK_WITH_BULK_CASE,
                    bulkCaseCaseTaskFactory.getCaseTask(bulkCaseDetails, SYSTEM_LINK_WITH_BULK_CASE),
                    systemUser,
                    serviceAuth);

            failedBulkCaseRemover.removeFailedCasesFromBulkListCaseDetails(
                    failedAwaitingPronouncementCases,
                    bulkCaseDetails,
                    systemUser,
                    serviceAuth
            );
        }

        scheduleCaseService.updateCourtHearingDetailsForCasesInBulk(bulkCaseDetails);
        return SubmittedCallbackResponse.builder().build();
    }

    private List<String> checkForDuplicates(final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails) {
        final List<String> caseIds = bulkListCaseDetails.stream()
            .map(caseDetails -> caseDetails.getValue().getCaseReference().getCaseReference())
            .toList();
        final List<String> duplicateCaseIds = new ArrayList<>();
        final Set<String> duplicateCheckSet = new java.util.HashSet<>();
        for (String caseId : caseIds) {
            if (duplicateCheckSet.contains(caseId)) {
                duplicateCaseIds.add(caseId);
            } else {
                duplicateCheckSet.add(caseId);
            }
        }
        return duplicateCaseIds;
    }

    private List<String> getNewCaseIds(final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails,
                                       final List<ListValue<BulkListCaseDetails>> beforeBulkListCaseDetails) {
        if (beforeBulkListCaseDetails == null) {
            return bulkListCaseDetails.stream()
                .map(caseDetails -> caseDetails.getValue().getCaseReference().getCaseReference())
                .toList();
        }

        return bulkListCaseDetails.stream()
            .filter(caseDetails -> !beforeBulkListCaseDetails.contains(caseDetails))
            .toList()
            .stream()
            .map(caseDetails -> caseDetails.getValue().getCaseReference().getCaseReference())
            .toList();
    }

    private List<String> validateData(final BulkActionCaseData bulkActionCaseData, final BulkActionCaseData beforeBulkActionCaseData) {
        final List<String> errors = new ArrayList<>();

        if (bulkActionCaseData.getDateAndTimeOfHearing().isBefore(LocalDateTime.now())) {
            errors.add(ERROR_HEARING_DATE_IN_PAST);
        }

        final List<String> duplicateCaseIds = checkForDuplicates(bulkActionCaseData.getBulkListCaseDetails());
        if (!duplicateCaseIds.isEmpty()) {
            errors.add(ERROR_CASE_IDS_DUPLICATED + String.join(", ", duplicateCaseIds));
            errors.add(ERROR_REMOVE_DUPLICATES);
            return errors;
        }

        final List<String> newCaseIds = getNewCaseIds(
            bulkActionCaseData.getBulkListCaseDetails(),
            beforeBulkActionCaseData.getBulkListCaseDetails()
        );

        if (newCaseIds.isEmpty()) {
            errors.add(ERROR_NO_NEW_CASES_ADDED);
            return errors;
        }

        final List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> caseDetailsList = ccdSearchService.searchForCases(
            newCaseIds,
            idamService.retrieveSystemUpdateUserDetails(),
            authTokenGenerator.generate()
        );

        if (!caseDetailsList.isEmpty()) {
            caseDetailsList.forEach(caseDetails -> {
                CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
                if (caseData.getBulkListCaseReferenceLink() != null) {
                    final String bulkCaseRef = caseData.getBulkListCaseReferenceLink().getCaseReference();
                    errors.add(ERROR_CASE_ID + caseDetails.getId() + ERROR_ALREADY_LINKED_TO_BULK_CASE + bulkCaseRef);
                }
            });
        }

        return errors;
    }
}
