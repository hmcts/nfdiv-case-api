package uk.gov.hmcts.divorce.systemupdate.service.print;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.firstElement;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.document.DocumentUtil.getLettersBasedOnContactPrivacy;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;


@Component
@Slf4j
public class CertificateOfEntitlementPrinter {

    public static final String LETTER_TYPE_CERTIFICATE_OF_ENTITLEMENT = "certificate-of-entitlement";

    private static final int EXPECTED_DOCUMENTS_SIZE = 2;

    @Autowired
    private BulkPrintService bulkPrintService;

    public void sendLetter(final CaseData caseData,
                           final Long caseId,
                           final DocumentType coverLetterDocumentType,
                           final Applicant applicant) {

        final List<Letter> certificateOfEntitlementLetters = certificateOfEntitlementLetters(caseData, coverLetterDocumentType);

        if (!isEmpty(certificateOfEntitlementLetters) && certificateOfEntitlementLetters.size() == EXPECTED_DOCUMENTS_SIZE) {
            final String caseIdString = caseId.toString();
            final Print print =
                new Print(
                    certificateOfEntitlementLetters,
                    caseIdString,
                    caseIdString,
                    LETTER_TYPE_CERTIFICATE_OF_ENTITLEMENT,
                    applicant.getFullName()
                );
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "Certificate of Entitlement print has missing documents. Expected documents with type {} , for Case ID: {}",
                List.of(coverLetterDocumentType, CERTIFICATE_OF_ENTITLEMENT),
                caseId);
        }
    }

    private List<Letter> certificateOfEntitlementLetters(CaseData caseData, DocumentType coverLetterDocumentType) {

        final List<Letter> coverLetters = getLettersBasedOnContactPrivacy(caseData, coverLetterDocumentType);
        final Letter coverLetter = firstElement(coverLetters);
        final Letter certificateOfEntitlement =
            new Letter(caseData.getConditionalOrder().getCertificateOfEntitlementDocument(), 1);

        final List<Letter> currentCertificateOfEntitlementLetters = new ArrayList<>();

        if (null != coverLetter) {
            currentCertificateOfEntitlementLetters.add(coverLetter);
        }

        currentCertificateOfEntitlementLetters.add(certificateOfEntitlement);

        return currentCertificateOfEntitlementLetters;
    }
}
