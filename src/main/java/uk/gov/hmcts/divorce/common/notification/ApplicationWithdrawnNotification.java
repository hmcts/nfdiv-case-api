package uk.gov.hmcts.divorce.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_OR_APPLICANT1;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_OR_APPLICANT2;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_ISSUE;
import static uk.gov.hmcts.divorce.notification.CommonContent.ISSUE_DATE_LABEL;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLICATION_WITHDRAWN;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_APPLICATION_WITHDRAWN;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApplicationWithdrawnNotification implements ApplicantNotification {
    private static final String IS_RESPONDENT = "isRespondent";
    private static final String RESPONDENT_PARTNER = "respondentPartner";
    private static final String IS_PENDING_REFUND = "isPendingRefund";

    private final CommonContent commonContent;

    private final DocmosisCommonContent docmosisCommonContent;

    private final NotificationService notificationService;

    @Override
    public void sendToApplicant1(final CaseDetails<CaseData, State> caseDetails) {
        long id = caseDetails.getId();
        CaseData caseData = caseDetails.getData();

        log.info("Sending application withdrawn notification to applicant 1 for: {}", id);
        final Map<String, String> templateVars =
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());
        templateVars.put(IS_RESPONDENT, NO);
        templateVars.put(RESPONDENT_PARTNER, "");
        templateVars.put(IS_PENDING_REFUND, shouldAddRefundText(caseData, true) ? YES : NO);


        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            CITIZEN_APPLICATION_WITHDRAWN,
            templateVars,
            caseData.getApplicant1().getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant2(final CaseDetails<CaseData, State> caseDetails) {
        long id = caseDetails.getId();
        CaseData caseData = caseDetails.getData();

        if (shouldSendNotificationToApplicant2(caseData, caseDetails.getState())) {
            log.info("Sending application withdrawn notification to applicant 2 for: {}", id);
            final Map<String, String> templateVars =
                commonContent.mainTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1());
            templateVars.put(IS_PENDING_REFUND, shouldAddRefundText(caseData, false) ? YES : NO);

            if (caseData.getApplicationType().isSole()) {
                templateVars.put(IS_RESPONDENT, YES);
                templateVars.put(
                    RESPONDENT_PARTNER,
                    commonContent.getPartner(caseData, caseData.getApplicant1(), caseData.getApplicant2().getLanguagePreference())
                );
            } else {
                templateVars.put(IS_RESPONDENT, NO);
                templateVars.put(RESPONDENT_PARTNER, "");
            }

            notificationService.sendEmail(
                caseData.getApplicant2().getEmail(),
                CITIZEN_APPLICATION_WITHDRAWN,
                templateVars,
                caseData.getApplicant2().getLanguagePreference(),
                id
            );
        }
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseDetails<CaseData, State> caseDetails) {
        final long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        log.info("Sending application withdrawn notification to applicant 1 solicitor for case : {}", caseId);

        String solicitorEmail = caseData.getApplicant1().getSolicitor().getEmail();
        final Map<String, String> templateVars =
            commonContent.solicitorTemplateVarsPreIssue(caseData, caseDetails.getId(), caseData.getApplicant1());

        addApplicantLabelAndIssueDateVars(templateVars, caseData, caseData.getApplicant1());

        notificationService.sendEmail(
            solicitorEmail,
            SOLICITOR_APPLICATION_WITHDRAWN,
            templateVars,
            caseData.getApplicant1().getLanguagePreference(),
            caseId
        );
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseDetails<CaseData, State> caseDetails) {
        final long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        if (!shouldSendNotificationToApplicant2(caseData, caseDetails.getState())) {
            return;
        }

        log.info("Sending application withdrawn notification to applicant 2 solicitor for case : {}", caseId);

        String solicitorEmail = caseData.getApplicant2().getSolicitor().getEmail();
        final Map<String, String> templateVars =
            commonContent.solicitorTemplateVarsPreIssue(caseData, caseDetails.getId(), caseData.getApplicant2());

        addApplicantLabelAndIssueDateVars(templateVars, caseData, caseData.getApplicant2());

        notificationService.sendEmail(
            solicitorEmail,
            SOLICITOR_APPLICATION_WITHDRAWN,
            templateVars,
            caseData.getApplicant2().getLanguagePreference(),
            caseId
        );
    }

    private void addApplicantLabelAndIssueDateVars(Map<String, String> templateVars, CaseData caseData, Applicant applicant) {
        final LanguagePreference languagePreference = applicant.getLanguagePreference();
        templateVars.put(ISSUE_DATE_LABEL, getIssueDateLabelInPreferredLanguage(caseData.getApplication().getIssueDate(),
            languagePreference));
        templateVars.put(DATE_OF_ISSUE, commonContent.getIssueDateInPreferredLanguage(caseData, applicant));
        templateVars.put(APPLICANT_OR_APPLICANT1, docmosisCommonContent.getApplicantOrApplicant1(caseData, languagePreference));
        templateVars.put(RESPONDENT_OR_APPLICANT2, docmosisCommonContent.getRespondentOrApplicant2(caseData, languagePreference));
    }

    private String getIssueDateLabelInPreferredLanguage(LocalDate issueDate, LanguagePreference languagePreference) {
        return isNotEmpty(issueDate)
            ? (WELSH.equals(languagePreference) ? "Dyddiad cyhoeddi:" : "Issue date:")
            : "";
    }

    private boolean shouldSendNotificationToApplicant2(final CaseData caseData, final State state) {
        return isNotEmpty(caseData.getApplicant2().getCorrespondenceEmail())
            && (jointApp2Invited(caseData, state) || soleRespondentInvited(caseData));
    }

    private boolean jointApp2Invited(final CaseData caseData, final State state) {
        List<State> preInviteStates = List.of(State.Draft, State.Archived);

        return !caseData.getApplicationType().isSole() && !preInviteStates.contains(state);
    }

    private boolean soleRespondentInvited(final CaseData caseData) {
        return caseData.getApplicationType().isSole() && !isNull(caseData.getApplication().getIssueDate());
    }

    private boolean shouldAddRefundText(CaseData caseData, boolean isApplicant1) {
        boolean isCaseSubmittedButNotIssued = caseData.getApplication().getDateSubmitted() != null
            && caseData.getApplication().getIssueDate() == null;
        if (isApplicant1 && isCaseSubmittedButNotIssued) {
            return true;
        }

        if (!isApplicant1 && !caseData.getApplicationType().isSole() && isCaseSubmittedButNotIssued) {
            return true;
        }
        return false;
    }
}
