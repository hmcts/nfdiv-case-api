package uk.gov.hmcts.divorce.solicitor.event.page;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEES_HELP_WITH;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class SolPaymentTest {

    @Mock
    private PbaService pbaService;

    @InjectMocks
    private SolPayment solPayment;

    @Test
    void shouldRetrieveAndSetPbaNumbersWhenPaymentMethodIsPba() {
        final CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setSolPaymentHowToPay(FEE_PAY_BY_ACCOUNT);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        List<DynamicListElement> pbaAccountNumbers = Stream.of("PBA0012345", "PBA0012346")
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

    @Test
    void shouldLogErrorAndRethrowFeignException() {
        final CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setSolPaymentHowToPay(FEE_PAY_BY_ACCOUNT);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        doThrow(new FeignException.NotFound("No PBAs associated with given email", mock(Request.class), null, null))
            .when(pbaService).populatePbaDynamicList();

        var response = solPayment.midEvent(details, details);

        assertThat(response.getErrors()).isEqualTo(List.of("No PBA numbers associated with the provided email address"));

    }

    @Test
    void shouldDoNothingAndReturnCaseDataIfHelpWithFeesSelected() {
        final CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setSolPaymentHowToPay(FEES_HELP_WITH);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = solPayment.midEvent(details, details);

        assertThat(response.getData()).isSameAs(caseData);
        verifyNoInteractions(pbaService);
    }
}
