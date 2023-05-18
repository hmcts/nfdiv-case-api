package uk.gov.hmcts.divorce.bulkscan.transformation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.from;
import static uk.gov.hmcts.divorce.bulkscan.transformation.CommonFormToCaseTransformer.OCR_FIELD_VALUE_BOTH;
import static uk.gov.hmcts.divorce.bulkscan.transformation.CommonFormToCaseTransformer.OCR_FIELD_VALUE_NO;
import static uk.gov.hmcts.divorce.bulkscan.transformation.CommonFormToCaseTransformer.OCR_FIELD_VALUE_YES;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.APPLICANT;

@Component
public class Applicant2Transformer implements Function<TransformationDetails, TransformationDetails> {

    private static final String MYSELF = "myself";
    private static final String MYSELF_CHILDREN = "myself,children";
    private static final String CHILDREN = "children";

    @Override
    public TransformationDetails apply(TransformationDetails transformationDetails) {
        CaseData caseData = transformationDetails.getCaseData();
        OcrDataFields ocrDataFields = transformationDetails.getOcrDataFields();
        caseData.setApplicant2(applicant2(ocrDataFields, transformationDetails.getTransformationWarnings()));
        setApp2FinancialOrders(transformationDetails);
        applicant2StatementOfTruth(ocrDataFields, caseData);
        return transformationDetails;
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
            || OCR_FIELD_VALUE_BOTH.equalsIgnoreCase(respondentOrApplicant2MarriedName)) {
            nameDifferentToMarriageCertificate = NO;
        } else if (OCR_FIELD_VALUE_NO.equalsIgnoreCase(respondentOrApplicant2MarriedName)) {
            nameDifferentToMarriageCertificate = YES;
        } else {
            warnings.add("Please review respondent/applicant2 name different to marriage certificate in the scanned form");
        }

        String applicant2Country = ocrDataFields.getRespondentOrApplicant2Country();
        String countryUK = "UK";
        if (!toBoolean(ocrDataFields.getServeOutOfUK()) && !applicant2Country.equals(countryUK)) {
            applicant2Country = countryUK;
            warnings.add(String.format(
                "Please review respondent/applicant2 Address.  Country changed from '%s' to '%s'.",
                ocrDataFields.getRespondentOrApplicant2Country(), countryUK
            ));
        }

        var applicant2 = Applicant
            .builder()
            .firstName(ocrDataFields.getRespondentOrApplicant2FirstName())
            .middleName(ocrDataFields.getRespondentOrApplicant2MiddleName())
            .lastName(ocrDataFields.getRespondentOrApplicant2LastName())
            .nameDifferentToMarriageCertificate(nameDifferentToMarriageCertificate)
            .nameDifferentToMarriageCertificateOtherDetails(ocrDataFields.getRespondentOrApplicant2WhyMarriedNameChanged())
            .solicitorRepresented(from(isNotEmpty(ocrDataFields.getRespondentOrApplicant2SolicitorName())))
            .address(
                AddressGlobalUK
                    .builder()
                    .addressLine1(ocrDataFields.getRespondentOrApplicant2BuildingAndStreet())
                    .addressLine2(ocrDataFields.getRespondentOrApplicant2SecondLineOfAddress())
                    .postTown(ocrDataFields.getRespondentOrApplicant2TownOrCity())
                    .county(ocrDataFields.getRespondentOrApplicant2County())
                    .country(applicant2Country)
                    .postCode(ocrDataFields.getRespondentOrApplicant2Postcode())
                    .build()
            )
            .phoneNumber(ocrDataFields.getRespondentOrApplicant2PhoneNo())
            .email(ocrDataFields.getRespondentOrApplicant2Email())
            .build();

        applicant2.setSolicitor(solicitor2(ocrDataFields));

        return applicant2;
    }

    private Solicitor solicitor2(OcrDataFields ocrDataFields) {
        String app2SolicitorAddress = Stream.of(
                ocrDataFields.getRespondentOrApplicant2SolicitorBuildingAndStreet(),
                ocrDataFields.getRespondentOrApplicant2SolicitorSecondLineOfAddress(),
                ocrDataFields.getRespondentOrApplicant2SolicitorTownOrCity(),
                ocrDataFields.getRespondentOrApplicant2SolicitorCounty(),
                ocrDataFields.getRespondentOrApplicant2SolicitorCountry(),
                ocrDataFields.getRespondentOrApplicant2SolicitorPostcode(),
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

    private void setApp2FinancialOrders(TransformationDetails transformationDetails) {
        CaseData caseData = transformationDetails.getCaseData();
        OcrDataFields ocrDataFields = transformationDetails.getOcrDataFields();
        Set<FinancialOrderFor> app2FinancialOrderFor =
            deriveFinancialOrderFor(ocrDataFields.getApplicant2FinancialOrderFor());

        if (OCR_FIELD_VALUE_YES.equalsIgnoreCase(ocrDataFields.getApplicant2FinancialOrder())
            || OCR_FIELD_VALUE_BOTH.equalsIgnoreCase(ocrDataFields.getApplicant2FinancialOrder())) {
            caseData.getApplicant2().setFinancialOrder(YES);
            if (isEmpty(app2FinancialOrderFor)) {
                transformationDetails.getTransformationWarnings().add("Please review applicant2 financial order for in scanned form");
            }
        } else {
            caseData.getApplicant2().setFinancialOrder(NO);
            if (!isEmpty(app2FinancialOrderFor)) {
                transformationDetails.getTransformationWarnings().add("Please review applicant2 financial order for in scanned form");
            }
        }
        if (OCR_FIELD_VALUE_YES.equalsIgnoreCase(ocrDataFields.getApplicant2FinancialOrder())
            && isEmpty(ocrDataFields.getApplicant2PrayerFinancialOrder())) {
            transformationDetails.getTransformationWarnings().add("Please review applicant2 financial order prayer for in scanned form");
        } else if (OCR_FIELD_VALUE_NO.equalsIgnoreCase(ocrDataFields.getApplicant2FinancialOrder())
            && isNotEmpty(ocrDataFields.getApplicant2PrayerFinancialOrder())) {
            transformationDetails.getTransformationWarnings().add("Please review applicant2 financial order prayer for in scanned form");
        }

        caseData.getApplicant2().setFinancialOrdersFor(app2FinancialOrderFor);
    }

    private void applicant2StatementOfTruth(OcrDataFields ocrDataFields, CaseData caseData) {
        caseData.getApplication().setApplicant2StatementOfTruth(
            from(toBoolean(ocrDataFields.getApplicant2StatementOfTruth()))
        );
        caseData.getApplication().setApplicant2SolSignStatementOfTruth(
            from(toBoolean(ocrDataFields.getApplicant2LegalRepStatementOfTruth()))
        );
        caseData.getApplication().setApplicant2SolStatementOfReconciliationName(ocrDataFields.getApplicant2OrLegalRepFullName());
        caseData.getApplication().setApplicant2SolStatementOfReconciliationFirm(ocrDataFields.getApplicant2LegalRepFirm());
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
