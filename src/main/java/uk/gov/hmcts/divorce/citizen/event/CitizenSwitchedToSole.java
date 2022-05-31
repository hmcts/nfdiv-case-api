package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.Applicant1SwitchToSoleNotification;
import uk.gov.hmcts.divorce.citizen.notification.Applicant2SwitchToSoleNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import javax.servlet.http.HttpServletRequest;

import static java.util.Objects.isNull;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Applicant2Approved;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant1Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class CitizenSwitchedToSole implements CCDConfig<CaseData, State, UserRole> {

    public static final String SWITCH_TO_SOLE = "switch-to-sole";

    @Autowired
    private CcdAccessService ccdAccessService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private Applicant1SwitchToSoleNotification applicant1SwitchToSoleNotification;

    @Autowired
    private Applicant2SwitchToSoleNotification applicant2SwitchToSoleNotification;

    @Autowired
    private IdamService idamService;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(SWITCH_TO_SOLE)
            .forStates(AwaitingApplicant1Response, AwaitingApplicant2Response, Applicant2Approved, AwaitingPayment)
            .name("Application switched to sole")
            .description("Application type switched to sole")
            .grant(CREATE_READ_UPDATE, CREATOR, APPLICANT_2)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Citizen switched to sole about to submit callback invoked for Case Id: {}", details.getId());
        CaseData data = details.getData();

        if (ccdAccessService.isApplicant1(httpServletRequest.getHeader(AUTHORIZATION), details.getId())) {
            notificationDispatcher.send(applicant1SwitchToSoleNotification, data, details.getId());
        } else {
            notificationDispatcher.send(applicant2SwitchToSoleNotification, data, details.getId());
        }
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);
        removeApplicant2AnswersFromCase(data);
        data.getApplication().setJurisdiction(null);

        CaseInvite caseInviteBefore = beforeDetails.getData().getCaseInvite();

        if (isNull(caseInviteBefore.accessCode())) {
            log.info("Unlinking Applicant 2 from Case");
            ccdAccessService.unlinkUserFromApplication(
                idamService.retrieveSystemUpdateUserDetails().getAuthToken(),
                details.getId(),
                caseInviteBefore.applicant2UserId()
            );
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(Draft)
            .build();
    }

    private CaseData removeApplicant2AnswersFromCase(CaseData caseData) {
        Applicant applicant2Existing = caseData.getApplicant2();
        Applicant applicant2 = Applicant.builder()
            .firstName(applicant2Existing.getFirstName())
            .middleName(applicant2Existing.getMiddleName())
            .lastName(applicant2Existing.getLastName())
            .gender(applicant2Existing.getGender())
            .address(applicant2Existing.isConfidentialContactDetails() ? null : applicant2Existing.getAddress())
            .build();
        caseData.setApplicant2(applicant2);

        CaseInvite caseInvite = caseData.getCaseInvite();
        caseData.setCaseInvite(new CaseInvite(caseInvite.applicant2InviteEmailAddress(), null, null));

        caseData.getDocuments().setApplicant2DocumentsUploaded(null);
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(null);
        caseData.getApplication().setApplicant2HelpWithFees(null);
        caseData.getApplicant2().getApplicantPrayer().setPrayerDissolveDivorce(null);
        caseData.getApplicant2().getApplicantPrayer().setPrayerEndCivilPartnership(null);
        caseData.getApplicant2().getApplicantPrayer().setPrayerFinancialOrdersThemselves(null);
        caseData.getApplicant2().getApplicantPrayer().setPrayerFinancialOrdersChild(null);
        caseData.getApplication().setApplicant2StatementOfTruth(null);
        caseData.getApplication().setApplicant2AgreeToReceiveEmails(null);
        caseData.getApplication().setApplicant2CannotUploadSupportingDocument(null);
        caseData.getApplication().setApplicant2CannotUpload(null);
        caseData.getApplication().setApplicant2ConfirmApplicant1Information(null);
        caseData.getApplication().setApplicant2ExplainsApplicant1IncorrectInformation(null);
        caseData.getApplication().setApplicant2ReminderSent(null);

        return caseData;
    }
}
