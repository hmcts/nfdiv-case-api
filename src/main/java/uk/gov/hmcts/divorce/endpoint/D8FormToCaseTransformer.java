package uk.gov.hmcts.divorce.endpoint;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.bulkscan.validation.OcrValidator;
import uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.endpoint.data.OcrValidationResponse;
import uk.gov.hmcts.reform.bsp.common.error.InvalidDataException;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.service.transformation.BulkScanFormTransformer;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.collections4.ListUtils.union;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.join;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.from;
import static uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields.transformOcrMapToObject;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PRIVATE;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PUBLIC;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_DOMICILED;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_LAST_RESIDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_RESIDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_DOMICILED;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_RESIDENT_JOINT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_RESIDENT_SIX_MONTHS;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_RESIDENT_TWELVE_MONTHS;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_2_DOMICILED;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_2_RESIDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.RESIDUAL_JURISDICTION;
import static uk.gov.hmcts.divorce.endpoint.data.FormType.D8;

@Component
@Slf4j
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.ExcessiveMethodLength", "PMD.NcssCount", "PMD.PreserveStackTrace"})
public class D8FormToCaseTransformer extends BulkScanFormTransformer {

    private static final String THE_SOLE_APPLICANT_OR_APPLICANT_1 = "theSoleApplicantOrApplicant1";
    private static final String FOR_THE_CHILDREN = "forTheChildren";
    private static final String THE_SOLE_APPLICANT_OR_APPLICANT_1_FOR_THE_CHILDREN = "theSoleApplicantOrApplicant1,forTheChildren";
    private static final String MYSELF = "myself";
    private static final String MYSELF_CHILDREN = "myself,children";
    private static final String CHILDREN = "children";
    private static final String APPLICANT_APPLICANT_1 = "applicant,applicant1";
    private static final String APPLICANT_2 = "applicant2";
    private static final String RESPONDENT = "respondent";

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private OcrValidator validator;

    @Override
    protected Map<String, String> getOcrToCCDMapping() {
        return Collections.emptyMap();
    }

    @Override
    protected Map<String, Object> runFormSpecificTransformation(List<OcrDataField> ocrDataMapFields) {
        OcrDataFields ocrDataFields = transformOcrMapToObject(ocrDataMapFields);

        OcrValidationResponse ocrValidationResponse = validator.validateOcrData(D8.getName(), ocrDataFields);

        if (!isEmpty(ocrValidationResponse.getErrors())) {
            throw new InvalidDataException(
                "Error during D8 transformation",
                ocrValidationResponse.getWarnings(),
                ocrValidationResponse.getErrors()
            );
        }
        var caseData = CaseData.builder().build();

        List<String> errors = new ArrayList<>();

        try {
            // Section 1 – Your application
            boolean marriageOrCivilPartnershipCert = toBoolean(ocrDataFields.getMarriageOrCivilPartnershipCertificate());
            caseData.getApplication().setScreenHasMarriageCert(from(marriageOrCivilPartnershipCert));
            final var marriageDetails = marriageDetails(ocrDataFields.getTranslation());

            caseData.getApplication().setMarriageDetails(marriageDetails);
            caseData.setDivorceOrDissolution(getDivorceType(ocrDataFields, errors));
            caseData.setApplicationType(getApplicationType(ocrDataFields, errors));

            // Section 2 – About you(the sole applicant or applicant 1)
            final var isApp1SolicitorRepresented = toBoolean(ocrDataFields.getSoleOrApplicant1Solicitor());
            caseData.setApplicant1(applicant1(ocrDataFields, isApp1SolicitorRepresented));
            if (isApp1SolicitorRepresented) {
                caseData.getApplicant1().setSolicitor(solicitor1(ocrDataFields));
            }

            // Section 3 – About the respondent or applicant 2
            final var isApp2SolicitorRepresented = ocrDataFields.getRespondentOrApplicant2SolicitorName() != null;
            caseData.setApplicant2(applicant2(ocrDataFields, isApp2SolicitorRepresented));
            if (isApp2SolicitorRepresented) {
                caseData.getApplicant1().setSolicitor(solicitor2(ocrDataFields));
            }

            caseData.getPaperFormDetails().setServiceOutsideUK(from(toBoolean(ocrDataFields.getServeOutOfUK())));
            caseData.getApplicant2().setOffline(from(toBoolean(ocrDataFields.getRespondentServePostOnly())));
            caseData.getPaperFormDetails().setApplicantWillServeApplication(
                from(toBoolean(ocrDataFields.getApplicantWillServeApplication()))
            );
            caseData.getPaperFormDetails().setRespondentDifferentServiceAddress(
                from(toBoolean(ocrDataFields.getRespondentDifferentServiceAddress()))
            );

            // Section 4 – Details of marriage/civil partnership
            marriageDetails.setMarriedInUk(from(toBoolean(ocrDataFields.getMarriageOutsideOfUK())));
            marriageDetails.setIssueApplicationWithoutMarriageCertificate(
                from(toBoolean(ocrDataFields.getMakingAnApplicationWithoutCertificate()))
            );
            marriageDetails.setDate(deriveMarriageDate(ocrDataFields, errors));
            marriageDetails.setApplicant1Name(ocrDataFields.getSoleOrApplicant1FullNameAsOnCert());
            marriageDetails.setApplicant2Name(ocrDataFields.getRespondentOrApplicant2FullNameAsOnCert());
            marriageDetails.setCertifyMarriageCertificateIsCorrect(from(toBoolean(ocrDataFields.getDetailsOnCertCorrect())));
            marriageDetails.setMarriageCertificateIsIncorrectDetails(ocrDataFields.getReasonWhyCertNotCorrect());

            // Section 5 – Why this court can deal with your case (Jurisdiction)
            caseData.getApplication().getJurisdiction().setConnections(deriveJurisdictionConnections(ocrDataFields, errors));

            // Section 6 – Statement of irretrievable breakdown (the legal reason for your divorce or dissolution)
            caseData.getApplication().setApplicant1ScreenHasMarriageBroken(
                from(toBoolean(ocrDataFields.getSoleOrApplicant1ConfirmationOfBreakdown()))
            );
            caseData.getApplication().setApplicant1ScreenHasMarriageBroken(
                from(toBoolean(ocrDataFields.getApplicant2ConfirmationOfBreakdown()))
            );

            // Section 7 – Existing or previous court cases
            caseData.getApplicant1().setLegalProceedings(from(toBoolean(ocrDataFields.getExistingOrPreviousCourtCases())));
            caseData.getApplicant1().setLegalProceedingsDetails(
                join(ocrDataFields.getExistingOrPreviousCourtCaseNumbers(), ocrDataFields.getSummaryOfExistingOrPreviousCourtCases())
            );

            // Section 8 – Dividing your money and property – Orders which are sought
            caseData.getApplicant1().setFinancialOrder(from(toBoolean(ocrDataFields.getSoleOrApplicant1FinancialOrder())));
            caseData.getApplicant1().setFinancialOrdersFor(deriveFinancialOrderFor(ocrDataFields.getSoleOrApplicant1FinancialOrderFor()));

            caseData.getApplicant2().setFinancialOrder(from(toBoolean(ocrDataFields.getApplicant2FinancialOrder())));
            caseData.getApplicant2().setFinancialOrdersFor(deriveFinancialOrderFor(ocrDataFields.getApplicant2FinancialOrderFor()));

            // Section 9 – Summary of what is being applied for (the prayer)
            final var isMarriageDissolved = toBoolean(ocrDataFields.getPrayerMarriageDissolved());
            final var isCivilPartnershipDissolved = toBoolean(ocrDataFields.getPrayerCivilPartnershipDissolved());
            if (SOLE_APPLICATION.equals(caseData.getApplicationType()) && (isMarriageDissolved || isCivilPartnershipDissolved)) {
                caseData.getApplication().setApplicant1PrayerHasBeenGivenCheckbox(Set.of(Application.ThePrayer.I_CONFIRM));
            } else if (JOINT_APPLICATION.equals(caseData.getApplicationType()) && (isMarriageDissolved || isCivilPartnershipDissolved)) {
                caseData.getApplication().setApplicant1PrayerHasBeenGivenCheckbox(Set.of(Application.ThePrayer.I_CONFIRM));
                caseData.getApplication().setApplicant2PrayerHasBeenGivenCheckbox(Set.of(Application.ThePrayer.I_CONFIRM));
            } else {
                errors.add("Error in OCR fields prayerMarriageDissolved and prayerCivilPartnershipDissolved");
            }

            applicantSummaryFinancialOrderFor(ocrDataFields.getSoleOrApplicant1prayerFinancialOrderFor());
            applicantSummaryFinancialOrderFor(ocrDataFields.getSoleOrApplicant1prayerFinancialOrderFor());

            caseData.getPaperFormDetails().setSummaryApplicant1FinancialOrdersFor(
                applicantSummaryFinancialOrderFor(ocrDataFields.getSoleOrApplicant1prayerFinancialOrderFor())
            );
            caseData.getPaperFormDetails().setSummaryApplicant2FinancialOrdersFor(
                applicantSummaryFinancialOrderFor(ocrDataFields.getApplicant2PrayerFinancialOrderFor())
            );

            // Section 10 – Statement of truth
            // Applicant 1
            caseData.getApplication().setApplicant1StatementOfTruth(
                from(toBoolean(ocrDataFields.getSoleApplicantOrApplicant1StatementOfTruth()))
            );
            caseData.getApplication().setSolSignStatementOfTruth(
                from(toBoolean(ocrDataFields.getSoleApplicantOrApplicant1LegalRepStatementOfTruth()))
            );
            caseData.getPaperFormDetails().setApplicant1SigningSOT(
                from(toBoolean(ocrDataFields.getSoleApplicantOrApplicant1Signing()))
            );
            caseData.getPaperFormDetails().setApplicant1LegalRepSigningSOT(
                from(toBoolean(ocrDataFields.getSoleApplicantOrApplicant1OrLegalRepSignature()))
            );
            caseData.getApplication().setSolStatementOfReconciliationName(ocrDataFields.getSoleApplicantOrApplicant1OrLegalRepFullName());
            caseData.getApplication().setSolStatementOfReconciliationFirm(ocrDataFields.getSoleApplicantOrApplicant1LegalRepFirm());
            caseData.getPaperFormDetails().setApplicant1LegalRepPosition(ocrDataFields.getSoleApplicantOrApplicant1LegalRepPosition());

            caseData.getPaperFormDetails().setApplicant1SOTSignedOn(
                deriveStatementOfTruthDate(
                    ocrDataFields.getStatementOfTruthDateDay(),
                    ocrDataFields.getStatementOfTruthDateMonth(),
                    ocrDataFields.getStatementOfTruthDateYear(),
                    errors
                )
            );

            // Applicant 2
            caseData.getApplication().setApplicant2StatementOfTruth(
                from(toBoolean(ocrDataFields.getSoleApplicantOrApplicant1StatementOfTruth()))
            );
            caseData.getApplication().setApplicant2SolSignStatementOfTruth(
                from(toBoolean(ocrDataFields.getSoleApplicantOrApplicant1LegalRepStatementOfTruth()))
            );

            caseData.getPaperFormDetails().setApplicant2SigningSOT(
                from(toBoolean(ocrDataFields.getApplicant2Signing()))
            );
            caseData.getPaperFormDetails().setApplicant2LegalRepSigningSOT(
                from(toBoolean(ocrDataFields.getApplicant2LegalRepSigning()))
            );

            caseData.getApplication().setApplicant2SolStatementOfReconciliationName(ocrDataFields.getApplicant2OrLegalRepFullName());
            caseData.getApplication().setApplicant2SolStatementOfReconciliationFirm(ocrDataFields.getApplicant2LegalRepFirm());
            caseData.getPaperFormDetails().setApplicant2LegalRepPosition(ocrDataFields.getApplicant2LegalRepPosition());

            caseData.getPaperFormDetails().setApplicant2SOTSignedOn(
                deriveStatementOfTruthDate(
                    ocrDataFields.getApplicant2StatementOfTruthDateDay(),
                    ocrDataFields.getApplicant2StatementOfTruthDateMonth(),
                    ocrDataFields.getApplicant2StatementOfTruthDateYear(),
                    errors
                )
            );

            // Court fee
            caseData.getPaperFormDetails().setFeeInPounds(ocrDataFields.getCourtFee());
            caseData.getPaperFormDetails().setApplicant1NoPaymentIncluded(
                from(toBoolean(ocrDataFields.getSoleOrApplicant1NoPaymentIncluded()))
            );
            caseData.getPaperFormDetails().setApplicant2NoPaymentIncluded(
                from(toBoolean(ocrDataFields.getApplicant2NoPaymentIncluded()))
            );

            caseData.getPaperFormDetails().setSoleOrApplicant1PaymentOther(
                from(toBoolean(ocrDataFields.getSoleOrApplicant1PaymentOther()))
            );
            caseData.getPaperFormDetails().setApplicant2PaymentOther(
                from(toBoolean(ocrDataFields.getApplicant2PaymentOther()))
            );

            caseData.getApplication().setApplicant1HelpWithFees(
                HelpWithFees
                    .builder()
                    .appliedForFees(from(toBoolean(ocrDataFields.getSoleOrApplicant1HWFConfirmation())))
                    .referenceNumber(ocrDataFields.getSoleOrApplicant1HWFNo())
                    .needHelp(from(toBoolean(ocrDataFields.getSoleOrApplicant1HWFApp())))
                    .build()
            );
            caseData.getApplication().setApplicant2HelpWithFees(
                HelpWithFees
                    .builder()
                    .appliedForFees(from(toBoolean(ocrDataFields.getApplicant2HWFConfirmation())))
                    .referenceNumber(ocrDataFields.getApplicant2HWFConfirmationNo())
                    .needHelp(from(toBoolean(ocrDataFields.getApplicant2HWFApp())))
                    .build()
            );

            caseData.getPaperFormDetails().setDebitCreditCardPayment(from(toBoolean(ocrDataFields.getDebitCreditCardPayment())));
            caseData.getPaperFormDetails().setDebitCreditCardPaymentPhone(from(toBoolean(ocrDataFields.getDebitCreditCardPaymentPhone())));
            caseData.getPaperFormDetails().setHowToPayEmail(from(toBoolean(ocrDataFields.getHowToPayEmail())));
            caseData.getPaperFormDetails().setPaymentDetailEmail(ocrDataFields.getPaymentDetailEmail());
            caseData.getPaperFormDetails().setChequeOrPostalOrderPayment(from(toBoolean(ocrDataFields.getChequeOrPostalOrderPayment())));

            // Set label content
            caseData.getLabelContent().setApplicationType(caseData.getApplicationType());
            caseData.getLabelContent().setUnionType(caseData.getDivorceOrDissolution());

            // Set gender
            caseData.deriveAndPopulateApplicantGenderDetails();

            return mapper.convertValue(caseData, new TypeReference<>() {
            });
        } catch (Exception exception) {
            //this will result in bulk scan service to create exception record
            log.error("Exception occurred while transforming D8 form with error {}", exception.getMessage());

            throw new InvalidDataException(
                "Exception occurred while transforming D8 form",
                ocrValidationResponse.getWarnings(),
                union(errors, ocrValidationResponse.getErrors())
            );
        }
    }

    private Set<FinancialOrderFor> applicantSummaryFinancialOrderFor(String applicantPrayerFinancialOrderFor) {
        Set<FinancialOrderFor> financialOrdersFor = new HashSet<>();
        if (THE_SOLE_APPLICANT_OR_APPLICANT_1.equalsIgnoreCase(applicantPrayerFinancialOrderFor)) {
            financialOrdersFor.add(APPLICANT);
        } else if (FOR_THE_CHILDREN.equalsIgnoreCase(applicantPrayerFinancialOrderFor)) {
            financialOrdersFor.add(APPLICANT);
            financialOrdersFor.add(FinancialOrderFor.CHILDREN);
        } else if (THE_SOLE_APPLICANT_OR_APPLICANT_1_FOR_THE_CHILDREN.equalsIgnoreCase(applicantPrayerFinancialOrderFor)) {
            financialOrdersFor.add(FinancialOrderFor.CHILDREN);
        }
        return financialOrdersFor;
    }

    private Set<FinancialOrderFor> deriveFinancialOrderFor(String financialOrderFor) {
        Set<FinancialOrderFor> financialOrdersFor = new HashSet<>();
        if (MYSELF.equalsIgnoreCase(financialOrderFor)) {
            financialOrdersFor.add(APPLICANT);
        } else if (MYSELF_CHILDREN.equalsIgnoreCase(financialOrderFor)) {
            financialOrdersFor.add(APPLICANT);
            financialOrdersFor.add(FinancialOrderFor.CHILDREN);
        } else if (CHILDREN.equalsIgnoreCase(financialOrderFor)) {
            financialOrdersFor.add(FinancialOrderFor.CHILDREN);
        }
        return financialOrdersFor;
    }

    private Set<JurisdictionConnections> deriveJurisdictionConnections(OcrDataFields ocrDataFields, List<String> errors) {
        Set<JurisdictionConnections> connections = new HashSet<>();
        if (toBoolean(ocrDataFields.getJurisdictionReasonsBothPartiesHabitual())) {
            connections.add(APP_1_APP_2_RESIDENT);
        } else if (toBoolean(ocrDataFields.getJurisdictionReasonsBothPartiesLastHabitual())) {
            connections.add(APP_1_APP_2_LAST_RESIDENT);
        } else if (toBoolean(ocrDataFields.getJurisdictionReasonsRespHabitual())) {
            connections.add(APP_2_RESIDENT);
        } else if (toBoolean(ocrDataFields.getJurisdictionReasonsJointHabitual())) {
            connections.add(APP_1_RESIDENT_JOINT);
        } else if (toBoolean(ocrDataFields.getJurisdictionReasons1YrHabitual())) {
            connections.add(APP_1_RESIDENT_TWELVE_MONTHS);
        } else if (toBoolean(ocrDataFields.getJurisdictionReasons6MonthsHabitual())) {
            connections.add(APP_1_RESIDENT_SIX_MONTHS);
        } else if (toBoolean(ocrDataFields.getJurisdictionReasonsBothPartiesDomiciled())) {
            connections.add(APP_1_APP_2_DOMICILED);
        } else if (toBoolean(ocrDataFields.getJurisdictionReasonsOnePartyDomiciled())) {
            if (APPLICANT_APPLICANT_1.equalsIgnoreCase(ocrDataFields.getJurisdictionReasonsOnePartyDomiciledWho())) {
                connections.add(APP_1_DOMICILED);
            } else if (APPLICANT_2.equalsIgnoreCase(ocrDataFields.getJurisdictionReasonsOnePartyDomiciledWho())
                || RESPONDENT.equalsIgnoreCase(ocrDataFields.getJurisdictionReasonsOnePartyDomiciledWho())) {
                connections.add(APP_2_DOMICILED);
            } else {
                errors.add("More than one option selected for only one party domiciled. OCR Field jurisdictionReasonsOnePartyDomiciledWho");
            }
        } else if (toBoolean(ocrDataFields.getJurisdictionReasonsSameSex())) {
            // only for civil partnership
            connections.add(RESIDUAL_JURISDICTION);
        }
        return connections;
    }

    private LocalDate deriveStatementOfTruthDate(String day, String month, String year, List<String> errors) {
        try {
            int dayParsed = Integer.parseInt(day); // format "18"
            int monthParsed = Integer.parseInt(month); //format "06"
            int yearParsed = Integer.parseInt(year); // format "2022"
            return LocalDate.of(dayParsed, monthParsed, yearParsed);
        } catch (DateTimeException exception) {
            // log and add error it as will be corrected manually the caseworker
            errors.add("Statement of truth date format is invalid. Expected format is 01/01/2022");
        }
        return null;
    }

    private LocalDate deriveMarriageDate(OcrDataFields ocrDataFields, List<String> errors) {
        try {
            String marriageMonth = ocrDataFields.getDateOfMarriageOrCivilPartnershipMonth();
            int dayParsed = Integer.parseInt(ocrDataFields.getDateOfMarriageOrCivilPartnershipDay()); // format "18"
            int monthParsed = Month.valueOf(marriageMonth.toUpperCase(Locale.UK)).getValue(); //format "January"
            int yearParsed = Integer.parseInt(ocrDataFields.getDateOfMarriageOrCivilPartnershipYear()); // format "2022"
            return LocalDate.of(dayParsed, monthParsed, yearParsed);
        } catch (DateTimeException exception) {
            // log and add validation it as will be corrected manually the caseworker
            errors.add("Marriage date format is invalid. Expected format is 01/January/2022");
        }
        return null;
    }

    private Applicant applicant2(OcrDataFields ocrDataFields, boolean isApp2SolicitorRepresented) {
        return Applicant
            .builder()
            .firstName(ocrDataFields.getRespondentOrApplicant2FirstName())
            .middleName(ocrDataFields.getRespondentOrApplicant2MiddleName())
            .lastName(ocrDataFields.getRespondentOrApplicant2LastName())
            .nameDifferentToMarriageCertificate(from(toBoolean(ocrDataFields.getRespondentOrApplicant2MarriedName())))
            .nameChangedHowOtherDetails(ocrDataFields.getRespondentOrApplicant2WhyMarriedNameChanged())
            .solicitorRepresented(from(isApp2SolicitorRepresented))
            .homeAddress(
                AddressGlobalUK
                    .builder()
                    .addressLine1(ocrDataFields.getRespondentOrApplicant2BuildingAndStreet())
                    .addressLine2(ocrDataFields.getRespondentOrApplicant2SecondLineOfAddress())
                    .postTown(ocrDataFields.getRespondentOrApplicant2TownOrCity())
                    .county(ocrDataFields.getRespondentOrApplicant2County())
                    .country(ocrDataFields.getRespondentOrApplicant2Country())// ste country to UK if not overseas

                    .postCode(ocrDataFields.getRespondentOrApplicant2Postcode())
                    .build()
            )
            .phoneNumber(ocrDataFields.getRespondentOrApplicant2PhoneNo())
            .email(ocrDataFields.getRespondentOrApplicant2Email())
            .build();
    }

    private MarriageDetails marriageDetails(String translation) {
        boolean isMarriageCertTranslationSubmitted = toBoolean(translation);
        return MarriageDetails
            .builder()
            .certificateInEnglish(from(!isMarriageCertTranslationSubmitted))
            .certifiedTranslation(from(isMarriageCertTranslationSubmitted))
            .build();
    }

    private ApplicationType getApplicationType(OcrDataFields ocrDataFields, List<String> errors) {
        boolean isSole = toBoolean(ocrDataFields.getSoleApplication());
        boolean isJoint = toBoolean(ocrDataFields.getJointApplication());
        if (isJoint && !isSole) {
            return JOINT_APPLICATION;
        } else if (isSole && !isJoint) {
            return SOLE_APPLICATION;
        } else {
            errors.add("Both sole and joint application selected. OCR fields aSoleApplication and aJointApplication");
        }
        return null;
    }

    private DivorceOrDissolution getDivorceType(OcrDataFields ocrDataFields, List<String> errors) {
        boolean isApplicationForDivorce = toBoolean(ocrDataFields.getApplicationForDivorce());
        boolean isApplicationForDissolution = toBoolean(ocrDataFields.getApplicationForDissolution());

        if (isApplicationForDissolution && !isApplicationForDivorce) {
            return DISSOLUTION;
        } else if (isApplicationForDivorce && !isApplicationForDissolution) {
            return DIVORCE;
        } else {
            errors.add("Both application type selected. OCR fields applicationForDivorce and applicationForDissolution");
        }
        return null;
    }

    private Applicant applicant1(OcrDataFields ocrDataFields, boolean isApp1SolicitorRepresented) {
        ContactDetailsType contactDetailsType = toBoolean(ocrDataFields.getConfidentialDetailsSpouseOrCivilPartner())
            ? PRIVATE
            : PUBLIC;

        return Applicant
            .builder()
            .firstName(ocrDataFields.getSoleApplicantOrApplicant1FirstName())
            .middleName(ocrDataFields.getSoleApplicantOrApplicant1MiddleName())
            .lastName(ocrDataFields.getSoleApplicantOrApplicant1LastName())
            .nameDifferentToMarriageCertificate(from(toBoolean(ocrDataFields.getSoleOrApplicant1MarriedName())))
            .nameChangedHowOtherDetails(ocrDataFields.getSoleOrApplicant1MarriedNameReason())
            .contactDetailsType(contactDetailsType)
            .homeAddress(
                AddressGlobalUK
                    .builder()
                    .addressLine1(ocrDataFields.getSoleOrApplicant1BuildingAndStreet())
                    .addressLine2(ocrDataFields.getSoleOrApplicant1SecondLineOfAddress())
                    .postTown(ocrDataFields.getSoleOrApplicant1TownOrCity())
                    .county(ocrDataFields.getSoleOrApplicant1County())
                    .country(ocrDataFields.getSoleOrApplicant1Country())
                    .postCode(ocrDataFields.getSoleOrApplicant1Postcode())
                    .build()
            )
            .phoneNumber(ocrDataFields.getSoleOrApplicant1PhoneNo())
            .email(ocrDataFields.getSoleOrApplicant1Email())
            .solicitorRepresented(from(isApp1SolicitorRepresented))
            .build();
    }

    private Solicitor solicitor1(OcrDataFields ocrDataFields) {
        String app1SolicitorAddress = Stream.of(
                ocrDataFields.getSoleOrApplicant1SolicitorBuildingAndStreet(),
                ocrDataFields.getSoleOrApplicant1SolicitorSecondLineOfAddress(),
                ocrDataFields.getSoleOrApplicant1SolicitorTownOrCity(),
                ocrDataFields.getSoleOrApplicant1SolicitorCounty(),
                ocrDataFields.getSoleOrApplicant1SolicitorCountry(),
                ocrDataFields.getSoleOrApplicant1SolicitorPostcode(),
                ocrDataFields.getSoleOrApplicant1SolicitorDX()
            )
            .filter(value -> value != null && !value.isEmpty())
            .collect(Collectors.joining("\n"));

        return Solicitor
            .builder()
            .name(ocrDataFields.getSoleOrApplicant1SolicitorName())
            .reference(ocrDataFields.getSoleOrApplicant1SolicitorRefNo())
            .firmName(ocrDataFields.getSoleOrApplicant1SolicitorFirm())
            .address(app1SolicitorAddress)
            .phone(ocrDataFields.getSoleOrApplicant1SolicitorPhoneNo())
            .email(ocrDataFields.getSoleOrApplicant1SolicitorEmail())
            .build();
    }

    private Solicitor solicitor2(OcrDataFields ocrDataFields) {
        String app2SolicitorAddress = Stream.of(
                ocrDataFields.getRespondentOrApplicant2BuildingAndStreet(),
                ocrDataFields.getRespondentOrApplicant2SecondLineOfAddress(),
                ocrDataFields.getRespondentOrApplicant2TownOrCity(),
                ocrDataFields.getRespondentOrApplicant2County(),
                ocrDataFields.getRespondentOrApplicant2Country(),
                ocrDataFields.getRespondentOrApplicant2Postcode(),
                ocrDataFields.getRespondentOrApplicant2SolicitorDX()
            )
            .filter(value -> value != null && !value.isEmpty())
            .collect(Collectors.joining("\n"));

        return Solicitor
            .builder()
            .name(ocrDataFields.getRespondentOrApplicant2SolicitorName())
            .reference(ocrDataFields.getRespondentOrApplicant2SolicitorRefNo())
            .firmName(ocrDataFields.getRespondentOrApplicant2SolicitorFirm())
            .address(app2SolicitorAddress)
            .phone(ocrDataFields.getApplicant2SolicitorPhone())
            .build();
    }

}
