package uk.gov.hmcts.divorce.caseworker.service.print;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.firstElement;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.DocumentUtil.getLettersBasedOnContactPrivacy;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;

@Component
@RequiredArgsConstructor
@Slf4j
public class AosPackPrinter {

    private static final String LETTER_TYPE_RESPONDENT_PACK = "respondent-aos-pack";
    private static final String LETTER_TYPE_APPLICANT_PACK = "applicant-aos-pack";

    private final BulkPrintService bulkPrintService;

    public void sendAosLetterToRespondent(final CaseData caseData, final Long caseId) {

        final List<Letter> currentAosLetters = aosLettersForRespondent(caseData);

        if (!isEmpty(currentAosLetters)) {

            final String caseIdString = caseId.toString();
            final var app2 = caseData.getApplicant2();
            final Print print = new Print(
                currentAosLetters,
                caseIdString,
                caseIdString,
                LETTER_TYPE_RESPONDENT_PACK,
                app2.getFullName(),
                app2.getCorrespondenceAddressIsOverseas()
            );

            boolean app2HasSolicitor = app2.isRepresented() && app2.getSolicitor() != null;
            boolean app2EmailIsEmpty = StringUtils.isEmpty(app2.getEmail());
            boolean app2IsOverseas = YES.equals(app2.getCorrespondenceAddressIsOverseas());

            var app2NeedsD10 = app2HasSolicitor ? !app2.getSolicitor().hasOrgId() : (app2EmailIsEmpty || app2IsOverseas);

            var d10Needed = caseData.getApplicationType().isSole() && app2NeedsD10;
            final UUID letterId = bulkPrintService.printAosRespondentPack(print, d10Needed);
            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "AoS Pack print for respondent has missing documents. Expected documents with type {} , for Case ID: {}",
                List.of(APPLICATION, NOTICE_OF_PROCEEDINGS_APP_2),
                caseId);
        }
    }

    public void sendAosLetterToApplicant(final CaseData caseData, final Long caseId) {

        final List<Letter> currentAosLetters = aosLetters(caseData);

        if (!isEmpty(currentAosLetters)) {

            final String caseIdString = caseId.toString();
            final Print print = new Print(
                currentAosLetters,
                caseIdString,
                caseIdString,
                LETTER_TYPE_APPLICANT_PACK,
                caseData.getApplicant1().getFullName(),
                caseData.getApplicant1().getCorrespondenceAddressIsOverseas()
            );
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
            final Print print = new Print(
                currentAosLetters,
                caseIdString,
                caseIdString,
                LETTER_TYPE_APPLICANT_PACK,
                caseData.getApplicant1().getFullName(),
                caseData.getApplicant1().getCorrespondenceAddressIsOverseas()
            );
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

    private List<Letter> aosLetters(CaseData caseData) {
        final List<Letter> divorceApplicationLetters = getLettersBasedOnContactPrivacy(caseData, APPLICATION);

        //Always get document on top of list as new document is added to top after generation
        final Letter divorceApplicationLetter = firstElement(divorceApplicationLetters);

        //Always get document on top of list as new document is added to top after generation
        final Letter notificationLetter = firstElement(getLettersBasedOnContactPrivacy(caseData, NOTICE_OF_PROCEEDINGS_APP_1));

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
        final Letter coversheetLetter = firstElement(getLettersBasedOnContactPrivacy(caseData, COVERSHEET));
        final Letter respondentInvitationLetter = firstElement(getLettersBasedOnContactPrivacy(caseData, NOTICE_OF_PROCEEDINGS_APP_2));
        final Letter divorceApplicationLetter = firstElement(getLettersBasedOnContactPrivacy(caseData,
            APPLICATION));

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
        final List<Letter> coversheetLetters = getLettersBasedOnContactPrivacy(caseData, COVERSHEET);
        final List<Letter> respondentInvitationLetters = getLettersBasedOnContactPrivacy(caseData, NOTICE_OF_PROCEEDINGS_APP_2);
        final List<Letter> divorceApplicationLetters = getLettersBasedOnContactPrivacy(caseData, APPLICATION);

        final Letter coversheetLetter = firstElement(coversheetLetters);
        final Letter respondentInvitationLetter = firstElement(respondentInvitationLetters);
        final Letter divorceApplicationLetter = firstElement(divorceApplicationLetters);
        final Letter notificationLetter = firstElement(getLettersBasedOnContactPrivacy(caseData, NOTICE_OF_PROCEEDINGS_APP_1));

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
}
