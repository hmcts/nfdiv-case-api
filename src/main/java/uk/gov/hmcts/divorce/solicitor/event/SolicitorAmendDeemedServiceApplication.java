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
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.event.page.AmendDeemedServiceApplicationActionPage;
import uk.gov.hmcts.divorce.solicitor.event.page.DeemedServiceConfirmPage;
import uk.gov.hmcts.divorce.solicitor.event.page.DeemedServiceDetailsAndUploadPage;
import uk.gov.hmcts.divorce.solicitor.event.page.DeemedServicePaymentPage;
import uk.gov.hmcts.divorce.solicitor.service.ServiceApplicationDraftSubmissionService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitorAmendDeemedServiceApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_AMEND_DEEMED_SERVICE_APPLICATION = "solicitor-amend-deemed-application";

    private final ServiceApplicationDraftSubmissionService serviceApplicationBuilderService;

    private static  final String ERROR_ALREADY_SUBMITTED
        = "The ongoing service application on this case has already been submitted and you cannot submit it again or amend it.";

    private static final String AMEND_SHOW_CONDITION = "applicant1DraftServiceApplicationAction=\"amend\"";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);

        final List<CcdPageConfiguration> pages = List.of(new AmendDeemedServiceApplicationActionPage(),
            new DeemedServiceConfirmPage(),
            new DeemedServicePaymentPage(),
            new DeemedServiceDetailsAndUploadPage());

        pages.forEach(page -> page.addWithShowCondition(pageBuilder, AMEND_SHOW_CONDITION));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {

        log.info("{} about to submit callback invoked for Case Id: {}", SOLICITOR_AMEND_DEEMED_SERVICE_APPLICATION, details.getId());

        final CaseData caseData = details.getData();
        final Applicant applicant = caseData.getApplicant1();

        serviceApplicationBuilderService.submitFromInterimOptions(details.getId(), caseData, applicant);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("{} About to Submit callback invoked for Case Id: {}", SOLICITOR_AMEND_DEEMED_SERVICE_APPLICATION, details.getId());

        CaseData caseData = details.getData();
        boolean alreadySubmitted = Optional.ofNullable(caseData.getAlternativeService())
            .map(AlternativeService::getServicePaymentFee)
            .map(fee -> hasText(fee.getPaymentReference()) || hasText(fee.getHelpWithFeesReferenceNumber()))
            .orElse(false);

        if (alreadySubmitted) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList(ERROR_ALREADY_SUBMITTED))
                .build();
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData).build();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }



    private PageBuilder addEventConfig(
        final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder
            .event(SOLICITOR_AMEND_DEEMED_SERVICE_APPLICATION)
            .forState(AosOverdue)
            .name("Amend Service App")
            .description("Amend Service App")
            .showCondition("serviceApplicationSubmittedOnline=\"Yes\" AND alternativeServiceType=\"*\"")
            .showSummary()
            .showEventNotes()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .endButtonLabel("Save Application")
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE));
    }
}
