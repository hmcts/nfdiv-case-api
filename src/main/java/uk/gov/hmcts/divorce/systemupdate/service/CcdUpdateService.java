package uk.gov.hmcts.divorce.systemupdate.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.springframework.http.HttpStatus.CONFLICT;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.CASE_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.JURISDICTION;

@Service
@Slf4j
public class CcdUpdateService {

    private static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY = "No Fault Divorce case submission event";
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION = "Submitting No Fault Divorce Case Event";

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private CcdCaseDataContentProvider ccdCaseDataContentProvider;

    @Autowired
    private CaseDetailsConverter caseDetailsConverter;

    @Autowired
    private CaseDetailsUpdater caseDetailsUpdater;

    public void submitEvent(final CaseDetails caseDetails,
                            final String eventId,
                            final User user,
                            final String serviceAuth) {

        final String caseId = caseDetails.getId().toString();
        final String userId = user.getUserDetails().getId();
        final String authorization = user.getAuthToken();

        log.info("Submit event for Case ID: {}, Event ID: {}", caseId, eventId);

        try {
            startAndSubmitEventForCaseworkers(caseDetails, eventId, serviceAuth, caseId, userId, authorization);
        } catch (final FeignException e) {

            final String message = format("Submit Event Failed for Case ID: %s, Event ID: %s", caseId, eventId);
            log.info(message, e);

            if (e.status() == CONFLICT.value()) {
                throw new CcdConflictException(message, e);
            }

            throw new CcdManagementException(message, e);
        }
    }

    public void submitEvent(final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails,
                            final String eventId,
                            final User user,
                            final String serviceAuth) {

        submitEvent(caseDetailsConverter.convertToReformModelFromCaseDetails(caseDetails), eventId, user, serviceAuth);
    }

    @Retryable(value = {FeignException.class, RuntimeException.class})
    public void submitEventWithRetry(final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails,
                                     final String eventId,
                                     final User user,
                                     final String serviceAuth) {

        submitEvent(caseDetails, eventId, user, serviceAuth);
    }

    @Retryable(value = {FeignException.class, RuntimeException.class})
    public void submitEventWithRetry(final CaseDetails caseDetails,
                                     final String eventId,
                                     final User user,
                                     final String serviceAuth) {

        submitEvent(caseDetails, eventId, user, serviceAuth);
    }

    @Retryable(value = {FeignException.class, RuntimeException.class})
    public void submitEventWithRetry(final String caseId,
                                     final String eventId,
                                     final CaseTask caseTask,
                                     final User user,
                                     final String serviceAuth) {

        final String userId = user.getUserDetails().getId();
        final String authorization = user.getAuthToken();

        final StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            authorization,
            serviceAuth,
            userId,
            JURISDICTION,
            CASE_TYPE,
            caseId,
            eventId);

        final Map<String, Object> data = startEventResponse.getCaseDetails().getData();

        //TODO: Remove temp logging for tracking certificate of entitlement
        log.info("****** Start event response for case id: {}, certificate of entitlement: {}",
            caseId,
            data.get("coCertificateOfEntitlementDocument"));

        final CaseData caseData = caseDetailsUpdater.updateCaseData(caseTask, startEventResponse).getData();

        //TODO: Remove temp logging for tracking certificate of entitlement
        final DivorceDocument certificateOfEntitlementDocument = caseData.getConditionalOrder().getCertificateOfEntitlementDocument();
        if (nonNull(certificateOfEntitlementDocument)) {
            log.info("****** After CaseData updated for case id: {}, certificate of entitlement: {}",
                caseId,
                certificateOfEntitlementDocument);
        } else {
            log.info("****** After CaseData updated for case id: {}, certificate of entitlement: NULL", caseId);
        }

        final CaseDataContent caseDataContent = ccdCaseDataContentProvider.createCaseDataContent(
            startEventResponse,
            DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
            DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
            caseData);

        coreCaseDataApi.submitEventForCaseWorker(
            authorization,
            serviceAuth,
            userId,
            JURISDICTION,
            CASE_TYPE,
            caseId,
            true,
            caseDataContent);
    }

    @Retryable(value = {FeignException.class, RuntimeException.class})
    public void updateBulkCaseWithRetries(final CaseDetails caseDetails,
                                          final String eventId,
                                          final User authorization,
                                          final String serviceAuth,
                                          final Long caseId) {

        log.info("Submit event for Case ID: {}, Event ID: {}", caseId, eventId);
        try {
            final String userId = authorization.getUserDetails().getId();

            final StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
                authorization.getAuthToken(),
                serviceAuth,
                userId,
                JURISDICTION,
                BulkActionCaseTypeConfig.CASE_TYPE,
                String.valueOf(caseId),
                eventId
            );

            final CaseDataContent caseDataContent = ccdCaseDataContentProvider.createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseDetails.getData());

            coreCaseDataApi.submitEventForCaseWorker(
                authorization.getAuthToken(),
                serviceAuth,
                userId,
                JURISDICTION,
                BulkActionCaseTypeConfig.CASE_TYPE,
                String.valueOf(caseId),
                true,
                caseDataContent);
        } catch (final FeignException e) {
            final String message = format("Submit Event Failed for Case ID: %s, Event ID: %s", caseId, eventId);
            log.info(message, e);

            throw new CcdManagementException(message, e);
        }
    }

    private void startAndSubmitEventForCaseworkers(final CaseDetails caseDetails,
                                                   final String eventId,
                                                   final String serviceAuth,
                                                   final String caseId,
                                                   final String userId,
                                                   final String authorization) {

        final StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            authorization,
            serviceAuth,
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
            serviceAuth,
            userId,
            JURISDICTION,
            CASE_TYPE,
            caseId,
            true,
            caseDataContent);
    }

    public void submitBulkActionEvent(final uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> caseDetails,
                                      final String eventId,
                                      final User user,
                                      final String serviceAuth) {

        updateBulkCaseWithRetries(
            caseDetailsConverter.convertToReformModelFromBulkActionCaseDetails(caseDetails),
            eventId,
            user,
            serviceAuth,
            caseDetails.getId()
        );
    }
}
