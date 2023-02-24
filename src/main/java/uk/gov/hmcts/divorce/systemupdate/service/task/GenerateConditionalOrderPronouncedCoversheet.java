package uk.gov.hmcts.divorce.systemupdate.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.join;
import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CO_GRANTED_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class GenerateConditionalOrderPronouncedCoversheet implements CaseTask {

    public static final String NAME = "name";
    public static final String ADDRESS = "address";
    public static final String PRONOUNCEMENT_DATE_PLUS_43 = "pronouncementDatePlus43";

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        if (caseData.getApplicant1().isOffline()) {
            log.info("Generating applicant 1 conditional order pronounced coversheet for case id {} ", caseId);
            generateConditionalOrderPronouncedCoversheet(
                caseData,
                caseId,
                caseData.getApplicant1(),
                CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1
            );
        }

        if (isBlank(caseData.getApplicant2EmailAddress()) || caseData.getApplicant2().isOffline()) {
            log.info("Generating applicant 2 conditional order pronounced coversheet for case id {} ", caseId);
            generateConditionalOrderPronouncedCoversheet(
                caseData,
                caseId,
                caseData.getApplicant2(),
                CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2
            );
        }

        return caseDetails;
    }

    private void generateConditionalOrderPronouncedCoversheet(final CaseData caseData,
                                                              final Long caseId,
                                                              final Applicant applicant,
                                                              final DocumentType documentType) {

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            documentType,
            templateVars(caseData, caseId, applicant),
            caseId,
            CO_GRANTED_COVER_LETTER_TEMPLATE_ID,
            applicant.getLanguagePreference(),
            formatDocumentName(caseId, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME, now(clock))
        );
    }

    private Map<String, Object> templateVars(final CaseData caseData,
                                             final Long caseId,
                                             final Applicant applicant) {

        final Map<String, Object> templateContent = new HashMap<>();

        if (applicant.isRepresented()) {
            templateContent.put(NAME, applicant.getSolicitor().getName());
        } else {
            templateContent.put(NAME, join(" ", applicant.getFirstName(), applicant.getLastName()));
        }

        templateContent.put(ADDRESS, applicant.getPostalAddress());
        templateContent.put(DATE, LocalDate.now().format(DATE_TIME_FORMATTER));
        templateContent.put(CASE_REFERENCE, caseId != null ? formatId(caseId) : null);
        templateContent.put(IS_DIVORCE, caseData.getDivorceOrDissolution().isDivorce());
        templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP,
            caseData.getDivorceOrDissolution().isDivorce() ? MARRIAGE : CIVIL_PARTNERSHIP);
        templateContent.put(PRONOUNCEMENT_DATE_PLUS_43,
            caseData.getConditionalOrder().getGrantedDate() != null
                ? caseData.getConditionalOrder().getGrantedDate().plusDays(43).format(DATE_TIME_FORMATTER)
                : null
        );

        return templateContent;
    }
}
