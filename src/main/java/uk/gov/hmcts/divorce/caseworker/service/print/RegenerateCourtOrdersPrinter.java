package uk.gov.hmcts.divorce.caseworker.service.print;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.springframework.util.CollectionUtils.firstElement;
import static uk.gov.hmcts.divorce.document.DocumentUtil.getLettersBasedOnContactPrivacy;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_2;

@Component
@Slf4j
public class RegenerateCourtOrdersPrinter {

    private static final String LETTER_TYPE_REGENERATE_COURT_ORDERS = "regenerate-court-orders-letter";
    private static final List<DocumentType> APPLICANT_1_DOCUMENTS = List.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1,
            CONDITIONAL_ORDER_GRANTED,
            FINAL_ORDER_GRANTED_COVER_LETTER_APP_1,
            FINAL_ORDER_GRANTED,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1,
            CERTIFICATE_OF_ENTITLEMENT
    );
    private static final List<DocumentType> APPLICANT_2_DOCUMENTS = List.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2,
            CONDITIONAL_ORDER_GRANTED,
            FINAL_ORDER_GRANTED_COVER_LETTER_APP_2,
            FINAL_ORDER_GRANTED,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2,
            CERTIFICATE_OF_ENTITLEMENT
    );

    @Autowired
    private BulkPrintService bulkPrintService;

    public void print(final CaseData caseData, final Long caseId, final boolean isApplicant1) {

        final List<Letter> regeneratedCourtOrderLetters = regeneratedCourtOrderLetters(caseData, isApplicant1);

        if (!isEmpty(regeneratedCourtOrderLetters)) {
            final String recipientName = isApplicant1 ? caseData.getApplicant1().getFullName() :
                    caseData.getApplicant2().getFullName();

            final String caseIdString = caseId.toString();
            final Print print =
                    new Print(
                            regeneratedCourtOrderLetters,
                            caseIdString,
                            caseIdString,
                            LETTER_TYPE_REGENERATE_COURT_ORDERS,
                            recipientName
                    );
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                    "Regenerate Court Orders Printer has missing documents. Expected documents with type {} , for Case ID: {}",
                    isApplicant1 ? APPLICANT_1_DOCUMENTS : APPLICANT_2_DOCUMENTS,
                    caseId
            );
        }
    }

    private List<Letter> regeneratedCourtOrderLetters(CaseData caseData, boolean isApplicant1) {

        final List<Letter> conditionalOrderGrantedCoverLetters = getLettersBasedOnContactPrivacy(
                caseData,
                isApplicant1 ? CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1 : CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2);

        final List<Letter> conditionalOrderGrantedCertificates = getLettersBasedOnContactPrivacy(
                caseData,
                CONDITIONAL_ORDER_GRANTED);

        final List<Letter> finalOrderGrantedCoverLetters = getLettersBasedOnContactPrivacy(
                caseData,
                isApplicant1 ? FINAL_ORDER_GRANTED_COVER_LETTER_APP_1 : FINAL_ORDER_GRANTED_COVER_LETTER_APP_2);

        final List<Letter> finalOrderGrantedCertificates = getLettersBasedOnContactPrivacy(
                caseData,
                FINAL_ORDER_GRANTED);

        final List<Letter> certificateOfEntitlementCoverLetters = getLettersBasedOnContactPrivacy(
                caseData,
                isApplicant1 ? CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1 : CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2);

        final List<Letter> certificateOfEntitlementDocuments = getLettersBasedOnContactPrivacy(
                caseData,
                CERTIFICATE_OF_ENTITLEMENT);

        final Letter conditionalOrderGrantedCoverLetter = firstElement(conditionalOrderGrantedCoverLetters);
        final Letter conditionalOrderGrantedCertificate = firstElement(conditionalOrderGrantedCertificates);

        final Letter finalOrderGrantedCoverLetter = firstElement(finalOrderGrantedCoverLetters);
        final Letter finalOrderGrantedCertificate = firstElement(finalOrderGrantedCertificates);

        final Letter certificateOfEntitlementCoverLetter = firstElement(certificateOfEntitlementCoverLetters);
        final Letter certificateOfEntitlementDocument = firstElement(certificateOfEntitlementDocuments);

        final List<Letter> regeneratedCourtOrderLetters = new ArrayList<>();

        if (conditionalOrderGrantedCoverLetter != null) {
            regeneratedCourtOrderLetters.add(conditionalOrderGrantedCoverLetter);
        }
        if (conditionalOrderGrantedCertificate != null) {
            regeneratedCourtOrderLetters.add(conditionalOrderGrantedCertificate);
        }
        if (finalOrderGrantedCoverLetter != null) {
            regeneratedCourtOrderLetters.add(finalOrderGrantedCoverLetter);
        }
        if (finalOrderGrantedCertificate != null) {
            regeneratedCourtOrderLetters.add(finalOrderGrantedCertificate);
        }
        if (certificateOfEntitlementCoverLetter != null) {
            regeneratedCourtOrderLetters.add(certificateOfEntitlementCoverLetter);
        }
        if (certificateOfEntitlementDocument != null) {
            regeneratedCourtOrderLetters.add(certificateOfEntitlementDocument);
        }

        return regeneratedCourtOrderLetters;
    }
}