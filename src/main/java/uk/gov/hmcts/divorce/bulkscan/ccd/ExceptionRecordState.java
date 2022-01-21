package uk.gov.hmcts.divorce.bulkscan.ccd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@Getter
public enum ExceptionRecordState {

    @CCD(
        name = "A scanned record has been received"
    )
    ScannedRecordReceived("ScannedRecordReceived"),

    @CCD(
        name = "The scanned record has been attached to existing case"
    )
    ScannedRecordAttachedToCase("ScannedRecordAttachedToCase"),

    @CCD(
        name = "A new case is created from scanned record"
    )
    ScannedRecordCaseCreated("ScannedRecordCaseCreated"),

    @CCD(
        name = "The scanned record has been rejected"
    )
    ScannedRecordRejected("ScannedRecordRejected"),

    @CCD(
        name = "The scanned record has been handled by CW manually"
    )
    ScannedRecordManuallyHandled("ScannedRecordManuallyHandled");

    private final String name;
}

