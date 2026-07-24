package uk.gov.hmcts.divorce.document;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetter;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetterDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;

import java.util.Optional;

import static org.springframework.util.CollectionUtils.firstElement;
import static uk.gov.hmcts.divorce.divorcecase.util.AddressUtil.getPostalAddress;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NAME_FORMAT;

@Component
public class GeneralLetterRecipientResolver {

    public GeneralLetterRecipient resolve(CaseData caseData) {
        return resolveIfAvailable(caseData)
            .orElse(new GeneralLetterRecipient(
                GeneralParties.OTHER,
                GeneralParties.OTHER.name(),
                null,
                YesOrNo.NO
            ));
    }

    public Optional<GeneralLetterRecipient> resolveIfAvailable(CaseData caseData) {
        return getGeneralLetterParties(caseData)
            .map(parties -> resolveByParty(caseData, parties));
    }

    private GeneralLetterRecipient resolveByParty(CaseData caseData, GeneralParties parties) {
        return switch (parties) {
            case RESPONDENT -> new GeneralLetterRecipient(
                parties,
                getRecipientName(caseData.getApplicant2().getFirstName(), caseData.getApplicant2().getLastName()),
                caseData.getApplicant2().getCorrespondenceAddressWithoutConfidentialCheck(),
                caseData.getApplicant2().getCorrespondenceAddressIsOverseas()
            );
            case APPLICANT -> new GeneralLetterRecipient(
                parties,
                getRecipientName(caseData.getApplicant1().getFirstName(), caseData.getApplicant1().getLastName()),
                caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck(),
                caseData.getApplicant1().getCorrespondenceAddressIsOverseas()
            );
            case OTHER -> new GeneralLetterRecipient(
                parties,
                Optional.ofNullable(caseData.getGeneralLetter()).map(GeneralLetter::getOtherRecipientName).orElse(parties.name()),
                Optional.ofNullable(caseData.getGeneralLetter())
                    .map(GeneralLetter::getOtherRecipientAddress)
                    .map(address -> getPostalAddress(address))
                    .orElse(null),
                YesOrNo.NO
            );
        };
    }

    private Optional<GeneralParties> getGeneralLetterParties(CaseData caseData) {
        return Optional.ofNullable(caseData.getGeneralLetter())
            .map(GeneralLetter::getGeneralLetterParties)
            .or(() -> Optional.ofNullable(firstElement(caseData.getGeneralLetters()))
                .map(ListValue::getValue)
                .map(GeneralLetterDetails::getGeneralLetterParties));
    }

    private String getRecipientName(String firstName, String lastName) {
        return String.format(NAME_FORMAT, firstName, lastName);
    }
}
