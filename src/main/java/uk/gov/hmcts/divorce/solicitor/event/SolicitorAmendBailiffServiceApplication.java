package uk.gov.hmcts.divorce.solicitor.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.divorcecase.validation.ServiceApplicationValidation;
import uk.gov.hmcts.divorce.solicitor.event.page.AmendServiceApplicationActionPage;
import uk.gov.hmcts.divorce.solicitor.event.page.BailiffServicePaymentMethodPage;
import uk.gov.hmcts.divorce.solicitor.event.page.BailiffServiceRespondentDescriptionPage;
import uk.gov.hmcts.divorce.solicitor.event.page.BailiffServiceRespondentHistoryPage;
import uk.gov.hmcts.divorce.solicitor.event.page.BailiffServiceRespondentHistoryThreePage;
import uk.gov.hmcts.divorce.solicitor.event.page.BailiffServiceRespondentHistoryTwoPage;
import uk.gov.hmcts.divorce.solicitor.event.page.BailiffServiceRespondentNameAddressPage;
import uk.gov.hmcts.divorce.solicitor.event.page.BailiffServiceRespondentPhoneAgePage;
import uk.gov.hmcts.divorce.solicitor.event.page.BailiffServiceRespondentServiceTimeVehiclePage;
import uk.gov.hmcts.divorce.solicitor.event.page.BailiffServiceUploadPhotoPage;
import uk.gov.hmcts.divorce.solicitor.service.ServiceApplicationDraftSubmissionService;

import java.util.List;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
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
public class SolicitorAmendBailiffServiceApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_AMEND_BAILIFF_SERVICE_APPLICATION = "solicitor-amend-bailiff-app";

    private final ServiceApplicationDraftSubmissionService serviceApplicationBuilderService;

    private static final String AMEND_SHOW_CONDITION = "applicant1DraftApplicationAction=\"amend\"";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);

        new AmendServiceApplicationActionPage().addTo(pageBuilder);
        final List<CcdPageConfiguration> pages = List.of(
            new BailiffServicePaymentMethodPage(),
            new BailiffServiceRespondentNameAddressPage(),
            new BailiffServiceRespondentPhoneAgePage(),
            new BailiffServiceRespondentDescriptionPage(),
            new BailiffServiceRespondentServiceTimeVehiclePage(),
            new BailiffServiceRespondentHistoryPage(),
            new BailiffServiceRespondentHistoryTwoPage(),
            new BailiffServiceRespondentHistoryThreePage(),
            new BailiffServiceUploadPhotoPage()
        );

        pages.forEach(page -> page.addWithShowCondition(pageBuilder, AMEND_SHOW_CONDITION));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("{} About to start callback invoked for Case Id: {}", SOLICITOR_AMEND_BAILIFF_SERVICE_APPLICATION, details.getId());

        final CaseData caseData = details.getData();

        List<String> validationError = ServiceApplicationValidation.validateNotAlreadySubmitted(caseData);

        if (!validationError.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(validationError)
                .build();
        }

        final Applicant applicant2 = caseData.getApplicant2();

        if (applicant2.isConfidentialContactDetails()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .build();
        }

        applicant2.setNonConfidentialAddress(applicant2.getAddress());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {

        log.info("{} about to submit callback invoked for Case Id: {}", SOLICITOR_AMEND_BAILIFF_SERVICE_APPLICATION, details.getId());

        final CaseData caseData = details.getData();
        final Applicant applicant1 = caseData.getApplicant1();
        final Applicant applicant2 = caseData.getApplicant2();

        applicant1.getInterimApplicationOptions().setDraftApplicationAction(null);
        applicant2.setNonConfidentialAddress(null);

        serviceApplicationBuilderService.submitFromInterimOptions(details.getId(), caseData, applicant1);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private PageBuilder addEventConfig(
        final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder
            .event(SOLICITOR_AMEND_BAILIFF_SERVICE_APPLICATION)
            .forState(AosOverdue)
            .name("Amend Service App")
            .description("Amend Service App")
            .showCondition("serviceApplicationSubmittedOnline=\"Yes\" AND alternativeServiceType=\"bailiff\"")
            .showSummary()
            .showEventNotes()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .endButtonLabel("Submit")
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE));
    }
}
