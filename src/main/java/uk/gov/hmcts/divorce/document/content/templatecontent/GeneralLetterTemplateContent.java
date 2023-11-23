package uk.gov.hmcts.divorce.document.content.templatecontent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetter;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;
import static uk.gov.hmcts.divorce.divorcecase.util.AddressUtil.getPostalAddress;
import static uk.gov.hmcts.divorce.document.DocumentConstants.GENERAL_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FEEDBACK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HUSBAND_OR_WIFE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_JOINT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NAME_FORMAT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATION;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeneralLetterTemplateContent implements TemplateContent{

    private final CommonContent commonContent;
    private final DocmosisCommonContent docmosisCommonContent;
    private final Clock clock;


    @Override
    public List<String> getSupportedTemplates() {
        return List.of(GENERAL_LETTER_TEMPLATE_ID);
    }

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {
        Map<String, Object> templateContent = docmosisCommonContent
                .getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", caseId, caseData.getDivorceOrDissolution());

        var generalLetter = caseData.getGeneralLetter();

        mapRecipientDetails(templateContent, generalLetter, caseData);

        templateContent.put(FEEDBACK, generalLetter.getGeneralLetterDetails());
        templateContent.put(ISSUE_DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(IS_JOINT, !caseData.getApplicationType().isSole());

        return templateContent;
    }

    private void mapRecipientDetails(final Map<String, Object> templateContent,
                                     final GeneralLetter generalLetter,
                                     final CaseData caseData) {
        switch (generalLetter.getGeneralLetterParties()) {
            case APPLICANT -> {
                templateContent.put(RECIPIENT_NAME, getRecipientName(caseData.getApplicant1()));
                templateContent.put(RECIPIENT_ADDRESS, caseData.getApplicant1().getPostalAddress());
                templateContent.put(RELATION, commonContent.getPartner(caseData, caseData.getApplicant2()));
            }
            case RESPONDENT -> {
                templateContent.put(RECIPIENT_NAME, getRecipientName(caseData.getApplicant2()));
                templateContent.put(RECIPIENT_ADDRESS, caseData.getApplicant2().getPostalAddress());
                templateContent.put(RELATION, commonContent.getPartner(caseData, caseData.getApplicant2()));
            }
            default -> {
                templateContent.put(RECIPIENT_NAME, generalLetter.getOtherRecipientName());
                templateContent.put(RECIPIENT_ADDRESS, getPostalAddress(generalLetter.getOtherRecipientAddress()));
                templateContent.put(RELATION, caseData.isDivorce() ? HUSBAND_OR_WIFE : CIVIL_PARTNER);
            }
        }
    }

    private String getRecipientName(final Applicant applicant) {
        return String.format(NAME_FORMAT, applicant.getFirstName(), applicant.getLastName());
    }
}
