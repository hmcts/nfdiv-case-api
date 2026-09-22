package uk.gov.hmcts.divorce.solicitor.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.divorcecase.validation.ServiceApplicationValidation;
import uk.gov.hmcts.divorce.solicitor.event.page.AmendServiceApplicationActionPage;
import uk.gov.hmcts.divorce.solicitor.event.page.dispense.DispenseWithServiceAddressAfterParting;
import uk.gov.hmcts.divorce.solicitor.event.page.dispense.DispenseWithServiceAnyChildrenAndContact;
import uk.gov.hmcts.divorce.solicitor.event.page.dispense.DispenseWithServiceAnyFriendsAndRelatives;
import uk.gov.hmcts.divorce.solicitor.event.page.dispense.DispenseWithServiceConfirmPage;
import uk.gov.hmcts.divorce.solicitor.event.page.dispense.DispenseWithServiceExistingDecree;
import uk.gov.hmcts.divorce.solicitor.event.page.dispense.DispenseWithServiceLastKnownEmployer;
import uk.gov.hmcts.divorce.solicitor.event.page.dispense.DispenseWithServiceLastSeenOrHeard;
import uk.gov.hmcts.divorce.solicitor.event.page.dispense.DispenseWithServiceLivedTogetherAnyTime;
import uk.gov.hmcts.divorce.solicitor.event.page.dispense.DispenseWithServiceRespondentEmailAddress;
import uk.gov.hmcts.divorce.solicitor.event.page.dispense.DispenseWithServiceRespondentTelephone;
import uk.gov.hmcts.divorce.solicitor.event.page.dispense.DispenseWithServiceSearchingOnline;
import uk.gov.hmcts.divorce.solicitor.event.page.dispense.DispenseWithServiceTracingAgents;
import uk.gov.hmcts.divorce.solicitor.event.page.dispense.DispenseWithServiceTracingOnline;
import uk.gov.hmcts.divorce.solicitor.event.page.dispense.DispenseWithServiceUploadPage;
import uk.gov.hmcts.divorce.solicitor.service.ServiceApplicationDraftSubmissionService;

import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.SOLICITOR_SERVICE_APPLICATION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitorAmendDispenseServiceApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_AMEND_DISPENSE_WITH_SERVICE_APPLICATION = "solicitor-amend-dispense-application";

    private final ServiceApplicationDraftSubmissionService serviceApplicationBuilderService;

    private static final String AMEND_SHOW_CONDITION = "applicant1DraftApplicationAction=\"amend\"";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);

        new AmendServiceApplicationActionPage().addTo(pageBuilder);
        final List<CcdPageConfiguration> pages = List.of(
            new DispenseWithServiceConfirmPage(),
            new DispenseWithServiceLivedTogetherAnyTime(),
            new DispenseWithServiceAddressAfterParting(),
            new DispenseWithServiceLastSeenOrHeard(),
            new DispenseWithServiceExistingDecree(),
            new DispenseWithServiceRespondentEmailAddress(),
            new DispenseWithServiceRespondentTelephone(),
            new DispenseWithServiceTracingAgents(),
            new DispenseWithServiceTracingOnline(),
            new DispenseWithServiceSearchingOnline(),
            new DispenseWithServiceLastKnownEmployer(),
            new DispenseWithServiceAnyChildrenAndContact(),
            new DispenseWithServiceAnyFriendsAndRelatives(),
            new DispenseWithServiceUploadPage()
        );

        pages.forEach(page -> page.addWithShowCondition(pageBuilder, AMEND_SHOW_CONDITION));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("{} About to start callback invoked for Case Id: {}", SOLICITOR_AMEND_DISPENSE_WITH_SERVICE_APPLICATION, details.getId());

        List<String> validationError = ServiceApplicationValidation.validateNotAlreadySubmitted(details.getData());

        if (!validationError.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(validationError)
                .build();
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData()).build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {

        log.info("{} about to submit callback invoked for Case Id: {}", SOLICITOR_AMEND_DISPENSE_WITH_SERVICE_APPLICATION, details.getId());

        final CaseData caseData = details.getData();
        final Applicant applicant = caseData.getApplicant1();

        serviceApplicationBuilderService.submitFromInterimOptions(details.getId(), caseData, applicant);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private PageBuilder addEventConfig(
        final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder
            .event(SOLICITOR_AMEND_DISPENSE_WITH_SERVICE_APPLICATION)
            .forStates(SOLICITOR_SERVICE_APPLICATION_STATES)
            .name("Amend Service App")
            .description("Amend Service App")
            .showCondition("serviceApplicationSubmittedOnline=\"Yes\" AND alternativeServiceType=\"dispensed\"")
            .showSummary()
            .showEventNotes()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .endButtonLabel("Save Application")
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE));
    }
}
