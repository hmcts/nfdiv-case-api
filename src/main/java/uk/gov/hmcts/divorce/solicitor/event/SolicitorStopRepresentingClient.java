package uk.gov.hmcts.divorce.solicitor.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.caseworker.event.NoticeType;
import uk.gov.hmcts.divorce.caseworker.service.CaseFlagsService;
import uk.gov.hmcts.divorce.caseworker.service.NoticeOfChangeService;
import uk.gov.hmcts.divorce.citizen.notification.NocSolRemovedCitizenNotification;
import uk.gov.hmcts.divorce.citizen.notification.NocSolRemovedSelfAsRepresentativeNotification;
import uk.gov.hmcts.divorce.citizen.notification.NocSolsToCitizenNotifications;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInviteApp1;
import uk.gov.hmcts.divorce.divorcecase.model.NoticeOfChange;
import uk.gov.hmcts.divorce.divorcecase.model.NoticeOfChange.WhichApplicant;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.noticeofchange.model.ChangeOfRepresentationAuthor;
import uk.gov.hmcts.divorce.noticeofchange.service.ChangeOfRepresentativeService;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.List;
import java.util.function.Function;

import static org.springframework.cloud.openfeign.security.OAuth2AccessTokenInterceptor.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitorStopRepresentingClient implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_STOP_REPRESENTING_CLIENT = "solicitor-stop-representation";

    private static final String WARNING_LABEL = "### If you're no longer representing a client \n\n"
            + "- You will no longer have access to this case\n"
            + "- If the case had been shared with any colleagues, they will also lose access\n"
            + "- Linked cases are not affected. To remove a legal representative from a \n"
            + "linked case, go to that case and repeat this action"
            + "- All other parties on the case will be notified about the change.";
    public static final String REPRESENTATIVE_REMOVED_CONFIRMATION_HEADER = "# Representative removed\n";

    public static final String REPRESENTATIVE_REMOVED_CONFIRMATION_LABEL =
        "You're no longer representing %s in this case. All other parties have been notified about this change.\n"
        + "### What happens next\nThis case will no longer appear in your case list.\n\n[View case list](/cases)";

    private final CcdAccessService ccdAccessService;

    private final HttpServletRequest httpServletRequest;

    private final NoticeOfChangeService noticeOfChangeService;

    private final ChangeOfRepresentativeService changeOfRepresentativeService;

    private final NocSolRemovedSelfAsRepresentativeNotification nocSolRemovedSelfNotifications;

    private final NocSolsToCitizenNotifications nocSolsToCitizenNotifications;

    private final NotificationDispatcher notificationDispatcher;

    private final CaseFlagsService caseFlagsService;

    private final NocSolRemovedCitizenNotification nocSolRemovedCitizenNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(configBuilder
            .event(SOLICITOR_STOP_REPRESENTING_CLIENT)
            .forStates(POST_SUBMISSION_STATES)
            .name("Stop representing client")
            .description(SOLICITOR_STOP_REPRESENTING_CLIENT)
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR, APPLICANT_2_SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, LEGAL_ADVISOR, JUDGE, SUPER_USER)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted))
            .page("solicitorStopRepresentation")
            .pageLabel("# Stop representing client")
            .label("warningStopRepresentation", WARNING_LABEL)
            .complex(CaseData::getNoticeOfChange)
                .optional(NoticeOfChange::getWhichApplicant, "nocAreTheyDigital=\"NEVER_SHOW\"")
                .optional(NoticeOfChange::getAreTheyDigital, "nocWhichApplicant=\"NEVER_SHOW\"")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked for Case Id: {}", SOLICITOR_STOP_REPRESENTING_CLIENT, details.getId());

        final boolean isRepresentingApplicant1 = isApplicant1Solicitor(details.getId());
        final UserRole orgPolicyRole = isRepresentingApplicant1 ? APPLICANT_1_SOLICITOR : APPLICANT_2_SOLICITOR;
        final List<String> rolesToRemove = isRepresentingApplicant1
            ? List.of(CREATOR.getRole(), APPLICANT_1_SOLICITOR.getRole())
            : List.of(APPLICANT_2.getRole(), APPLICANT_2_SOLICITOR.getRole());
        final Function<CaseData, Applicant> applicant = isRepresentingApplicant1 ? CaseData::getApplicant1 : CaseData::getApplicant2;

        recordNoticeOfChangePartyInCaseData(details, isRepresentingApplicant1);

        removeSolicitorDetailsFromCaseData(applicant.apply(details.getData()), orgPolicyRole);

        if (details.getData().getApplicationType() == ApplicationType.JOINT_APPLICATION) {
            final Applicant otherApplicant = isRepresentingApplicant1 ? details.getData().getApplicant2()
                : details.getData().getApplicant1();
            otherApplicant.setOffline(YES);
        }

        changeOfRepresentativeService.buildChangeOfRepresentative(
            details.getData(),
            beforeDetails.getData(),
            ChangeOfRepresentationAuthor.SOLICITOR_STOP_REPRESENTING_CLIENT.getValue(),
            isRepresentingApplicant1
        );

        caseFlagsService.resetSolicitorCaseFlags(details.getData(), isRepresentingApplicant1);

        NoticeType.ORG_REMOVED.applyNoticeOfChange(
            applicant.apply(details.getData()),
            applicant.apply(beforeDetails.getData()),
            rolesToRemove,
            orgPolicyRole.getRole(),
            details,
            noticeOfChangeService
        );

        if (shouldSendInviteToParty(details.getData(), isRepresentingApplicant1)) {
            generateCaseInvite(details.getData(), isRepresentingApplicant1, applicant.apply(details.getData()));
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} submitted callback invoked for Case Id: {}", SOLICITOR_STOP_REPRESENTING_CLIENT, details.getId());

        final CaseData data = details.getData();
        boolean wasRepresentingApplicant1 = data.getNoticeOfChange().getWhichApplicant() == WhichApplicant.APPLICANT_1;

        notificationDispatcher.sendNOC(nocSolRemovedSelfNotifications, details.getData(),
            beforeDetails.getData(), details.getId(), wasRepresentingApplicant1, NoticeType.ORG_REMOVED);

        if (shouldSendInviteToParty(details.getData(), wasRepresentingApplicant1)) {
            notificationDispatcher.sendNOCCaseInvite(nocSolsToCitizenNotifications, details.getData(), details.getId(),
                wasRepresentingApplicant1);
        } else {
            nocSolRemovedCitizenNotification.send(data, wasRepresentingApplicant1, details.getId());
        }

        String litigantName = wasRepresentingApplicant1
            ? data.getApplicant1().getFullName() : data.getApplicant2().getFullName();

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(REPRESENTATIVE_REMOVED_CONFIRMATION_HEADER)
            .confirmationBody(String.format(REPRESENTATIVE_REMOVED_CONFIRMATION_LABEL, litigantName))
            .build();
    }

    private void recordNoticeOfChangePartyInCaseData(CaseDetails<CaseData, State> details, boolean isRepresentingApplicant1) {
        details.getData().setNoticeOfChange(
            NoticeOfChange.builder()
                .whichApplicant(
                    isRepresentingApplicant1
                        ? WhichApplicant.APPLICANT_1
                        : WhichApplicant.APPLICANT_2
                ).build()
        );
    }

    private void removeSolicitorDetailsFromCaseData(Applicant applicant, UserRole solicitorRole) {
        Solicitor blankSolicitor = Solicitor.builder().organisationPolicy(
            OrganisationPolicy.<UserRole>builder()
                .orgPolicyCaseAssignedRole(solicitorRole)
                .organisation(new Organisation(null, null))
                .build()
            ).build();

        applicant.setSolicitor(blankSolicitor);
        applicant.setSolicitorRepresented(NO);
        applicant.setOffline(YES);
    }

    private boolean isApplicant1Solicitor(long caseId) {
        String authHeader = httpServletRequest.getHeader(AUTHORIZATION);

        return ccdAccessService.isApplicant1(authHeader, caseId);
    }

    private boolean shouldSendInviteToParty(final CaseData data, boolean isApplicant1) {
        Applicant applicant = isApplicant1 ? data.getApplicant1() : data.getApplicant2();
        boolean hasEmailAddressOnCase = StringUtils.isNotEmpty(applicant.getEmail());

        return (hasEmailAddressOnCase
            && (data.getApplicationType() == ApplicationType.SOLE_APPLICATION)
            && (isApplicant1 || (!isApplicant1 && ObjectUtils.isNotEmpty(data.getApplication().getIssueDate()))));
    }

    private void generateCaseInvite(final CaseData data, boolean isApplicant1, Applicant applicant) {
        if (isApplicant1) {
            CaseInviteApp1 invite = CaseInviteApp1.builder()
                .applicant1InviteEmailAddress(applicant.getEmail())
                .build()
                .generateAccessCode();
            data.setCaseInviteApp1(invite);
        } else {
            CaseInvite invite = CaseInvite.builder()
                .applicant2InviteEmailAddress(applicant.getEmail())
                .build()
                .generateAccessCode();
            data.setCaseInvite(invite);
        }
    }
}
