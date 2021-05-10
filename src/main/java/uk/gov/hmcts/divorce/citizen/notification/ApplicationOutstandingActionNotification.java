package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Locale;
import java.util.Map;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OUTSTANDING_ACTIONS;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CIVIL_PARTNERSHIP_CERTIFICATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FOREIGN_CIVIL_PARTNERSHIP_CERTIFICATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FOREIGN_CIVIL_PARTNERSHIP_CERTIFICATE_TRANSLATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FOREIGN_MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FOREIGN_MARRIAGE_CERTIFICATE_TRANSLATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.NAME_CHANGE_PROOF;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAPERS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PARTNER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SERVICE;

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
        templateVars.put(SERVICE, getService(caseData.getDivorceOrDissolution()));
        templateVars.put(PARTNER, getPartner(caseData));
        templateVars.put(PAPERS, getPapers(caseData.getDivorceOrDissolution()));

        setDefaultSupportingDocumentType(templateVars);
        if (caseData.getCannotUploadSupportingDocument() != null && !caseData.getCannotUploadSupportingDocument().isEmpty()) {
            setMissingSupportingDocumentType(templateVars, caseData);
        }

        log.info("Sending application outstanding actions notification for case : {}", id);

        notificationService.sendEmail(
            caseData.getPetitionerEmail(),
            OUTSTANDING_ACTIONS,
            templateVars,
            caseData.getLanguagePreference()
        );
    }

    private String getService(DivorceOrDissolution divorceOrDissolution) {
        return divorceOrDissolution.isDivorce() ? "divorce" : "civil partnership";
    }

    private String getPartner(CaseData caseData) {
        return caseData.getDivorceOrDissolution().isDivorce()
            ? caseData.getDivorceWho().getLabel().toLowerCase(Locale.UK) : "civil partner";
    }

    private String getPapers(DivorceOrDissolution divorceOrDissolution) {
        return divorceOrDissolution.isDivorce() ?  "divorce " + PAPERS : PAPERS;
    }

    private void setDefaultSupportingDocumentType(Map<String, String> templateVars) {
        templateVars.put(MARRIAGE_CERTIFICATE, NO);
        templateVars.put(CIVIL_PARTNERSHIP_CERTIFICATE, NO);
        templateVars.put(FOREIGN_MARRIAGE_CERTIFICATE, NO);
        templateVars.put(FOREIGN_CIVIL_PARTNERSHIP_CERTIFICATE, NO);
        templateVars.put(FOREIGN_MARRIAGE_CERTIFICATE_TRANSLATION, NO);
        templateVars.put(FOREIGN_CIVIL_PARTNERSHIP_CERTIFICATE_TRANSLATION, NO);
        templateVars.put(NAME_CHANGE_PROOF, NO);
    }

    private void setMissingSupportingDocumentType(Map<String, String> templateVars,  CaseData caseData) {
        for (DocumentType docType : caseData.getCannotUploadSupportingDocument()) {
            switch (docType) {
                case MARRIAGE_CERTIFICATE:
                    if (caseData.getMarriedInUk().toBoolean()) {
                        templateVars.put(
                            caseData.getDivorceOrDissolution().isDivorce()
                                ? MARRIAGE_CERTIFICATE : CIVIL_PARTNERSHIP_CERTIFICATE, YES);
                    } else {
                        templateVars.put(
                            caseData.getDivorceOrDissolution().isDivorce()
                                ? FOREIGN_MARRIAGE_CERTIFICATE : FOREIGN_CIVIL_PARTNERSHIP_CERTIFICATE, YES);
                    }
                    break;
                case MARRIAGE_CERTIFICATE_TRANSLATION:
                    templateVars.put(
                        caseData.getDivorceOrDissolution().isDivorce()
                            ? FOREIGN_MARRIAGE_CERTIFICATE_TRANSLATION
                            : FOREIGN_CIVIL_PARTNERSHIP_CERTIFICATE_TRANSLATION, YES);
                    break;
                case NAME_CHANGE_EVIDENCE:
                    templateVars.put(NAME_CHANGE_PROOF, YES);
                    break;
                default:
                    break;
            }
        }
    }
}
