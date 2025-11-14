package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.LocalDateTime;
<<<<<<< Updated upstream
import java.util.List;
=======
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
>>>>>>> Stashed changes

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Created;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.flattenLists;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateBulkListErroredCases;
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

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        new BulkActionPageBuilder(configBuilder
            .event(CASEWORKER_SCHEDULE_CASE)
            .forStates(Created, Listed)
            .name(SCHEDULE_CASES_FOR_LISTING)
            .description(SCHEDULE_CASES_FOR_LISTING)
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .aboutToStartCallback(this::aboutToStart)
            .submittedCallback(this::submitted)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SYSTEMUPDATE))
            .page("scheduleForListing")
            .pageLabel(SCHEDULE_CASES_FOR_LISTING)
            .mandatory(BulkActionCaseData::getCourt)
            .mandatory(BulkActionCaseData::getDateAndTimeOfHearing)
            .mandatoryNoSummary(BulkActionCaseData::getBulkListCaseDetails);
    }

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> aboutToStart(final CaseDetails<BulkActionCaseData,
        BulkActionState> bulkCaseDetails) {

        log.info("{} aboutToStart-callback invoked for case id: {}", CASEWORKER_SCHEDULE_CASE, bulkCaseDetails.getId());

        List<String> validationErrors = validateBulkListErroredCases(bulkCaseDetails);

        if (!isEmpty(validationErrors)) {
            return AboutToStartOrSubmitResponse
                .<BulkActionCaseData, BulkActionState>builder()
                .errors(validationErrors)
                .data(bulkCaseDetails.getData())
                .build();
        }

        return AboutToStartOrSubmitResponse.<BulkActionCaseData, BulkActionState>builder()
            .data(bulkCaseDetails.getData())
            .build();
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
<<<<<<< Updated upstream
=======

    private List<String> validateData(final BulkActionCaseData bulkData, final BulkActionCaseData beforeBulkData, final Long bulkCaseId) {
        final Set<String> beforeUniqueCaseRefs = new HashSet<>(beforeBulkData.getCaseReferences());
        final Set<String> afterUniqueCaseRefs = new HashSet<>(bulkData.getCaseReferences());

        Set<String> newlyAddedCaseReferences = new HashSet<>(afterUniqueCaseRefs);
        newlyAddedCaseReferences.removeAll(beforeUniqueCaseRefs);

        return flattenLists(
            validateHearingDate(bulkData),
            validateCasesNotRemoved(afterUniqueCaseRefs, beforeUniqueCaseRefs),
            validateDuplicates(bulkData.getCaseReferences(), afterUniqueCaseRefs),
            validateLinkedCases(newlyAddedCaseReferences, bulkCaseId)
        );
    }

    private List<String> validateHearingDate(final BulkActionCaseData bulkData) {
        return bulkData.getDateAndTimeOfHearing().isBefore(LocalDateTime.now()) ?
            List.of("Please do not use a hearing date in the past") :
            Collections.emptyList();
    }

    private List<String> validateCasesNotRemoved(final Set<String> afterCaseRefs, final Set<String> beforeCaseRefs) {
        Set<String> removedCases = new HashSet<>(beforeCaseRefs);
        removedCases.removeAll(afterCaseRefs);

        return !removedCases.isEmpty()
            ? List.of("Please do not remove cases from the list.")
            : Collections.emptyList();
    }

    private List<String> validateDuplicates(List<String> afterCaseReferences, Set<String> uniqueCaseReferences) {
        boolean hasDuplicates = uniqueCaseReferences.size() < afterCaseReferences.size();

        return hasDuplicates ?
            List.of("Please remove duplicate case references from the bulk list.") :
            Collections.emptyList();
    }

    private List<String> validateLinkedCases(Set<String> addedCaseRefs, Long bulkCaseId) {
        if (addedCaseRefs.isEmpty()) {
            return Collections.emptyList();
        }

        final List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> caseSearchResults = ccdSearchService.searchForCases(
            addedCaseRefs.stream().toList(),
            idamService.retrieveSystemUpdateUserDetails(),
            authTokenGenerator.generate()
        );

        List<String> errors = new ArrayList<>();

        Set<String> missingCaseRefs = new HashSet<>(addedCaseRefs);
        caseSearchResults.forEach(caseDetails -> {
            missingCaseRefs.remove(caseDetails.getId().toString());
            errors.addAll(validateLinkedCaseDetails(caseDetails, bulkCaseId));
        });

        errors.add(String.format(
            "Some cases were not found: %s", String.join(", ", missingCaseRefs)
        ));

        return errors;
    }

    private List<String> validateLinkedCaseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails details, Long bulkCaseId) {
        List<String> errors = new ArrayList<>();
        long caseId = details.getId();

        if (!AwaitingPronouncement.toString().equals(details.getState())) {
            errors.add(String.format("Case %s is not in the correct state for bulk list processing.", caseId));
        }

        CaseLink bulkCaseLink = objectMapper.convertValue(details.getData(), CaseData.class)
            .getBulkListCaseReferenceLink();

        if (bulkCaseLink != null && !bulkCaseLink.getCaseReference().equals(bulkCaseId.toString())) {
            errors.add(String.format("Case %s is already linked to bulk list %s.", caseId, bulkCaseLink.getCaseReference()));
        }

        return errors;
    }
>>>>>>> Stashed changes
}
