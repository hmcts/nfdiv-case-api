package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.event.page.UpdateContactDetails;
import uk.gov.hmcts.divorce.caseworker.service.CaseFlagsService;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.ProcessConfidentialDocumentsService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.caseworker.event.page.UpdateContactDetails.TITLE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseworkerUpdateContactDetails implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_UPDATE_CONTACT_DETAILS = "caseworker-update-contact-details";

    private final UpdateContactDetails updateContactDetails;

    private final ProcessConfidentialDocumentsService confidentialDocumentsService;

    private final CaseFlagsService caseFlagsService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        var pageBuilder = addEventConfig(configBuilder);
        updateContactDetails.addTo(pageBuilder);
    }

    private PageBuilder addEventConfig(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder
            .event(CASEWORKER_UPDATE_CONTACT_DETAILS)
            .forStates(POST_SUBMISSION_STATES)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .name(TITLE)
            .description(TITLE)
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(
                SUPER_USER,
                LEGAL_ADVISOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Callback invoked for {}, Case Id: {}", CASEWORKER_UPDATE_CONTACT_DETAILS, details.getId());

        var caseData = details.getData();

        confidentialDocumentsService.processDocuments(caseData, details.getId());

        if (hasNameBeenUpdatedForApplicant(beforeDetails.getData().getApplicant1(), caseData.getApplicant1())) {
            caseFlagsService.updatePartyNameInCaseFlags(caseData, CaseFlagsService.PartyFlagType.APPLICANT_1);
        }

        if (hasNameBeenUpdatedForApplicant(beforeDetails.getData().getApplicant2(), caseData.getApplicant2())) {
            caseFlagsService.updatePartyNameInCaseFlags(caseData, CaseFlagsService.PartyFlagType.APPLICANT_2);
        }

        if (caseData.getApplicant1().isRepresented()
            && hasNameBeenUpdatedForSolicitor(beforeDetails.getData().getApplicant1().getSolicitor(),
            details.getData().getApplicant1().getSolicitor())) {
            caseFlagsService.updatePartyNameInCaseFlags(caseData, CaseFlagsService.PartyFlagType.APPLICANT_1_SOLICITOR);
        }

        if (caseData.getApplicant2().isRepresented()
            && hasNameBeenUpdatedForSolicitor(beforeDetails.getData().getApplicant2().getSolicitor(),
            details.getData().getApplicant2().getSolicitor())) {
            caseFlagsService.updatePartyNameInCaseFlags(caseData, CaseFlagsService.PartyFlagType.APPLICANT_2_SOLICITOR);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    boolean hasNameBeenUpdatedForApplicant(Applicant before, Applicant after) {
        return !after.getFullName().equals(before.getFullName());
    }

    boolean hasNameBeenUpdatedForSolicitor(Solicitor before, Solicitor after) {
        return !after.getName().equals(before.getName());
    }
}
