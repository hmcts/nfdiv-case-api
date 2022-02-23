package uk.gov.hmcts.divorce.bulkscan.transformation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Set;
import java.util.function.Function;

import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.Application.ThePrayer.I_CONFIRM;

@Component
public class D8SPrayerTransformer implements Function<TransformationDetails, TransformationDetails> {

    @Override
    public TransformationDetails apply(TransformationDetails transformationDetails) {

        CaseData caseData = transformationDetails.getCaseData();
        OcrDataFields ocrDataFields = transformationDetails.getOcrDataFields();

        if (toBoolean(ocrDataFields.getPrayerApplicant1JudiciallySeparated())) {
            caseData.getApplication().setApplicant1PrayerHasBeenGivenCheckbox(Set.of(I_CONFIRM));
            caseData.setIsJudicialSeparation(YES);
        } else {
            caseData.getTransformationAndOcrWarnings().add("Please review prayer in the scanned form");
        }

        return transformationDetails;
    }
}
