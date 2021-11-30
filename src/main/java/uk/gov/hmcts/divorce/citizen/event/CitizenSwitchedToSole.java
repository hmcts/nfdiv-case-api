package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.SwitchToSoleNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.idam.client.models.User;

import javax.servlet.http.HttpServletRequest;

import static java.util.Objects.isNull;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Applicant2Approved;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant1Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class CitizenSwitchedToSole implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    private CcdAccessService ccdAccessService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private SwitchToSoleNotification switchToSoleNotification;

    @Autowired
    private IdamService idamService;

    public static final String SWITCH_TO_SOLE = "switch-to-sole";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(SWITCH_TO_SOLE)
            .forStates(AwaitingApplicant1Response, AwaitingApplicant2Response, Applicant2Approved, AwaitingPayment)
            .name("Application switched to sole")
            .description("Application type switched to sole")
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Citizen switched to sole about to submit callback invoked");
        CaseData data = details.getData();

        if (isNull(data.getCaseInvite().getAccessCode())) {
            log.info("Unlinking Applicant 2 from Case");
            ccdAccessService.unlinkUserFromApplication(
                httpServletRequest.getHeader(AUTHORIZATION),
                details.getId(),
                data.getCaseInvite().getApplicant2UserId()
            );
        } else {
            log.info("Removing the case invite access code for Applicant 2");
            data.getCaseInvite().setAccessCode(null);
        }

        User user = idamService.retrieveUser(httpServletRequest.getHeader(AUTHORIZATION));

        if (data.getCaseInvite().isApplicant2(user.getUserDetails().getId())) {
            switchToSoleNotification.sendApplicant2SwitchToSoleNotificationToApplicant1(data, details.getId());
            switchToSoleNotification.sendApplicant2SwitchToSoleNotificationToApplicant2(data, details.getId());

        } else {
            switchToSoleNotification.sendApplicant1SwitchToSoleNotificationToApplicant1(data, details.getId());
            if (data.getApplication().getApplicant2ScreenHasMarriageBroken() != YesOrNo.NO) {
                switchToSoleNotification.sendApplicant1SwitchToSoleNotificationToApplicant2(data, details.getId());
            }
        }

        data.setApplicationType(ApplicationType.SOLE_APPLICATION);
        removeApplicant2AnswersFromCase(data);

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
            .homeAddress(applicant2Existing.isConfidentialContactDetails() ? null : applicant2Existing.getHomeAddress())
            .build();
        caseData.setApplicant2(applicant2);

        CaseInvite caseInvite = caseData.getCaseInvite();
        caseData.setCaseInvite(CaseInvite.builder()
            .applicant2InviteEmailAddress(caseInvite.getApplicant2InviteEmailAddress())
            .build());

        caseData.setApplicant2DocumentsUploaded(null);
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(null);
        caseData.getApplication().setApplicant2HelpWithFees(null);
        caseData.getApplication().setApplicant2PrayerHasBeenGiven(null);
        caseData.getApplication().setApplicant2StatementOfTruth(null);
        caseData.getApplication().setApplicant2AgreeToReceiveEmails(null);
        caseData.getApplication().setApplicant2CannotUploadSupportingDocument(null);
        caseData.getApplication().setApplicant2ConfirmApplicant1Information(null);
        caseData.getApplication().setApplicant2ExplainsApplicant1IncorrectInformation(null);
        caseData.getApplication().setApplicant2ReminderSent(null);

        return caseData;
    }
}
