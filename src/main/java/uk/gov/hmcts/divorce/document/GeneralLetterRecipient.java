package uk.gov.hmcts.divorce.document;

import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;

public record GeneralLetterRecipient(
    GeneralParties party,
    String recipientName,
    String recipientAddress,
    YesOrNo correspondenceAddressOverseas,
    String partnerRelation
) {
}
