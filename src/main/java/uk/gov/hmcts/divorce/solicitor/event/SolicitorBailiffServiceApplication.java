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
import uk.gov.hmcts.divorce.payment.service.PaymentService;
import uk.gov.hmcts.divorce.solicitor.event.page.BailiffServicePaymentPage;
import uk.gov.hmcts.divorce.solicitor.event.page.BailiffServiceRespondentDescriptionPage;
import uk.gov.hmcts.divorce.solicitor.event.page.BailiffServiceRespondentHistoryPage;
import uk.gov.hmcts.divorce.solicitor.event.page.BailiffServiceRespondentHistoryThreePage;
import uk.gov.hmcts.divorce.solicitor.event.page.BailiffServiceRespondentHistoryTwoPage;
import uk.gov.hmcts.divorce.solicitor.event.page.BailiffServiceRespondentNameAddressPage;
import uk.gov.hmcts.divorce.solicitor.event.page.BailiffServiceRespondentPhoneAgePage;
import uk.gov.hmcts.divorce.solicitor.event.page.BailiffServiceRespondentServiceTimeVehiclePage;
import uk.gov.hmcts.divorce.solicitor.event.page.BailiffServiceUploadPhotoPage;
import uk.gov.hmcts.divorce.solicitor.service.ServiceApplicationDraftSubmissionService;

import java.util.EnumSet;
import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.payment.FeesAndPaymentsUtil.formatAmount;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.EVENT_ENFORCEMENT;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.KEYWORD_BAILIFF;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.SERVICE_OTHER;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitorBailiffServiceApplication implements CCDConfig<CaseData, State, UserRole> {

    private static final String BAILIFF_SERVICE = "Bailiff Service App";

    public static final String SOLICITOR_BAILIFF_SERVICE_APPLICATION = "sol-bailiff-service-app";

    private final PaymentService paymentService;

    private final ServiceApplicationDraftSubmissionService serviceApplicationDraftSubmissionService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);

        final List<CcdPageConfiguration> pages = asList(
            new BailiffServicePaymentPage(),
            new BailiffServiceRespondentNameAddressPage(),
            new BailiffServiceRespondentPhoneAgePage(),
            new BailiffServiceRespondentDescriptionPage(),
            new BailiffServiceRespondentServiceTimeVehiclePage(),
            new BailiffServiceRespondentHistoryPage(),
            new BailiffServiceRespondentHistoryTwoPage(),
            new BailiffServiceRespondentHistoryThreePage(),
            new BailiffServiceUploadPhotoPage()
        );

        pages.forEach(page -> page.addTo(pageBuilder));
    }

    private PageBuilder addEventConfig(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        var states = EnumSet.complementOf(POST_SUBMISSION_STATES);
        states.add(AosOverdue);

        return new PageBuilder(configBuilder
            .event(SOLICITOR_BAILIFF_SERVICE_APPLICATION)
            .forStates(states)
            .name(BAILIFF_SERVICE)
            .description(BAILIFF_SERVICE)
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, LEGAL_ADVISOR, SUPER_USER, JUDGE)
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
        );
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> caseDetails) {
        String bailiffServiceFeeAmount = formatAmount(paymentService.getServiceCost(SERVICE_OTHER, EVENT_ENFORCEMENT, KEYWORD_BAILIFF));
        caseDetails.getData().getApplicant1().getInterimApplicationOptions().getBailiffServiceJourneyOptions()
            .setBailiffServiceFeeAmount(bailiffServiceFeeAmount);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseDetails.getData())
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("{} about to submit callback invoked for Case Id: {}", SOLICITOR_BAILIFF_SERVICE_APPLICATION, details.getId());
        final CaseData caseData = details.getData();
        final Applicant applicant = caseData.getApplicant1();

        InterimApplicationOptions options = applicant.getInterimApplicationOptions();
        options.setInterimApplicationType(InterimApplicationType.BAILIFF_SERVICE);

        serviceApplicationDraftSubmissionService.submitFromInterimOptions(details.getId(), caseData, applicant);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
