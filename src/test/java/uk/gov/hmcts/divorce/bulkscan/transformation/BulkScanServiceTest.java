package uk.gov.hmcts.divorce.bulkscan.transformation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.bulkscan.endpoint.model.input.TransformationInput;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BulkScanServiceTest {
    @Mock
    private BulkScanFormTransformerFactory bulkScanFormTransformerFactory;

    @Mock
    private D8FormToCaseTransformer d8FormToCaseTransformer;

    @InjectMocks
    private BulkScanService bulkScanService;

    @Test
    void shouldValidateOcrRequestForD8FormType() {
        var exceptionRecord = TransformationInput.builder().build();
        Map<String, Object> transformedData =
            Map.of(
                "applicationForDivorce", "true",
                "aSoleApplication", "true",
                "marriageOrCivilPartnershipCertificate", "true",
                "translation", "false"
            );

        when(bulkScanFormTransformerFactory.getTransformer(exceptionRecord.getFormType())).thenReturn(d8FormToCaseTransformer);
        when(d8FormToCaseTransformer.transformIntoCaseData(exceptionRecord)).thenReturn(transformedData);

        Map<String, Object> actualTransformedResponse = bulkScanService.transformBulkScanForm(exceptionRecord);

        assertThat(actualTransformedResponse).isEqualTo(transformedData);
    }
}
