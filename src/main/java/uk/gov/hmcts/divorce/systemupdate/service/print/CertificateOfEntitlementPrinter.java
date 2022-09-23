package uk.gov.hmcts.divorce.systemupdate.service.print;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateCertificateOfEntitlement;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.firstElement;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER;


@Component
@Slf4j
public class CertificateOfEntitlementPrinter {

    public static final String LETTER_TYPE_CERTIFICATE_OF_ENTITLEMENT = "certificate-of-entitlement";

    private static final int EXPECTED_DOCUMENTS_SIZE = 2;

    @Autowired
    private BulkPrintService bulkPrintService;

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private GenerateCertificateOfEntitlement generateCertificateOfEntitlement;

    public void sendLetter(final CaseData caseData, final Long caseId, final Applicant applicant) {

        generateCertificateOfEntitlement.generateCertificateOfEntitlementCoverLetter(caseData, caseId, applicant);

        final List<Letter> certificateOfEntitlementLetters = certificateOfEntitlementLetters(caseData);

        if (!isEmpty(certificateOfEntitlementLetters) && certificateOfEntitlementLetters.size() == EXPECTED_DOCUMENTS_SIZE) {
            final String caseIdString = caseId.toString();
            final Print print =
                new Print(certificateOfEntitlementLetters, caseIdString, caseIdString, LETTER_TYPE_CERTIFICATE_OF_ENTITLEMENT);
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "Certificate of Entitlement print has missing documents. Expected documents with type {} , for Case ID: {}",
                List.of(CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER, CERTIFICATE_OF_ENTITLEMENT),
                caseId);
        }
    }

    private List<Letter> certificateOfEntitlementLetters(CaseData caseData) {
        final List<Letter> coverLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER);

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
