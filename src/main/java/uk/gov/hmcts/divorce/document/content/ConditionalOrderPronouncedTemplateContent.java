package uk.gov.hmcts.divorce.document.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.document.content.templatecontent.TemplateContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.lang.String.join;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_PRONOUNCED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CO_GRANTED_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CO_GRANTED_SOL_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CO_PRONOUNCED_COVER_LETTER_OFFLINE_RESPONDENT_SOL_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CO_PRONOUNCED_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_PRONOUNCED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COUNTRY_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CO_PRONOUNCED_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_FO_ELIGIBLE_FROM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DOCUMENTS_ISSUED_ON;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_SOLE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.JUDGE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_REPRESENTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PLACE_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_FIRM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TIME_OF_HEARING;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
@RequiredArgsConstructor
public class ConditionalOrderPronouncedTemplateContent implements TemplateContent {

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private DocmosisCommonContent docmosisCommonContent;

    @Value("${final_order.eligible_from_offset_days}")
    private long finalOrderOffsetDays;

    public static final String PRONOUNCEMENT_DATE_PLUS_43 = "pronouncementDatePlus43";

    public Map<String, Object> getTemplateContent(CaseData caseData,
                                                  Long caseId, Applicant applicant) {
        return apply(caseData, caseId, applicant);
    }

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long caseId,
                                     final Applicant applicant) {


        log.info("For ccd case reference {} ", caseId);

        final var applicant1 = caseData.getApplicant1();
        final var applicant2 = caseData.getApplicant2();
        final boolean isDivorce = caseData.getDivorceOrDissolution().isDivorce();
        final ConditionalOrder conditionalOrder = caseData.getConditionalOrder();

        LanguagePreference languagePreference = applicant.getLanguagePreference();
        Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(languagePreference);

        addSolicitorInfo(caseData, applicant, templateContent);
        if (applicant.isRepresented()) {
            templateContent.put(CommonContent.NAME, applicant.getSolicitor().getName());
        } else {
            templateContent.put(CommonContent.NAME, join(" ", applicant.getFirstName(), applicant.getLastName()));
        }
        templateContent.put(CommonContent.ADDRESS, applicant.getCorrespondenceAddressWithoutConfidentialCheck());

        templateContent.put(DATE, LocalDate.now().format(DATE_TIME_FORMATTER));
        templateContent.put(PRONOUNCEMENT_DATE_PLUS_43,
            caseData.getConditionalOrder().getGrantedDate() != null
                ? caseData.getConditionalOrder().getGrantedDate().plusDays(finalOrderOffsetDays).format(DATE_TIME_FORMATTER)
                : null
        );
        final LocalDateTime dateAndTimeOfHearing = conditionalOrder.getDateAndTimeOfHearing();
        final String dateOfHearing = nonNull(dateAndTimeOfHearing) ? dateAndTimeOfHearing.format(DATE_TIME_FORMATTER) : null;
        final String timeOfHearing = nonNull(dateAndTimeOfHearing) ? dateAndTimeOfHearing.format(TIME_FORMATTER) : null;

        templateContent.put(DATE_OF_HEARING, dateOfHearing);
        templateContent.put(TIME_OF_HEARING, timeOfHearing);
        templateContent.put(JUDGE_NAME, conditionalOrder.getPronouncementJudge());
        templateContent.put(COURT_NAME, conditionalOrder.getCourt() != null ? conditionalOrder.getCourt().getLabel() : null);
        templateContent.put(CO_PRONOUNCED_DATE, conditionalOrder.getGrantedDate() != null
            ? conditionalOrder.getGrantedDate().format(DATE_TIME_FORMATTER) : null);
        templateContent.put(DATE_FO_ELIGIBLE_FROM, conditionalOrder.getGrantedDate() != null
            ? conditionalOrder.getGrantedDate().plusDays(finalOrderOffsetDays).format(DATE_TIME_FORMATTER) : null);

        templateContent.put(IS_SOLE, caseData.getApplicationType().isSole());
        templateContent.put(IS_DIVORCE, isDivorce);
        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(DOCUMENTS_ISSUED_ON, conditionalOrder.getDateAndTimeOfHearing() != null
            ? conditionalOrder.getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER) : null);
        templateContent.put(APPLICANT_1_FULL_NAME, applicant1.getFullName());
        templateContent.put(APPLICANT_2_FULL_NAME, applicant2.getFullName());

        templateContent.put(PARTNER, getPartnerInfo(caseData,applicant,languagePreference));

        templateContent.put(PLACE_OF_MARRIAGE, caseData.getApplication().getMarriageDetails().getPlaceOfMarriage());
        templateContent.put(COUNTRY_OF_MARRIAGE, caseData.getApplication().getMarriageDetails().getCountryOfMarriage());
        templateContent.put(MARRIAGE_DATE,
            ofNullable(caseData.getApplication().getMarriageDetails().getDate())
                .map(marriageDate -> marriageDate.format(DATE_TIME_FORMATTER))
                .orElse(null));

        if (WELSH.equals(languagePreference)) {
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, isDivorce ? MARRIAGE_CY : CIVIL_PARTNERSHIP_CY);
        } else {
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, isDivorce ? MARRIAGE : CIVIL_PARTNERSHIP);
        }

        return templateContent;
    }

    private void addSolicitorInfo(CaseData caseData, Applicant applicant, Map<String, Object> templateContent) {
        if (caseData.isJudicialSeparationCase() && applicant.isRepresented()) {
            templateVarsForJSSolicitor(templateContent, caseData, applicant);
        }
    }

    private String getPartnerInfo(CaseData caseData, Applicant applicant, LanguagePreference languagePreference) {
        final var applicant1 = caseData.getApplicant1();
        final var applicant2 = caseData.getApplicant2();

        if (applicant.equals(caseData.getApplicant1())) {
            return commonContent.getPartner(caseData, caseData.getApplicant2(), languagePreference);
        }
        return commonContent.getPartner(caseData, caseData.getApplicant1(), languagePreference);
    }

    void templateVarsForJSSolicitor(Map<String, Object> templateContent,
                                                   final CaseData caseData,
                                                   final Applicant applicant) {

        final Applicant applicant1 = caseData.getApplicant1();
        final Applicant applicant2 = caseData.getApplicant2();
        templateContent.put(SOLICITOR_NAME, applicant.getSolicitor().getName());
        templateContent.put(SOLICITOR_FIRM, applicant.getSolicitor().getFirmName());
        templateContent.put(SOLICITOR_ADDRESS, applicant.getSolicitor().getAddress());
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

    }

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(CONDITIONAL_ORDER_PRONOUNCED_TEMPLATE_ID, JUDICIAL_SEPARATION_ORDER_PRONOUNCED_TEMPLATE_ID,
            CO_PRONOUNCED_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID,
            JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID, CO_GRANTED_COVER_LETTER_TEMPLATE_ID,
            CO_GRANTED_SOL_COVER_LETTER_TEMPLATE_ID, CO_PRONOUNCED_COVER_LETTER_OFFLINE_RESPONDENT_SOL_TEMPLATE_ID);
    }
}
