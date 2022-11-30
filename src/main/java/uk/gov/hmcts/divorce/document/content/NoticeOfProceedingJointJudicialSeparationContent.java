package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.HAS_CASE_BEEN_REISSUED;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.REISSUE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class NoticeOfProceedingJointJudicialSeparationContent {

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private DocmosisCommonContent docmosisCommonContent;

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference,
                                     Applicant applicant,
                                     Applicant partner) {

        final LanguagePreference applicantLanguagePreference = applicant.getLanguagePreference();
        final Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(applicantLanguagePreference);

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

        templateContent.put(FIRST_NAME, applicant.getFirstName());
        templateContent.put(LAST_NAME, applicant.getLastName());
        templateContent.put(ADDRESS, applicant.getPostalAddress());
        templateContent.put(CASE_REFERENCE, formatId(ccdCaseReference));

        templateContent.put(ISSUE_DATE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));
        templateContent.put(PARTNER, commonContent.getPartner(caseData, partner, applicant.getLanguagePreference()));
        templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP,
            caseData.getDivorceOrDissolution().isDivorce() ? MARRIAGE : CIVIL_PARTNERSHIP);

        if (nonNull(caseData.getApplication().getReissueDate())) {
            templateContent.put(HAS_CASE_BEEN_REISSUED, true);
            templateContent.put(REISSUE_DATE, caseData.getApplication().getReissueDate().format(DATE_TIME_FORMATTER));
        }

        return templateContent;
    }
}
