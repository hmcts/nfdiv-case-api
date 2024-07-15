package uk.gov.hmcts.divorce.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.event.page.Applicant2SolStatementOfTruth;
import uk.gov.hmcts.divorce.common.event.page.SolicitorDetailsWithStatementOfTruth;
import uk.gov.hmcts.divorce.common.service.SubmitAosService;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AOS_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemIssueAosDisputed.SYSTEM_ISSUE_AOS_DISPUTED;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemIssueAosUnDisputed.SYSTEM_ISSUE_AOS_UNDISPUTED;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubmitAos implements CCDConfig<CaseData, State, UserRole> {

    public static final String SUBMIT_AOS = "submit-aos";

    private final List<CcdPageConfiguration> pages = List.of(
        new Applicant2SolStatementOfTruth(),
        new SolicitorDetailsWithStatementOfTruth()
    );

    private final SubmitAosService submitAosService;
    private final CcdUpdateService ccdUpdateService;
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);
        pages.forEach(page -> page.addTo(pageBuilder));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("Submit AoS about to start callback invoked for Case Id: {}", details.getId());

        final var caseData = details.getData();
        final var acknowledgementOfService = caseData.getAcknowledgementOfService();

        if (null != acknowledgementOfService && null != acknowledgementOfService.getDateAosSubmitted()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(Collections.singletonList("The Acknowledgement Of Service has already been submitted."))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Submit AoS About to Submit callback invoked for Case Id: {}", details.getId());

        final var caseData = details.getData();

        final List<String> errors = validateAos(caseData);

        if (!errors.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(errors)
                .build();
        }

        final CaseDetails<CaseData, State> updateDetails = submitAosService.submitAos(details);

        submitAosService.submitAosNotifications(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(updateDetails.getData())
            .state(updateDetails.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(final CaseDetails<CaseData, State> details,
                                               final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Submit AoS submitted callback invoked for Case Id: {}", details.getId());

        final AcknowledgementOfService acknowledgementOfService = details.getData().getAcknowledgementOfService();

        String eventId = DISPUTE_DIVORCE.equals(acknowledgementOfService.getHowToRespondApplication())
            ? SYSTEM_ISSUE_AOS_DISPUTED
            : SYSTEM_ISSUE_AOS_UNDISPUTED;

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();

        log.info("Submitting event id {} for case id {}", eventId, details.getId());
        ccdUpdateService.submitEvent(details.getId(), eventId, user, serviceAuthorization);

        return SubmittedCallbackResponse.builder().build();
    }

    private List<String> validateAos(final CaseData caseData) {
        final var acknowledgementOfService = caseData.getAcknowledgementOfService();

        final List<String> errors = new ArrayList<>();

        if (!YES.equals(acknowledgementOfService.getStatementOfTruth())) {
            errors.add("You must be authorised by the respondent to sign this statement.");
        }

        if (!YES.equals(acknowledgementOfService.getConfirmReadPetition())) {
            errors.add("The respondent must have read the application.");
        }

        if (acknowledgementOfService.getJurisdictionAgree() == null) {
            errors.add("The respondent must agree or disagree to claimed jurisdiction.");
        }

        if (NO.equals(acknowledgementOfService.getJurisdictionAgree())) {
            if (acknowledgementOfService.getReasonCourtsOfEnglandAndWalesHaveNoJurisdiction() == null) {
                errors.add("The respondent must have a reason for refusing jurisdiction.");
            }
            if (acknowledgementOfService.getInWhichCountryIsYourLifeMainlyBased() == null) {
                errors.add("The respondent must answer in which country is their life mainly based question.");
            }
        }

        if (acknowledgementOfService.getHowToRespondApplication() == null) {
            errors.add("The respondent must answer how they want to respond to the application.");
        }

        if (caseData.getApplicant2().getLegalProceedings() == null) {
            errors.add("The respondent must confirm if they have any other legal proceedings.");
        }

        if (YES.equals(caseData.getApplicant2().getLegalProceedings()) && caseData.getApplicant2().getLegalProceedingsDetails() == null) {
            errors.add("The respondent must enter the details of their other legal proceedings.");
        }

        return errors;
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(SUBMIT_AOS)
            .forStates(ArrayUtils.addAll(AOS_STATES, AosDrafted, AosOverdue, OfflineDocumentReceived, AwaitingService))
            .name("Submit AoS")
            .description("Submit AoS")
            .showCondition("applicationType=\"soleApplication\" AND aosIsDrafted=\"Yes\"")
            .showSummary()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR, APPLICANT_2)
            .endButtonLabel("Submit Application")
            .grantHistoryOnly(
                CASE_WORKER,
                LEGAL_ADVISOR,
                SUPER_USER,
                JUDGE));
    }
}
