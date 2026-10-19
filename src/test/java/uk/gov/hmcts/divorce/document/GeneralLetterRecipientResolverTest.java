package uk.gov.hmcts.divorce.document;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetter;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.notification.CommonContent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HUSBAND_OR_WIFE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.buildCaseDataWithGeneralLetter;

@ExtendWith(MockitoExtension.class)
class GeneralLetterRecipientResolverTest {

    @Mock
    private CommonContent commonContent;

    private GeneralLetterRecipientResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new GeneralLetterRecipientResolver(commonContent);
    }

    @Test
    void shouldResolveApplicantRecipient() {
        var caseData = buildCaseDataWithGeneralLetter(GeneralParties.APPLICANT);
        when(commonContent.getPartner(any(), any())).thenReturn("partner");

        GeneralLetterRecipient recipient = resolver.resolve(caseData, GeneralParties.APPLICANT);

        assertThat(recipient.party()).isEqualTo(GeneralParties.APPLICANT);
        assertThat(recipient.recipientName()).isEqualTo("test_first_name test_last_name");
        assertThat(recipient.recipientAddress()).isEqualTo("line 1\ntown\nUK\npostcode");
        assertThat(recipient.correspondenceAddressOverseas()).isNull();
        assertThat(recipient.partnerRelation()).isEqualTo("partner");
    }

    @Test
    void shouldResolveRespondentRecipient() {
        var caseData = buildCaseDataWithGeneralLetter(GeneralParties.RESPONDENT);
        when(commonContent.getPartner(any(), any())).thenReturn("partner");

        GeneralLetterRecipient recipient = resolver.resolve(caseData, GeneralParties.RESPONDENT);

        assertThat(recipient.party()).isEqualTo(GeneralParties.RESPONDENT);
        assertThat(recipient.recipientName()).isEqualTo("test_first_name test_last_name");
        assertThat(recipient.recipientAddress()).isEqualTo("line 1\ntown\nUK\npostcode");
        assertThat(recipient.correspondenceAddressOverseas()).isNull();
        assertThat(recipient.partnerRelation()).isEqualTo("partner");
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
        assertThat(recipient.partnerRelation()).isEqualTo(HUSBAND_OR_WIFE);
    }

    @Test
    void shouldThrowExceptionWhenPartyIsNull() {
        var caseData = buildCaseDataWithGeneralLetter(GeneralParties.APPLICANT);

        assertThrows(NullPointerException.class, () -> resolver.resolve(caseData, null));
    }
}
