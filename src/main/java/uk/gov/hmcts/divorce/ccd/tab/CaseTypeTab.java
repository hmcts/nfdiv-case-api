package uk.gov.hmcts.divorce.ccd.tab;

import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.ccd.CcdConfiguration;
import uk.gov.hmcts.divorce.ccd.model.CaseData;
import uk.gov.hmcts.divorce.ccd.model.State;
import uk.gov.hmcts.divorce.ccd.model.UserRole;

public class CaseTypeTab implements CcdConfiguration {

    @Override
    public void applyTo(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder.tab("petitionDetails", "Petition")
            .field(CaseData::getDivorceOrDissolution)
            .field(CaseData::getInferredApplicantGender)
            .field(CaseData::getInferredRespondentGender)
            .field(CaseData::getMarriageIsSameSexCouple)
            .field(CaseData::getMarriageDate)
            .field(CaseData::getMarriedInUk)
            .field(CaseData::getCertificateInEnglish);

        configBuilder.tab("paymentDetailsCourtAdmin", "Payment")
            .field(CaseData::getHelpWithFeesReferenceNumber);

        configBuilder.tab("documents","Documents")
            .field(CaseData:: getDocumentsGenerated);
    }
}
