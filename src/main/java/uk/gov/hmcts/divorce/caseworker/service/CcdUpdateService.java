package uk.gov.hmcts.divorce.caseworker.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.User;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.CONFLICT;
import static uk.gov.hmcts.divorce.ccd.NoFaultDivorce.CASE_TYPE;
import static uk.gov.hmcts.divorce.ccd.NoFaultDivorce.JURISDICTION;

@Service
@Slf4j
public class CcdUpdateService {

    private static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY = "No Fault Divorce case submission event";
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION = "Submitting No Fault Divorce Case Event";

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private CcdCaseDataContentProvider ccdCaseDataContentProvider;

    public void submitEvent(final CaseDetails caseDetails, final String eventId) {

        final User caseWorkerDetails = idamService.retrieveCaseWorkerDetails();
        final String serviceAuthorization = authTokenGenerator.generate();
        final String caseId = caseDetails.getId().toString();
        final String userId = caseWorkerDetails.getUserDetails().getId();
        final String authorization = caseWorkerDetails.getAuthToken();

        log.info("Submit event for Case ID: {}, Event ID: {}", caseId, eventId);

        try {
            final StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
                authorization,
                serviceAuthorization,
                userId,
                JURISDICTION,
                CASE_TYPE,
                caseId,
                eventId);

            final CaseDataContent caseDataContent = ccdCaseDataContentProvider.createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseDetails.getData());

            coreCaseDataApi.submitEventForCaseWorker(
                authorization,
                serviceAuthorization,
                userId,
                JURISDICTION,
                CASE_TYPE,
                caseId,
                true,
                caseDataContent);
        } catch (final FeignException e) {

            final String message = format("Submit Event Failed for Case ID: %s, Event ID: %s", caseId, eventId);
            log.info(message, e);

            if (e.status() == CONFLICT.value()) {
                throw new CcdConflictException(message, e);
            }

            throw new CcdManagementException(message, e);
        }
    }
}
