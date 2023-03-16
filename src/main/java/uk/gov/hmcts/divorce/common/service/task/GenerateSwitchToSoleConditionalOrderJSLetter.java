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
import java.util.Map;

import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_JS_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.SWITCH_TO_SOLE_CO_LETTER;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class GenerateSwitchToSoleConditionalOrderJSLetter {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private DocmosisCommonContent docmosisCommonContent;

    @Autowired
    private Clock clock;

    public void apply(final CaseData caseData,
                      final Long caseId,
                      final Applicant applicant,
                      final Applicant respondent) {

        log.info("Generating JS Switched to sole cover letter for case id {} ", caseId);
        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            SWITCH_TO_SOLE_CO_LETTER,
            templateContent(caseData, caseId, applicant, respondent),
            caseId,
            SWITCH_TO_SOLE_CO_JS_LETTER_TEMPLATE_ID,
            respondent.getLanguagePreference(),
            formatDocumentName(caseId, SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME, now(clock))
        );
    }

    private Map<String, Object> templateContent(final CaseData caseData,
                                                final Long caseId,
                                                final Applicant applicant,
                                                final Applicant respondent) {

        final Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
            respondent.getLanguagePreference());

        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(IS_DIVORCE, caseData.getDivorceOrDissolution().isDivorce());
        templateContent.put(FIRST_NAME, respondent.getFirstName());
        templateContent.put(LAST_NAME, respondent.getLastName());
        templateContent.put(ADDRESS, respondent.getPostalAddress());
        templateContent.put(PARTNER, commonContent.getPartner(caseData, applicant, respondent.getLanguagePreference()));
        templateContent.put(DATE, now(clock).format(DATE_TIME_FORMATTER));

        return templateContent;
    }
}
