package uk.gov.hmcts.divorce.ccd.tab;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

@Component
public class CaseTypeTab implements CCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder.tab("applicationDetails", "Application")
            .field("LabelCreatedDate", null, "${[CREATED_DATE]}")
            .field(CaseData::getDateSubmitted)
            .field(CaseData::getApplicationType)
            .field(CaseData::getDivorceOrDissolution)
            .field("LabelApplicant1-Heading", null, "### Applicant 1")
            .field(CaseData::getApplicant1FirstName)
            .field(CaseData::getApplicant1MiddleName)
            .field(CaseData::getApplicant1LastName)
            .field(CaseData::getInferredApplicant1Gender)
            .field(CaseData::getApplicant1NameDifferentToMarriageCertificate)
            .field(CaseData::getApplicant1NameChangedHow, "applicant1NameDifferentToMarriageCertificate=\"YES\"")
            .field(CaseData::getApplicant1NameChangedHowOtherDetails, "applicant1NameChangedHow=\"other\"")
            .field("LabelApplicant1DetailsAreConfidential-Heading",
                "applicant1ContactDetailsConfidential=\"keep\"",
                "#### Applicant 1's contact details are confidential")
            .field(CaseData::getApplicant1ContactDetailsConfidential)
            .field(CaseData::getApplicant1HomeAddress, "applicant1ContactDetailsConfidential=\"share\"")
            .field(CaseData::getApplicant1PhoneNumber, "applicant1ContactDetailsConfidential=\"share\"")
            .field(CaseData::getDivorceWho)
            .field(CaseData::getApplicant1SolicitorRepresented)
            .field("LabelApplicant1sSolicitor-Heading",
                "applicant1SolicitorRepresented=\"YES\"",
                "#### Applicant 1's Solicitor")
            .field(CaseData::getApplicant1SolicitorName, "applicant1SolicitorRepresented=\"YES\"")
            .field(CaseData::getApplicant1SolicitorPhone, "applicant1SolicitorRepresented=\"YES\"")
            .field(CaseData::getApplicant1SolicitorEmail, "applicant1SolicitorRepresented=\"YES\"")
            .field(CaseData::getApplicant1OrganisationPolicy, "applicant1SolicitorRepresented=\"YES\"")
            .field(CaseData::getSolicitorReference, "applicant1SolicitorRepresented=\"YES\"")
            .field("LabelApplicant2-Heading", null, "### Applicant 2")
            .field(CaseData::getApplicant2FirstName)
            .field(CaseData::getApplicant2MiddleName)
            .field(CaseData::getApplicant2LastName)
            .field(CaseData::getInferredApplicant2Gender)
            .field(CaseData::getApplicant2NameDifferentToMarriageCertificate)
            .field(CaseData::getApplicant2NameChangedHow, "applicant2NameDifferentToMarriageCertificate=\"YES\"")
            .field(CaseData::getApplicant2NameChangedHowOtherDetails, "applicant2NameChangedHow=\"other\"")
            .field(CaseData::getApplicant2SolicitorRepresented)
            .field("LabelApplicant2sSolicitor-Heading",
                "applicant2SolicitorRepresented=\"YES\"",
                "#### Applicant 2's Solicitor")
            .field(CaseData::getApplicant2SolicitorName, "applicant2SolicitorRepresented=\"YES\"")
            .field(CaseData::getApplicant2SolicitorPhone, "applicant2SolicitorRepresented=\"YES\"")
            .field(CaseData::getApplicant2SolicitorEmail, "applicant2SolicitorRepresented=\"YES\"")
            .field(CaseData::getApplicant2OrganisationPolicy, "applicant2SolicitorRepresented=\"YES\"")
            .field(CaseData::getApplicant2SolicitorReference, "applicant2SolicitorRepresented=\"YES\"")
            .field(CaseData::getApplicant2CorrespondenceAddress)
            .field(CaseData::getLegalConnections)
            .field("LabelMarriage-Heading", null, "### Marriage and certificate")
            .field("marriageDate")
            .field("marriageIsSameSexCouple")
            .field("marriageMarriedInUk")
            .field("marriagePlaceOfMarriage", "marriageMarriedInUk=\"NO\"")
            .field("marriageCountryOfMarriage", "marriageMarriedInUk=\"NO\"")
            .field("marriageCertificateInEnglish")
            .field("marriageCertifiedTranslation", "marriageCertificateInEnglish=\"NO\"")
            .field(CaseData::getMarriageApplicant1Name)
            .field(CaseData::getMarriageApplicant2Name)
            .field("LabelClaimCosts-Heading", null, "### Claim costs")
            .field(CaseData::getDivorceCostsClaim)
            .field(CaseData::getDivorceClaimFrom, "divorceCostsClaim=\"YES\"")
            .field("LabelOtherLegalProceedings-Heading", null, "### Other legal proceedings")
            .field(CaseData::getLegalProceedings)
            .field(CaseData::getLegalProceedingsRelated, "legalProceedings=\"YES\"")
            .field(CaseData::getLegalProceedingsDetails, "legalProceedings=\"YES\"")
            .field("LabelFinancialOrder-Heading", null, "### Financial order")
            .field(CaseData::getFinancialOrder)
            .field(CaseData::getFinancialOrderFor, "financialOrder=\"YES\"");

        configBuilder.tab("paymentDetailsCourtAdmin", "Payment")
            .field(CaseData::getHelpWithFeesReferenceNumber);

        configBuilder.tab("languageDetails", "Language")
            .field(CaseData::getLanguagePreferenceWelsh);

        configBuilder.tab("documents", "Documents")
            .field(CaseData::getDocumentsGenerated)
            .field(CaseData::getDocumentsUploaded);
    }
}
