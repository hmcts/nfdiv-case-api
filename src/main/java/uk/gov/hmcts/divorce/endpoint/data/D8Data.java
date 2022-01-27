package uk.gov.hmcts.divorce.endpoint.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.type.KeyValue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@Builder
public class D8Data {

    //TODO - take all fields from spreadsheet and add to D8Data class
    private String applicationForDivorce;
    private String applicationForDissolution;
    private String marriageOrCivilPartnershipCertificate;
    private String translation;

    //TODO - change this transform to return D8Data
    public static Map<String, String> transformData(List<KeyValue> ocrDataFields) {
        return ocrDataFields.stream()
            .collect(Collectors.toMap(KeyValue::getKey, KeyValue::getValue));
    }
}
