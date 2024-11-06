package uk.gov.hmcts.divorce.solicitor.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.caseworker.event.NoticeType;
import uk.gov.hmcts.divorce.caseworker.service.NoticeOfChangeService;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.NoticeOfChange;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.noticeofchange.model.ChangeOfRepresentationAuthor;
import uk.gov.hmcts.divorce.noticeofchange.service.ChangeOfRepresentativeService;
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
public class SolicitorRemoveRepresentation implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_REMOVE_REPRESENTATION = "solicitor-remove-representation";

    private static final String WARNING_LABEL = "### If you're no longer representing a client \n\n"
            + "- You will no longer have access to this case\n"
            + "- If the case had been shared with any colleagues, they will also lose access\n"
            + "- Linked cases are not affected. To remove a legal representative from a \n"
            + "linked case, go to that case and repeat this action";
    public static final String REPRESENTATIVE_REMOVED_CONFIRMATION_HEADER = "# Representative removed\n";

    public static final String REPRESENTATIVE_REMOVED_CONFIRMATION_LABEL =
        "You're no longer representing %s in this case. All other parties have been notified about this change.\n"
        + "### What happens next\nThis case will no longer appear in your case list.\n\n[View case list](/cases)";

    private final CcdAccessService ccdAccessService;

    private final HttpServletRequest httpServletRequest;

    private final NoticeOfChangeService noticeOfChangeService;

    private final ChangeOfRepresentativeService changeOfRepresentativeService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(configBuilder
            .event(SOLICITOR_REMOVE_REPRESENTATION)
            .forStates(POST_SUBMISSION_STATES)
            .name("Stop representing client")
            .showSummary()
            .showEventNotes()
            .description(SOLICITOR_REMOVE_REPRESENTATION)
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR, APPLICANT_2_SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, LEGAL_ADVISOR, JUDGE, SUPER_USER)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted))
            .page("solicitorStopRepresentation")
            .pageLabel("# Stop representing client")
            .label("eventWarning", WARNING_LABEL)
            .complex(CaseData::getNoticeOfChange)
                .optional(NoticeOfChange::getWhichApplicant, "nocAreTheyDigital=\"NEVER_SHOW\"")
                .optional(NoticeOfChange::getAreTheyDigital, "nocWhichApplicant=\"NEVER_SHOW\"")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked for Case Id: {}", SOLICITOR_REMOVE_REPRESENTATION, details.getId());

        final boolean isRepresentingApplicant1 = isApplicant1Solicitor(details.getId());
        final UserRole orgPolicyRole = isRepresentingApplicant1 ? APPLICANT_1_SOLICITOR : APPLICANT_2_SOLICITOR;
        final List<String> rolesToRemove = isRepresentingApplicant1
            ? List.of(CREATOR.getRole(), APPLICANT_1_SOLICITOR.getRole())
            : List.of(APPLICANT_2.getRole(), APPLICANT_2_SOLICITOR.getRole());
        final Function<CaseData, Applicant> applicant = isRepresentingApplicant1 ? CaseData::getApplicant1 : CaseData::getApplicant2;

        removeSolicitorDetailsFromCaseData(applicant.apply(details.getData()), orgPolicyRole);

        changeOfRepresentativeService.buildChangeOfRepresentative(
            details.getData(),
            beforeDetails.getData(),
            ChangeOfRepresentationAuthor.SOLICITOR_REMOVE_REPRESENTATION.getValue(),
            isRepresentingApplicant1
        );

        NoticeType.ORG_REMOVED.applyNoticeOfChange(
            applicant.apply(details.getData()),
            applicant.apply(beforeDetails.getData()),
            rolesToRemove,
            orgPolicyRole.getRole(),
            details,
            noticeOfChangeService
        );

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} submitted callback invoked for Case Id: {}", SOLICITOR_REMOVE_REPRESENTATION, details.getId());

        final CaseData data = details.getData();

        String applicantName = isApplicant1Solicitor(details.getId()) ?
            data.getApplicant1().getFullName() : data.getApplicant2().getFullName();

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(REPRESENTATIVE_REMOVED_CONFIRMATION_HEADER)
            .confirmationBody(String.format(REPRESENTATIVE_REMOVED_CONFIRMATION_LABEL, applicantName))
            .build();
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
}
