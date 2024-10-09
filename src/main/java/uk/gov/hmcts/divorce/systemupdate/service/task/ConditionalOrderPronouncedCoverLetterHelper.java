package uk.gov.hmcts.divorce.systemupdate.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
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
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
@Deprecated
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

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            documentType,
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

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2,
            templateVarsForOfflineRespondent,
            caseId,
            getCoverLetterTemplateId(caseData.isJudicialSeparationCase(), applicant.isRepresented(), true),
            applicant.getLanguagePreference(),
            formatDocumentName(caseId, getCoverLetterDocumentName(caseData, applicant.isRepresented()), now(clock))
        );
    }

    /*
    The below code to get the specific template id is out of date. NFDIV-4260 has introduced
    solicitor specific templates for conditional order granted coversheet. tech-dent ticket
    NFDIV-4363 has been created to ensure that the regeneration code is up to date and picks up
    correct template to use and is tested for end use.
     */

    private String getCoverLetterTemplateId(final boolean isJudicialSeparation,
                                            final boolean isRepresented,
                                            final boolean isOfflineRespondent) {

        if (isJudicialSeparation) {
            return isRepresented
                ? JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID
                : JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID;
        } else {
            return isOfflineRespondent
                ? CO_PRONOUNCED_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID
                : CO_GRANTED_COVER_LETTER_TEMPLATE_ID;
        }
    }

    private String getCoverLetterDocumentName(final CaseData caseData, final boolean isRepresented) {
        if (caseData.isJudicialSeparationCase()) {
            if (caseData.isDivorce()) {
                return isRepresented
                    ? JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME
                    : JUDICIAL_SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
            } else {
                return isRepresented
                    ? SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME
                    : SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
            }
        }

        return CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
    }

    private Map<String, Object> templateVars(final CaseData caseData, final Long caseId, final Applicant applicant) {

        final Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
            applicant.getLanguagePreference());

        if (applicant.isRepresented()) {
            templateContent.put(NAME, applicant.getSolicitor().getName());
        } else {
            templateContent.put(NAME, join(" ", applicant.getFirstName(), applicant.getLastName()));
        }

        templateContent.put(ADDRESS, applicant.getCorrespondenceAddressWithoutConfidentialCheck());
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
        final ConditionalOrder conditionalOrder = caseData.getConditionalOrder();
        String courtLabel = null;
        if (conditionalOrder != null && conditionalOrder.getCourt() != null) {
            courtLabel = conditionalOrder.getCourt().getLabel();
        }
        templateContent.put(COURT_NAME, courtLabel);
        templateContent.put(PARTNER, commonContent.getPartner(caseData, partner, applicant.getLanguagePreference()));

        return templateContent;
    }

    private Map<String, Object> templateVarsForJSSolicitor(final CaseData caseData,
                                                           final Long caseId,
                                                           final Applicant applicant) {

        final Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
            applicant.getLanguagePreference());

        Applicant applicant1 = caseData.getApplicant1();
        Applicant applicant2 = caseData.getApplicant2();
        templateContent.put(SOLICITOR_NAME, applicant.getSolicitor().getName());
        templateContent.put(SOLICITOR_FIRM, applicant.getSolicitor().getFirmName());
        templateContent.put(SOLICITOR_ADDRESS, applicant.getSolicitor().getAddress());
        templateContent.put(DATE, LocalDate.now().format(DATE_TIME_FORMATTER));
        templateContent.put(CASE_REFERENCE, caseId != null ? formatId(caseId) : null);
        templateContent.put(IS_JOINT, !caseData.getApplicationType().isSole());
        templateContent.put(APPLICANT_1_FULL_NAME, applicant1.getFullName());
        templateContent.put(APPLICANT_2_FULL_NAME, applicant2.getFullName());
        templateContent.put(APPLICANT_1_SOLICITOR_NAME, applicant1.isRepresented() && applicant1.getSolicitor() != null
            ? applicant1.getSolicitor().getName()
            : NOT_REPRESENTED
        );
        templateContent.put(APPLICANT_2_SOLICITOR_NAME, applicant2.isRepresented() && applicant2.getSolicitor() != null
            ? applicant2.getSolicitor().getName()
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
