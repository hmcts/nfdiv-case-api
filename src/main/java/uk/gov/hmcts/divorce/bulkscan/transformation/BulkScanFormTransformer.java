package uk.gov.hmcts.divorce.bulkscan.transformation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType;
import uk.gov.hmcts.divorce.bulkscan.endpoint.model.input.OcrDataField;
import uk.gov.hmcts.divorce.bulkscan.endpoint.model.input.TransformationInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Slf4j
public abstract class BulkScanFormTransformer {

    public static final String BULK_SCAN_CASE_REFERENCE = "bulkScanCaseReference";

    public Map<String, Object> transformIntoCaseData(TransformationInput transformationInput) {
        log.info(
            "Processing transformation request with envelope id {}, exception record id {} and isAutomatedProcess flag {}",
            transformationInput.getEnvelopeId(),
            transformationInput.getExceptionRecordId(),
            transformationInput.isAutomatedProcessCreation()
        );

        List<OcrDataField> ocrDataFields = transformationInput.getOcrDataFields();

        Map<String, Object> caseData = new HashMap<>();

        // Need to store the Exception Record id as part of the CCD data
        caseData.put(BULK_SCAN_CASE_REFERENCE, transformationInput.getId());

        caseData.put("scannedDocuments", transformScannedDocuments(transformationInput));

        Map<String, Object> formSpecificMap = runFormSpecificTransformation(
            ocrDataFields,
            transformationInput.isAutomatedProcessCreation(),
            transformationInput.getEnvelopeId()
        );
        caseData.putAll(formSpecificMap);

        return caseData;
    }

    private List<ListValue<ScannedDocument>> transformScannedDocuments(final TransformationInput transformationInput) {
        List<ListValue<ScannedDocument>> scannedDocuments = new ArrayList<>();
        transformationInput.getScannedDocuments().forEach(
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

    abstract Map<String, Object> runFormSpecificTransformation(
        List<OcrDataField> ocrDataFields,
        boolean automatedProcessCreation,
        String envelopeId
    );
}
