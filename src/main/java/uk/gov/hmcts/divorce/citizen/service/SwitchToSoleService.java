package uk.gov.hmcts.divorce.citizen.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
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
public class SwitchToSoleService {

    @Autowired
    private CcdAccessService ccdAccessService;

    @Autowired
    private CaseAssignmentApi caseAssignmentApi;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    public void switchUserRoles(final CaseData caseData, final Long caseId) {
        if (caseData.getApplicant1().isRepresented() && caseData.getApplicant2().isRepresented()) {
            switchSolicitorUserRoles(caseId);
        } else if (caseData.getApplicant1().isRepresented() && !caseData.getApplicant2().isRepresented()) {
            switchSolicitorAndCitizenUserRoles(caseId);
        } else if (!caseData.getApplicant1().isRepresented() && caseData.getApplicant2().isRepresented()) {
            switchCitizenAndSolicitorUserRoles(caseId);
        } else {
            switchCitizenUserRoles(caseId);
        }
    }

    private void switchCitizenUserRoles(final Long caseId) {
        final String auth = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
        final String s2sToken = authTokenGenerator.generate();
        final CaseAssignmentUserRolesResource response =
            caseAssignmentApi.getUserRoles(auth, s2sToken, List.of(caseId.toString()));

        final List<CaseAssignmentUserRole> creatorUser = response.getCaseAssignmentUserRoles().stream()
            .filter(caseAssignmentUserRole -> CREATOR.getRole().equals(caseAssignmentUserRole.getCaseRole()))
            .limit(1)
            .toList();

        final List<CaseAssignmentUserRole> applicant2User = response.getCaseAssignmentUserRoles().stream()
            .filter(caseAssignmentUserRole -> APPLICANT_2.getRole().equals(caseAssignmentUserRole.getCaseRole()))
            .limit(1)
            .toList();

        final String currentCreatorUserId = !creatorUser.isEmpty() ? creatorUser.get(0).getUserId() : "";
        final String currentApplicant2UserId = !applicant2User.isEmpty() ? applicant2User.get(0).getUserId() : "";

        removeCaseUserRoles(caseId, auth, s2sToken, currentCreatorUserId, CREATOR);
        removeCaseUserRoles(caseId, auth, s2sToken, currentApplicant2UserId, APPLICANT_2);

        addCaseUserRoles(caseId, auth, s2sToken, currentApplicant2UserId, CREATOR);
        addCaseUserRoles(caseId, auth, s2sToken, currentCreatorUserId, APPLICANT_2);
    }

    private void switchSolicitorUserRoles(final Long caseId) {
        final String auth = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
        final String s2sToken = authTokenGenerator.generate();
        final CaseAssignmentUserRolesResource response =
            caseAssignmentApi.getUserRoles(auth, s2sToken, List.of(caseId.toString()));

        final List<CaseAssignmentUserRole> applicant1SolicitorUser = response.getCaseAssignmentUserRoles().stream()
            .filter(caseAssignmentUserRole -> APPLICANT_1_SOLICITOR.getRole().equals(caseAssignmentUserRole.getCaseRole()))
            .limit(1)
            .toList();

        final List<CaseAssignmentUserRole> applicant2SolicitorUser = response.getCaseAssignmentUserRoles().stream()
            .filter(caseAssignmentUserRole -> APPLICANT_2_SOLICITOR.getRole().equals(caseAssignmentUserRole.getCaseRole()))
            .limit(1)
            .toList();

        final String currentApplicant1SolicitorUserId =
            !applicant1SolicitorUser.isEmpty()
                ? applicant1SolicitorUser.get(0).getUserId()
                : "";
        final String currentApplicant2SolicitorUserId =
            !applicant2SolicitorUser.isEmpty()
                ? applicant2SolicitorUser.get(0).getUserId()
                : "";

        removeCaseUserRoles(caseId, auth, s2sToken, currentApplicant1SolicitorUserId, APPLICANT_1_SOLICITOR);
        removeCaseUserRoles(caseId, auth, s2sToken, currentApplicant2SolicitorUserId, APPLICANT_2_SOLICITOR);

        addCaseUserRoles(caseId, auth, s2sToken, currentApplicant2SolicitorUserId, APPLICANT_1_SOLICITOR);
        addCaseUserRoles(caseId, auth, s2sToken, currentApplicant1SolicitorUserId, APPLICANT_2_SOLICITOR);
    }

    private void switchCitizenAndSolicitorUserRoles(final Long caseId) {
        final String auth = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
        final String s2sToken = authTokenGenerator.generate();
        final CaseAssignmentUserRolesResource response =
            caseAssignmentApi.getUserRoles(auth, s2sToken, List.of(caseId.toString()));

        final List<CaseAssignmentUserRole> creatorUser = response.getCaseAssignmentUserRoles().stream()
            .filter(caseAssignmentUserRole -> CREATOR.getRole().equals(caseAssignmentUserRole.getCaseRole()))
            .limit(1)
            .toList();

        final List<CaseAssignmentUserRole> applicant2SolicitorUser = response.getCaseAssignmentUserRoles().stream()
            .filter(caseAssignmentUserRole -> APPLICANT_2_SOLICITOR.getRole().equals(caseAssignmentUserRole.getCaseRole()))
            .limit(1)
            .toList();

        final String currentCreatorUserId = !creatorUser.isEmpty() ? creatorUser.get(0).getUserId() : "";
        final String currentApplicant2SolicitorUserId =
            !applicant2SolicitorUser.isEmpty()
                ? applicant2SolicitorUser.get(0).getUserId()
                : "";

        removeCaseUserRoles(caseId, auth, s2sToken, currentCreatorUserId, CREATOR);
        removeCaseUserRoles(caseId, auth, s2sToken, currentApplicant2SolicitorUserId, APPLICANT_2_SOLICITOR);

        addCaseUserRoles(caseId, auth, s2sToken, currentApplicant2SolicitorUserId, APPLICANT_1_SOLICITOR);
        addCaseUserRoles(caseId, auth, s2sToken, currentCreatorUserId, APPLICANT_2);
    }

    private void switchSolicitorAndCitizenUserRoles(final Long caseId) {
        final String auth = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
        final String s2sToken = authTokenGenerator.generate();
        final CaseAssignmentUserRolesResource response =
            caseAssignmentApi.getUserRoles(auth, s2sToken, List.of(caseId.toString()));

        final List<CaseAssignmentUserRole> applicant1SolicitorUser = response.getCaseAssignmentUserRoles().stream()
            .filter(caseAssignmentUserRole -> APPLICANT_1_SOLICITOR.getRole().equals(caseAssignmentUserRole.getCaseRole()))
            .limit(1)
            .toList();

        final List<CaseAssignmentUserRole> applicant2User = response.getCaseAssignmentUserRoles().stream()
            .filter(caseAssignmentUserRole -> APPLICANT_2.getRole().equals(caseAssignmentUserRole.getCaseRole()))
            .limit(1)
            .toList();

        final String currentApplicant1SolicitorUserId =
            !applicant1SolicitorUser.isEmpty()
                ? applicant1SolicitorUser.get(0).getUserId()
                : "";
        final String currentApplicant2UserId = !applicant2User.isEmpty() ? applicant2User.get(0).getUserId() : "";

        removeCaseUserRoles(caseId, auth, s2sToken, currentApplicant1SolicitorUserId, APPLICANT_1_SOLICITOR);
        removeCaseUserRoles(caseId, auth, s2sToken, currentApplicant2UserId, APPLICANT_2);

        addCaseUserRoles(caseId, auth, s2sToken, currentApplicant2UserId, CREATOR);
        addCaseUserRoles(caseId, auth, s2sToken, currentApplicant1SolicitorUserId, APPLICANT_2_SOLICITOR);
    }

    private void removeCaseUserRoles(final Long caseId,
                                     final String auth,
                                     final String s2sToken,
                                     final String userId,
                                     final UserRole role) {

        caseAssignmentApi.removeCaseUserRoles(
            auth,
            s2sToken,
            ccdAccessService.getCaseAssignmentRequest(caseId, userId, role)
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
            ccdAccessService.getCaseAssignmentRequest(caseId, userId, role)
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
