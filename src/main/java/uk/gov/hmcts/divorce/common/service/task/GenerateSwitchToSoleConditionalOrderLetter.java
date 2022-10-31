package uk.gov.hmcts.divorce.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICATION_TO_END_THE_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.THE_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.SWITCH_TO_SOLE_CO_LETTER;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class GenerateSwitchToSoleConditionalOrderLetter {

    public static final String GET_A_DIVORCE = "get a divorce";
    public static final String END_YOUR_CIVIL_PARTNERSHIP = "end your civil partnership";
    public static final String YOU_ARE_DIVORCED = "you are divorced";
    public static final String CIVIL_PARTNERSHIP_LEGALLY_ENDED = "your civil partnership is legally ended";
    public static final String DIVORCED_OR_CP_LEGALLY_ENDED = "divorcedOrCivilPartnershipLegallyEnded";

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private Clock clock;

    @Autowired
    private DocmosisCommonContent docmosisCommonContent;

    public void apply(final CaseData caseData,
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

        final Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
                        applicant.getLanguagePreference());

        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(FIRST_NAME, respondent.getFirstName());
        templateContent.put(LAST_NAME, respondent.getLastName());
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
