package uk.gov.hmcts.reform.divorce.caseapi.service;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.divorce.caseapi.clients.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.divorce.caseapi.model.payments.FeeResponse;

import java.util.Collections;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.caseapi.caseapi.util.TestDataHelper.getFeeItem;
import static uk.gov.hmcts.reform.divorce.caseapi.caseapi.util.TestDataHelper.getOrderSummary;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.DEFAULT_CHANNEL;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.DIVORCE;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.FAMILY;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.FAMILY_COURT;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.ISSUE_EVENT;

@ExtendWith(MockitoExtension.class)
public class SolicitorSubmitPetitionServiceTest {
    @Mock
    private FeesAndPaymentsClient feesAndPaymentsClient;

    @InjectMocks
    private SolicitorSubmitPetitionService solicitorSubmitPetitionService;

    @Test
    public void shouldSuccessfullyRetrieveOrderSummary() {
        when(feesAndPaymentsClient.getPetitionIssueFee(
            DEFAULT_CHANNEL,
            ISSUE_EVENT,
            FAMILY,
            FAMILY_COURT,
            DIVORCE,
            null
        )).thenReturn(getFeeResponse());

        assertThat(solicitorSubmitPetitionService.getOrderSummary()).isEqualTo(
            getOrderSummary(
                getFeeItem(
                    10.0, "FEECODE1", "Issue Fee", 1
                )));
    }

    private FeeResponse getFeeResponse() {
        return FeeResponse
            .builder()
            .feeCode("FEECODE1")
            .amount(10.0)
            .description("Issue Fee")
            .version(1)
            .build();
    }

    @Test
    void shouldThrowFeignExceptionWhenIssueEventIsNotFoundInFeesEndpoint() {
        byte[] emptyBody = {};
        Request request = Request.create(GET, EMPTY, Map.of(), emptyBody, UTF_8, null);

        FeignException feignException = FeignException.errorStatus(
            "feeLookupNotFound",
            Response.builder()
                .request(request)
                .status(404)
                .headers(Collections.emptyMap())
                .reason("Fee Not found")
                .build()
        );

        doThrow(feignException)
            .when(feesAndPaymentsClient)
            .getPetitionIssueFee(
                "default",
                "issue",
                "family",
                "family court",
                "divorce",
                null
            );

        assertThatThrownBy(() -> solicitorSubmitPetitionService.getOrderSummary())
            .isInstanceOf(FeignException.class)
            .hasMessageContaining("404 Fee Not found");


        verify(feesAndPaymentsClient).getPetitionIssueFee(
            DEFAULT_CHANNEL,
            ISSUE_EVENT,
            FAMILY,
            FAMILY_COURT,
            DIVORCE,
            null
        );

        verifyNoMoreInteractions(feesAndPaymentsClient);
    }
}
