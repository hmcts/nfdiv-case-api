package uk.gov.hmcts.divorce.bulkscan.transformation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.bulkscan.validation.OcrValidator;
import uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.endpoint.data.OcrValidationResponse;
import uk.gov.hmcts.reform.bsp.common.error.InvalidDataException;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.service.transformation.BulkScanFormTransformer;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

import static java.util.Objects.isNull;
import static org.apache.commons.collections4.ListUtils.union;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.join;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.from;
import static uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields.transformOcrMapToObject;
import static uk.gov.hmcts.divorce.divorcecase.model.Application.ThePrayer.I_CONFIRM;
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
import static uk.gov.hmcts.divorce.divorcecase.model.MarriageFormation.OPPOSITE_SEX_COUPLE;
import static uk.gov.hmcts.divorce.divorcecase.model.MarriageFormation.SAME_SEX_COUPLE;
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
    private static final int HWF_NO_VALID_LENGTH = 6;
    private static final String OCR_FIELD_VALUE_YES = "yes";
    private static final String OCR_FIELD_VALUE_NO = "no";
    private static final String OCR_FIELD_VALUE_BOTH = "both";
    public static final String TRANSFORMATION_AND_OCR_WARNINGS = "transformationAndOcrWarnings";

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private OcrValidator validator;

    @Autowired
    private Clock clock;

    @Override
    protected Map<String, String> getOcrToCCDMapping() {
        return Collections.emptyMap();
    }

    @Override
    protected Map<String, Object> runFormSpecificTransformation(List<OcrDataField> ocrDataFieldList) {
        OcrDataFields ocrDataFields = transformOcrMapToObject(ocrDataFieldList);

        OcrValidationResponse ocrValidationResponse = validator.validateOcrData(D8.getName(), ocrDataFields);

        if (!CollectionUtils.isEmpty(ocrValidationResponse.getErrors())) {
            throw new InvalidDataException(
                "Error during D8 transformation",
                ocrValidationResponse.getWarnings(),
                ocrValidationResponse.getErrors()
            );
        }
        var caseData = CaseData.builder().build();

        List<String> transformationWarnings = new ArrayList<>();

        try {
            // Section 1 – Your application
            caseData.setDivorceOrDissolution(getDivorceType(ocrDataFields, transformationWarnings));
            caseData.setApplicationType(getApplicationType(ocrDataFields, transformationWarnings));

            marriageCertificateDetails(ocrDataFields, caseData, transformationWarnings);

            // Section 2 – About you(the sole applicant or applicant 1)
            final var isApp1SolicitorRepresented =
                isApp1SolicitorRepresented(ocrDataFields.getSoleOrApplicant1Solicitor(), transformationWarnings);
            caseData.setApplicant1(applicant1(ocrDataFields, isApp1SolicitorRepresented, transformationWarnings));

            // Section 3 – About the respondent or applicant 2
            caseData.setApplicant2(applicant2(ocrDataFields, transformationWarnings));
            verifyRespondentEmailAccess(ocrDataFields, transformationWarnings);

            caseData.getPaperFormDetails().setServiceOutsideUK(ocrDataFields.getServeOutOfUK());
            verifyServeOutOfUK(caseData.getApplicationType(), ocrDataFields, transformationWarnings);

            caseData.getApplicant2().setOffline(from(toBoolean(ocrDataFields.getRespondentServePostOnly())));
            caseData.getPaperFormDetails().setApplicantWillServeApplication(
                from(toBoolean(ocrDataFields.getApplicantWillServeApplication()))
            );
            caseData.getPaperFormDetails().setRespondentDifferentServiceAddress(
                from(toBoolean(ocrDataFields.getRespondentDifferentServiceAddress()))
            );
            verifyHowApplicationIsServed(caseData.getApplicationType(), ocrDataFields, transformationWarnings);

            // Section 4 – Details of marriage/civil partnership
            setMarriageOrCivilPartnershipDetails(ocrDataFields, caseData, transformationWarnings);

            // Section 5 – Why this court can deal with your case (Jurisdiction)
            caseData.getApplication().getJurisdiction().setConnections(
                deriveJurisdictionConnections(ocrDataFields, transformationWarnings)
            );

            // Section 6 – Statement of irretrievable breakdown (the legal reason for your divorce or dissolution)
            setMarriageBrokenDetails(ocrDataFields, caseData, transformationWarnings);

            // Section 7 – Existing or previous court cases
            setLegalProceedings(ocrDataFields, caseData, transformationWarnings);

            // Section 8 – Dividing your money and property – Orders which are sought
            setApp1FinancialOrders(ocrDataFields, caseData, transformationWarnings);
            setApp2FinancialOrders(ocrDataFields, caseData, transformationWarnings);

            // Section 9 – Summary of what is being applied for (the prayer)
            setPrayer(ocrDataFields, caseData, transformationWarnings);

            setApp1FinancialOrderSummary(ocrDataFields, caseData, transformationWarnings);
            setApp2FinancialOrderSummary(ocrDataFields, caseData, transformationWarnings);

            // Section 10 – Statement of truth
            applicant1StatementOfTruth(ocrDataFields, caseData, transformationWarnings);
            applicant2StatementOfTruth(ocrDataFields, caseData, transformationWarnings);

            // Court fee
            setCourtFee(ocrDataFields, caseData, transformationWarnings);

            // Set label content
            caseData.getLabelContent().setApplicationType(caseData.getApplicationType());
            caseData.getLabelContent().setUnionType(caseData.getDivorceOrDissolution());

            // Set gender
            caseData.getApplication().getMarriageDetails().setFormationType(
                toBoolean(ocrDataFields.getJurisdictionReasonsSameSex()) ? SAME_SEX_COUPLE : OPPOSITE_SEX_COUPLE
            );
            caseData.deriveAndPopulateApplicantGenderDetails();

            //Set application submitted date
            caseData.getApplication().setDateSubmitted(LocalDateTime.now(clock));

            Map<String, Object> transformedCaseData = mapper.convertValue(caseData, new TypeReference<>() {
            });

            transformedCaseData.put(TRANSFORMATION_AND_OCR_WARNINGS, union(ocrValidationResponse.getWarnings(), transformationWarnings));

            return transformedCaseData;

        } catch (Exception exception) {
            //this will result in bulk scan service to create exception record if case creation is automatic case creation
            // In case of caseworker triggering the event it will result into error/transformationWarnings shown on the UI
            log.error("Exception occurred while transforming D8 form with error", exception);
            throw new InvalidDataException(
                exception.getMessage(),
                union(ocrValidationResponse.getWarnings(), transformationWarnings),
                null
            );
        }
    }

    private void setCourtFee(OcrDataFields ocrDataFields, CaseData caseData, List<String> warnings) {
        if (isNotEmpty(ocrDataFields.getSoleOrApplicant1HWFNo())
            && ocrDataFields.getSoleOrApplicant1HWFNo().length() != HWF_NO_VALID_LENGTH) {
            warnings.add("Please review HWF number for applicant1 in scanned form");
        }

        if (isNotEmpty(ocrDataFields.getApplicant2HWFConfirmationNo())
            && ocrDataFields.getApplicant2HWFConfirmationNo().length() != HWF_NO_VALID_LENGTH) {
            warnings.add("Please review HWF number for applicant2 in scanned form");
        }

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
    }

    private void applicant2StatementOfTruth(OcrDataFields ocrDataFields, CaseData caseData, List<String> warnings) {
        if (!toBoolean(ocrDataFields.getApplicant2StatementOfTruth())
            && !toBoolean(ocrDataFields.getApplicant2LegalRepStatementOfTruth())) {
            warnings.add("Please review statement of truth for applicant1 in scanned form");
        }
        if (!toBoolean(ocrDataFields.getApplicant2Signing())
            && !toBoolean(ocrDataFields.getApplicant2LegalRepSigning())) {
            warnings.add("Please review statement of truth for applicant1 in scanned form");
        }
        if (isEmpty(ocrDataFields.getApplicant2OrLegalRepFullName())) {
            warnings.add("Please sole or applicant2/legal representative name in scanned form");
        }
        caseData.getApplication().setApplicant2StatementOfTruth(
            from(toBoolean(ocrDataFields.getApplicant2StatementOfTruth()))
        );
        caseData.getApplication().setApplicant2SolSignStatementOfTruth(
            from(toBoolean(ocrDataFields.getApplicant2LegalRepStatementOfTruth()))
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

        String day = ocrDataFields.getApplicant2StatementOfTruthDateDay();
        String month = ocrDataFields.getApplicant2StatementOfTruthDateMonth();
        String year = ocrDataFields.getApplicant2StatementOfTruthDateYear();
        if (isEmpty(day) || isEmpty(month) || isEmpty(year)) {
            warnings.add("Please review statement of truth date for applicant1 in scanned form");
        }
        caseData.getPaperFormDetails().setApplicant1SOTSignedOn(
            deriveStatementOfTruthDate(day, month, year)
        );

        caseData.getPaperFormDetails().setApplicant2SOTSignedOn(
            deriveStatementOfTruthDate(day, month, year)
        );
    }

    private void applicant1StatementOfTruth(OcrDataFields ocrDataFields, CaseData caseData, List<String> warnings) {
        if (!toBoolean(ocrDataFields.getSoleApplicantOrApplicant1StatementOfTruth())
            && !toBoolean(ocrDataFields.getSoleApplicantOrApplicant1LegalRepStatementOfTruth())) {
            warnings.add("Please review statement of truth for applicant1 in scanned form");
        }
        if (!toBoolean(ocrDataFields.getSoleApplicantOrApplicant1Signing())
            && !toBoolean(ocrDataFields.getSoleApplicantOrApplicant1OrLegalRepSignature())) {
            warnings.add("Please review statement of truth for applicant1 in scanned form");
        }
        if (isEmpty(ocrDataFields.getSoleApplicantOrApplicant1OrLegalRepFullName())) {
            warnings.add("Please sole or applicant1/legal representative name in scanned form");
        }
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

        String day = ocrDataFields.getStatementOfTruthDateDay();
        String month = ocrDataFields.getStatementOfTruthDateMonth();
        String year = ocrDataFields.getStatementOfTruthDateYear();
        if (isEmpty(day) || isEmpty(month) || isEmpty(year)) {
            warnings.add("Please review statement of truth date for applicant1 in scanned form");
        }
        caseData.getPaperFormDetails().setApplicant1SOTSignedOn(
            deriveStatementOfTruthDate(day, month, year)
        );
    }

    private void setApp1FinancialOrderSummary(OcrDataFields ocrDataFields, CaseData caseData, List<String> warnings) {
        if (YES.equals(caseData.getApplicant1().getFinancialOrder())
            && isEmpty(ocrDataFields.getSoleOrApplicant1prayerFinancialOrderFor())) {
            warnings.add("Please review financial order summary for applicant1 in scanned form");
        } else if (NO.equals(caseData.getApplicant1().getFinancialOrder())
            && isNotEmpty(ocrDataFields.getSoleOrApplicant1prayerFinancialOrderFor())) {
            warnings.add("Please review financial order summary for applicant1 in scanned form");
        }

        caseData.getPaperFormDetails().setSummaryApplicant1FinancialOrdersFor(
            applicantSummaryFinancialOrderFor(ocrDataFields.getSoleOrApplicant1prayerFinancialOrderFor())
        );
    }

    private void setApp2FinancialOrderSummary(OcrDataFields ocrDataFields, CaseData caseData, List<String> warnings) {
        if (YES.equals(caseData.getApplicant2().getFinancialOrder())
            && isEmpty(ocrDataFields.getApplicant2PrayerFinancialOrderFor())) {
            warnings.add("Please review financial order summary for applicant2 in scanned form");
        } else if (NO.equals(caseData.getApplicant2().getFinancialOrder())
            && isNotEmpty(ocrDataFields.getApplicant2PrayerFinancialOrderFor())) {
            warnings.add("Please review financial order summary for applicant2 in scanned form");
        }

        caseData.getPaperFormDetails().setSummaryApplicant2FinancialOrdersFor(
            applicantSummaryFinancialOrderFor(ocrDataFields.getApplicant2PrayerFinancialOrderFor())
        );
    }

    private void setPrayer(OcrDataFields ocrDataFields, CaseData caseData, List<String> warnings) {
        final var isMarriageDissolved = toBoolean(ocrDataFields.getPrayerMarriageDissolved());
        final var isCivilPartnershipDissolved = toBoolean(ocrDataFields.getPrayerCivilPartnershipDissolved());

        if (DIVORCE.equals(caseData.getDivorceOrDissolution()) && isMarriageDissolved) {
            caseData.getApplication().setApplicant1PrayerHasBeenGivenCheckbox(Set.of(I_CONFIRM));
            caseData.getApplication().setApplicant2PrayerHasBeenGivenCheckbox(Set.of(I_CONFIRM));
        } else if (DISSOLUTION.equals(caseData.getDivorceOrDissolution()) && isCivilPartnershipDissolved) {
            caseData.getApplication().setApplicant1PrayerHasBeenGivenCheckbox(Set.of(I_CONFIRM));
            caseData.getApplication().setApplicant2PrayerHasBeenGivenCheckbox(Set.of(I_CONFIRM));
        } else {
            warnings.add("Please review prayer in the scanned form");
        }
    }

    private void setApp1FinancialOrders(OcrDataFields ocrDataFields, CaseData caseData, List<String> warnings) {
        Set<FinancialOrderFor> app1FinancialOrderFor = deriveFinancialOrderFor(ocrDataFields.getSoleOrApplicant1FinancialOrderFor());

        if (OCR_FIELD_VALUE_YES.equalsIgnoreCase(ocrDataFields.getSoleOrApplicant1FinancialOrder())
            || OCR_FIELD_VALUE_BOTH.equalsIgnoreCase(ocrDataFields.getSoleOrApplicant1FinancialOrder())) {
            caseData.getApplicant1().setFinancialOrder(YES);

            if (CollectionUtils.isEmpty(app1FinancialOrderFor)) {
                warnings.add("Please review applicant1 financial for in scanned form");
            }

        } else {
            caseData.getApplicant1().setFinancialOrder(NO);
            if (!CollectionUtils.isEmpty(app1FinancialOrderFor)) {
                warnings.add("Please review applicant1 financial for in scanned form");
            }
        }
        caseData.getApplicant1().setFinancialOrdersFor(app1FinancialOrderFor);
    }

    private void setApp2FinancialOrders(OcrDataFields ocrDataFields, CaseData caseData, List<String> warnings) {
        Set<FinancialOrderFor> app2FinancialOrderFor = deriveFinancialOrderFor(ocrDataFields.getApplicant2FinancialOrderFor());

        if (OCR_FIELD_VALUE_YES.equalsIgnoreCase(ocrDataFields.getApplicant2FinancialOrderFor())
            || OCR_FIELD_VALUE_BOTH.equalsIgnoreCase(ocrDataFields.getApplicant2FinancialOrderFor())) {
            caseData.getApplicant2().setFinancialOrder(YES);
            if (CollectionUtils.isEmpty(app2FinancialOrderFor)) {
                warnings.add("Please review applicant2 financial for in scanned form");
            }

        } else {
            caseData.getApplicant2().setFinancialOrder(NO);
            if (!CollectionUtils.isEmpty(app2FinancialOrderFor)) {
                warnings.add("Please review applicant2 financial for in scanned form");
            }
        }
        caseData.getApplicant2().setFinancialOrdersFor(app2FinancialOrderFor);
    }

    private void setLegalProceedings(OcrDataFields ocrDataFields, CaseData caseData, List<String> warnings) {
        if (OCR_FIELD_VALUE_YES.equalsIgnoreCase(ocrDataFields.getExistingOrPreviousCourtCases())) {
            if (isEmpty(ocrDataFields.getExistingOrPreviousCourtCaseNumbers())
                && isEmpty(ocrDataFields.getSummaryOfExistingOrPreviousCourtCases())) {
                warnings.add("Please review existing or previous court cases in the scanned form");
            }
            caseData.getApplicant1().setLegalProceedings(YES);
        } else if (OCR_FIELD_VALUE_NO.equalsIgnoreCase(ocrDataFields.getExistingOrPreviousCourtCases())) {
            caseData.getApplicant1().setLegalProceedings(NO);
        } else {
            warnings.add("Please review existing or previous court cases in the scanned form");
        }

        caseData.getApplicant1().setLegalProceedingsDetails(
            join("Case Number(s):",
                ocrDataFields.getExistingOrPreviousCourtCaseNumbers(),
                ocrDataFields.getSummaryOfExistingOrPreviousCourtCases()
            )
        );
    }

    private void setMarriageBrokenDetails(OcrDataFields ocrDataFields, CaseData caseData, List<String> warnings) {

        if (SOLE_APPLICATION.equals(caseData.getApplicationType())
            && (!toBoolean(ocrDataFields.getSoleOrApplicant1ConfirmationOfBreakdown())
            || toBoolean(ocrDataFields.getApplicant2ConfirmationOfBreakdown()))) {
            warnings.add("Please review confirmation of breakdown in the scanned form");
        } else if (JOINT_APPLICATION.equals(caseData.getApplicationType())
            && (!toBoolean(ocrDataFields.getSoleOrApplicant1ConfirmationOfBreakdown())
            || !toBoolean(ocrDataFields.getApplicant2ConfirmationOfBreakdown()))) {
            warnings.add("Please review confirmation of breakdown in the scanned form");
        }
        caseData.getApplication().setApplicant1ScreenHasMarriageBroken(
            from(toBoolean(ocrDataFields.getSoleOrApplicant1ConfirmationOfBreakdown()))
        );
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(
            from(toBoolean(ocrDataFields.getApplicant2ConfirmationOfBreakdown()))
        );
    }

    private void setMarriageOrCivilPartnershipDetails(OcrDataFields ocrDataFields, CaseData caseData, List<String> warnings) {
        if (isEmpty(ocrDataFields.getSoleOrApplicant1FullNameAsOnCert())) {
            warnings.add("Please review applicant1's full name as on marriage cert in the scanned form");
        }
        if (isEmpty(ocrDataFields.getRespondentOrApplicant2FullNameAsOnCert())) {
            warnings.add("Please review respondent/applicant2's full name as on marriage cert in the scanned form");
        }

        caseData.getApplication().getMarriageDetails().setMarriedInUk(
            deriveMarriedInTheUK(ocrDataFields.getMarriageOutsideOfUK(), warnings)
        );
        caseData.getApplication().getMarriageDetails().setIssueApplicationWithoutMarriageCertificate(
            deriveApplicationWithoutCert(ocrDataFields.getMakingAnApplicationWithoutCertificate(), warnings)
        );
        caseData.getApplication().getMarriageDetails().setDate(deriveMarriageDate(ocrDataFields, warnings));
        caseData.getApplication().getMarriageDetails().setApplicant1Name(ocrDataFields.getSoleOrApplicant1FullNameAsOnCert());
        caseData.getApplication().getMarriageDetails().setApplicant2Name(ocrDataFields.getRespondentOrApplicant2FullNameAsOnCert());
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage(setPlaceOfMarriageOrCivilPartnership(ocrDataFields, warnings));

        if (OCR_FIELD_VALUE_YES.equalsIgnoreCase(ocrDataFields.getDetailsOnCertCorrect())) {
            caseData.getApplication().getMarriageDetails().setCertifyMarriageCertificateIsCorrect(YES);
        } else if (OCR_FIELD_VALUE_NO.equalsIgnoreCase(ocrDataFields.getDetailsOnCertCorrect())) {
            caseData.getApplication().getMarriageDetails().setCertifyMarriageCertificateIsCorrect(NO);
        } else {
            warnings.add("Please review marriage certificate details is correct in the scanned form");
        }

        if (OCR_FIELD_VALUE_NO.equalsIgnoreCase(ocrDataFields.getDetailsOnCertCorrect())
            && isEmpty(ocrDataFields.getReasonWhyCertNotCorrect())) {
            warnings.add("Please review reasons why cert not correct in the scanned form");
        }
        caseData.getApplication().getMarriageDetails().setMarriageCertificateIsIncorrectDetails(ocrDataFields.getReasonWhyCertNotCorrect());
    }

    private String setPlaceOfMarriageOrCivilPartnership(OcrDataFields ocrDataFields, List<String> warnings) {
        if (isEmpty(ocrDataFields.getPlaceOfMarriageOrCivilPartnership())
            && (OCR_FIELD_VALUE_YES.equalsIgnoreCase(ocrDataFields.getMarriageOutsideOfUK())
            || OCR_FIELD_VALUE_YES.equalsIgnoreCase(ocrDataFields.getMakingAnApplicationWithoutCertificate()))) {
            warnings.add("Please review making an application with marriage certificate in the scanned form");
            return ocrDataFields.getPlaceOfMarriageOrCivilPartnership();
        }
        return ocrDataFields.getPlaceOfMarriageOrCivilPartnership();
    }


    private YesOrNo deriveApplicationWithoutCert(String makingAnApplicationWithoutCertificate, List<String> warnings) {
        if (OCR_FIELD_VALUE_YES.equalsIgnoreCase(makingAnApplicationWithoutCertificate)
            || OCR_FIELD_VALUE_BOTH.equalsIgnoreCase(makingAnApplicationWithoutCertificate)) {
            warnings.add("Please review making an application with marriage certificate in the scanned form");
            return YES;
        } else if (OCR_FIELD_VALUE_NO.equalsIgnoreCase(makingAnApplicationWithoutCertificate)) {
            return NO;
        } else {
            warnings.add("Please review making an application with marriage certificate in the scanned form");
            return null;
        }
    }

    private YesOrNo deriveMarriedInTheUK(String marriageOutsideOfUK, List<String> warnings) {
        if (OCR_FIELD_VALUE_NO.equalsIgnoreCase(marriageOutsideOfUK)) {
            return YES;
        } else if (OCR_FIELD_VALUE_YES.equalsIgnoreCase(marriageOutsideOfUK)) {
            return NO;
        } else {
            warnings.add("Please review applicant1 name different to marriage certificate in the scanned form");
            return null;
        }
    }

    private void verifyHowApplicationIsServed(ApplicationType applicationType, OcrDataFields ocrDataFields, List<String> warnings) {
        if (SOLE_APPLICATION.equals(applicationType)) {
            if (isEmpty(ocrDataFields.getRespondentServePostOnly()) || isEmpty(ocrDataFields.getApplicantWillServeApplication())) {
                warnings.add("Please review respondent by post and applicant will serve application in the scanned form");
            }
            if (isEmpty(ocrDataFields.getRespondentDifferentServiceAddress())) {
                warnings.add("Please review respondent address different to service address in the scanned form");
            }
        }
    }

    private void verifyServeOutOfUK(ApplicationType applicationType, OcrDataFields ocrDataFields, List<String> warnings) {
        if (SOLE_APPLICATION.equals(applicationType)
            && (OCR_FIELD_VALUE_BOTH.equalsIgnoreCase(ocrDataFields.getServeOutOfUK()) || isEmpty(ocrDataFields.getServeOutOfUK()))) {
            warnings.add("Please review serve out of UK in the scanned form");
        }
    }

    private void verifyRespondentEmailAccess(OcrDataFields ocrDataFields, List<String> warnings) {
        if (isNotEmpty(ocrDataFields.getRespondentOrApplicant2Email()) && isEmpty(ocrDataFields.getRespondentEmailAccess())) {
            warnings.add("Please verify respondent email access in scanned form");
        }
    }

    private boolean isApp1SolicitorRepresented(String soleOrApplicant1Solicitor, List<String> warnings) {
        if (OCR_FIELD_VALUE_YES.equalsIgnoreCase(soleOrApplicant1Solicitor)) {
            return true;
        } else if (OCR_FIELD_VALUE_BOTH.equalsIgnoreCase(soleOrApplicant1Solicitor)) {
            return true;
        } else if (OCR_FIELD_VALUE_NO.equalsIgnoreCase(soleOrApplicant1Solicitor)) {
            return false;
        } else {
            warnings.add("Please review applicant1 name different to marriage certificate in the scanned form");
            return false;
        }
    }

    private void marriageCertificateDetails(OcrDataFields ocrDataFields, CaseData caseData, List<String> warnings) {
        boolean marriageOrCivilPartnershipCert = toBoolean(ocrDataFields.getMarriageOrCivilPartnershipCertificate());
        boolean translation = toBoolean(ocrDataFields.getTranslation());

        if (marriageOrCivilPartnershipCert && !translation) {
            caseData.getApplication().getMarriageDetails().setCertificateInEnglish(YES);
        } else if (!marriageOrCivilPartnershipCert && translation) {
            caseData.getApplication().getMarriageDetails().setCertifiedTranslation(YES);
        } else {
            warnings.add("Please review marriage certificate/translation in the scanned form");
        }

        caseData.getApplication().setScreenHasMarriageCert(from(marriageOrCivilPartnershipCert));
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

    private Set<JurisdictionConnections> deriveJurisdictionConnections(OcrDataFields ocrDataFields, List<String> warnings) {
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
                warnings.add("Please verify jurisdiction connections in scanned form");
            }
        } else if (toBoolean(ocrDataFields.getJurisdictionReasonsSameSex())) {
            // only for civil partnership/same-sex
            connections.add(RESIDUAL_JURISDICTION);
        }

        if (CollectionUtils.isEmpty(connections)) {
            warnings.add("Please verify jurisdiction connections in scanned form");
        }
        return connections;
    }

    private String deriveStatementOfTruthDate(String day, String month, String year) {
        return Stream.of(day, month, year)
            .filter(s -> s != null && !s.isEmpty())
            .collect(Collectors.joining(" "));
    }

    private LocalDate deriveMarriageDate(OcrDataFields ocrDataFields, List<String> warnings) {
        try {
            String marriageMonth = ocrDataFields.getDateOfMarriageOrCivilPartnershipMonth();
            int dayParsed = Integer.parseInt(ocrDataFields.getDateOfMarriageOrCivilPartnershipDay()); // format "18"
            int monthParsed = Month.valueOf(marriageMonth.toUpperCase(Locale.UK)).getValue(); //format "January"
            int yearParsed = Integer.parseInt(ocrDataFields.getDateOfMarriageOrCivilPartnershipYear()); // format "2022"
            return LocalDate.of(yearParsed, dayParsed, monthParsed);
        } catch (DateTimeException exception) {
            // log and add validation it as will be corrected manually the caseworker
            warnings.add("Please review marriage date in the scanned form");
        }
        return null;
    }

    private Applicant applicant2(OcrDataFields ocrDataFields, List<String> warnings) {

        if (isEmpty(ocrDataFields.getRespondentOrApplicant2FirstName())) {
            warnings.add("Please review respondent/applicant2 first name");
        }
        if (isEmpty(ocrDataFields.getRespondentOrApplicant2LastName())) {
            warnings.add("Please review respondent/applicant2 last name");
        }

        String respondentOrApplicant2MarriedName = ocrDataFields.getRespondentOrApplicant2MarriedName();
        YesOrNo nameDifferentToMarriageCertificate = null;
        if (OCR_FIELD_VALUE_YES.equalsIgnoreCase(respondentOrApplicant2MarriedName)
            || OCR_FIELD_VALUE_NO.equalsIgnoreCase(respondentOrApplicant2MarriedName)) {
            nameDifferentToMarriageCertificate = YesOrNo.valueOf(respondentOrApplicant2MarriedName);
        } else if (OCR_FIELD_VALUE_BOTH.equalsIgnoreCase(respondentOrApplicant2MarriedName)) {
            nameDifferentToMarriageCertificate = YES;
        } else {
            warnings.add("Please review respondent/applicant2 name different to marriage certificate in the scanned form");
        }

        var applicant2 = Applicant
            .builder()
            .firstName(ocrDataFields.getRespondentOrApplicant2FirstName())
            .middleName(ocrDataFields.getRespondentOrApplicant2MiddleName())
            .lastName(ocrDataFields.getRespondentOrApplicant2LastName())
            .nameDifferentToMarriageCertificate(nameDifferentToMarriageCertificate)
            .nameChangedHowOtherDetails(ocrDataFields.getRespondentOrApplicant2WhyMarriedNameChanged())
            .solicitorRepresented(from(!isNull(ocrDataFields.getRespondentOrApplicant2SolicitorName())))
            .homeAddress(
                AddressGlobalUK
                    .builder()
                    .addressLine1(ocrDataFields.getRespondentOrApplicant2BuildingAndStreet())
                    .addressLine2(ocrDataFields.getRespondentOrApplicant2SecondLineOfAddress())
                    .postTown(ocrDataFields.getRespondentOrApplicant2TownOrCity())
                    .county(ocrDataFields.getRespondentOrApplicant2County())
                    // set country to UK if not overseas
                    .country(toBoolean(ocrDataFields.getServeOutOfUK()) ? "UK" : ocrDataFields.getRespondentOrApplicant2Country())
                    .postCode(ocrDataFields.getRespondentOrApplicant2Postcode())
                    .build()
            )
            .phoneNumber(ocrDataFields.getRespondentOrApplicant2PhoneNo())
            .email(ocrDataFields.getRespondentOrApplicant2Email())
            .build();

        applicant2.setSolicitor(solicitor2(ocrDataFields));

        return applicant2;
    }

    private ApplicationType getApplicationType(OcrDataFields ocrDataFields, List<String> warnings) {
        boolean isSole = toBoolean(ocrDataFields.getSoleApplication());
        boolean isJoint = toBoolean(ocrDataFields.getJointApplication());
        if (isJoint && !isSole) {
            return JOINT_APPLICATION;
        } else if (isSole && !isJoint) {
            return SOLE_APPLICATION;
        } else {
            warnings.add("Please review application type in the scanned form");
            return SOLE_APPLICATION;
        }
    }

    private DivorceOrDissolution getDivorceType(OcrDataFields ocrDataFields, List<String> warnings) {
        boolean isApplicationForDivorce = toBoolean(ocrDataFields.getApplicationForDivorce());
        boolean isApplicationForDissolution = toBoolean(ocrDataFields.getApplicationForDissolution());

        if (isApplicationForDissolution && !isApplicationForDivorce) {
            return DISSOLUTION;
        } else if (isApplicationForDivorce && !isApplicationForDissolution) {
            return DIVORCE;
        } else {
            warnings.add("Please review divorce type in the scanned form");
            return DIVORCE;
        }
    }

    private Applicant applicant1(OcrDataFields ocrDataFields, boolean isApp1SolicitorRepresented, List<String> warnings) {
        String confidentialDetailsSpouseOrCivilPartner = ocrDataFields.getConfidentialDetailsSpouseOrCivilPartner();

        ContactDetailsType contactDetailsType = null;
        if (OCR_FIELD_VALUE_YES.equalsIgnoreCase(confidentialDetailsSpouseOrCivilPartner)
            || OCR_FIELD_VALUE_BOTH.equalsIgnoreCase(confidentialDetailsSpouseOrCivilPartner)) {
            contactDetailsType = PRIVATE;
        } else if (OCR_FIELD_VALUE_NO.equalsIgnoreCase(confidentialDetailsSpouseOrCivilPartner)
            || isNull(confidentialDetailsSpouseOrCivilPartner)) {
            contactDetailsType = PUBLIC;
        }

        String soleOrApplicant1MarriedName = ocrDataFields.getSoleOrApplicant1MarriedName();
        YesOrNo nameDifferentToMarriageCertificate = null;
        if (OCR_FIELD_VALUE_YES.equalsIgnoreCase(soleOrApplicant1MarriedName)
            || OCR_FIELD_VALUE_NO.equalsIgnoreCase(soleOrApplicant1MarriedName)) {
            nameDifferentToMarriageCertificate = YesOrNo.valueOf(soleOrApplicant1MarriedName);
        } else if (OCR_FIELD_VALUE_BOTH.equalsIgnoreCase(soleOrApplicant1MarriedName)) {
            nameDifferentToMarriageCertificate = YES;
        } else {
            warnings.add("Please review applicant1 name different to marriage certificate in the scanned form");
        }

        var applicant = Applicant
            .builder()
            .firstName(ocrDataFields.getSoleApplicantOrApplicant1FirstName())
            .middleName(ocrDataFields.getSoleApplicantOrApplicant1MiddleName())
            .lastName(ocrDataFields.getSoleApplicantOrApplicant1LastName())
            .nameDifferentToMarriageCertificate(nameDifferentToMarriageCertificate)
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

        if (isApp1SolicitorRepresented) {
            if (isNull(ocrDataFields.getSoleOrApplicant1SolicitorName())) {
                warnings.add("Please review applicant1 solicitor name in the scanned form");
            } else if (isNull(ocrDataFields.getSoleOrApplicant1SolicitorFirm())) {
                warnings.add("Please review applicant1 solicitor firm in the scanned form");
            } else if (isNull(ocrDataFields.getSoleOrApplicant1BuildingAndStreet())) {
                warnings.add("Please review applicant1 solicitor building and street in the scanned form");
            }
            applicant.setSolicitor(solicitor1(ocrDataFields));
        }
        return applicant;
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
