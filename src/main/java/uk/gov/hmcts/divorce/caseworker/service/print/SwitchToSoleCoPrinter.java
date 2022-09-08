package uk.gov.hmcts.divorce.caseworker.service.print;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.join;
import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.springframework.util.CollectionUtils.firstElement;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICATION_TO_END_THE_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.THE_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.SWITCH_TO_SOLE_CO_LETTER;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class SwitchToSoleCoPrinter {

    public static final String GET_A_DIVORCE = "get a divorce";
    public static final String END_YOUR_CIVIL_PARTNERSHIP = "end your civil partnership";
    public static final String YOU_ARE_DIVORCED = "you are divorced";
    public static final String CIVIL_PARTNERSHIP_LEGALLY_ENDED = "your civil partnership is legally ended";
    public static final String DIVORCED_OR_CP_LEGALLY_ENDED = "divorcedOrCivilPartnershipLegallyEnded";

    private static final String LETTER_TYPE_SWITCH_TO_SOLE_CO = "switch-to-sole-co-letter";

    @Autowired
    private BulkPrintService bulkPrintService;

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private Clock clock;

    public void print(final CaseData caseData,
                      final Long caseId,
                      final Applicant applicant,
                      final Applicant respondent) {

        generateSwitchToSoleCoLetter(caseData, caseId, applicant, respondent);

        final List<Letter> switchToSoleCoLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            SWITCH_TO_SOLE_CO_LETTER);

        final Letter switchToSoleCoLetter = firstElement(switchToSoleCoLetters);

        if (!isEmpty(switchToSoleCoLetter)) {

            final String caseIdString = caseId.toString();
            final Print print =
                new Print(singletonList(switchToSoleCoLetter), caseIdString, caseIdString, LETTER_TYPE_SWITCH_TO_SOLE_CO);
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "Switch To Sole Conditional Order has missing documents. Expected document with type {} , for Case ID: {}",
                List.of(SWITCH_TO_SOLE_CO_LETTER),
                caseId
            );
        }
    }

    private void generateSwitchToSoleCoLetter(final CaseData caseData,
                                              final Long caseId,
                                              final Applicant applicant,
                                              final Applicant respondent) {

        log.info("Generating coversheet for sole case id {} ", caseId);
        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            SWITCH_TO_SOLE_CO_LETTER,
            templateContent(caseData, caseId, applicant, respondent),
            caseId,
            SWITCH_TO_SOLE_CO_LETTER_TEMPLATE_ID,
            respondent.getLanguagePreference(),
            formatDocumentName(caseId, SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME, now(clock))
        );
    }

    private Map<String, Object> templateContent(final CaseData caseData,
                                                final Long caseId,
                                                final Applicant applicant,
                                                final Applicant respondent) {

        final Map<String, Object> templateContent = new HashMap<>();

        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(NAME, join(" ", respondent.getFirstName(), respondent.getLastName()));
        templateContent.put(ADDRESS, respondent.getPostalAddress());
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(PARTNER, commonContent.getPartner(caseData, applicant, respondent.getLanguagePreference()));

        templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, caseData.isDivorce() ? GET_A_DIVORCE :  END_YOUR_CIVIL_PARTNERSHIP);
        templateContent.put(DIVORCED_OR_CP_LEGALLY_ENDED, caseData.isDivorce() ? YOU_ARE_DIVORCED : CIVIL_PARTNERSHIP_LEGALLY_ENDED);
        templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, caseData.isDivorce() ? MARRIAGE : CIVIL_PARTNERSHIP);
        templateContent.put(THE_APPLICATION, caseData.isDivorce() ? DIVORCE : APPLICATION_TO_END_THE_CIVIL_PARTNERSHIP);

        return templateContent;
    }
}
