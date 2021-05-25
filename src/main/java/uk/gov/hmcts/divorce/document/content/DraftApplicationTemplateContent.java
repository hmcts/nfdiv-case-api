package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.FinancialOrderFor;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationsResponse;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.util.CollectionUtils.firstElement;
import static org.springframework.util.ObjectUtils.isEmpty;
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
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CHILDREN_OF_THE_APPLICANT_1_AND_APPLICANT_2;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONDITIONAL_ORDER_OF_DIVORCE_FROM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COSTS_RELATED_TO_ENDING_THE_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURT_CASE_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DISSOLUTION_OF_THE_CIVIL_PARTNERSHIP_WITH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_COSTS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION_COST;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FINANCIAL_ORDER_CHILD;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FINANCIAL_ORDER_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FOR_A_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_COST_ORDERS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDERS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDERS_FOR_CHILD;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_RELATIONSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.OF_THE_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATIONSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TO_END_A_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TO_END_THE_CIVIL_PARTNERSHIP;

@Component
@Slf4j
public class DraftApplicationTemplateContent {

    @Autowired
    private OrganisationClient organisationClient;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    public Map<String, Object> apply(
        CaseData caseData,
        Long ccdCaseReference,
        LocalDate createdDate,
        String userAuth
    ) {
        Map<String, Object> templateData = new HashMap<>();

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateData.put(DIVORCE_OR_DISSOLUTION, FOR_A_DIVORCE);
            templateData.put(MARRIAGE_OR_RELATIONSHIP, MARRIAGE);
            templateData.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
            templateData.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, OF_THE_DIVORCE);
            templateData.put(FINANCIAL_ORDER_OR_DISSOLUTION, CONDITIONAL_ORDER_OF_DIVORCE_FROM);
            templateData.put(DIVORCE_OR_DISSOLUTION_COST, DIVORCE_COSTS);

        } else {
            templateData.put(DIVORCE_OR_DISSOLUTION, TO_END_A_CIVIL_PARTNERSHIP);
            templateData.put(MARRIAGE_OR_RELATIONSHIP, RELATIONSHIP);
            templateData.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
            templateData.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, TO_END_THE_CIVIL_PARTNERSHIP);
            templateData.put(FINANCIAL_ORDER_OR_DISSOLUTION, DISSOLUTION_OF_THE_CIVIL_PARTNERSHIP_WITH);
            templateData.put(DIVORCE_OR_DISSOLUTION_COST, COSTS_RELATED_TO_ENDING_THE_CIVIL_PARTNERSHIP);
        }

        templateData.put(CCD_CASE_REFERENCE, ccdCaseReference);
        templateData.put(ISSUE_DATE, createdDate.toString());

        templateData.put(APPLICANT_1_FIRST_NAME, caseData.getApplicant1FirstName());
        templateData.put(APPLICANT_1_MIDDLE_NAME, caseData.getApplicant1MiddleName());
        templateData.put(APPLICANT_1_LAST_NAME, caseData.getApplicant1LastName());

        templateData.put(APPLICANT_2_FIRST_NAME, caseData.getApplicant2FirstName());
        templateData.put(APPLICANT_2_MIDDLE_NAME, caseData.getApplicant2MiddleName());
        templateData.put(APPLICANT_2_LAST_NAME, caseData.getApplicant2LastName());

        templateData.put(APPLICANT_1_FULL_NAME, caseData.getMarriageApplicant1Name());
        templateData.put(APPLICANT_2_FULL_NAME, caseData.getMarriageApplicant2Name());

        templateData.put(MARRIAGE_DATE, caseData.getMarriageDetails().getDate());
        templateData.put(COURT_CASE_DETAILS, caseData.getLegalProceedingsOther());

        templateData.put(HAS_COST_ORDERS, caseData.getDivorceCostsClaim().toBoolean());
        templateData.put(HAS_FINANCIAL_ORDERS, caseData.getFinancialOrder().toBoolean());

        boolean hasFinancialOrdersForChild =
            null != caseData.getFinancialOrderFor()
                && caseData.getFinancialOrderFor().contains(FinancialOrderFor.CHILDREN);

        templateData.put(HAS_FINANCIAL_ORDERS_FOR_CHILD, hasFinancialOrdersForChild);
        templateData.put(FINANCIAL_ORDER_CHILD, CHILDREN_OF_THE_APPLICANT_1_AND_APPLICANT_2);

        String applicant2PostalAddress;
        AddressGlobalUK applicant2HomeAddress = caseData.getApplicant2HomeAddress();

        if (applicant2HomeAddress == null) {
            applicant2PostalAddress = populateApplicant2PostAddress(caseData, ccdCaseReference, userAuth);
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

        return templateData;
    }

    private String populateApplicant2PostAddress(CaseData caseData, Long ccdCaseReference, String userAuth) {
        OrganisationsResponse organisationsResponse = organisationClient.getUserOrganisationByEmail(
            userAuth,
            authTokenGenerator.generate(),
            caseData.getApplicant2SolicitorEmail()
        );

        if (isEmpty(organisationsResponse.getContactInformation())) {
            log.info("Cannot retrieve applicant 2 solicitor organisation details from Prd Api for case id {}",
                ccdCaseReference
            );

            return null;
        }

        log.info("Solicitor organisation {} retrieved from Prd Api for case id {} for applicant 2 solicitor ",
            organisationsResponse.getOrganisationIdentifier(),
            ccdCaseReference
        );

        var orgContactInformation = firstElement(organisationsResponse.getContactInformation());

        assert orgContactInformation != null;

        return Stream.of(
            orgContactInformation.getAddressLine1(),
            orgContactInformation.getAddressLine2(),
            orgContactInformation.getAddressLine3(),
            orgContactInformation.getTownCity(),
            orgContactInformation.getCounty(),
            orgContactInformation.getPostCode(),
            orgContactInformation.getCountry()
        )
            .filter(value -> value != null && !value.isEmpty())
            .collect(Collectors.joining("\n"));
    }
}
