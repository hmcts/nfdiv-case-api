package uk.gov.hmcts.divorce.systemupdate.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static java.lang.String.join;
import static java.time.LocalDateTime.now;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CO_GRANTED_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CO_PRONOUNCED_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_REPRESENTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_FIRM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TIME_OF_HEARING;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;
import static uk.gov.hmcts.divorce.document.model.DocumentType.JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class ConditionalOrderPronouncedCoverLetterHelper {

    public static final String NAME = "name";
    public static final String ADDRESS = "address";
    public static final String PRONOUNCEMENT_DATE_PLUS_43 = "pronouncementDatePlus43";

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private Clock clock;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private DocmosisCommonContent docmosisCommonContent;

    public void generateConditionalOrderPronouncedCoversheet(final CaseData caseData,
                                                              final Long caseId,
                                                              final Applicant applicant,
                                                              final DocumentType documentType) {

        final Map<String, Object> templateVars = caseData.isJudicialSeparationCase() && applicant.isRepresented()
            ? templateVarsForJSSolicitor(caseData, caseId, applicant)
            : templateVars(caseData, caseId, applicant);

        DocumentType coverLetterType = caseData.isJudicialSeparationCase()
            ? getJSCoverLetterType(applicant.isRepresented(), caseData.isDivorce(), false)
            : documentType;

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            coverLetterType,
            templateVars,
            caseId,
            getCoverLetterTemplateId(caseData.isJudicialSeparationCase(), applicant.isRepresented(), false),
            applicant.getLanguagePreference(),
            formatDocumentName(caseId, getCoverLetterDocumentName(caseData, applicant.isRepresented()), now(clock))
        );
    }

    public void generateConditionalOrderPronouncedCoversheetOfflineRespondent(final CaseData caseData,
                                                              final Long caseId,
                                                              final Applicant applicant,
                                                              final Applicant partner) {

        final Map<String, Object> templateVarsForOfflineRespondent = caseData.isJudicialSeparationCase() && applicant.isRepresented()
            ? templateVarsForJSSolicitor(caseData, caseId, applicant)
            : templateVarsForOfflineRespondent(caseData, caseId, applicant, partner);

        final DocumentType coverLetterType = caseData.isJudicialSeparationCase()
            ? getJSCoverLetterType(applicant.isRepresented(), caseData.isDivorce(), true)
            : CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            coverLetterType,
            templateVarsForOfflineRespondent,
            caseId,
            getCoverLetterTemplateId(caseData.isJudicialSeparationCase(), applicant.isRepresented(), true),
            applicant.getLanguagePreference(),
            formatDocumentName(caseId, getCoverLetterDocumentName(caseData, applicant.isRepresented()), now(clock))
        );
    }

    private DocumentType getJSCoverLetterType(final boolean isRepresented, final boolean isDivorce, final boolean isOfflineRespondent) {

        if (isRepresented) {
            return isDivorce
                ? JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET
                : SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET;
        } else if (isOfflineRespondent) {
            return CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;
        } else {
            return CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
        }
    }

    private String getCoverLetterTemplateId(final boolean isJudicialSeparation,
                                            final boolean isRepresented,
                                            final boolean isOfflineRespondent) {
        String templateId = isOfflineRespondent
            ? CO_PRONOUNCED_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID
            : CO_GRANTED_COVER_LETTER_TEMPLATE_ID;

        if (isJudicialSeparation) {
            templateId = isRepresented
                ? JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID
                : JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID;
        }

        return templateId;
    }

    private String getCoverLetterDocumentName(final CaseData caseData, final boolean isRepresented) {
        String documentName = CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
        if (caseData.isJudicialSeparationCase()) {
            if (caseData.isDivorce()) {
                documentName = isRepresented
                    ? JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME
                    : JUDICIAL_SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
            } else {
                documentName = isRepresented
                    ? SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME
                    : SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
            }
        }

        return documentName;
    }

    private Map<String, Object> templateVars(final CaseData caseData, final Long caseId, final Applicant applicant) {

        final Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
            applicant.getLanguagePreference());

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

    private Map<String, Object> templateVarsForOfflineRespondent(final CaseData caseData,
                                             final Long caseId,
                                             final Applicant applicant,
                                             final Applicant partner) {

        final Map<String, Object> templateContent = templateVars(caseData, caseId, applicant);

        final LocalDateTime dateAndTimeOfHearing = caseData.getConditionalOrder().getDateAndTimeOfHearing();
        final String dateOfHearing = nonNull(dateAndTimeOfHearing) ? dateAndTimeOfHearing.format(DATE_TIME_FORMATTER) : null;
        final String timeOfHearing = nonNull(dateAndTimeOfHearing) ? dateAndTimeOfHearing.format(TIME_FORMATTER) : null;

        templateContent.put(DATE_OF_HEARING, dateOfHearing);
        templateContent.put(TIME_OF_HEARING, timeOfHearing);
        templateContent.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        templateContent.put(PARTNER, commonContent.getPartner(caseData, partner, applicant.getLanguagePreference()));

        return templateContent;
    }

    private Map<String, Object> templateVarsForJSSolicitor(final CaseData caseData,
                                                           final Long caseId,
                                                           final Applicant applicant) {

        final Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
            applicant.getLanguagePreference());

        templateContent.put(SOLICITOR_NAME, applicant.getSolicitor().getName());
        templateContent.put(SOLICITOR_FIRM, applicant.getSolicitor().getFirmName());
        templateContent.put(SOLICITOR_ADDRESS, applicant.getSolicitor().getAddress());
        templateContent.put(DATE, LocalDate.now().format(DATE_TIME_FORMATTER));
        templateContent.put(CASE_REFERENCE, caseId != null ? formatId(caseId) : null);
        templateContent.put(IS_JOINT, !caseData.getApplicationType().isSole());
        templateContent.put(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName());
        templateContent.put(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName());
        templateContent.put(APPLICANT_1_SOLICITOR_NAME, caseData.getApplicant1().getSolicitor() != null
            ? caseData.getApplicant1().getSolicitor().getName()
            : NOT_REPRESENTED
        );
        templateContent.put(APPLICANT_2_SOLICITOR_NAME, caseData.getApplicant2().getSolicitor() != null
            ? caseData.getApplicant2().getSolicitor().getName()
            : NOT_REPRESENTED
        );
        templateContent.put(SOLICITOR_REFERENCE, applicant.getSolicitor().getReference() != null
            ? applicant.getSolicitor().getReference()
            : NOT_PROVIDED
        );
        templateContent.put(IS_DIVORCE, caseData.getDivorceOrDissolution().isDivorce());

        return templateContent;
    }
}
