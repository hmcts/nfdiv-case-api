package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.Jurisdiction;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.SOT_REQUIRED;

public class CorrectPaperCase implements CcdPageConfiguration {

    @Override
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("Correct paper case", this::midEvent)
            .pageLabel("Correct paper case")
            .label("Label-CorrectYourApplication", "### Your application details")
            .mandatory(CaseData::getDivorceOrDissolution)
            .mandatory(CaseData::getApplicationType)
            .label("Label-CorrectApplicant1Details",
                "### ${labelContentApplicantsOrApplicant1s} details")
            .complex(CaseData::getApplicant1)
                .mandatory(Applicant::getFirstName)
                .optional(Applicant::getMiddleName)
                .mandatory(Applicant::getLastName)
                .mandatory(Applicant::getNameDifferentToMarriageCertificate)
                .mandatory(Applicant::getNameChangedHowOtherDetails,
                    "applicant1NameDifferentToMarriageCertificate=\"Yes\"")
                .mandatory(Applicant::getContactDetailsType)
                .mandatory(Applicant::getHomeAddress)
                .mandatory(Applicant::getPhoneNumber)
                .mandatory(Applicant::getEmail)
                .mandatory(Applicant::getSolicitorRepresented)
                .label("Label-CorrectApplicant1SolDetails",
                    "### ${labelContentApplicantsOrApplicant1s} solicitor details")
                .complex(Applicant::getSolicitor)
                    .mandatory(Solicitor::getName, "applicant1SolicitorRepresented=\"Yes\"")
                    .mandatory(Solicitor::getReference, "applicant1SolicitorRepresented=\"Yes\"")
                    .mandatory(Solicitor::getFirmName, "applicant1SolicitorRepresented=\"Yes\"")
                    .mandatory(Solicitor::getAddress, "applicant1SolicitorRepresented=\"Yes\"")
                    .mandatory(Solicitor::getPhone, "applicant1SolicitorRepresented=\"Yes\"")
                    .mandatory(Solicitor::getEmail, "applicant1SolicitorRepresented=\"Yes\"")
                .done()
                .label("Label-CorrectApplicant1FODetails",
                    "### ${labelContentApplicantsOrApplicant1s} financial order details")
                .mandatory(Applicant::getFinancialOrder)
                .mandatory(Applicant::getFinancialOrdersFor, "applicant1FinancialOrder=\"Yes\"")
                .label("Label-CorrectApplicant1LegalProceedingsDetails",
                    "### ${labelContentApplicantsOrApplicant1s} legal proceedings details")
                .mandatory(Applicant::getLegalProceedings)
                .mandatory(Applicant::getLegalProceedingsDetails, "applicant1LegalProceedings=\"Yes\"")
            .done()
            .label("Label-CorrectApplicant1SOTDetails",
                "### ${labelContentApplicantsOrApplicant1s} statement of truth details")
            .complex(CaseData::getApplication)
                .mandatory(Application::getApplicant1StatementOfTruth)
                .mandatory(Application::getSolSignStatementOfTruth)
            .done()
            .label("Label-CorrectApplicant2Details",
                "### ${labelContentRespondentsOrApplicant2s} details")
            .complex(CaseData::getApplicant2)
                .mandatory(Applicant::getFirstName)
                .optional(Applicant::getMiddleName)
                .mandatory(Applicant::getLastName)
                .mandatory(Applicant::getNameDifferentToMarriageCertificate)
                .mandatory(Applicant::getNameChangedHowOtherDetails,
                    "applicant2NameDifferentToMarriageCertificate=\"Yes\"")
                .mandatory(Applicant::getSolicitorRepresented)
                .mandatory(Applicant::getHomeAddress)
                .mandatory(Applicant::getPhoneNumber)
                .mandatory(Applicant::getEmail)
                .label("Label-CorrectApplicant2SolDetails",
                    "### ${labelContentRespondentsOrApplicant2s} solicitor details")
                .complex(Applicant::getSolicitor)
                    .mandatory(Solicitor::getName, "applicant2SolicitorRepresented=\"Yes\"")
                    .mandatory(Solicitor::getReference, "applicant2SolicitorRepresented=\"Yes\"")
                    .mandatory(Solicitor::getFirmName, "applicant2SolicitorRepresented=\"Yes\"")
                    .mandatory(Solicitor::getAddress, "applicant2SolicitorRepresented=\"Yes\"")
                    .mandatory(Solicitor::getPhone, "applicant2SolicitorRepresented=\"Yes\"")
                .done()
                .label("Label-CorrectApplicant2FODetails",
                    "### ${labelContentRespondentsOrApplicant2s} financial order details")
                .mandatory(Applicant::getFinancialOrder)
                .mandatory(Applicant::getFinancialOrdersFor, "applicant2FinancialOrder=\"Yes\"")
            .done()
            .label("Label-CorrectApplicant2SOTDetails",
                "### ${labelContentRespondentsOrApplicant2s} statement of truth details",
                "applicationType=\"jointApplication\"")
            .complex(CaseData::getApplication)
                .mandatory(Application::getApplicant2StatementOfTruth, "applicationType=\"jointApplication\"")
                .mandatory(Application::getApplicant2SolSignStatementOfTruth, "applicationType=\"jointApplication\"")
                .mandatory(Application::getApplicant2SolStatementOfReconciliationName, "applicationType=\"jointApplication\"")
                .mandatory(Application::getApplicant2SolStatementOfReconciliationFirm, "applicationType=\"jointApplication\"")
                .label("Label-CorrectJurisdictionDetails", "### Jurisdiction connection details")
                .complex(Application::getJurisdiction)
                    .mandatory(Jurisdiction::getConnections)
                .done()
                .label("Label-CorrectPrayerDetails", "### Prayer details")
                .readonly(Application::getDateSubmitted)
                .mandatory(Application::getApplicant1ScreenHasMarriageBroken)
                .mandatory(Application::getApplicant2ScreenHasMarriageBroken)
                .mandatory(Application::getApplicant1PrayerHasBeenGivenCheckbox)
                .mandatory(Application::getApplicant2PrayerHasBeenGivenCheckbox)
                .label("Label-CorrectApp1HWFDetails",
                    "### ${labelContentApplicantsOrApplicant1s} Help With Fees details")
                .complex(Application::getApplicant1HelpWithFees)
                    .optional(HelpWithFees::getReferenceNumber)
                    .mandatory(HelpWithFees::getAppliedForFees)
                    .mandatory(HelpWithFees::getNeedHelp)
                .done()
                .label("Label-CorrectApp2HWFDetails",
                    "### ${labelContentRespondentsOrApplicant2s} Help With Fees details")
                .complex(Application::getApplicant2HelpWithFees)
                    .optional(HelpWithFees::getReferenceNumber)
                    .mandatory(HelpWithFees::getAppliedForFees)
                    .mandatory(HelpWithFees::getNeedHelp)
                .done()
                .mandatory(Application::getScreenHasMarriageCert)
                .label("Label-CorrectMarriageDetails", "### Marriage details")
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
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        final CaseData data = details.getData();
        final Application application = data.getApplication();
        final List<String> errors = new ArrayList<>();

        data.getLabelContent().setApplicationType(data.getApplicationType());
        data.getLabelContent().setUnionType(data.getDivorceOrDissolution());

        if (!application.getApplicant1ScreenHasMarriageBroken().toBoolean()) {
            errors.add("To continue, applicant 1 must believe and declare that their marriage has irrevocably broken");
        }

        if (application.getApplicant2ScreenHasMarriageBroken() != null && !application.getApplicant2ScreenHasMarriageBroken().toBoolean()) {
            errors.add("To continue, applicant 2 must believe and declare that their marriage has irrevocably broken");
        }

        if (Objects.isNull(application.getApplicant1PrayerHasBeenGivenCheckbox())) {
            errors.add("Applicant 1 prayer must not be empty");
        } else if (application.getApplicant1PrayerHasBeenGivenCheckbox().isEmpty()) {
            errors.add("Applicant 1 prayer must be yes");
        }

        if (Objects.isNull(application.getApplicant2PrayerHasBeenGivenCheckbox())) {
            errors.add("Applicant 2 prayer must not be empty");
        } else if (application.getApplicant2PrayerHasBeenGivenCheckbox().isEmpty()) {
            errors.add("Applicant 2 prayer must be yes");
        }

        if (!application.hasStatementOfTruth()) {
            errors.add(SOT_REQUIRED);
        }

        if (!data.getApplicationType().isSole() && !application.hasApplicant2StatementOfTruth()) {
            errors.add("Statement of truth must be accepted by Applicant 2 for joint applications");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
