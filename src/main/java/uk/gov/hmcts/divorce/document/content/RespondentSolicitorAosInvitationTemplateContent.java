package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ACCESS_CODE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class RespondentSolicitorAosInvitationTemplateContent {

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference,
                                     final LocalDate createdDate) {

        Map<String, Object> templateContent = new HashMap<>();

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

        templateContent.put(CCD_CASE_REFERENCE, ccdCaseReference);
        templateContent.put(ISSUE_DATE, createdDate.format(DATE_TIME_FORMATTER));

        templateContent.put(APPLICANT_1_FIRST_NAME, caseData.getApplicant1().getFirstName());
        templateContent.put(APPLICANT_1_MIDDLE_NAME, caseData.getApplicant1().getMiddleName());
        templateContent.put(APPLICANT_1_LAST_NAME, caseData.getApplicant1().getLastName());

        templateContent.put(APPLICANT_2_FIRST_NAME, caseData.getApplicant2().getFirstName());
        templateContent.put(APPLICANT_2_MIDDLE_NAME, caseData.getApplicant2().getMiddleName());
        templateContent.put(APPLICANT_2_LAST_NAME, caseData.getApplicant2().getLastName());

        templateContent.put(APPLICANT_1_FULL_NAME, caseData.getApplication().getMarriageDetails().getApplicant1Name());
        templateContent.put(APPLICANT_2_FULL_NAME, caseData.getApplication().getMarriageDetails().getApplicant2Name());

        templateContent.put(MARRIAGE_DATE,
            ofNullable(caseData.getApplication().getMarriageDetails().getDate())
                .map(marriageDate -> marriageDate.format(DATE_TIME_FORMATTER))
                .orElse(null));


        String applicant2PostalAddress;
        AddressGlobalUK applicant2HomeAddress = caseData.getApplicant2().getHomeAddress();

        if (applicant2HomeAddress == null) {
            applicant2PostalAddress = caseData.getApplicant2().getSolicitor().getAddress();
        } else {
            applicant2PostalAddress =
                Stream.of(
                        applicant2HomeAddress.getAddressLine1(),
                        applicant2HomeAddress.getAddressLine2(),
                        applicant2HomeAddress.getAddressLine3(),
                        applicant2HomeAddress.getPostTown(),
                        applicant2HomeAddress.getCounty(),
                        applicant2HomeAddress.getPostCode(),
                        applicant2HomeAddress.getCountry()
                    )
                    .filter(value -> value != null && !value.isEmpty())
                    .collect(Collectors.joining("\n"));
        }
        templateContent.put(APPLICANT_2_POSTAL_ADDRESS, applicant2PostalAddress);

        templateContent.put(ACCESS_CODE, caseData.getCaseInvite().getAccessCode());
        return templateContent;
    }
}
