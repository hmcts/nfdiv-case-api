package uk.gov.hmcts.divorce.solicitor.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.event.page.serviceapplicationpages.ServiceApplicationPages;
import uk.gov.hmcts.divorce.solicitor.service.ServiceApplicationDraftSubmissionService;

import java.util.EnumSet;

import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.ALWAYS_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.solicitor.event.page.serviceapplicationpages.ServiceApplicationPages.ALTERNATIVE_SERVICE_APP;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitorAlternativeServiceApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_ALTERNATIVE_SERVICE_APPLICATION = "sol-alternative-service-app";

    private final ServiceApplicationDraftSubmissionService serviceApplicationBuilderService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);

        ServiceApplicationPages.addAlternativeServicePages(pageBuilder, ALWAYS_SHOW);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("{} about to submit callback invoked for Case Id: {}", ALTERNATIVE_SERVICE_APP, details.getId());
        final CaseData caseData = details.getData();
        final Applicant applicant = caseData.getApplicant1();

        InterimApplicationOptions options = applicant.getInterimApplicationOptions();
        options.setInterimApplicationType(InterimApplicationType.ALTERNATIVE_SERVICE);

        serviceApplicationBuilderService.submitFromInterimOptions(details.getId(), caseData, applicant);


        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private PageBuilder addEventConfig(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        var states = EnumSet.complementOf(POST_SUBMISSION_STATES);
        states.add(AosOverdue);

        return new PageBuilder(configBuilder
            .event(SOLICITOR_ALTERNATIVE_SERVICE_APPLICATION)
            .forStates(states)
            .showCondition(
                "alternativeServiceType!=\"deemed\" AND alternativeServiceType!=\"dispensed\" AND alternativeServiceType!=\"bailiff\" "
                    + "AND alternativeServiceType!=\"alternativeService\"")
            .name(ALTERNATIVE_SERVICE_APP)
            .description(ALTERNATIVE_SERVICE_APP)
            .showSummary()
            .showEventNotes()
            .endButtonLabel("Save Application")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, JUDGE, LEGAL_ADVISOR, SUPER_USER));
    }
}
