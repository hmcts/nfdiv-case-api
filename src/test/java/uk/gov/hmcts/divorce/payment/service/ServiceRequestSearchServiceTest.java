package uk.gov.hmcts.divorce.payment.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.payment.client.PaymentClient;
import uk.gov.hmcts.divorce.payment.model.CaseServiceRequestsResponse;
import uk.gov.hmcts.divorce.payment.model.ServiceRequestDto;
import uk.gov.hmcts.divorce.payment.model.ServiceRequestDto.FeeDto;
import uk.gov.hmcts.divorce.payment.model.ServiceRequestDto.PaymentDto;
import uk.gov.hmcts.divorce.payment.model.ServiceRequestStatus;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.payment.FeesAndPayConstants.DIVORCE_APPLICATION_FEE_CODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_NAME;

@ExtendWith(MockitoExtension.class)
public class ServiceRequestSearchServiceTest {
    @Mock
    private PaymentClient paymentClient;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private ServiceRequestSearchService serviceRequestSearchService;

    @Test
    void shouldReturnEmptyOptionalWhenNoServiceRequestsFound() {
        List<ServiceRequestDto> caseServiceRequests = Collections.emptyList();
        stubServiceRequestSearch(caseServiceRequests);

        Optional<ServiceRequestDto> response = serviceRequestSearchService.findUnpaidServiceRequest(
            TEST_CASE_ID, Fee.builder().build(), TEST_ORG_NAME
        );

        assertThat(response).isEmpty();
    }

    @Test
    void shouldReturnServiceRequestWithMatchingAmountAndSingleUseFee() {
        List<ServiceRequestDto> caseServiceRequests = List.of(
            ServiceRequestDto.builder()
                .fees(List.of(buildFeeDto(DIVORCE_APPLICATION_FEE_CODE, 593.00)))
                .serviceRequestStatus(ServiceRequestStatus.NOT_PAID)
                .build()
        );
        stubServiceRequestSearch(caseServiceRequests);

        Optional<ServiceRequestDto> response = serviceRequestSearchService.findUnpaidServiceRequest(
            TEST_CASE_ID,
            Fee.builder().code(DIVORCE_APPLICATION_FEE_CODE).amount("59300").build(),
            TEST_ORG_NAME
        );

        assertThat(response.get()).isEqualTo(caseServiceRequests.get(0));
    }

    @Test
    void shouldReturnServiceRequestWithMatchingAmountAndFeeCodeAndOrganisation() {
        List<ServiceRequestDto> caseServiceRequests = List.of(
            ServiceRequestDto.builder()
                .fees(List.of(buildFeeDto("FEE002", 10)))
                .payments(List.of(PaymentDto.builder().organisationName(TEST_ORG_NAME).build()))
                .serviceRequestStatus(ServiceRequestStatus.NOT_PAID)
                .build()
        );
        stubServiceRequestSearch(caseServiceRequests);

        Optional<ServiceRequestDto> response = serviceRequestSearchService.findUnpaidServiceRequest(
            TEST_CASE_ID, Fee.builder().code("FEE002").amount("1000").build(), TEST_ORG_NAME
        );

        assertThat(response.get()).isEqualTo(caseServiceRequests.get(0));
    }

    @Test
    void shouldNotReturnMatchingServiceRequestThatHasBeenPaid() {
        List<ServiceRequestDto> caseServiceRequests = List.of(
            ServiceRequestDto.builder()
                .fees(List.of(buildFeeDto("FEE002", 10)))
                .payments(List.of(PaymentDto.builder().organisationName(TEST_ORG_NAME).build()))
                .serviceRequestStatus(ServiceRequestStatus.PAID)
                .build()
        );
        stubServiceRequestSearch(caseServiceRequests);

        Optional<ServiceRequestDto> response = serviceRequestSearchService.findUnpaidServiceRequest(
            TEST_CASE_ID, Fee.builder().code("FEE002").amount("1000").build(), TEST_ORG_NAME
        );

        assertThat(response).isEmpty();
    }

    @Test
    void shouldNotReturnServiceRequestWithMatchingAmountAndFeeCodeButDifferentOrganisation() {
        List<ServiceRequestDto> caseServiceRequests = List.of(
            ServiceRequestDto.builder()
                .fees(List.of(buildFeeDto("FEE002", 10)))
                .payments(List.of(PaymentDto.builder().organisationName("Org 2").build()))
                .serviceRequestStatus(ServiceRequestStatus.NOT_PAID)
                .build()
        );
        stubServiceRequestSearch(caseServiceRequests);

        Optional<ServiceRequestDto> response = serviceRequestSearchService.findUnpaidServiceRequest(
            TEST_CASE_ID, Fee.builder().code("FEE002").amount("1000").build(), TEST_ORG_NAME
        );

        assertThat(response).isEmpty();
    }

    @Test
    void shouldNotReturnServiceRequestWithMatchingFeeCodeAndOrganisationButDifferentAmount() {
        List<ServiceRequestDto> caseServiceRequests = List.of(
            ServiceRequestDto.builder()
                .fees(List.of(buildFeeDto("FEE002", 20)))
                .payments(List.of(PaymentDto.builder().organisationName(TEST_ORG_NAME).build()))
                .serviceRequestStatus(ServiceRequestStatus.NOT_PAID)
                .build()
        );
        stubServiceRequestSearch(caseServiceRequests);

        Optional<ServiceRequestDto> response = serviceRequestSearchService.findUnpaidServiceRequest(
            TEST_CASE_ID, Fee.builder().code("FEE002").amount("1000").build(), TEST_ORG_NAME
        );

        assertThat(response).isEmpty();
    }

    @Test
    void shouldNotReturnServiceRequestWithMatchingAmountAndOrganisationButDifferentFeeCode() {
        List<ServiceRequestDto> caseServiceRequests = List.of(
            ServiceRequestDto.builder()
                .fees(List.of(buildFeeDto("FEE003", 10)))
                .payments(List.of(PaymentDto.builder().organisationName(TEST_ORG_NAME).build()))
                .serviceRequestStatus(ServiceRequestStatus.NOT_PAID)
                .build()
        );
        stubServiceRequestSearch(caseServiceRequests);

        Optional<ServiceRequestDto> response = serviceRequestSearchService.findUnpaidServiceRequest(
            TEST_CASE_ID, Fee.builder().code("FEE002").amount("1000").build(), TEST_ORG_NAME
        );

        assertThat(response).isEmpty();
    }

    private FeeDto buildFeeDto(String feeCode, double amount) {
        return FeeDto.builder()
            .code(feeCode)
            .amountDue(new BigDecimal(amount))
            .build();
    }

    private void stubServiceRequestSearch(List<ServiceRequestDto> caseServiceRequests) {
        var userDetails = UserInfo.builder().uid(SYSTEM_USER_USER_ID).build();
        User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, userDetails);

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(paymentClient.getServiceRequests(user.getAuthToken(), TEST_AUTHORIZATION_TOKEN, String.valueOf(TEST_CASE_ID)))
            .thenReturn(CaseServiceRequestsResponse.builder().serviceRequests(caseServiceRequests).build());
    }
}
