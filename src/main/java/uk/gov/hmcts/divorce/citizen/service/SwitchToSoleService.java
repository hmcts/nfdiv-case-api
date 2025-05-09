package uk.gov.hmcts.divorce.citizen.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.service.CaseFlagsService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.SwitchedToSole;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.HUSBAND;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.WIFE;

@Service
@Slf4j
@RequiredArgsConstructor
public class SwitchToSoleService {

    private final CcdAccessService ccdAccessService;

    private final CaseAssignmentApi caseAssignmentApi;

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    private final CaseFlagsService caseFlagsService;

    public void switchUserRoles(final CaseData caseData, final Long caseId) {

        final String auth = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
        final String s2sToken = authTokenGenerator.generate();

        final CaseAssignmentUserRolesResource response =
            caseAssignmentApi.getUserRoles(auth, s2sToken, List.of(caseId.toString()));

        if (caseData.getApplicant1().isRepresented()) {
            String app1SolUserId = getUserIdForRole(response, APPLICANT_1_SOLICITOR);
            switchRole(app1SolUserId, auth, s2sToken, APPLICANT_1_SOLICITOR, APPLICANT_2_SOLICITOR, caseId);
        }

        if (caseData.getApplicant2().isRepresented()) {
            String app2SolUserId = getUserIdForRole(response, APPLICANT_2_SOLICITOR);
            switchRole(app2SolUserId, auth, s2sToken, APPLICANT_2_SOLICITOR, APPLICANT_1_SOLICITOR, caseId);
        }

        String creatorUserId = getUserIdForRole(response, CREATOR);
        switchRole(creatorUserId, auth, s2sToken, CREATOR, APPLICANT_2, caseId);

        String app2UserId = getUserIdForRole(response, APPLICANT_2);
        switchRole(app2UserId, auth, s2sToken, APPLICANT_2, CREATOR, caseId);
    }

    private String getUserIdForRole(final CaseAssignmentUserRolesResource response, final UserRole userRole) {
        final List<CaseAssignmentUserRole> user = response.getCaseAssignmentUserRoles().stream()
            .filter(caseAssignmentUserRole -> userRole.getRole().equals(caseAssignmentUserRole.getCaseRole()))
            .limit(1)
            .toList();
        return  !user.isEmpty()
            ? user.get(0).getUserId()
            : "";
    }

    private void switchRole(final String userId,
                            final String auth,
                            final String s2sToken,
                            final UserRole oldRole,
                            final UserRole newRole,
                            final Long caseId) {
        if (StringUtils.isEmpty(userId)) {
            log.info("Switch to sole User ID is empty, skipping {} role removal and reassignment for case {}", oldRole, caseId);
            return;
        }

        caseAssignmentApi.removeCaseUserRoles(
            auth,
            s2sToken,
            ccdAccessService.getCaseAssignmentRequest(caseId, userId, oldRole)
        );

        caseAssignmentApi.addCaseUserRoles(
            auth,
            s2sToken,
            ccdAccessService.getCaseAssignmentRequest(caseId, userId, newRole)
        );
    }

    public void switchApplicantData(final CaseData data) {
        final Application application = data.getApplication();
        final Applicant applicant1 = data.getApplicant1();
        final Applicant applicant2 = data.getApplicant2();
        data.setApplicant1(applicant2);
        data.setApplicant2(applicant1);

        switchApplicationData(data, application, data.getApplicant2());
        switchOrgPolicyCaseAssignedRoles(data);
        populateSwitchedToSoleData(application);
        switchConditionalOrderAnswers(data.getConditionalOrder());
        data.setCaseInvite(new CaseInvite(data.getApplicant2().getEmail(), null, null));
        switchFinalOrderAnswers(data.getFinalOrder());

        caseFlagsService.switchCaseFlags(data);
    }

    private void switchApplicationData(final CaseData data, final Application application, final Applicant applicant2) {

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

        List<ListValue<DivorceDocument>> currentApplicant1DocumentsUploaded = data.getDocuments().getApplicant1DocumentsUploaded();
        List<ListValue<DivorceDocument>> currentApplicant2DocumentsUploaded = data.getDocuments().getApplicant2DocumentsUploaded();
        data.getDocuments().setApplicant1DocumentsUploaded(currentApplicant2DocumentsUploaded);
        data.getDocuments().setApplicant2DocumentsUploaded(currentApplicant1DocumentsUploaded);
    }

    private void switchOrgPolicyCaseAssignedRoles(final CaseData data) {
        Solicitor app1Solicitor = data.getApplicant1().getSolicitor();
        Solicitor app2Solicitor = data.getApplicant2().getSolicitor();

        if (app1Solicitor != null && app1Solicitor.getOrganisationPolicy() != null) {
            app1Solicitor.getOrganisationPolicy().setOrgPolicyCaseAssignedRole(APPLICANT_1_SOLICITOR);
        }

        if (app2Solicitor != null && app2Solicitor.getOrganisationPolicy() != null) {
            app2Solicitor.getOrganisationPolicy().setOrgPolicyCaseAssignedRole(APPLICANT_2_SOLICITOR);
        }
    }

    private void switchConditionalOrderAnswers(ConditionalOrder conditionalOrder) {
        ConditionalOrderQuestions conditionalOrderApplicant1Questions = conditionalOrder.getConditionalOrderApplicant1Questions();
        ConditionalOrderQuestions conditionalOrderApplicant2Questions = conditionalOrder.getConditionalOrderApplicant2Questions();
        conditionalOrder.setConditionalOrderApplicant1Questions(conditionalOrderApplicant2Questions);
        conditionalOrder.setConditionalOrderApplicant2Questions(conditionalOrderApplicant1Questions);

        YesOrNo offlineCertificateOfEntitlementDocumentSentToApplicant1 =
            conditionalOrder.getOfflineCertificateOfEntitlementDocumentSentToApplicant1();
        YesOrNo offlineCertificateOfEntitlementDocumentSentToApplicant2 =
            conditionalOrder.getOfflineCertificateOfEntitlementDocumentSentToApplicant2();

        conditionalOrder.setOfflineCertificateOfEntitlementDocumentSentToApplicant1(
            offlineCertificateOfEntitlementDocumentSentToApplicant2);
        conditionalOrder.setOfflineCertificateOfEntitlementDocumentSentToApplicant2(
            offlineCertificateOfEntitlementDocumentSentToApplicant1);
    }

    private void switchFinalOrderAnswers(FinalOrder finalOrder) {

        YesOrNo doesApplicant1WantToApplyForFinalOrder = finalOrder.getDoesApplicant1WantToApplyForFinalOrder();
        YesOrNo doesApplicant2WantToApplyForFinalOrder = finalOrder.getDoesApplicant2WantToApplyForFinalOrder();

        finalOrder.setDoesApplicant1WantToApplyForFinalOrder(doesApplicant2WantToApplyForFinalOrder);
        finalOrder.setDoesApplicant2WantToApplyForFinalOrder(doesApplicant1WantToApplyForFinalOrder);

        YesOrNo applicant1AppliedForFinalOrderFirst = finalOrder.getApplicant1AppliedForFinalOrderFirst();
        YesOrNo applicant2AppliedForFinalOrderFirst = finalOrder.getApplicant2AppliedForFinalOrderFirst();

        finalOrder.setApplicant1AppliedForFinalOrderFirst(applicant2AppliedForFinalOrderFirst);
        finalOrder.setApplicant2AppliedForFinalOrderFirst(applicant1AppliedForFinalOrderFirst);

        YesOrNo applicant1CanIntendToSwitchToSoleFo = finalOrder.getApplicant1CanIntendToSwitchToSoleFo();
        YesOrNo applicant2CanIntendToSwitchToSoleFo = finalOrder.getApplicant2CanIntendToSwitchToSoleFo();

        finalOrder.setApplicant1CanIntendToSwitchToSoleFo(applicant2CanIntendToSwitchToSoleFo);
        finalOrder.setApplicant2CanIntendToSwitchToSoleFo(applicant1CanIntendToSwitchToSoleFo);

        Set<FinalOrder.IntendsToSwitchToSole> applicant1IntendsToSwitchToSole =
            finalOrder.getApplicant1IntendsToSwitchToSole();
        Set<FinalOrder.IntendsToSwitchToSole> applicant2IntendsToSwitchToSole =
            finalOrder.getApplicant2IntendsToSwitchToSole();

        finalOrder.setApplicant1IntendsToSwitchToSole(applicant2IntendsToSwitchToSole);
        finalOrder.setApplicant2IntendsToSwitchToSole(applicant1IntendsToSwitchToSole);

        YesOrNo doesApplicant1IntendToSwitchToSole = finalOrder.getDoesApplicant1IntendToSwitchToSole();
        YesOrNo doesApplicant2IntendToSwitchToSole = finalOrder.getDoesApplicant2IntendToSwitchToSole();

        finalOrder.setDoesApplicant1IntendToSwitchToSole(doesApplicant2IntendToSwitchToSole);
        finalOrder.setDoesApplicant2IntendToSwitchToSole(doesApplicant1IntendToSwitchToSole);

        LocalDate dateApplicant1DeclaredIntentionToSwitchToSoleFo =
            finalOrder.getDateApplicant1DeclaredIntentionToSwitchToSoleFo();
        LocalDate dateApplicant2DeclaredIntentionToSwitchToSoleFo =
            finalOrder.getDateApplicant2DeclaredIntentionToSwitchToSoleFo();

        finalOrder.setDateApplicant1DeclaredIntentionToSwitchToSoleFo(dateApplicant2DeclaredIntentionToSwitchToSoleFo);
        finalOrder.setDateApplicant2DeclaredIntentionToSwitchToSoleFo(dateApplicant1DeclaredIntentionToSwitchToSoleFo);

        String applicant1FinalOrderLateExplanation = finalOrder.getApplicant1FinalOrderLateExplanation();
        String applicant2FinalOrderLateExplanation = finalOrder.getApplicant2FinalOrderLateExplanation();

        finalOrder.setApplicant2FinalOrderLateExplanation(applicant1FinalOrderLateExplanation);
        finalOrder.setApplicant1FinalOrderLateExplanation(applicant2FinalOrderLateExplanation);

        String applicant1FinalOrderLateExplanationTranslated = finalOrder.getApplicant1FinalOrderLateExplanationTranslated();
        String applicant2FinalOrderLateExplanationTranslated = finalOrder.getApplicant2FinalOrderLateExplanationTranslated();

        finalOrder.setApplicant2FinalOrderLateExplanationTranslated(applicant1FinalOrderLateExplanationTranslated);
        finalOrder.setApplicant1FinalOrderLateExplanationTranslated(applicant2FinalOrderLateExplanationTranslated);
    }

    private void populateSwitchedToSoleData(final Application application) {

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
