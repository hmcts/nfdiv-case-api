package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OUTSTANDING_ACTIONS;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CERTIFICATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FOREIGN_CERTIFICATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FOREIGN_CERTIFICATE_TRANSLATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.NAME_CHANGE_PROOF;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAPERS_SERVED_ANOTHER_WAY_APPLY;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAPERS_SERVED_ANOTHER_WAY_PARAGRAPH;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAPERS_SERVED_ANOTHER_WAY_TITLE;

@Component
@Slf4j
public class ApplicationOutstandingActionNotification {

    public static final String YES = "yes";
    public static final String NO = "no";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void send(CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.templateVarsFor(caseData);

        templateVars.put(APPLICATION_REFERENCE, formatId(id));

        setDefaultVariables(templateVars);
        if (caseData.getApplicant1WantsToHavePapersServedAnotherWay() == YesOrNo.YES) {
            setPapersServedAnotherWay(templateVars, caseData);
        }
        if (caseData.getCannotUploadSupportingDocument() != null && !caseData.getCannotUploadSupportingDocument().isEmpty()) {
            setMissingSupportingDocumentType(templateVars, caseData);
        }

        log.info("Sending application outstanding actions notification for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            OUTSTANDING_ACTIONS,
            templateVars,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    private String getPapers(DivorceOrDissolution divorceOrDissolution) {
        return divorceOrDissolution.isDivorce() ?  "divorce papers" : "papers";
    }

    private void setDefaultVariables(Map<String, String> templateVars) {
        templateVars.put(PAPERS_SERVED_ANOTHER_WAY_TITLE, "");
        templateVars.put(PAPERS_SERVED_ANOTHER_WAY_PARAGRAPH, "");
        templateVars.put(PAPERS_SERVED_ANOTHER_WAY_APPLY, "");
        templateVars.put(CERTIFICATE, "");
        templateVars.put(FOREIGN_CERTIFICATE, "");
        templateVars.put(FOREIGN_CERTIFICATE_TRANSLATION, "");
        templateVars.put(NAME_CHANGE_PROOF, "");
    }

    private void setPapersServedAnotherWay(Map<String, String> templateVars,  CaseData caseData) {
        String papersServedAnotherWayTitle = "# Apply to serve the %s another way";
        String papersServedAnotherWayParagraph =
            "You need to apply to serve the %s papers to your %s another way. This is because you did not provide their"
                + " postal address in the application. For example you could try to serve them by email, text message or social media.";

        templateVars.put(PAPERS_SERVED_ANOTHER_WAY_TITLE,
            String.format(papersServedAnotherWayTitle, getPapers(caseData.getDivorceOrDissolution())));
        templateVars.put(PAPERS_SERVED_ANOTHER_WAY_PARAGRAPH,
            String.format(papersServedAnotherWayParagraph,
                commonContent.getService(caseData.getDivorceOrDissolution()),
                commonContent.getPartner(caseData)));
        templateVars.put(PAPERS_SERVED_ANOTHER_WAY_APPLY,
            "You can apply here: https://www.gov.uk/government/publications/form-d11-application-notice");
    }

    private void setMissingSupportingDocumentType(Map<String, String> templateVars,  CaseData caseData) {
        String marriageOrCivilPartnership = caseData.getDivorceOrDissolution().isDivorce() ? "marriage" : "civil partnership";
        String certificate = String.format("* Your original %s certificate or a certified copy", marriageOrCivilPartnership);
        String foreignCertificate = String.format("* Your original foreign %s certificate", marriageOrCivilPartnership);
        String foreignCertificateTranslation =
            String.format("* A certified translation of your foreign %s certificate", marriageOrCivilPartnership);
        String nameChangeProof = "* Proof that you changed your name. For example deed poll or statutory declaration";

        for (DocumentType docType : caseData.getCannotUploadSupportingDocument()) {
            switch (docType) {
                case MARRIAGE_CERTIFICATE:
                    if (caseData.getMarriageDetails().getMarriedInUk().toBoolean()) {
                        templateVars.put(CERTIFICATE, certificate);
                    } else {
                        templateVars.put(FOREIGN_CERTIFICATE, foreignCertificate);
                    }
                    break;
                case MARRIAGE_CERTIFICATE_TRANSLATION:
                    templateVars.put(FOREIGN_CERTIFICATE_TRANSLATION, foreignCertificateTranslation);
                    break;
                case NAME_CHANGE_EVIDENCE:
                    templateVars.put(NAME_CHANGE_PROOF, nameChangeProof);
                    break;
                default:
                    break;
            }
        }
    }
}
