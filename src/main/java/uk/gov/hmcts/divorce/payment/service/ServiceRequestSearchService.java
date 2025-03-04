package uk.gov.hmcts.divorce.payment.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.payment.client.PaymentClient;
import uk.gov.hmcts.divorce.payment.model.ServiceRequestDto;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.payment.FeesAndPayConstants.SINGLE_USE_FEE_CODES;
import static uk.gov.hmcts.divorce.payment.FeesAndPaymentsUtil.penceToPounds;
import static uk.gov.hmcts.divorce.payment.model.ServiceRequestStatus.NOT_PAID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceRequestSearchService {
    private final PaymentClient paymentClient;

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    public Optional<ServiceRequestDto> findUnpaidServiceRequest(Long caseId, Fee fee, String responsibleParty) {
        List<ServiceRequestDto> serviceRequests;
        final User user = idamService.retrieveSystemUpdateUserDetails();

        try {
            serviceRequests = paymentClient.getServiceRequests(
                user.getAuthToken(),
                authTokenGenerator.generate(),
                String.valueOf(caseId)
            ).getServiceRequests();

            return serviceRequests.stream()
                .filter(sr -> serviceRequestIsUnpaidAndMatchesFee(sr, fee, responsibleParty))
                .findAny();
        } catch (FeignException e) {
            log.info("No service requests found for: {}, Exception: {}", caseId, e.getMessage());
        }

        return Optional.empty();
    }

    private boolean serviceRequestIsUnpaidAndMatchesFee(ServiceRequestDto sr, Fee fee, String responsibleParty) {
        if (sr == null || isEmpty(sr.getFees()) || !NOT_PAID.equals(sr.getServiceRequestStatus())) {
            return false;
        }

        String expectedFee = fee.getCode();
        BigDecimal expectedAmountDue = new BigDecimal(penceToPounds(fee.getAmount()));

        boolean feeCodeMatches = sr.getFees().stream()
            .anyMatch(f -> expectedFee.equals(f.getCode()) && expectedAmountDue.compareTo(f.getAmountDue()) == 0);
        boolean feeExpectedOncePerCase = SINGLE_USE_FEE_CODES.contains(fee.getCode());
        boolean srCreatedByTheSameParty = isNotEmpty(sr.getPayments()) && sr.getPayments().stream()
            .anyMatch(p -> isNotBlank(responsibleParty) && responsibleParty.equals(p.getOrganisationName()));

        return feeCodeMatches && (feeExpectedOncePerCase || srCreatedByTheSameParty);
    }
}
