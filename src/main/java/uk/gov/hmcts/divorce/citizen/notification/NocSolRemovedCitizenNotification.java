package uk.gov.hmcts.divorce.citizen.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.util.AddressUtil;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_SOL_STOPPED_REP_NOTIFY_APP_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_SOL_STOPPED_REP_NOTIFY_APP_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_STOP_REPRESENTATION_SELF_NOTIFY_CITIZEN;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
@RequiredArgsConstructor
public class NocSolRemovedCitizenNotification {

    private final CommonContent commonContent;
    private final NotificationService notificationService;
    private final DocmosisCommonContent docmosisCommonContent;
    private final CaseDataDocumentService caseDataDocumentService;
    private final BulkPrintService bulkPrintService;

    public static final String LETTER_TYPE_NOTIFY_CITIZEN_SOL_STOPPED_REP = "notify-citizen-sol-stopped-rep";
    private static final String RECIPIENT_ADDRESS = "address";

    public void send(final CaseData data, final boolean isApplicant1, final Long caseId) {
        Applicant applicant = isApplicant1 ? data.getApplicant1() : data.getApplicant2();
        Applicant partner = isApplicant1 ? data.getApplicant2() : data.getApplicant1();

        generateNoCNotificationLetterAndSend(caseId, applicant);

        if (StringUtils.isNotEmpty(applicant.getEmail())) {
            Map<String, String> templateVars = commonContent.mainTemplateVars(data, caseId, applicant, partner);

            notificationService.sendEmail(
                applicant.getEmail(),
                SOLICITOR_STOP_REPRESENTATION_SELF_NOTIFY_CITIZEN,
                templateVars,
                applicant.getLanguagePreference(),
                caseId
            );
        }
    }

    private void generateNoCNotificationLetterAndSend(Long caseId, Applicant applicant) {

        Document generatedDocument = generateDocument(caseId, applicant);

        Letter letter = new  Letter(generatedDocument, 1);
        String caseIdString = String.valueOf(caseId);

        final Print print = new Print(
            List.of(letter),
            caseIdString,
            caseIdString,
            LETTER_TYPE_NOTIFY_CITIZEN_SOL_STOPPED_REP,
            applicant.getFullName(),
            applicant.getAddressOverseas()
        );

        final UUID letterId = bulkPrintService.print(print);

        log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
    }

    private Document generateDocument(final long caseId,
                                      final Applicant applicant) {

        return caseDataDocumentService.renderDocument(getTemplateContent(caseId, applicant),
            caseId,
            NFD_SOL_STOPPED_REP_NOTIFY_APP_TEMPLATE_ID,
            applicant.getLanguagePreference(),
            NFD_SOL_STOPPED_REP_NOTIFY_APP_DOCUMENT_NAME);
    }

    private Map<String, Object> getTemplateContent(Long caseId, Applicant applicant) {
        Map<String, Object> templateContent = docmosisCommonContent
            .getBasicDocmosisTemplateContent(applicant.getLanguagePreference());
        templateContent.put(FIRST_NAME, applicant.getFirstName());
        templateContent.put(LAST_NAME, applicant.getLastName());
        templateContent.put(RECIPIENT_ADDRESS, AddressUtil.getPostalAddress(applicant.getAddress()));
        templateContent.put(CASE_REFERENCE, formatId(caseId));

        return templateContent;
    }
}
