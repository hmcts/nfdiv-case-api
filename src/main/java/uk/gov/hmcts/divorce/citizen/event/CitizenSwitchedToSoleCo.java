package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.Applicant1SwitchToSoleCoNotification;
import uk.gov.hmcts.divorce.citizen.notification.Applicant2SwitchToSoleCoNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.SwitchedToSole;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;

import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.HUSBAND;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.WIFE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class CitizenSwitchedToSoleCo implements CCDConfig<CaseData, State, UserRole> {

    public static final String SWITCH_TO_SOLE_CO = "switch-to-sole-co";

    @Autowired
    private CcdAccessService ccdAccessService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private Applicant1SwitchToSoleCoNotification applicant1SwitchToSoleCoNotification;

    @Autowired
    private Applicant2SwitchToSoleCoNotification applicant2SwitchToSoleCoNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private CaseAssignmentApi caseAssignmentApi;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(SWITCH_TO_SOLE_CO)
            .forStateTransition(ConditionalOrderPending, AwaitingLegalAdvisorReferral)
            .name("SwitchedToSoleCO")
            .description("Application type switched to sole post CO submission")
            .grant(CREATE_READ_UPDATE, CREATOR, APPLICANT_2)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        Long caseId = details.getId();
        log.info("SwitchedToSoleCO aboutToSubmit callback invoked for Case Id: {}", caseId);
        CaseData data = details.getData();

        data.setApplicationType(SOLE_APPLICATION);
        data.getApplication().setSwitchedToSoleCo(YES);
        data.getLabelContent().setApplicationType(SOLE_APPLICATION);
        data.getConditionalOrder().setSwitchedToSole(YES);

        if (ConditionalOrder.D84WhoApplying.APPLICANT_2.equals(data.getConditionalOrder().getD84WhoApplying())) {
            switchUserRoles(caseId);
            switchApplicantData(data);
        }

        if (ccdAccessService.isApplicant1(httpServletRequest.getHeader(AUTHORIZATION), caseId)) {
            notificationDispatcher.send(applicant1SwitchToSoleCoNotification, data, caseId);
        } else {
            notificationDispatcher.send(applicant2SwitchToSoleCoNotification, data, caseId);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

    private void switchUserRoles(final Long caseId) {
        final String auth = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
        final String s2sToken = authTokenGenerator.generate();
        final CaseAssignmentUserRolesResource response =
            caseAssignmentApi.getUserRoles(auth, s2sToken, List.of(caseId.toString()));

        final List<CaseAssignmentUserRole> creatorUser = response.getCaseAssignmentUserRoles().stream()
            .filter(caseAssignmentUserRole -> CREATOR.getRole().equals(caseAssignmentUserRole.getCaseRole()))
            .limit(1)
            .collect(toList());

        final List<CaseAssignmentUserRole> applicant2User = response.getCaseAssignmentUserRoles().stream()
            .filter(caseAssignmentUserRole -> APPLICANT_2.getRole().equals(caseAssignmentUserRole.getCaseRole()))
            .limit(1)
            .collect(toList());

        final String currentCreatorUserId = !creatorUser.isEmpty() ? creatorUser.get(0).getUserId() : "";
        final String currentApplicant2UserId = !applicant2User.isEmpty() ? applicant2User.get(0).getUserId() : "";

        removeCaseUserRoles(caseId, auth, s2sToken, currentCreatorUserId, CREATOR);
        removeCaseUserRoles(caseId, auth, s2sToken, currentApplicant2UserId, APPLICANT_2);

        addCaseUserRoles(caseId, auth, s2sToken, currentApplicant2UserId, CREATOR);
        addCaseUserRoles(caseId, auth, s2sToken, currentCreatorUserId, APPLICANT_2);
    }

    private void removeCaseUserRoles(final Long caseId,
                                     final String auth,
                                     final String s2sToken,
                                     final String userId,
                                     final UserRole role) {

        caseAssignmentApi.removeCaseUserRoles(
            auth,
            s2sToken,
            ccdAccessService.getCaseAssignmentRequest(caseId, userId, null, role)
        );
    }

    private void addCaseUserRoles(final Long caseId,
                                  final String auth,
                                  final String s2sToken,
                                  final String userId,
                                  final UserRole role) {

        caseAssignmentApi.addCaseUserRoles(
            auth,
            s2sToken,
            ccdAccessService.getCaseAssignmentRequest(caseId, userId, null, role)
        );
    }

    private void switchApplicantData(final CaseData data) {

        Applicant applicant1 = data.getApplicant1();
        Applicant applicant2 = data.getApplicant2();
        data.setApplicant1(applicant2);
        data.setApplicant2(applicant1);

        Application application = data.getApplication();

        if (isNotEmpty(applicant2.getGender())) {
            application.setDivorceWho(MALE.equals(applicant2.getGender()) ? HUSBAND : WIFE);
        } else {
            application.setDivorceWho(null);
        }

        HelpWithFees currentApplicant1HelpWithFees = application.getApplicant1HelpWithFees();
        HelpWithFees currentApplicant2HelpWithFees = application.getApplicant2HelpWithFees();
        application.setApplicant1HelpWithFees(currentApplicant2HelpWithFees);
        application.setApplicant2HelpWithFees(currentApplicant1HelpWithFees);

        String currentMarriageApplicant1Name = application.getMarriageDetails().getApplicant1Name();
        String currentMarriageApplicant2Name = application.getMarriageDetails().getApplicant2Name();
        application.getMarriageDetails().setApplicant1Name(currentMarriageApplicant2Name);
        application.getMarriageDetails().setApplicant2Name(currentMarriageApplicant1Name);

        YesOrNo currentApplicant1ScreenHasMarriageBroken = application.getApplicant1ScreenHasMarriageBroken();
        YesOrNo currentApplicant2ScreenHasMarriageBroken = application.getApplicant2ScreenHasMarriageBroken();
        application.setApplicant1ScreenHasMarriageBroken(currentApplicant2ScreenHasMarriageBroken);
        application.setApplicant2ScreenHasMarriageBroken(currentApplicant1ScreenHasMarriageBroken);

        YesOrNo currentApplicant1CannotUpload = application.getApplicant1CannotUpload();
        YesOrNo currentApplicant2CannotUpload = application.getApplicant2CannotUpload();
        application.setApplicant1CannotUpload(currentApplicant2CannotUpload);
        application.setApplicant2CannotUpload(currentApplicant1CannotUpload);

        Set<DocumentType> currentApplicant1CannotUploadSupportingDocument =
            application.getApplicant1CannotUploadSupportingDocument();
        Set<DocumentType> currentApplicant2CannotUploadSupportingDocument =
            application.getApplicant2CannotUploadSupportingDocument();
        application.setApplicant1CannotUploadSupportingDocument(currentApplicant2CannotUploadSupportingDocument);
        application.setApplicant2CannotUploadSupportingDocument(currentApplicant1CannotUploadSupportingDocument);

        YesOrNo currentApplicant1StatementOfTruth = data.getApplication().getApplicant1StatementOfTruth();
        YesOrNo currentApplicant2StatementOfTruth = data.getApplication().getApplicant2StatementOfTruth();
        data.getApplication().setApplicant1StatementOfTruth(currentApplicant2StatementOfTruth);
        data.getApplication().setApplicant2StatementOfTruth(currentApplicant1StatementOfTruth);

        YesOrNo currentApplicant1ReminderSent = application.getApplicant1ReminderSent();
        YesOrNo currentApplicant2ReminderSent = application.getApplicant2ReminderSent();
        application.setApplicant1ReminderSent(currentApplicant2ReminderSent);
        application.setApplicant2ReminderSent(currentApplicant1ReminderSent);

        YesOrNo solSignStatementOfTruth = application.getSolSignStatementOfTruth();
        YesOrNo applicant2SolSignStatementOfTruth = application.getApplicant2SolSignStatementOfTruth();
        application.setSolSignStatementOfTruth(applicant2SolSignStatementOfTruth);
        application.setApplicant2SolSignStatementOfTruth(solSignStatementOfTruth);

        String currentSolStatementOfReconciliationName = application.getSolStatementOfReconciliationName();
        String currentApplicant2SolStatementOfReconciliationName = application.getApplicant2SolStatementOfReconciliationName();
        application.setSolStatementOfReconciliationName(currentApplicant2SolStatementOfReconciliationName);
        application.setApplicant2SolStatementOfReconciliationName(currentSolStatementOfReconciliationName);

        String currentSolStatementOfReconciliationFirm = application.getSolStatementOfReconciliationFirm();
        String currentApplicant2SolStatementOfReconciliationFirm = application.getApplicant2SolStatementOfReconciliationFirm();
        application.setSolStatementOfReconciliationFirm(currentApplicant2SolStatementOfReconciliationFirm);
        application.setApplicant2SolStatementOfReconciliationFirm(currentSolStatementOfReconciliationFirm);

        String currentStatementOfReconciliationComments = application.getStatementOfReconciliationComments();
        String currentApplicant2StatementOfReconciliationComments = application.getApplicant2StatementOfReconciliationComments();
        application.setStatementOfReconciliationComments(currentApplicant2StatementOfReconciliationComments);
        application.setApplicant2StatementOfReconciliationComments(currentStatementOfReconciliationComments);

        Document currentApplicant1SolicitorAnswersLink = application.getApplicant1SolicitorAnswersLink();
        Document currentApplicant2SolicitorAnswersLink = application.getApplicant2SolicitorAnswersLink();
        application.setApplicant1SolicitorAnswersLink(currentApplicant2SolicitorAnswersLink);
        application.setApplicant2SolicitorAnswersLink(currentApplicant1SolicitorAnswersLink);

        SwitchedToSole switchedToSole = SwitchedToSole.builder()
            .applicant1KnowsApplicant2EmailAddress(application.getApplicant1KnowsApplicant2EmailAddress())
            .applicant1KnowsApplicant2Address(application.getApplicant1KnowsApplicant2Address())
            .app2ContactMethodIsDigital(application.getApp2ContactMethodIsDigital())
            .applicant1WantsToHavePapersServedAnotherWay(application.getApplicant1WantsToHavePapersServedAnotherWay())
            .applicant2ConfirmApplicant1Information(application.getApplicant2ConfirmApplicant1Information())
            .applicant2ExplainsApplicant1IncorrectInformation(application.getApplicant2ExplainsApplicant1IncorrectInformation())
            .applicant1IsApplicant2Represented(application.getApplicant1IsApplicant2Represented())
            .applicant2AgreeToReceiveEmails(application.getApplicant2AgreeToReceiveEmails())
            .applicant2NeedsHelpWithFees(application.getApplicant2NeedsHelpWithFees())
            .solStatementOfReconciliationCertify(application.getSolStatementOfReconciliationCertify())
            .solStatementOfReconciliationDiscussed(application.getSolStatementOfReconciliationDiscussed())
            .build();
        application.setSwitchedToSole(switchedToSole);

        application.setApplicant1KnowsApplicant2EmailAddress(null);
        application.setApplicant1KnowsApplicant2Address(null);
        application.setApp2ContactMethodIsDigital(null);
        application.setApplicant1WantsToHavePapersServedAnotherWay(null);
        application.setApplicant2ConfirmApplicant1Information(null);
        application.setApplicant2ExplainsApplicant1IncorrectInformation(null);
        application.setApplicant1IsApplicant2Represented(null);
        application.setApplicant2AgreeToReceiveEmails(null);
        application.setApplicant2NeedsHelpWithFees(null);
        application.setSolStatementOfReconciliationCertify(null);
        application.setSolStatementOfReconciliationDiscussed(null);
    }
}
