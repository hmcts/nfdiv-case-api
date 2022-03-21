package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.Jurisdiction;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.SOT_REQUIRED;

public class CorrectPaperCase implements CcdPageConfiguration {

    @Override
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("Correct paper case", this::midEvent)
            .pageLabel("Correct paper case")
            .complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getApplicantsOrApplicant1s, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getTheApplicantOrApplicant1, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getApplicantOrApplicant1UC, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getRespondentsOrApplicant2s, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getTheApplicant2, NEVER_SHOW)
            .done()
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
                .mandatoryWithLabel(Applicant::getAddress, "${labelContentApplicantsOrApplicant1s} address")
                .optionalWithLabel(Applicant::getPhoneNumber, "${labelContentApplicantsOrApplicant1s} phone number")
                .optionalWithLabel(Applicant::getEmail, "${labelContentApplicantsOrApplicant1s} email address")
                .mandatoryWithLabel(Applicant::getSolicitorRepresented,
                    "Is ${labelContentTheApplicantOrApplicant1} represented by a solicitor?")
                .complex(Applicant::getSolicitor, "applicant1SolicitorRepresented=\"Yes\"")
                    .label("Label-Applicant1SolDetails",
                        "### ${labelContentApplicantsOrApplicant1s} solicitor details",
                        "applicant1SolicitorRepresented=\"Yes\"")
                    .mandatory(Solicitor::getName, "applicant1SolicitorRepresented=\"Yes\"")
                    .optional(Solicitor::getReference, "applicant1SolicitorRepresented=\"Yes\"")
                    .mandatory(Solicitor::getFirmName, "applicant1SolicitorRepresented=\"Yes\"")
                    .mandatory(Solicitor::getAddress, "applicant1SolicitorRepresented=\"Yes\"")
                    .optional(Solicitor::getPhone, "applicant1SolicitorRepresented=\"Yes\"")
                    .mandatory(Solicitor::getEmail, "applicant1SolicitorRepresented=\"Yes\"")
                .done()
                .label("Label-CorrectApplicant1FODetails",
                    "### ${labelContentApplicantsOrApplicant1s} financial order details")
                .mandatoryWithLabel(Applicant::getFinancialOrder,
                    "Does ${labelContentTheApplicantOrApplicant1} wish to apply for a financial order?")
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
                .mandatoryWithLabel(Applicant::getAddress, "${labelContentRespondentsOrApplicant2s} address")
                .optionalWithLabel(Applicant::getPhoneNumber, "${labelContentRespondentsOrApplicant2s} phone number")
                .optionalWithLabel(Applicant::getEmail, "${labelContentRespondentsOrApplicant2s} email address")
                .mandatoryWithLabel(Applicant::getSolicitorRepresented,
                    "Is ${labelContentTheApplicant2} represented by a solicitor?")
                .complex(Applicant::getSolicitor)
                    .label("Label-CorrectApplicant2SolDetails",
                        "### ${labelContentRespondentsOrApplicant2s} solicitor details",
                        "applicant2SolicitorRepresented=\"Yes\"")
                    .mandatory(Solicitor::getName, "applicant2SolicitorRepresented=\"Yes\"")
                    .mandatory(Solicitor::getReference, "applicant2SolicitorRepresented=\"Yes\"")
                    .mandatory(Solicitor::getFirmName, "applicant2SolicitorRepresented=\"Yes\"")
                    .mandatory(Solicitor::getAddress, "applicant2SolicitorRepresented=\"Yes\"")
                    .mandatory(Solicitor::getPhone, "applicant2SolicitorRepresented=\"Yes\"")
                .done()
                .label("Label-CorrectApplicant2FODetails",
                    "### ${labelContentRespondentsOrApplicant2s} financial order details")
                .mandatoryWithLabel(Applicant::getFinancialOrder,
                    "Does ${labelContentTheApplicant2} wish to apply for a financial order?")
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
                .readonly(Application::getDateSubmitted)
                .mandatoryWithLabel(Application::getApplicant1ScreenHasMarriageBroken,
                    "Has the ${labelContentApplicantsOrApplicant1s} ${labelContentMarriageOrCivilPartnership} broken down irretrievably?")
                .mandatoryWithLabel(Application::getApplicant2ScreenHasMarriageBroken,
                    "Has the ${labelContentRespondentsOrApplicant2s} ${labelContentMarriageOrCivilPartnership} broken down irretrievably?")
                .label("Label-CorrectApp1HWFDetails",
                    "### ${labelContentApplicantsOrApplicant1s} Help With Fees details")
                .complex(Application::getApplicant1HelpWithFees)
                    .optional(HelpWithFees::getReferenceNumber)
                    .mandatory(HelpWithFees::getAppliedForFees)
                    .mandatory(HelpWithFees::getNeedHelp)
                .done()
                .complex(Application::getApplicant2HelpWithFees, "applicationType=\"jointApplication\"")
                    .label("Label-CorrectApp2HWFDetails",
                        "### ${labelContentRespondentsOrApplicant2s} Help With Fees details",
                        "applicationType=\"jointApplication\"")
                    .optional(HelpWithFees::getReferenceNumber, "applicationType=\"jointApplication\"")
                    .mandatory(HelpWithFees::getAppliedForFees, "applicationType=\"jointApplication\"")
                    .mandatory(HelpWithFees::getNeedHelp, "applicationType=\"jointApplication\"")
                .done()
                .mandatory(Application::getScreenHasMarriageCert)
                .label("Label-CorrectMarriageDetails", "### Marriage details")
                .complex(Application::getMarriageDetails)
                    .mandatory(MarriageDetails::getCertificateInEnglish)
                    .mandatory(MarriageDetails::getCertifiedTranslation,
                        "marriageCertificateInEnglish=\"No\"")
                    .mandatory(MarriageDetails::getMarriedInUk)
                    .mandatoryWithLabel(MarriageDetails::getIssueApplicationWithoutMarriageCertificate,
                        "Are you making a separate application to issue without your marriage or civil partnership certificate?")
                    .mandatory(MarriageDetails::getDate)
                    .mandatoryWithLabel(MarriageDetails::getApplicant1Name,
                        "${labelContentApplicantsOrApplicant1s} full name as on marriage certificate")
                    .mandatoryWithLabel(MarriageDetails::getApplicant2Name,
                        "${labelContentRespondentsOrApplicant2s} full name as on marriage certificate")
                    .mandatory(MarriageDetails::getCertifyMarriageCertificateIsCorrect)
                    .mandatory(MarriageDetails::getMarriageCertificateIsIncorrectDetails,
                        "marriageCertifyMarriageCertificateIsCorrect=\"No\"")
                .done()
            .done()
            .label("Label-CorrectScannedDocuments",
                "### Scanned Documents")
            .complex(CaseData::getDocuments)
                .optional(CaseDocuments::getScannedDocuments)
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

        if (!data.getApplicationType().isSole()
            && application.getApplicant2ScreenHasMarriageBroken() != null
            && !application.getApplicant2ScreenHasMarriageBroken().toBoolean()
        ) {
            errors.add("To continue, applicant 2 must believe and declare that their marriage has irrevocably broken");
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
