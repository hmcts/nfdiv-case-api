package uk.gov.hmcts.divorce.api.ccd.tab;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.api.ccd.model.CaseData;
import uk.gov.hmcts.divorce.api.ccd.model.State;
import uk.gov.hmcts.divorce.api.ccd.model.UserRole;

@Component
public class CaseTypeTab implements CCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder.tab("petitionDetails", "Petition")
            .field(CaseData::getDivorceOrDissolution)
            .field(CaseData::getInferredPetitionerGender)
            .field(CaseData::getInferredRespondentGender)
            .field(CaseData::getMarriageIsSameSexCouple)
            .field(CaseData::getMarriageDate)
            .field(CaseData::getMarriedInUk)
            .field(CaseData::getCertificateInEnglish);

        configBuilder.tab("paymentDetailsCourtAdmin", "Payment")
            .field(CaseData::getHelpWithFeesReferenceNumber);

        configBuilder.tab("documents", "Documents")
            .field(CaseData::getDocumentsGenerated);
    }
}
