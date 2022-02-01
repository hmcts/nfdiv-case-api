package uk.gov.hmcts.divorce.systemupdate.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;

import static java.lang.String.format;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.CASE_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.JURISDICTION;

@Service
@Slf4j
public class CcdFetchCaseService {

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Retryable(value = {FeignException.class, RuntimeException.class})
    public CaseDetails fetchCaseById(final String caseId,
                                     final User user,
                                     final String serviceAuth) {

        final String userId = user.getUserDetails().getId();
        final String authorization = user.getAuthToken();

        log.info("Fetching case {} from CCD", caseId);

        try {
            return coreCaseDataApi.readForCaseWorker(
                authorization,
                serviceAuth,
                userId,
                JURISDICTION,
                CASE_TYPE,
                caseId
            );
        } catch (final FeignException e) {
            final String message = format("Fetch case failed for case ID: %s", caseId);
            log.info(message, e);
            log.info(e.contentUTF8());

            throw new CcdSearchCaseException(message, e);
        }
    }
}
