package uk.gov.hmcts.divorce.solicitor.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaOrganisationResponse;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class SolPaymentTest {

    @Mock
    private PbaService pbaService;

    @InjectMocks
    private SolPayment solPayment;

    @Mock
    private ResponseEntity<PbaOrganisationResponse> responseEntity;

    @Test
    public void shouldRetrieveAndSetPbaNumbersWhenPaymentMethodIsPba() {
        final CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setSolPaymentHowToPay(FEE_PAY_BY_ACCOUNT);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        List<DynamicListElement> pbaAccountNumbers = List.of("PBA0012345", "PBA0012346")
            .stream()
            .map(pbaNumber -> DynamicListElement.builder().label(pbaNumber).code(UUID.randomUUID()).build())
            .collect(Collectors.toList());

        DynamicList pbaNumbers = DynamicList
            .builder()
            .value(DynamicListElement.builder().label("pbaNumber").code(UUID.randomUUID()).build())
            .listItems(pbaAccountNumbers)
            .build();

        when(pbaService.populatePbaDynamicList())
            .thenReturn(pbaNumbers);

        AboutToStartOrSubmitResponse<CaseData, State> response = solPayment.midEvent(details, details);

        DynamicList pbaNumbersResponse = response.getData().getApplication().getPbaNumbers();

        assertThat(pbaNumbersResponse).isNotNull();
        assertThat(pbaNumbersResponse.getListItems())
            .extracting("label")
            .containsExactlyInAnyOrder("PBA0012345", "PBA0012346");
    }
}
