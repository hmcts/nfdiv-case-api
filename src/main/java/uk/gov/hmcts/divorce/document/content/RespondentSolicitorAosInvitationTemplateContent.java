package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.common.model.CaseData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
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

@Component
@Slf4j
public class RespondentSolicitorAosInvitationTemplateContent {

    private static final DateTimeFormatter TEMPLATE_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy");

    public Supplier<Map<String, Object>> apply(final CaseData caseData,
                                               final Long ccdCaseReference,
                                               final LocalDate createdDate) {

        return () -> {
            Map<String, Object> templateData = new HashMap<>();

            log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

            templateData.put(CCD_CASE_REFERENCE, ccdCaseReference);
            templateData.put(ISSUE_DATE, createdDate.format(TEMPLATE_DATE_FORMAT));

            templateData.put(APPLICANT_1_FIRST_NAME, caseData.getApplicant1().getFirstName());
            templateData.put(APPLICANT_1_MIDDLE_NAME, caseData.getApplicant1().getMiddleName());
            templateData.put(APPLICANT_1_LAST_NAME, caseData.getApplicant1().getLastName());

            templateData.put(APPLICANT_2_FIRST_NAME, caseData.getApplicant2().getFirstName());
            templateData.put(APPLICANT_2_MIDDLE_NAME, caseData.getApplicant2().getMiddleName());
            templateData.put(APPLICANT_2_LAST_NAME, caseData.getApplicant2().getLastName());

            templateData.put(APPLICANT_1_FULL_NAME, caseData.getMarriageDetails().getApplicant1Name());
            templateData.put(APPLICANT_2_FULL_NAME, caseData.getMarriageDetails().getApplicant2Name());

            templateData.put(MARRIAGE_DATE,
                ofNullable(caseData.getMarriageDetails().getDate())
                    .map(marriageDate -> marriageDate.format(TEMPLATE_DATE_FORMAT))
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
            templateData.put(APPLICANT_2_POSTAL_ADDRESS, applicant2PostalAddress);

            templateData.put(ACCESS_CODE, caseData.getAccessCode());
            return templateData;
        };
    }
}
