package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.common.model.SupportingDocumentType;
import uk.gov.hmcts.divorce.notification.NotificationConstants;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static java.lang.String.join;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OUTSTANDING_ACTIONS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CIVIL_PARTNERSHIP_CERTIFICATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CIVIL_PARTNERSHIP_FOREIGN_UNION_CERTIFICATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CIVIL_PARTNERSHIP_FOREIGN_UNION_CERTIFICATE_TRANSLATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.MARRIAGE_FOREIGN_UNION_CERTIFICATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.MARRIAGE_FOREIGN_UNION_CERTIFICATE_TRANSLATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAPERS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PARTNER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SERVICE;

@Component
@Slf4j
public class ApplicationOutstandingActionNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void send(CaseData caseData, Long id) {
        Map<String, String> templateVars = new HashMap<>();
        commonContent.apply(templateVars, caseData);

        templateVars.put(APPLICATION_REFERENCE, formatId(id));
        templateVars.put(SERVICE, getService(caseData.getDivorceOrDissolution()));
        templateVars.put(PARTNER, getPartner(caseData));
        templateVars.put(PAPERS, getPapers(caseData.getDivorceOrDissolution()));


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

    private String formatId(Long id) {
        return join("-", id.toString().split("(?<=\\G....)"));
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

    private void setMissingSupportingDocumentType(Map<String, String> templateVars,  CaseData caseData) {
        templateVars.put(MARRIAGE_CERTIFICATE, "no");
        templateVars.put(CIVIL_PARTNERSHIP_CERTIFICATE, "no");
        templateVars.put(MARRIAGE_FOREIGN_UNION_CERTIFICATE, "no");
        templateVars.put(CIVIL_PARTNERSHIP_FOREIGN_UNION_CERTIFICATE, "no");
        templateVars.put(MARRIAGE_FOREIGN_UNION_CERTIFICATE_TRANSLATION, "no");
        templateVars.put(CIVIL_PARTNERSHIP_FOREIGN_UNION_CERTIFICATE_TRANSLATION, "no");
        templateVars.put(NotificationConstants.NAME_CHANGE_PROOF, "no");

        for (SupportingDocumentType docType : caseData.getCannotUploadSupportingDocument()) {
            switch (docType) {
                case UNION_CERTIFICATE:
                    templateVars.put(
                        caseData.getDivorceOrDissolution().isDivorce()
                            ? MARRIAGE_CERTIFICATE : CIVIL_PARTNERSHIP_CERTIFICATE, "yes");
                    break;
                case FOREIGN_UNION_CERTIFICATE:
                    templateVars.put(
                        caseData.getDivorceOrDissolution().isDivorce()
                            ? MARRIAGE_FOREIGN_UNION_CERTIFICATE : CIVIL_PARTNERSHIP_FOREIGN_UNION_CERTIFICATE, "yes");
                    break;
                case FOREIGN_UNION_CERTIFICATE_TRANSLATION:
                    templateVars.put(
                        caseData.getDivorceOrDissolution().isDivorce()
                            ? MARRIAGE_FOREIGN_UNION_CERTIFICATE_TRANSLATION
                            : CIVIL_PARTNERSHIP_FOREIGN_UNION_CERTIFICATE_TRANSLATION, "yes");
                    break;
                case NAME_CHANGE_PROOF:
                    templateVars.put(NotificationConstants.NAME_CHANGE_PROOF, "yes");
                    break;
                default:
                    break;
            }
        }
    }
}
