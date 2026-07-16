package uk.gov.hmcts.divorce.solicitor.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.notification.RespondentDetailsUpdatedNotification;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.Collections;
import java.util.Objects;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFEvidence;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFPartPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApplicantSolicitorUpdateRespondentContactDetails implements CCDConfig<CaseData, State, UserRole> {

    public static final String APP_SOLICITOR_UPDATE_RESPONDENT_DETAILS = "app-solicitor-update-respondent-details";
    private static final String CANNOT_USE_EVENT =
        "You cannot use this event at this stage of the case.";
    private static final String CANNOT_REMOVE_EMAIL_ERROR =
        "You cannot remove the respondent's email address using this event.";

    private final NotificationDispatcher notificationDispatcher;

    private final RespondentDetailsUpdatedNotification respondentDetailsUpdatedNotification;

    private static final State[] UPDATE_RESPONDENT_DETAILS_STATES = {
        Submitted,
        AwaitingService,
        AwaitingAos,
        AosOverdue,
        AwaitingDocuments,
        AwaitingConditionalOrder,
        AwaitingHWFEvidence,
        AwaitingHWFDecision,
        AwaitingHWFPartPayment
    };

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(APP_SOLICITOR_UPDATE_RESPONDENT_DETAILS)
            .forStates(UPDATE_RESPONDENT_DETAILS_STATES)
            .showCondition("applicationType=\"soleApplication\"")
            .name("Update respondent details")
            .description("Update respondent contact details")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .aboutToStartCallback(this::aboutToStart)
            .submittedCallback(this::submitted)
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR)
            .grantHistoryOnly(
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE))
            .page("updateRespondentContactDetails")
            .pageLabel("Respondent Contact Details")
            .complex(CaseData::getApplicant2)
            .optionalWithLabel(Applicant::getNonConfidentialEmail, "Respondent's email address")
            .mandatoryWithLabel(Applicant::getNonConfidentialAddress, "Respondent's postal address")
            .mandatoryWithLabel(Applicant::getAddressOverseas, "Is the respondent's postal address international?")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("{} about to start callback invoked for Case Id: {}", APP_SOLICITOR_UPDATE_RESPONDENT_DETAILS, details.getId());

        final CaseData caseData = details.getData();
        AcknowledgementOfService acknowledgementOfService = caseData.getAcknowledgementOfService();

        boolean hasSubmittedAos = acknowledgementOfService != null && acknowledgementOfService.getDateAosSubmitted() != null;
        boolean hasApplicantSubmittedCO =
            !Objects.isNull(caseData.getConditionalOrder().getConditionalOrderApplicant1Questions().getSubmittedDate());

        if (hasSubmittedAos || hasApplicantSubmittedCO) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(Collections.singletonList(CANNOT_USE_EVENT))
                .build();
        }

        final Applicant applicant2 = caseData.getApplicant2();

        applicant2.setNonConfidentialAddress(applicant2.getAddress());
        applicant2.setNonConfidentialEmail(applicant2.getEmail());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("{} about to submit callback invoked for Case Id: {}", APP_SOLICITOR_UPDATE_RESPONDENT_DETAILS, details.getId());

        var caseData = details.getData();
        var beforeCaseData = beforeDetails.getData();

        if (!StringUtils.isEmpty(beforeCaseData.getApplicant2().getEmail())
            && StringUtils.isEmpty(caseData.getApplicant2().getNonConfidentialEmail())) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(Collections.singletonList(CANNOT_REMOVE_EMAIL_ERROR))
                .build();
        }

        caseData.getApplicant2().setAddress(caseData.getApplicant2().getNonConfidentialAddress());
        caseData.getApplicant2().setEmail(caseData.getApplicant2().getNonConfidentialEmail());

        caseData.getApplicant2().setNonConfidentialAddress(null);
        caseData.getApplicant2().setNonConfidentialEmail(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} submitted callback invoked for case id: {}", APP_SOLICITOR_UPDATE_RESPONDENT_DETAILS, details.getId());
        notificationDispatcher.send(respondentDetailsUpdatedNotification, details.getData(), details.getId());
        return SubmittedCallbackResponse.builder().build();
    }
}
