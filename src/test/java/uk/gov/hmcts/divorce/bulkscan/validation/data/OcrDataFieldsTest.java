package uk.gov.hmcts.divorce.bulkscan.validation.data;

import org.junit.jupiter.api.Test;

class OcrDataFieldsTest {

    @Test
    void transformDataShouldHandleNull() {
        OcrDataFields.transformData(null);
    }

    @Test
    void transformOcrMapToObjectShouldHandleNull() {
        OcrDataFields.transformOcrMapToObject(null);
    }
}
