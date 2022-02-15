package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Jurisdiction;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.PaperFormDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;

public class CorrectPaperCase implements CcdPageConfiguration {

    @Override
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("Correct paper case")
            .pageLabel("Correct paper case")
            .label("Label-CorrectYourApplication", "Your applications details")
            .mandatory(CaseData::getDivorceOrDissolution)
            .mandatory(CaseData::getApplicationType)
            .label("Label-CorrectApplicant1Details", "${labelContentApplicantsOrApplicant1s} details")
            .complex(CaseData::getApplicant1)
                .mandatory(Applicant::getFirstName)
                .mandatory(Applicant::getMiddleName)
                .mandatory(Applicant::getLastName)
                .mandatory(Applicant::getNameDifferentToMarriageCertificate)
                .mandatory(Applicant::getNameChangedHowOtherDetails)
                .mandatory(Applicant::getContactDetailsType)
                .mandatory(Applicant::getHomeAddress)
                .mandatory(Applicant::getPhoneNumber)
                .mandatory(Applicant::getEmail)
                .mandatory(Applicant::getSolicitorRepresented)
                .label("Label-CorrectApplicant1SolDetails",
                    "${labelContentApplicantsOrApplicant1s} solicitor details")
                .complex(Applicant::getSolicitor)
                .mandatory(Solicitor::getName)
                .mandatory(Solicitor::getReference)
                .mandatory(Solicitor::getFirmName)
                .mandatory(Solicitor::getAddress)
                .mandatory(Solicitor::getPhone)
                .mandatory(Solicitor::getEmail)
                .done()
                .label("Label-CorrectApplicant1FODetails",
                    "${labelContentApplicantsOrApplicant1s} financial order details")
                .mandatory(Applicant::getFinancialOrder)
                .mandatory(Applicant::getFinancialOrdersFor)
                .label("Label-CorrectApplicant1LegalProceedingsDetails",
                    "${labelContentApplicantsOrApplicant1s} legal proceedings details")
                .mandatory(Applicant::getLegalProceedings)
                .mandatory(Applicant::getLegalProceedingsDetails)
            .done()
            .label("Label-CorrectApplicant1SOTDetails",
                "${labelContentApplicantsOrApplicant1s} statement of truth details")
            .complex(CaseData::getApplication)
                .mandatory(Application::getApplicant1StatementOfTruth)
                .mandatory(Application::getSolSignStatementOfTruth)
            .done()
            .label("Label-CorrectApplicant2Details", "${labelContentRespondentsOrApplicant2s} details")
            .complex(CaseData::getApplicant2)
                .mandatory(Applicant::getFirstName)
                .mandatory(Applicant::getMiddleName)
                .mandatory(Applicant::getLastName)
                .mandatory(Applicant::getNameDifferentToMarriageCertificate)
                .mandatory(Applicant::getNameChangedHowOtherDetails)
                .mandatory(Applicant::getSolicitorRepresented)
                .mandatory(Applicant::getHomeAddress)
                .mandatory(Applicant::getPhoneNumber)
                .mandatory(Applicant::getEmail)
                .label("Label-CorrectApplicant2SolDetails",
                    "${labelContentRespondentsOrApplicant2s} solicitor details")
                .complex(Applicant::getSolicitor)
                .mandatory(Solicitor::getName)
                .mandatory(Solicitor::getReference)
                .mandatory(Solicitor::getFirmName)
                .mandatory(Solicitor::getAddress)
                .mandatory(Solicitor::getPhone)
                .done()
                .label("Label-CorrectApplicant2FODetails",
                    "${labelContentRespondentsOrApplicant2s} financial order details")
                .mandatory(Applicant::getFinancialOrder)
                .mandatory(Applicant::getFinancialOrdersFor)
            .done()
            .label("Label-CorrectApplicant2SOTDetails",
                "${labelContentRespondentsOrApplicant2s} statement of truth details")
            .complex(CaseData::getApplication)
                .mandatory(Application::getApplicant2StatementOfTruth)
                .mandatory(Application::getApplicant2SolSignStatementOfTruth)
                .mandatory(Application::getApplicant2SolStatementOfReconciliationName)
                .mandatory(Application::getApplicant2SolStatementOfReconciliationFirm)
                .label("Label-CorrectJurisdictionDetails", "Jurisdiction connection details")
                .complex(Application::getJurisdiction)
                    .mandatory(Jurisdiction::getConnections)
                .done()
                .label("Label-CorrectPrayerDetails", "Prayer details")
                .readonly(Application::getDateSubmitted)
                .mandatory(Application::getApplicant1ScreenHasMarriageBroken)
                .mandatory(Application::getApplicant2ScreenHasMarriageBroken)
                .mandatory(Application::getApplicant1PrayerHasBeenGivenCheckbox)
                .mandatory(Application::getApplicant2PrayerHasBeenGivenCheckbox)
                .mandatory(Application::getApplicant1HelpWithFees)
                .mandatory(Application::getApplicant2HelpWithFees)
                .mandatory(Application::getScreenHasMarriageCert)
                .label("Label-CorrectMarriageDetails", "Marriage details")
                .complex(Application::getMarriageDetails)
                    .mandatory(MarriageDetails::getCertificateInEnglish)
                    .mandatory(MarriageDetails::getCertifiedTranslation,
                        "marriageCertificateInEnglish=\"No\"")
                    .mandatory(MarriageDetails::getMarriedInUk)
                    .mandatory(MarriageDetails::getIssueApplicationWithoutMarriageCertificate)
                    .mandatory(MarriageDetails::getDate)
                    .mandatory(MarriageDetails::getApplicant1Name)
                    .mandatory(MarriageDetails::getApplicant2Name)
                    .mandatory(MarriageDetails::getFormationType)
                    .mandatory(MarriageDetails::getPlaceOfMarriage)
                    .mandatory(MarriageDetails::getCertifyMarriageCertificateIsCorrect)
                    .mandatory(MarriageDetails::getMarriageCertificateIsIncorrectDetails,
                        "marriageCertifyMarriageCertificateIsCorrect=\"No\"")
                .done()
            .done()
            .label("Label-CorrectPaperFormDetails", "Paper form details")
            .complex(CaseData::getPaperFormDetails)
                .mandatory(PaperFormDetails::getServiceOutsideUK)
                .mandatory(PaperFormDetails::getApplicantWillServeApplication)
                .mandatory(PaperFormDetails::getRespondentDifferentServiceAddress)
                .mandatory(PaperFormDetails::getSummaryApplicant1FinancialOrdersFor)
                .mandatory(PaperFormDetails::getSummaryApplicant2FinancialOrdersFor)
                .mandatory(PaperFormDetails::getApplicant1SigningSOT)
                .mandatory(PaperFormDetails::getApplicant1LegalRepSigningSOT)
                .mandatory(PaperFormDetails::getApplicant1LegalRepPosition)
                .mandatory(PaperFormDetails::getApplicant1SOTSignedOn)
                .mandatory(PaperFormDetails::getApplicant2SigningSOT)
                .mandatory(PaperFormDetails::getApplicant2LegalRepSigningSOT)
                .mandatory(PaperFormDetails::getApplicant2LegalRepPosition)
                .mandatory(PaperFormDetails::getApplicant2SOTSignedOn)
                .mandatory(PaperFormDetails::getFeeInPounds)
                .mandatory(PaperFormDetails::getApplicant1NoPaymentIncluded)
                .mandatory(PaperFormDetails::getApplicant2NoPaymentIncluded)
                .mandatory(PaperFormDetails::getSoleOrApplicant1PaymentOther)
                .mandatory(PaperFormDetails::getApplicant2PaymentOther)
            .done();
    }
}
