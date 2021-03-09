package uk.gov.hmcts.reform.divorce.ccd.tab;

import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.CcdConfiguration;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

public class CaseTypeTab implements CcdConfiguration {

    @Override
    public void applyTo(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder.tab("petitionDetails", "Petition")
            .field(CaseData::getDivorceOrDissolution)
            .field(CaseData::getD8InferredPetitionerGender)
            .field(CaseData::getD8InferredRespondentGender)
            .field(CaseData::getD8MarriageIsSameSexCouple)
            .field(CaseData::getD8MarriageDate)
            .field(CaseData::getD8MarriedInUk)
            .field(CaseData::getD8CertificateInEnglish);

        configBuilder.tab("paymentDetailsCourtAdmin", "Payment")
            .field(CaseData::getD8HelpWithFeesReferenceNumber);
    }
}
