package uk.gov.hmcts.divorce.solicitor.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SetApplicantContactDetailsTest {

    @InjectMocks
    private SetApplicantContactDetails setApplicantContactDetails;

    @Test
    void shouldSetApplicantAddressesToMatchNonConfidentialAddressesEnteredBySolicitor() {
        AddressGlobalUK app1Address = AddressGlobalUK
            .builder().addressLine1("app1Address").build();
        AddressGlobalUK app2Address = AddressGlobalUK
            .builder().addressLine1("app2Address").build();
        final var caseData = CaseData.builder()
            .applicant1(Applicant.builder().nonConfidentialAddress(app1Address).build())
            .applicant2(Applicant.builder().nonConfidentialAddress(app2Address).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = setApplicantContactDetails.apply(caseDetails);

        assertThat(result.getData().getApplicant1().getAddress()).isEqualTo(app1Address);
        assertThat(result.getData().getApplicant2().getAddress()).isEqualTo(app2Address);
    }
}
