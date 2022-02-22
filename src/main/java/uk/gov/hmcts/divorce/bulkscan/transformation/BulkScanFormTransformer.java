package uk.gov.hmcts.divorce.bulkscan.transformation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType;
import uk.gov.hmcts.divorce.endpoint.model.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.bsp.common.config.BspCommonFields.BULK_SCAN_CASE_REFERENCE;

@Slf4j
public abstract class BulkScanFormTransformer {

    public Map<String, Object> transformIntoCaseData(ExceptionRecord exceptionRecord) {
        List<OcrDataField> ocrDataFields = exceptionRecord.getOcrDataFields();

        Map<String, Object> caseData = new HashMap<>();

        // Need to store the Exception Record ID as part of the CCD data
        caseData.put(BULK_SCAN_CASE_REFERENCE, exceptionRecord.getId());

        caseData.put("scannedDocuments", transformScannedDocuments(exceptionRecord));

        Map<String, Object> formSpecificMap = runFormSpecificTransformation(ocrDataFields);
        caseData.putAll(formSpecificMap);

        return caseData;
    }

    private List<ListValue<ScannedDocument>> transformScannedDocuments(final ExceptionRecord exceptionRecord) {
        List<ListValue<ScannedDocument>> scannedDocuments = new ArrayList<>();
        exceptionRecord.getScannedDocuments().forEach(
            inputScannedDoc -> {
                var scannedDocListValue = ListValue.<ScannedDocument>builder()
                    .value(ScannedDocument
                        .builder()
                        .controlNumber(inputScannedDoc.getControlNumber())
                        .deliveryDate(inputScannedDoc.getDeliveryDate())
                        .scannedDate(inputScannedDoc.getScannedDate())
                        .type(EnumUtils.getEnum(ScannedDocumentType.class, inputScannedDoc.getType().toUpperCase(Locale.ROOT)))
                        .subtype(inputScannedDoc.getSubtype())
                        .fileName(inputScannedDoc.getFileName())
                        .url(
                            Document
                                .builder()
                                .binaryUrl(inputScannedDoc.getDocument().getBinaryUrl())
                                .url(inputScannedDoc.getDocument().getUrl())
                                .filename(inputScannedDoc.getDocument().getFilename())
                                .build()
                        )
                        .build()
                    )
                    .id(UUID.randomUUID().toString())
                    .build();

                scannedDocuments.add(scannedDocListValue);
            }
        );
        return scannedDocuments;
    }

    abstract Map<String, Object> runFormSpecificTransformation(List<OcrDataField> ocrDataFields);
}
