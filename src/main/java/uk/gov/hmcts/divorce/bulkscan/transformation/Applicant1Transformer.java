package uk.gov.hmcts.divorce.bulkscan.transformation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.join;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.from;
import static uk.gov.hmcts.divorce.bulkscan.transformation.CommonFormToCaseTransformer.OCR_FIELD_VALUE_BOTH;
import static uk.gov.hmcts.divorce.bulkscan.transformation.CommonFormToCaseTransformer.OCR_FIELD_VALUE_NO;
import static uk.gov.hmcts.divorce.bulkscan.transformation.CommonFormToCaseTransformer.OCR_FIELD_VALUE_YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PRIVATE;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PUBLIC;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.APPLICANT;

@Component
public class Applicant1Transformer implements Function<TransformationDetails, TransformationDetails> {

    private static final String MYSELF = "myself";
    private static final String MYSELF_CHILDREN = "myself,children";
    private static final String CHILDREN = "children";

    @Override
    public TransformationDetails apply(TransformationDetails transformationDetails) {
        CaseData caseData = transformationDetails.getCaseData();
        OcrDataFields ocrDataFields = transformationDetails.getOcrDataFields();
        final var isApp1SolicitorRepresented = deriveSolicitorRepresented(
            ocrDataFields.getSoleOrApplicant1Solicitor(),
            transformationDetails.getTransformationWarnings()
        );
        caseData.setApplicant1(applicant1(ocrDataFields, isApp1SolicitorRepresented, transformationDetails.getTransformationWarnings()));

        setApp1FinancialOrders(transformationDetails);
        setLegalProceedings(transformationDetails);
        applicant1StatementOfTruth(ocrDataFields, caseData);
        return transformationDetails;
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
            || OCR_FIELD_VALUE_BOTH.equalsIgnoreCase(soleOrApplicant1MarriedName)) {
            nameDifferentToMarriageCertificate = NO;
        } else if (OCR_FIELD_VALUE_NO.equalsIgnoreCase(soleOrApplicant1MarriedName)) {
            nameDifferentToMarriageCertificate = YES;
        } else {
            warnings.add("Please review applicant1 name different to marriage certificate in the scanned form");
        }

        var applicant = Applicant
            .builder()
            .firstName(ocrDataFields.getSoleApplicantOrApplicant1FirstName())
            .middleName(ocrDataFields.getSoleApplicantOrApplicant1MiddleName())
            .lastName(ocrDataFields.getSoleApplicantOrApplicant1LastName())
            .email(ocrDataFields.getSoleOrApplicant1Email())
            .nameDifferentToMarriageCertificate(nameDifferentToMarriageCertificate)
            .nameDifferentToMarriageCertificateOtherDetails(ocrDataFields.getSoleOrApplicant1MarriedNameReason())
            .contactDetailsType(contactDetailsType)
            .address(
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
            .solicitorRepresented(from(isApp1SolicitorRepresented))
            .build();

        if (isApp1SolicitorRepresented) {
            if (isEmpty(ocrDataFields.getSoleOrApplicant1SolicitorName())) {
                warnings.add("Please review applicant1 solicitor name in the scanned form");
            }
            if (isEmpty(ocrDataFields.getSoleOrApplicant1SolicitorFirm())) {
                warnings.add("Please review applicant1 solicitor firm in the scanned form");
            }
            if (isEmpty(ocrDataFields.getSoleOrApplicant1BuildingAndStreet())) {
                warnings.add("Please review applicant1 solicitor building and street in the scanned form");
            }
            applicant.setSolicitor(solicitor1(ocrDataFields));
        }
        return applicant;
    }

    private boolean deriveSolicitorRepresented(String soleOrApplicant1Solicitor, List<String> warnings) {
        if (OCR_FIELD_VALUE_YES.equalsIgnoreCase(soleOrApplicant1Solicitor)
            || OCR_FIELD_VALUE_BOTH.equalsIgnoreCase(soleOrApplicant1Solicitor)) {
            return true;
        } else if (OCR_FIELD_VALUE_NO.equalsIgnoreCase(soleOrApplicant1Solicitor)) {
            return false;
        } else {
            warnings.add("Please review applicant1 solicitor details in the scanned form");
            return false;
        }
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

    private void setLegalProceedings(TransformationDetails transformationDetails) {
        OcrDataFields ocrDataFields = transformationDetails.getOcrDataFields();
        CaseData caseData = transformationDetails.getCaseData();

        if (OCR_FIELD_VALUE_YES.equalsIgnoreCase(ocrDataFields.getExistingOrPreviousCourtCases())) {
            if (isEmpty(ocrDataFields.getExistingOrPreviousCourtCaseNumbers())
                && isEmpty(ocrDataFields.getSummaryOfExistingOrPreviousCourtCases())) {
                transformationDetails.getTransformationWarnings().add("Please review existing or previous court cases in the scanned form");
            }
            caseData.getApplicant1().setLegalProceedings(YES);
        } else if (OCR_FIELD_VALUE_NO.equalsIgnoreCase(ocrDataFields.getExistingOrPreviousCourtCases())) {
            caseData.getApplicant1().setLegalProceedings(NO);
        } else {
            transformationDetails.getTransformationWarnings().add("Please review existing or previous court cases in the scanned form");
        }

        caseData.getApplicant1().setLegalProceedingsDetails(
            join("Case Number(s):",
                ocrDataFields.getExistingOrPreviousCourtCaseNumbers(),
                ocrDataFields.getSummaryOfExistingOrPreviousCourtCases()
            )
        );
    }

    private void setApp1FinancialOrders(TransformationDetails transformationDetails) {
        OcrDataFields ocrDataFields = transformationDetails.getOcrDataFields();
        CaseData caseData = transformationDetails.getCaseData();
        Set<FinancialOrderFor> app1FinancialOrderFor =
            deriveFinancialOrderFor(ocrDataFields.getSoleOrApplicant1FinancialOrderFor());

        if (OCR_FIELD_VALUE_YES.equalsIgnoreCase(ocrDataFields.getSoleOrApplicant1FinancialOrder())
            || OCR_FIELD_VALUE_BOTH.equalsIgnoreCase(ocrDataFields.getSoleOrApplicant1FinancialOrder())) {
            caseData.getApplicant1().setFinancialOrder(YES);

            if (isEmpty(app1FinancialOrderFor)) {
                transformationDetails.getTransformationWarnings().add("Please review applicant1 financial order for in scanned form");
            }

        } else {
            caseData.getApplicant1().setFinancialOrder(NO);
            if (!isEmpty(app1FinancialOrderFor)) {
                transformationDetails.getTransformationWarnings().add("Please review applicant1 financial order for in scanned form");
            }
        }

        if (OCR_FIELD_VALUE_YES.equalsIgnoreCase(ocrDataFields.getSoleOrApplicant1FinancialOrder())
            && isEmpty(ocrDataFields.getSoleOrApplicant1prayerFinancialOrder())) {
            transformationDetails.getTransformationWarnings().add("Please review applicant1 financial order prayer for in scanned form");
        } else if (OCR_FIELD_VALUE_NO.equalsIgnoreCase(ocrDataFields.getSoleOrApplicant1FinancialOrder())
            && isNotEmpty(ocrDataFields.getSoleOrApplicant1prayerFinancialOrder())) {
            transformationDetails.getTransformationWarnings().add("Please review applicant1 financial order prayer for in scanned form");
        }
        caseData.getApplicant1().setFinancialOrdersFor(app1FinancialOrderFor);
    }

    private void applicant1StatementOfTruth(OcrDataFields ocrDataFields, CaseData caseData) {
        caseData.getApplication().setApplicant1StatementOfTruth(
            from(toBoolean(ocrDataFields.getSoleApplicantOrApplicant1StatementOfTruth()))
        );
        caseData.getApplication().setSolSignStatementOfTruth(
            from(toBoolean(ocrDataFields.getSoleApplicantOrApplicant1LegalRepStatementOfTruth()))
        );

        caseData.getApplication().setSolStatementOfReconciliationName(ocrDataFields.getSoleApplicantOrApplicant1OrLegalRepFullName());
        caseData.getApplication().setSolStatementOfReconciliationFirm(ocrDataFields.getSoleApplicantOrApplicant1LegalRepFirm());
    }


    public Set<FinancialOrderFor> deriveFinancialOrderFor(String financialOrderFor) {
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
}
