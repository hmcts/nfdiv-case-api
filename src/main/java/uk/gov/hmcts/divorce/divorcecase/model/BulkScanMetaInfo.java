package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.BulkScanEnvelope;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerBulkScanAccess;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BulkScanMetaInfo {
    @CCD(
        label = "Transformation and OCR warnings",
        typeOverride = Collection,
        typeParameterOverride = "TextArea",
        access = {CaseworkerBulkScanAccess.class}
    )
    @Builder.Default
    private List<ListValue<String>> warnings = new ArrayList<>();

    @CCD(
        label = "Bulk Scan Envelopes",
        typeOverride = Collection,
        typeParameterOverride = "BulkScanEnvelope",
        access = {CaseworkerBulkScanAccess.class}
    )
    private List<ListValue<BulkScanEnvelope>> bulkScanEnvelopes;

    @CCD(
        label = "Exception record reference",
        access = {CaseworkerBulkScanAccess.class}
    )
    private String bulkScanCaseReference;

    private YesOrNo evidenceHandled;
}
