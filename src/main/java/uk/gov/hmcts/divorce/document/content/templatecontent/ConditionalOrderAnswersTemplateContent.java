package uk.gov.hmcts.divorce.document.content.templatecontent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_ANSWERS_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConditionalOrderAnswersTemplateContent implements TemplateContent {

    public static final String CO_REASON_INFO_NOT_CORRECT = "reasonInformationNotCorrect";
    public static final String CO_CONFIRM_INFO_STILL_CORRECT = "confirmInformationStillCorrect";

    private final Clock clock;

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(CONDITIONAL_ORDER_ANSWERS_TEMPLATE_ID);
    }

    public Map<String, Object> getTemplateContent(final CaseData caseData,
                                                  final Long caseId,
                                                  final Applicant applicant) {

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", caseId, caseData.getDivorceOrDissolution());

        final var isSole = caseData.getApplicationType().isSole();
        final var isDivorce = caseData.getDivorceOrDissolution().isDivorce();

        final var applicant1 = caseData.getApplicant1();
        final var applicant2 = caseData.getApplicant2();

        Map<String, Object> templateContent = new HashMap<>();

        templateContent.put("isSole", isSole);
        templateContent.put("isDivorce", isDivorce);
        templateContent.put(CCD_CASE_REFERENCE, formatId(caseId));
        templateContent.put("documentDate", LocalDate.now(clock).format(DATE_TIME_FORMATTER));

        templateContent.put(APPLICANT_1_FIRST_NAME, applicant1.getFirstName());
        templateContent.put(APPLICANT_1_LAST_NAME, applicant1.getLastName());
        templateContent.put(APPLICANT_1_FULL_NAME, applicant1.getFullName());

        templateContent.put(APPLICANT_2_FIRST_NAME, applicant2.getFirstName());
        templateContent.put(APPLICANT_2_LAST_NAME, applicant2.getLastName());
        templateContent.put(APPLICANT_2_FULL_NAME, applicant2.getFullName());

        mapConditionalOrderDetails(caseData, templateContent);

        return templateContent;
    }

    private void mapConditionalOrderDetails(final CaseData caseData, Map<String, Object> templateContent) {
        ConditionalOrder conditionalOrder = caseData.getConditionalOrder();
        ConditionalOrderQuestions applicant1Questions = conditionalOrder.getConditionalOrderApplicant1Questions();
        ConditionalOrderQuestions applicant2Questions = conditionalOrder.getConditionalOrderApplicant2Questions();

        boolean confirmInfoStillCorrect;

        if (caseData.getApplicationType().isSole()) {
            confirmInfoStillCorrect = YES.equals(applicant1Questions.getConfirmInformationStillCorrect());

            if (!confirmInfoStillCorrect) {
                templateContent.put(CO_REASON_INFO_NOT_CORRECT, applicant1Questions.getReasonInformationNotCorrect());
            }
        } else {
            confirmInfoStillCorrect = YES.equals(applicant1Questions.getConfirmInformationStillCorrect())
                && YES.equals(applicant2Questions.getConfirmInformationStillCorrect());

            if (!confirmInfoStillCorrect) {
                String reason = Stream.of(
                        applicant1Questions.getReasonInformationNotCorrect(),
                        applicant2Questions.getReasonInformationNotCorrect()
                    )
                    .filter(StringUtils::isNotBlank)
                    .collect(joining("\n"));

                templateContent.put(CO_REASON_INFO_NOT_CORRECT, reason);
            }
        }

        templateContent.put(CO_CONFIRM_INFO_STILL_CORRECT, confirmInfoStillCorrect);
    }
}
