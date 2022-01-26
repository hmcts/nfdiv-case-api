package uk.gov.hmcts.divorce.bulkscan.tab;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.type.ExceptionRecord;
import uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordState;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

@Component
public class ExceptionRecordTab implements CCDConfig<ExceptionRecord, ExceptionRecordState, UserRole> {

    @Override
    public void configure(final ConfigBuilder<ExceptionRecord, ExceptionRecordState, UserRole> configBuilder) {
        buildOcrWarningsTab(configBuilder);
        buildEnvelopeTab(configBuilder);
        buildDocumentsTab(configBuilder);
        buildOcrTab(configBuilder);
    }

    private void buildOcrWarningsTab(ConfigBuilder<ExceptionRecord, ExceptionRecordState, UserRole> configBuilder) {
        configBuilder.tab("warnings", "warnings")
            .field(ExceptionRecord::getOcrDataValidationWarnings)
            .field(ExceptionRecord::getDisplayWarnings, "displayWarnings=\"ALWAYS_HIDE\"");
    }

    private void buildEnvelopeTab(ConfigBuilder<ExceptionRecord, ExceptionRecordState, UserRole> configBuilder) {
        configBuilder.tab("envelope", "Envelope")
            .field(ExceptionRecord::getEnvelopeLabel)
            .field(ExceptionRecord::getJourneyClassification)
            .field(ExceptionRecord::getPoBox)
            .field(ExceptionRecord::getPoBoxJurisdiction)
            .field(ExceptionRecord::getDeliveryDate)
            .field(ExceptionRecord::getOpeningDate)
            .field(ExceptionRecord::getCaseReference)
            .field(ExceptionRecord::getAttachToCaseReference)
            .field(ExceptionRecord::getContainsPayments);
    }

    private void buildDocumentsTab(ConfigBuilder<ExceptionRecord, ExceptionRecordState, UserRole> configBuilder) {
        configBuilder.tab("documentation", "Documentation")
            .field(ExceptionRecord::getScannedDocuments);
    }

    private void buildOcrTab(ConfigBuilder<ExceptionRecord, ExceptionRecordState, UserRole> configBuilder) {
        configBuilder.tab("ocr", "Form OCR")
            .field(ExceptionRecord::getFormType)
            .field(ExceptionRecord::getScanOCRData);
    }
}
