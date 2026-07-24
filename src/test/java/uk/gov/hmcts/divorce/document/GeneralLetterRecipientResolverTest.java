package uk.gov.hmcts.divorce.document;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetter;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetterDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.buildCaseDataWithGeneralLetter;

class GeneralLetterRecipientResolverTest {

    private final GeneralLetterRecipientResolver resolver = new GeneralLetterRecipientResolver();

    @Test
    void shouldResolveApplicantRecipient() {
        var caseData = buildCaseDataWithGeneralLetter(GeneralParties.APPLICANT);

        GeneralLetterRecipient recipient = resolver.resolve(caseData, GeneralParties.APPLICANT);

        assertThat(recipient.party()).isEqualTo(GeneralParties.APPLICANT);
        assertThat(recipient.recipientName()).isEqualTo("test_first_name test_last_name");
        assertThat(recipient.recipientAddress()).isEqualTo("line 1\ntown\nUK\npostcode");
        assertThat(recipient.correspondenceAddressOverseas()).isNull();
    }

    @Test
    void shouldResolveRespondentRecipient() {
        var caseData = buildCaseDataWithGeneralLetter(GeneralParties.RESPONDENT);

        GeneralLetterRecipient recipient = resolver.resolve(caseData, GeneralParties.RESPONDENT);

        assertThat(recipient.party()).isEqualTo(GeneralParties.RESPONDENT);
        assertThat(recipient.recipientName()).isEqualTo("test_first_name test_last_name");
        assertThat(recipient.recipientAddress()).isEqualTo("line 1\ntown\nUK\npostcode");
        assertThat(recipient.correspondenceAddressOverseas()).isNull();
    }

    @Test
    void shouldResolveOtherRecipient() {
        var caseData = buildCaseDataWithGeneralLetter(GeneralParties.OTHER);
        caseData.setGeneralLetter(GeneralLetter.builder()
            .generalLetterParties(GeneralParties.OTHER)
            .otherRecipientName("Other Person")
            .otherRecipientAddress(AddressGlobalUK.builder()
                .addressLine1("10 Some Road")
                .postTown("Leeds")
                .postCode("LS1 1AA")
                .build())
            .build());

        GeneralLetterRecipient recipient = resolver.resolve(caseData, GeneralParties.OTHER);

        assertThat(recipient.party()).isEqualTo(GeneralParties.OTHER);
        assertThat(recipient.recipientName()).isEqualTo("Other Person");
        assertThat(recipient.recipientAddress()).isEqualTo("10 Some Road\nLeeds\nLS1 1AA");
        assertThat(recipient.correspondenceAddressOverseas()).isEqualTo(YesOrNo.NO);
    }

    @Test
    void shouldUseFallbackWhenPartyIsNull() {
        var caseData = buildCaseDataWithGeneralLetter(GeneralParties.APPLICANT);
        caseData.setGeneralLetter(null);
        caseData.setGeneralLetters(List.of(
            ListValue.<GeneralLetterDetails>builder()
                .value(GeneralLetterDetails.builder().generalLetterParties(GeneralParties.RESPONDENT).build())
                .build()
        ));

        GeneralLetterRecipient recipient = resolver.resolve(caseData, null);

        assertThat(recipient.party()).isEqualTo(GeneralParties.OTHER);
        assertThat(recipient.recipientAddress()).isNull();
    }
}
