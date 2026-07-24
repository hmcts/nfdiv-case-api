package uk.gov.hmcts.divorce.document;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetter;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.util.Optional;
import java.util.Objects;

import static uk.gov.hmcts.divorce.divorcecase.util.AddressUtil.getPostalAddress;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HUSBAND_OR_WIFE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NAME_FORMAT;

@Component
@RequiredArgsConstructor
public class GeneralLetterRecipientResolver {

    private final CommonContent commonContent;

    public GeneralLetterRecipient resolve(CaseData caseData, GeneralParties party) {
        return resolveByParty(caseData, Objects.requireNonNull(party, "General letter party must be set"));
    }

    private GeneralLetterRecipient resolveByParty(CaseData caseData, GeneralParties party) {
        return switch (party) {
            case RESPONDENT -> new GeneralLetterRecipient(
                party,
                getRecipientName(caseData.getApplicant2().getFirstName(), caseData.getApplicant2().getLastName()),
                caseData.getApplicant2().getCorrespondenceAddressWithoutConfidentialCheck(),
                caseData.getApplicant2().getCorrespondenceAddressIsOverseas(),
                commonContent.getPartner(caseData, caseData.getApplicant1())
            );
            case APPLICANT -> new GeneralLetterRecipient(
                party,
                getRecipientName(caseData.getApplicant1().getFirstName(), caseData.getApplicant1().getLastName()),
                caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck(),
                caseData.getApplicant1().getCorrespondenceAddressIsOverseas(),
                commonContent.getPartner(caseData, caseData.getApplicant2())

            );
            case OTHER -> new GeneralLetterRecipient(
                party,
                Optional.ofNullable(caseData.getGeneralLetter()).map(GeneralLetter::getOtherRecipientName).orElse(party.name()),
                Optional.ofNullable(caseData.getGeneralLetter())
                    .map(GeneralLetter::getOtherRecipientAddress)
                    .map(address -> getPostalAddress(address))
                    .orElse(null),
                YesOrNo.NO,
                caseData.isDivorce() ? HUSBAND_OR_WIFE : CIVIL_PARTNER
            );
        };
    }

    private String getRecipientName(String firstName, String lastName) {
        return String.format(NAME_FORMAT, firstName, lastName);
    }
}
