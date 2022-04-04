package uk.gov.hmcts.divorce.caseworker.service.print;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

import static org.springframework.util.CollectionUtils.firstElement;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.document.DocumentUtil.getConfidentialDocumentType;
import static uk.gov.hmcts.divorce.document.DocumentUtil.isApplicableForConfidentiality;
import static uk.gov.hmcts.divorce.document.DocumentUtil.isConfidential;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithAosScannedDocument;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithConfidentialDocumentType;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.ACKNOWLEDGEMENT_OF_SERVICE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.AOS_RESPONSE_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;


@Component
@Slf4j
public class AosPackPrinter {

    private static final String LETTER_TYPE_RESPONDENT_PACK = "respondent-aos-pack";
    private static final String LETTER_TYPE_APPLICANT_PACK = "applicant-aos-pack";
    private static final String LETTER_TYPE_AOS_RESPONSE_PACK = "aos-response-pack";

    @Autowired
    private BulkPrintService bulkPrintService;

    public void sendAosLetterToRespondent(final CaseData caseData, final Long caseId) {

        final List<Letter> currentAosLetters = aosLettersForRespondent(caseData);

        if (!isEmpty(currentAosLetters)) {
            final String caseIdString = caseId.toString();
            final Print print = new Print(currentAosLetters, caseIdString, caseIdString, LETTER_TYPE_RESPONDENT_PACK);
            final var app2 = caseData.getApplicant2();

            boolean includeD10Document = app2.isRepresented() && app2.getSolicitor() != null
                ? !app2.getSolicitor().hasOrgId()
                : StringUtils.isEmpty(caseData.getApplicant2().getEmail());
            final UUID letterId = bulkPrintService.printAosRespondentPack(print, includeD10Document);
            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "AoS Pack print for respondent has missing documents. Expected documents with type {} , for Case ID: {}",
                List.of(APPLICATION, NOTICE_OF_PROCEEDINGS_APP_2),
                caseId);
        }
    }

    public void sendAosLetterToApplicant(final CaseData caseData, final Long caseId) {

        final List<Letter> currentAosLetters = aosLetters(caseData, NOTICE_OF_PROCEEDINGS_APP_1);

        if (!isEmpty(currentAosLetters)) {

            final String caseIdString = caseId.toString();
            final Print print = new Print(currentAosLetters, caseIdString, caseIdString, LETTER_TYPE_APPLICANT_PACK);
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "AoS Pack for print applicant has missing documents. Expected documents with type {} , for Case ID: {}",
                List.of(APPLICATION, NOTICE_OF_PROCEEDINGS_APP_1),
                caseId
            );
        }
    }

    public void sendAosLetterAndRespondentAosPackToApplicant(final CaseData caseData, final Long caseId) {

        final List<Letter> currentAosLetters = personalServiceLetters(caseData);

        if (!isEmpty(currentAosLetters)) {

            final String caseIdString = caseId.toString();
            final Print print = new Print(currentAosLetters, caseIdString, caseIdString, LETTER_TYPE_APPLICANT_PACK);
            final UUID letterId = bulkPrintService.printWithD10Form(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "AoS Pack for print applicant has missing documents. Expected documents with type {} , for Case ID: {}",
                List.of(APPLICATION, NOTICE_OF_PROCEEDINGS_APP_1, COVERSHEET, NOTICE_OF_PROCEEDINGS_APP_2),
                caseId
            );
        }
    }

    public void sendAosResponseLetterToApplicant(final CaseData caseData, final Long caseId) {

        final List<Letter> aosResponseLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            AOS_RESPONSE_LETTER);

        final List<Letter> aosLetters = lettersWithAosScannedDocument(caseData.getDocuments().getScannedDocuments());

        final Letter aosResponseLetter = firstElement(aosResponseLetters);

        final Letter aosLetter = firstElement(aosLetters);

        final List<Letter> aosResponseLetterWithAos = new ArrayList<>();

        if (null != aosResponseLetter) {
            aosResponseLetterWithAos.add(aosResponseLetter);
        }
        if (null != aosLetter) {
            aosResponseLetterWithAos.add(aosLetter);
        }

        if (!isEmpty(aosResponseLetterWithAos)) {

            final String caseIdString = caseId.toString();
            final Print print = new Print(aosResponseLetterWithAos, caseIdString, caseIdString, LETTER_TYPE_AOS_RESPONSE_PACK);
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "Aos response letter for applicant has missing documents. Expected documents with type {} , for case id: {}",
                List.of(AOS_RESPONSE_LETTER, ACKNOWLEDGEMENT_OF_SERVICE),
                caseId
            );
        }
    }

    private List<Letter> aosLetters(CaseData caseData, DocumentType documentType) {
        final List<Letter> divorceApplicationLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            APPLICATION);

        //Always get document on top of list as new document is added to top after generation
        final Letter divorceApplicationLetter = firstElement(divorceApplicationLetters);

        //Always get document on top of list as new document is added to top after generation
        final Letter notificationLetter = firstElement(getNotificationLetters(caseData, documentType));

        final List<Letter> currentAosLetters = new ArrayList<>();

        if (null != notificationLetter) {
            currentAosLetters.add(notificationLetter);
        }
        if (null != divorceApplicationLetter) {
            currentAosLetters.add(divorceApplicationLetter);
        }
        return currentAosLetters;
    }

    private List<Letter> aosLettersForRespondent(CaseData caseData) {
        final List<Letter> coversheetLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            COVERSHEET);

        final List<Letter> respondentInvitationLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            NOTICE_OF_PROCEEDINGS_APP_2);

        final List<Letter> divorceApplicationLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            APPLICATION);

        final Letter coversheetLetter = firstElement(coversheetLetters);
        final Letter respondentInvitationLetter = firstElement(respondentInvitationLetters);
        final Letter divorceApplicationLetter = firstElement(divorceApplicationLetters);

        final List<Letter> currentAosLetters = new ArrayList<>();

        if (null != coversheetLetter) {
            currentAosLetters.add(coversheetLetter);
        }
        if (null != respondentInvitationLetter) {
            currentAosLetters.add(respondentInvitationLetter);
        }
        if (null != divorceApplicationLetter) {
            currentAosLetters.add(divorceApplicationLetter);
        }
        return currentAosLetters;
    }

    private List<Letter> personalServiceLetters(final CaseData caseData) {
        final List<Letter> coversheetLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            COVERSHEET);

        final List<Letter> respondentInvitationLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            NOTICE_OF_PROCEEDINGS_APP_2);

        final List<Letter> divorceApplicationLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            APPLICATION);

        final Letter coversheetLetter = firstElement(coversheetLetters);
        final Letter respondentInvitationLetter = firstElement(respondentInvitationLetters);
        final Letter divorceApplicationLetter = firstElement(divorceApplicationLetters);
        final Letter notificationLetter = firstElement(getNotificationLetters(caseData, NOTICE_OF_PROCEEDINGS_APP_1));

        final List<Letter> currentAosLetters = new ArrayList<>();

        if (null != notificationLetter) {
            currentAosLetters.add(notificationLetter);
        }

        if (null != divorceApplicationLetter) {
            currentAosLetters.add(divorceApplicationLetter);
        }
        if (null != coversheetLetter) {
            currentAosLetters.add(coversheetLetter);
        }

        if (null != respondentInvitationLetter) {
            currentAosLetters.add(respondentInvitationLetter);
        }

        if (null != divorceApplicationLetter) {
            currentAosLetters.add(divorceApplicationLetter);
        }

        return currentAosLetters;
    }

    private List<Letter> getNotificationLetters(final CaseData caseData, final DocumentType documentType) {
        List<Letter> notificationLetters;

        if (isApplicableForConfidentiality(documentType, null) && isConfidential(caseData, documentType)) {
            notificationLetters = lettersWithConfidentialDocumentType(
                caseData.getDocuments().getConfidentialDocumentsGenerated(),
                getConfidentialDocumentType(documentType));
        } else {
            notificationLetters = lettersWithDocumentType(
                caseData.getDocuments().getDocumentsGenerated(),
                documentType);
        }
        return notificationLetters;
    }
}
