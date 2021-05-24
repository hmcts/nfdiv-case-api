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
            .field("LabelCreatedDate", null, "[CREATED_DATE]")  // format to use #DATETIMEDISPLAY(d  MMMM yyyy)
            .field(CaseData::getDateSubmitted) // format to use #DATETIMEDISPLAY(d  MMMM yyyy)
            .field(CaseData::getApplicationType)
            .field(CaseData::getDivorceOrDissolution)

            .field("LabelApplicant1-Heading", null, "### Applicant 1")
            .field(CaseData::getApplicant1FirstName)
            .field(CaseData::getApplicant1MiddleName)
            .field(CaseData::getApplicant1LastName)
            .field(CaseData::getInferredApplicant1Gender)
            .field(CaseData::getApplicant1NameDifferentToMarriageCertificate)
            .field(CaseData::getApplicant1NameChangedHow, "applicant1NameDifferentToMarriageCertificate=\"Yes\"")
            .field(CaseData::getApplicant1NameChangedHowOtherDetails, "applicant1NameChangedHow=\"other\"")

            .field("LabelApplicant1DetailsAreConfidential-Heading",
                "applicant1ContactDetailsConfidential=\"keep\"",
                "#### Applicant 1's contact details are confidential")
            .field(CaseData::getApplicant1ContactDetailsConfidential)
            .field(CaseData::getApplicant1HomeAddress, "applicant1ContactDetailsConfidential=\"share\"")
            .field(CaseData::getApplicant1PhoneNumber, "applicant1ContactDetailsConfidential=\"share\"")
            .field(CaseData::getDivorceWho)

            .field("LabelApplicant1sSolicitor-Heading",
                "applicant1SolicitorRepresented=\"Yes\"",
                "#### Applicant 1's Solicitor")
            .field(CaseData::getApplicant1SolicitorName)
            .field(CaseData::getApplicant1SolicitorPhone)
            .field(CaseData::getApplicant1SolicitorEmail)
            .field(CaseData::getApplicant1OrganisationPolicy)
            .field(CaseData::getSolicitorReference)

            .field("LabelApplicant2-Heading", null, "### Applicant 2")
            .field(CaseData::getApplicant2FirstName)
            .field(CaseData::getApplicant2MiddleName)
            .field(CaseData::getApplicant2LastName)
            .field(CaseData::getInferredApplicant2Gender)
            .field(CaseData::getApplicant2NameDifferentToMarriageCertificate)
            .field(CaseData::getApplicant2NameChangedHow, "applicant2NameDifferentToMarriageCertificate=\"Yes\"")
            .field(CaseData::getApplicant2NameChangedHowOtherDetails, "applicant2NameChangedHow=\"other\"")
            .field(CaseData::getApplicant2SolicitorRepresented)

            .field("LabelApplicant2sSolicitor-Heading",
                "applicant2SolicitorRepresented=\"Yes\"",
                "#### Applicant 2's Solicitor")
            .field(CaseData::getApplicant2SolicitorName, "applicant2SolicitorRepresented=\"Yes\"")
            .field(CaseData::getApplicant2SolicitorPhone, "applicant2SolicitorRepresented=\"Yes\"")
            .field(CaseData::getApplicant2SolicitorEmail, "applicant2SolicitorRepresented=\"Yes\"")
            .field(CaseData::getApplicant2OrganisationPolicy, "applicant2SolicitorRepresented=\"Yes\"")
            .field(CaseData::getApplicant2SolicitorReference, "applicant2SolicitorRepresented=\"Yes\"")
            .field(CaseData::getApplicant2CorrespondenceAddress)
            .field(CaseData::getLegalConnections)

            .label("LabelMarriage-Heading", null, "Marriage and certificate")
            .field("marriageDate") // #DATETIMEDISPLAY(d  MMMM yyyy)
            .field("marriageIsSameSexCouple")
            .field("marriageMarriedInUk")
            .field("marriagePlaceOfMarriage", "marriedInUk=\"No\"")
            .field("marriageCountryOfMarriage", "marriedInUk=\"No\"")
            .field("marriageCertificateInEnglish")
            .field("marriageCertifiedTranslation")
            .field(CaseData::getMarriageApplicant1Name)
            .field(CaseData::getMarriageApplicant2Name)

            .label("LabelClaimCosts-Heading", null, "Claim costs")
            .field(CaseData::getDivorceCostsClaim)
            .field(CaseData::getDivorceClaimFrom)

            .label("LabelOtherLegalProceedings-Heading", null, "Other legal proceedings")
            .field(CaseData::getLegalProceedings)
            .field(CaseData::getLegalProceedingsRelated)
            .field(CaseData::getLegalProceedingsDetails)
            .field(CaseData::getFinancialOrder)
            .field(CaseData::getFinancialOrderFor);

        configBuilder.tab("paymentDetailsCourtAdmin", "Payment")
            .field(CaseData::getHelpWithFeesReferenceNumber);

        configBuilder.tab("languageDetails", "Language")
            .field(CaseData::getLanguagePreferenceWelsh);

        configBuilder.tab("documents", "Documents")
            .field(CaseData::getDocumentsGenerated)
            .field(CaseData::getDocumentsUploaded);
    }
}
