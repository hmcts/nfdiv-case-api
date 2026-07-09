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
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.event.page.DeemedServiceConfirmPage;
import uk.gov.hmcts.divorce.solicitor.event.page.DeemedServiceDetailsAndUploadPage;
import uk.gov.hmcts.divorce.solicitor.event.page.DeemedServicePaymentPage;
import uk.gov.hmcts.divorce.solicitor.service.ServiceApplicationSubmissionService;

import java.util.List;

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
public class SolicitorDeemedServiceApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_DEEMED_SERVICE_APPLICATION = "solicitor-deemed-service-application";

    private final ServiceApplicationSubmissionService serviceApplicationBuilderService;


    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);

        final List<CcdPageConfiguration> pages = List.of(new DeemedServiceConfirmPage(), new DeemedServicePaymentPage(),
            new DeemedServiceDetailsAndUploadPage());

        pages.forEach(page -> page.addTo(pageBuilder));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("{} about to submit callback invoked for Case Id: {}", SOLICITOR_DEEMED_SERVICE_APPLICATION, details.getId());
        final CaseData caseData = details.getData();
        final Applicant applicant = caseData.getApplicant1();

        InterimApplicationOptions options = applicant.getInterimApplicationOptions();
        options.setInterimApplicationType(InterimApplicationType.DEEMED_SERVICE);

        serviceApplicationBuilderService.submitFromInterimOptions(details.getId(), caseData, applicant);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }


    private PageBuilder addEventConfig(
        final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder
            .event(SOLICITOR_DEEMED_SERVICE_APPLICATION)
            .forState(AosOverdue)
            .name("Deemed service App")
            .description("Deemed service App")
            .showCondition("alternativeServiceType!=\"*\"")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .endButtonLabel("Save Application")
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE));
    }
}
