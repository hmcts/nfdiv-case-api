package uk.gov.hmcts.divorce.bulkscan.transformation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Set;
import java.util.function.Function;

import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static uk.gov.hmcts.divorce.divorcecase.model.Application.ThePrayer.I_CONFIRM;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;

@Component
public class D8PrayerTransformer implements Function<TransformationDetails, TransformationDetails> {

    @Override
    public TransformationDetails apply(TransformationDetails transformationDetails) {

        CaseData caseData = transformationDetails.getCaseData();
        OcrDataFields ocrDataFields = transformationDetails.getOcrDataFields();

        final var isMarriageDissolved = toBoolean(ocrDataFields.getPrayerMarriageDissolved());
        final var isCivilPartnershipDissolved = toBoolean(ocrDataFields.getPrayerCivilPartnershipDissolved());

        if (DIVORCE.equals(caseData.getDivorceOrDissolution()) && isMarriageDissolved) {
            caseData.getApplication().setApplicant1PrayerHasBeenGivenCheckbox(Set.of(I_CONFIRM));
            caseData.getApplication().setApplicant2PrayerHasBeenGivenCheckbox(Set.of(I_CONFIRM));
        } else if (DISSOLUTION.equals(caseData.getDivorceOrDissolution()) && isCivilPartnershipDissolved) {
            caseData.getApplication().setApplicant1PrayerHasBeenGivenCheckbox(Set.of(I_CONFIRM));
            caseData.getApplication().setApplicant2PrayerHasBeenGivenCheckbox(Set.of(I_CONFIRM));
        } else {
            caseData.getTransformationAndOcrWarnings().add("Please review prayer in the scanned form");
        }

        return transformationDetails;
    }
}
