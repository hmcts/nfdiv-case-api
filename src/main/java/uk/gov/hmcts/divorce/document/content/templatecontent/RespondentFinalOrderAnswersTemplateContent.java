package uk.gov.hmcts.divorce.document.content.templatecontent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_FINAL_ORDER_ANSWERS_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_DIVORCE;

import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@RequiredArgsConstructor
@Slf4j
public class RespondentFinalOrderAnswersTemplateContent implements TemplateContent {

    private final Clock clock;

    private final DocmosisCommonContent docmosisCommonContent;

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(RESPONDENT_FINAL_ORDER_ANSWERS_TEMPLATE_ID);
    }

    public Map<String, Object> getTemplateContent(final CaseData caseData,
                                                  final Long caseId,
                                                  final Applicant applicant) {

        log.info("For ccd case reference {}", caseId);

        final var isDivorce = caseData.getDivorceOrDissolution().isDivorce();

        final var applicant1 = caseData.getApplicant1();
        final var applicant2 = caseData.getApplicant2();

        DateTimeFormatter dateTimeFormatter =
                getDateTimeFormatterForPreferredLanguage(applicant2.getLanguagePreference());

        Map<String, Object> templateContent = docmosisCommonContent
            .getBasicDocmosisTemplateContent(applicant2.getLanguagePreference());

        templateContent.put(IS_DIVORCE, isDivorce);
        templateContent.put(CCD_CASE_REFERENCE, formatId(caseId));
        templateContent.put("documentDate", dateTimeFormatter.format(LocalDate.now(clock)));

        templateContent.put(APPLICANT_1_FULL_NAME, applicant1.getFullName());
        templateContent.put(APPLICANT_2_FULL_NAME, applicant2.getFullName());
        templateContent.put(CCD_CASE_REFERENCE, formatId(caseId));

        FinalOrder finalOrder = caseData.getFinalOrder();

        templateContent.put("reason", applicant2.isRepresented()
            ? finalOrder.getApplicant2SolFinalOrderWhyNeedToApply()
            : finalOrder.getApplicant2FinalOrderExplanation());

        return templateContent;
    }
}
