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
import java.util.Map;

import static java.lang.String.join;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
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
        templateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, isMarriageOrCivilPartnership(caseData.getDivorceOrDissolution()));


        if (caseData.getCannotUploadSupportingDocument() != null && !caseData.getCannotUploadSupportingDocument().isEmpty()) {
            setMissingSupportingDocumentType(templateVars, caseData);
        }

        log.info("Sending application outstanding actions notification for case : {}", id);

        notificationService.sendEmail(
            caseData.getPetitionerEmail(),
            APPLICATION_SUBMITTED,
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
        return caseData.getDivorceOrDissolution().isDivorce() ? caseData.getDivorceWho().getLabel().toLowerCase() : "civil partner";
    }

    private String getPapers(DivorceOrDissolution divorceOrDissolution) {
        return divorceOrDissolution.isDivorce() ?  "divorce " + PAPERS : PAPERS;
    }

    private String isMarriageOrCivilPartnership(DivorceOrDissolution divorceOrDissolution) {
        return divorceOrDissolution.isDivorce() ? "marriage" : "civil partnership";
    }

    private void setMissingSupportingDocumentType(Map<String, String> templateVars,  CaseData caseData) {
        for (SupportingDocumentType docType : caseData.getCannotUploadSupportingDocument()) {
            switch (docType) {
                case UNION_CERTIFICATE:
                    templateVars.put(NotificationConstants.UNION_CERTIFICATE, "yes");
                    break;
                case FOREIGN_UNION_CERTIFICATE:
                    templateVars.put(NotificationConstants.FOREIGN_UNION_CERTIFICATE, "yes");
                    break;
                case FOREIGN_UNION_CERTIFICATE_TRANSLATION:
                    templateVars.put(NotificationConstants.FOREIGN_UNION_CERTIFICATE_TRANSLATION, "yes");
                    break;
                case NAME_CHANGE_PROOF:
                    templateVars.put(NotificationConstants.NAME_CHANGE_PROOF, "yes");
                    break;
            }
        }
    }
}
