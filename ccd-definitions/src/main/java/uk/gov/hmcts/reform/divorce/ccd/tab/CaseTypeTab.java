package uk.gov.hmcts.reform.divorce.ccd.tab;

import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.CcdConfig;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

public class CaseTypeTab implements CcdConfig {

    @Override
    public void applyTo(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder.tab("petitionDetails", "Petition")
            .field("D8MarriageIsSameSexCouple")
            .field("D8InferredPetitionerGender")
            .field("D8InferredRespondentGender")
            .field("D8MarriageDate");

        configBuilder.tab("paymentDetailsCourtAdmin", "Payment")
            .field("D8HelpWithFeesReferenceNumber");
    }
}
