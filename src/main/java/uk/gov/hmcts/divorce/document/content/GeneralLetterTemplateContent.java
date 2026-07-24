package uk.gov.hmcts.divorce.document.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetter;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.document.GeneralLetterRecipientResolver;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FEEDBACK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_JOINT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATION;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeneralLetterTemplateContent {

    private final DocmosisCommonContent docmosisCommonContent;

    private final CommonContent commonContent;

    private final GeneralLetterRecipientResolver generalLetterRecipientResolver;

    private final Clock clock;

    public Map<String, Object> apply(final CaseData caseData, final Long ccdCaseReference, LanguagePreference languagePreference) {

        Map<String, Object> templateContent = docmosisCommonContent
                .getBasicDocmosisTemplateContent(languagePreference);

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

        var generalLetter = caseData.getGeneralLetter();

        mapRecipientDetails(templateContent, caseData, generalLetter);

        templateContent.put(FEEDBACK, generalLetter.getGeneralLetterDetails());
        templateContent.put(ISSUE_DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(CASE_REFERENCE, formatId(ccdCaseReference));
        templateContent.put(IS_JOINT, !caseData.getApplicationType().isSole());

        return templateContent;
    }

    private void mapRecipientDetails(final Map<String, Object> templateContent,
                                     final CaseData caseData,
                                     final GeneralLetter generalLetter) {
        var recipient = generalLetterRecipientResolver.resolve(caseData, generalLetter.getGeneralLetterParties());

        templateContent.put(RECIPIENT_NAME, recipient.recipientName());
        templateContent.put(RECIPIENT_ADDRESS, recipient.recipientAddress());
        templateContent.put(RELATION, commonContent.getPartner(caseData, caseData.getApplicant2()));
    }
}
