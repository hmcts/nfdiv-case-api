package uk.gov.hmcts.divorce.bulkscan.ccd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@Getter
public enum ExceptionRecordState {

    @CCD(
        label = "A scanned record has been received"
    )
    ScannedRecordReceived,

    @CCD(
        label = "The scanned record has been attached to existing case"
    )
    ScannedRecordAttachedToCase,

    @CCD(
        label = "A new case is created from scanned record"
    )
    ScannedRecordCaseCreated,

    @CCD(
        label = "The scanned record has been rejected"
    )
    ScannedRecordRejected,

    @CCD(
        label = "The scanned record has been handled by CW manually"
    )
    ScannedRecordManuallyHandled;

}

