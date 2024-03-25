package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.Jurisdiction;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.PaperFormDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import static uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.SOT_REQUIRED;

public class CorrectPaperCase implements CcdPageConfiguration {

    private static final String TITLE = "Correct paper case";
    private static final String JOINT_APPLICATION = "applicationType=\"jointApplication\"";
    private static final String JOINT_DIVORCE_APPLICATION
        = "applicationType=\"jointApplication\" AND divorceOrDissolution = \"divorce\" AND supplementaryCaseType = \"notApplicable\"";
    private static final String JOINT_DISSOLUTION_APPLICATION
        = "applicationType=\"jointApplication\" AND divorceOrDissolution = \"dissolution\" AND supplementaryCaseType = \"notApplicable\"";
    private static final String APPLICANT_1_SOLICITOR_REPRESENTED_YES = "applicant1SolicitorRepresented=\"Yes\"";
    private static final String APPLICANT_2_SOLICITOR_REPRESENTED_YES = "applicant2SolicitorRepresented=\"Yes\"";
    private static final String DIVORCE_APPLICATION = "divorceOrDissolution = \"divorce\" AND supplementaryCaseType = \"notApplicable\"";
    private static final String DISSOLUTION_APPLICATION
        = "divorceOrDissolution = \"dissolution\" AND supplementaryCaseType = \"notApplicable\"";
    private static final String JUDICIAL_SEPARATION_APPLICATION = "supplementaryCaseType = \"judicialSeparation\"";
    private static final String JOINT_JUDICIAL_SEPARATION_APPLICATION
        = "supplementaryCaseType = \"judicialSeparation\" AND applicationType=\"jointApplication\"";
    private static final String SEPARATION_APPLICATION = "supplementaryCaseType = \"separation\"";
    private static final String JOINT_SEPARATION_APPLICATION
        = "supplementaryCaseType = \"separation\" AND applicationType=\"jointApplication\"";
    private static final String JOINT_APPLICATION_APP2_REPRESENTED
        = "applicationType=\"jointApplication\" AND applicant2SolicitorRepresented=\"Yes\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        FieldCollectionBuilder<CaseData, State, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder
            = pageBuilder
            .page("correctPaperCase", this::midEvent)
            .pageLabel(TITLE)
            .complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getApplicantOrApplicant1UC, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getApplicant2, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getApplicant2UC, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getTheApplicant2, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getTheApplicant2UC, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getUnionType, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getUnionTypeUC, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getDivorceOrCivilPartnership, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getDivorceOrCivilPartnershipApplication, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getFinaliseDivorceOrEndCivilPartnership, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getApplicantOrApplicant1, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getDivorceOrEndCivilPartnership, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getMarriageOrCivilPartnership, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getMarriageOrCivilPartnershipUC, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getDivorceOrLegallyEnd, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getApplicantsOrApplicant1s, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getTheApplicantOrApplicant1, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getTheApplicantOrApplicant1UC, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getGotMarriedOrFormedCivilPartnership, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getRespondentsOrApplicant2s, NEVER_SHOW)
            .done();

        buildApplicant1Fields(fieldCollectionBuilder);

        buildApplicant2Fields(fieldCollectionBuilder);

        buildMarriageDetailFields(fieldCollectionBuilder);

        buildJurisdictionFields(fieldCollectionBuilder);

        buildStatementOfBreakdownFields(fieldCollectionBuilder);

        buildExistingCaseDetailFields(fieldCollectionBuilder);

        buildFinancialRemedyFields(fieldCollectionBuilder);

        buildPrayerFields(fieldCollectionBuilder);

        buildSOTFields(fieldCollectionBuilder);

        buildCourtFeeFields(fieldCollectionBuilder);

        fieldCollectionBuilder
            .label("Label-CorrectScannedDocuments", "### Scanned Documents")
            .complex(CaseData::getDocuments)
                .optional(CaseDocuments::getScannedDocuments)
            .done();
    }

    private void buildApplicant1Fields(
        FieldCollectionBuilder<CaseData, State, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {

        fieldCollectionBuilder
            .label("Label-CorrectApplicant1Details",
                "### ${labelContentApplicantsOrApplicant1s} details")
            .complex(CaseData::getApplicant1)
                .mandatory(Applicant::getFirstName)
                .optional(Applicant::getMiddleName)
                .mandatory(Applicant::getLastName)
                .mandatoryWithLabel(Applicant::getNameDifferentToMarriageCertificate,
                    "Have they changed their name since they ${labelContentGotMarriedOrFormedCivilPartnership}?")
                .mandatory(Applicant::getNameDifferentToMarriageCertificateOtherDetails,
                "applicant1NameDifferentToMarriageCertificate=\"Yes\"")
                .mandatory(Applicant::getContactDetailsType)
                .optionalWithLabel(Applicant::getAddressOverseas, "Is ${labelContentApplicantsOrApplicant1s} address international?")
                .mandatoryWithLabel(Applicant::getAddress, "${labelContentApplicantsOrApplicant1s} address")
                .optionalWithLabel(Applicant::getPhoneNumber, "${labelContentApplicantsOrApplicant1s} phone number")
                .optionalWithLabel(Applicant::getEmail, "${labelContentApplicantsOrApplicant1s} email address")
                .mandatoryWithLabel(Applicant::getSolicitorRepresented,
                "Is ${labelContentTheApplicantOrApplicant1} represented by a solicitor?")
                .complex(Applicant::getSolicitor, APPLICANT_1_SOLICITOR_REPRESENTED_YES)
                    .label("Label-Applicant1SolDetails",
                "### ${labelContentApplicantsOrApplicant1s} solicitor details",
                APPLICANT_1_SOLICITOR_REPRESENTED_YES)
                    .mandatory(Solicitor::getName, APPLICANT_1_SOLICITOR_REPRESENTED_YES)
                    .mandatory(Solicitor::getReference, APPLICANT_1_SOLICITOR_REPRESENTED_YES)
                    .mandatory(Solicitor::getFirmName, APPLICANT_1_SOLICITOR_REPRESENTED_YES)
                    .mandatory(Solicitor::getAddress, APPLICANT_1_SOLICITOR_REPRESENTED_YES)
                    .mandatory(Solicitor::getAddressOverseas, APPLICANT_1_SOLICITOR_REPRESENTED_YES)
                    .optional(Solicitor::getPhone, APPLICANT_1_SOLICITOR_REPRESENTED_YES)
                    .optional(Solicitor::getEmail, APPLICANT_1_SOLICITOR_REPRESENTED_YES)
                .done()
            .done();
    }

    private void buildApplicant2Fields(FieldCollectionBuilder<CaseData, State, EventBuilder<CaseData, UserRole, State>>
                                           fieldCollectionBuilder) {
        fieldCollectionBuilder
            .label("Label-CorrectApplicant2Details",
                "### ${labelContentRespondentsOrApplicant2s} details")
            .complex(CaseData::getApplicant2)
                .mandatory(Applicant::getFirstName)
                .optional(Applicant::getMiddleName)
                .mandatory(Applicant::getLastName)
                .mandatoryWithLabel(Applicant::getNameDifferentToMarriageCertificate,
                    "Have they changed their name since they ${labelContentGotMarriedOrFormedCivilPartnership}?")
                .mandatory(Applicant::getNameDifferentToMarriageCertificateOtherDetails,
                    "applicant2NameDifferentToMarriageCertificate=\"Yes\"")
                .optionalWithLabel(Applicant::getAddressOverseas, "Is ${labelContentRespondentsOrApplicant2s} address international?")
                .mandatoryWithLabel(Applicant::getAddress, "${labelContentRespondentsOrApplicant2s} address")
                .optionalWithLabel(Applicant::getPhoneNumber, "${labelContentRespondentsOrApplicant2s} phone number")
                .optionalWithLabel(Applicant::getEmail, "${labelContentRespondentsOrApplicant2s} email address")
            .done()
            .label("Label-CorrectServiceDetails", "### Service details")
                .complex(CaseData::getPaperFormDetails)
                    .mandatory(PaperFormDetails::getServeOutOfUK)
                    .mandatory(PaperFormDetails::getRespondentServePostOnly)
                    .mandatory(PaperFormDetails::getApplicantWillServeApplication)
                .done()
            .complex(CaseData::getApplicant2)
                .mandatoryWithLabel(Applicant::getSolicitorRepresented,
                "Is ${labelContentTheApplicant2} represented by a solicitor?")
                .complex(Applicant::getSolicitor)
                    .label("Label-CorrectApplicant2SolDetails",
                "### ${labelContentRespondentsOrApplicant2s} solicitor details", APPLICANT_2_SOLICITOR_REPRESENTED_YES)
                    .mandatory(Solicitor::getName, APPLICANT_2_SOLICITOR_REPRESENTED_YES)
                    .optional(Solicitor::getReference, APPLICANT_2_SOLICITOR_REPRESENTED_YES)
                    .mandatory(Solicitor::getFirmName, APPLICANT_2_SOLICITOR_REPRESENTED_YES)
                    .mandatory(Solicitor::getAddress, APPLICANT_2_SOLICITOR_REPRESENTED_YES)
                    .mandatory(Solicitor::getAddressOverseas, APPLICANT_2_SOLICITOR_REPRESENTED_YES)
                    .optional(Solicitor::getPhone, APPLICANT_2_SOLICITOR_REPRESENTED_YES)
                    .optional(Solicitor::getEmail, APPLICANT_2_SOLICITOR_REPRESENTED_YES)
                .done()
            .done();
    }

    private void buildMarriageDetailFields(FieldCollectionBuilder<CaseData, State, EventBuilder<CaseData, UserRole, State>>
                                             fieldCollectionBuilder) {
        fieldCollectionBuilder
            .complex(CaseData::getApplication)
                .label("Label-CorrectMarriageDetails", "### ${labelContentMarriageOrCivilPartnershipUC} details")
                .complex(Application::getMarriageDetails)
                    .mandatory(MarriageDetails::getMarriedInUk)
                    .mandatoryWithLabel(MarriageDetails::getIssueApplicationWithoutMarriageCertificate,
                "Are you making a separate application to issue without your ${labelContentMarriageOrCivilPartnership} certificate?")
                    .mandatoryWithLabel(MarriageDetails::getDate, "Date of ${labelContentMarriageOrCivilPartnershipUC}")
                    .mandatoryWithLabel(MarriageDetails::getApplicant1Name,
                "${labelContentApplicantsOrApplicant1s} full name as on ${labelContentMarriageOrCivilPartnership} certificate")
                    .mandatoryWithLabel(MarriageDetails::getApplicant2Name,
                "${labelContentRespondentsOrApplicant2s} full name as on ${labelContentMarriageOrCivilPartnership} certificate")
                    .mandatory(MarriageDetails::getCertifyMarriageCertificateIsCorrect) //fix
                    .mandatory(MarriageDetails::getMarriageCertificateIsIncorrectDetails,
                "marriageCertifyMarriageCertificateIsCorrect=\"No\"")
                .done()
            .done();
    }

    private void buildJurisdictionFields(FieldCollectionBuilder<CaseData, State, EventBuilder<CaseData, UserRole, State>>
                                             fieldCollectionBuilder) {
        fieldCollectionBuilder
            .complex(CaseData::getApplication)
                .label("Label-CorrectJurisdictionDetails", "### Jurisdiction connection details")
                .complex(Application::getJurisdiction)
                    .mandatory(Jurisdiction::getConnections)
                .done()
            .done();
    }

    private void buildStatementOfBreakdownFields(FieldCollectionBuilder<CaseData, State, EventBuilder<CaseData, UserRole, State>>
                                                     fieldCollectionBuilder) {
        fieldCollectionBuilder
            .label("Label-CorrectStatementOfIrretrievableBreakdown", "### Statement of irretrievable breakdown")
            .complex(CaseData::getApplication)
                .mandatoryWithLabel(Application::getApplicant1ScreenHasMarriageBroken,
                "Has the ${labelContentApplicantsOrApplicant1s} ${labelContentMarriageOrCivilPartnership} broken down irretrievably?")
                .mandatory(Application::getApplicant2ScreenHasMarriageBroken, JOINT_APPLICATION, null,
                "Has the ${labelContentRespondentsOrApplicant2s} ${labelContentMarriageOrCivilPartnership} broken down irretrievably?")
            .done();
    }

    private void buildExistingCaseDetailFields(FieldCollectionBuilder<CaseData, State, EventBuilder<CaseData, UserRole, State>>
                                                   fieldCollectionBuilder) {
        fieldCollectionBuilder
            .complex(CaseData::getApplicant1)
                .label("Label-CorrectApplicant1LegalProceedingsDetails",
                "### ${labelContentApplicantsOrApplicant1s} legal proceedings details")
                .mandatory(Applicant::getLegalProceedings)
                .mandatory(Applicant::getLegalProceedingsDetails, "applicant1LegalProceedings=\"Yes\"")
            .done();
    }

    private void buildSOTFields(FieldCollectionBuilder<CaseData, State, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {
        fieldCollectionBuilder
            .complex(CaseData::getApplication)
                .label("Label-CorrectApplicant1SOTDetails",
                "### ${labelContentApplicantsOrApplicant1s} statement of truth details")
                .mandatory(Application::getApplicant1StatementOfTruth)
                .mandatory(Application::getSolSignStatementOfTruth)
                .label("Label-CorrectApplicant2SOTDetails",
                "### ${labelContentRespondentsOrApplicant2s} statement of truth details", JOINT_APPLICATION)
                .mandatory(Application::getApplicant2StatementOfTruth, JOINT_APPLICATION)
                .mandatory(Application::getApplicant2SolSignStatementOfTruth, JOINT_APPLICATION)
                .mandatory(Application::getApplicant2SolStatementOfReconciliationName, JOINT_APPLICATION)
                .mandatory(Application::getApplicant2SolStatementOfReconciliationFirm, JOINT_APPLICATION_APP2_REPRESENTED)
            .done();
    }

    private void buildFinancialRemedyFields(FieldCollectionBuilder<CaseData, State, EventBuilder<CaseData, UserRole, State>>
                                                fieldCollectionBuilder) {
        fieldCollectionBuilder
            .complex(CaseData::getApplicant1)
                .label("Label-CorrectApplicant1FODetails",
                "### ${labelContentApplicantsOrApplicant1s} financial order details")
                .mandatory(Applicant::getFinancialOrder)
                .mandatory(Applicant::getFinancialOrdersFor, "applicant1FinancialOrder=\"Yes\"")
            .done()
            .complex(CaseData::getApplicant2, JOINT_APPLICATION)
                .label("Label-CorrectApplicant2FODetails",
                "### ${labelContentRespondentsOrApplicant2s} financial order details", JOINT_APPLICATION)
                .mandatory(Applicant::getFinancialOrder, JOINT_APPLICATION, null,
                "Does ${labelContentTheApplicant2} wish to apply for a financial order?")
                .mandatory(Applicant::getFinancialOrdersFor, "applicant2FinancialOrder=\"Yes\" AND applicationType=\"jointApplication\"")
            .done();
    }

    private void buildPrayerFields(FieldCollectionBuilder<CaseData, State, EventBuilder<CaseData, UserRole, State>>
                                       fieldCollectionBuilder) {
        fieldCollectionBuilder
            .label("Label-CorrectPrayerDetails-App1",
                "### ${labelContentApplicantsOrApplicant1s} prayer details")
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getApplicantPrayer)
                .mandatory(ApplicantPrayer::getPrayerDissolveDivorce, DIVORCE_APPLICATION)
                .mandatory(ApplicantPrayer::getPrayerEndCivilPartnership, DISSOLUTION_APPLICATION)
                .mandatory(ApplicantPrayer::getPrayerJudicialSeparation, JUDICIAL_SEPARATION_APPLICATION)
                .mandatory(ApplicantPrayer::getPrayerSeparation, SEPARATION_APPLICATION)
                .optional(ApplicantPrayer::getPrayerFinancialOrdersThemselves)
                .optional(ApplicantPrayer::getPrayerFinancialOrdersChild)
                .done()
            .done()
            .label("Label-CorrectPrayerDetails-App2",
                "### Applicant 2's prayer details",
                JOINT_APPLICATION)
            .complex(CaseData::getApplicant2)
                .complex(Applicant::getApplicantPrayer)
                .mandatory(ApplicantPrayer::getPrayerDissolveDivorce, JOINT_DIVORCE_APPLICATION)
                .mandatory(ApplicantPrayer::getPrayerEndCivilPartnership, JOINT_DISSOLUTION_APPLICATION)
                .mandatory(ApplicantPrayer::getPrayerJudicialSeparation, JOINT_JUDICIAL_SEPARATION_APPLICATION)
                .mandatory(ApplicantPrayer::getPrayerSeparation, JOINT_SEPARATION_APPLICATION)
                .optional(ApplicantPrayer::getPrayerFinancialOrdersThemselves, JOINT_APPLICATION)
                .optional(ApplicantPrayer::getPrayerFinancialOrdersChild, JOINT_APPLICATION)
                .done()
            .done();
    }

    private void buildCourtFeeFields(FieldCollectionBuilder<CaseData, State, EventBuilder<CaseData, UserRole, State>>
                                         fieldCollectionBuilder) {
        fieldCollectionBuilder
            .complex(CaseData::getApplication)
                .label("Label-CorrectApp1HWFDetails",
                "### ${labelContentApplicantsOrApplicant1s} Help With Fees details")
                .complex(Application::getApplicant1HelpWithFees)
                    .optional(HelpWithFees::getReferenceNumber)
                    .mandatory(HelpWithFees::getAppliedForFees)
                    .mandatory(HelpWithFees::getNeedHelp)
                .done()
                .complex(Application::getApplicant2HelpWithFees, JOINT_APPLICATION)
                    .label("Label-CorrectApp2HWFDetails",
                "### ${labelContentRespondentsOrApplicant2s} Help With Fees details", JOINT_APPLICATION)
                    .optional(HelpWithFees::getReferenceNumber, JOINT_APPLICATION)
                    .mandatory(HelpWithFees::getAppliedForFees, JOINT_APPLICATION)
                    .mandatory(HelpWithFees::getNeedHelp, JOINT_APPLICATION)
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
