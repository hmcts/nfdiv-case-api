package uk.gov.hmcts.divorce.systemupdate.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseTask;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static org.springframework.http.HttpStatus.CONFLICT;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.JURISDICTION;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.getCaseType;

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

    @Autowired
    private BulkCaseDetailsUpdater bulkCaseDetailsUpdater;

    public void submitEvent(final Long caseId,
                            final String eventId,
                            final User user,
                            final String serviceAuth) {

        final String userId = user.getUserDetails().getUid();
        final String authorization = user.getAuthToken();

        log.info("Submit event for Case ID: {}, Event ID: {}", caseId, eventId);

        try {
            startAndSubmitEventForCaseworkers(eventId, serviceAuth, caseId.toString(), userId, authorization);
        } catch (final FeignException e) {

            final String message = format("Submit Event Failed for Case ID: %s, Event ID: %s", caseId, eventId);
            log.info(message, e);
            log.info(e.contentUTF8());

            if (e.status() == CONFLICT.value()) {
                throw new CcdConflictException(message, e);
            }

            throw new CcdManagementException(e.status(), message, e);
        }
    }

    @Retryable(retryFor = {CcdManagementException.class})
    public void submitEventWithRetry(final String caseId,
                                     final String eventId,
                                     final CaseTask caseTask,
                                     final User user,
                                     final String serviceAuth) {

        log.info("Submit event with retry for Case ID: {}, Event ID: {}", caseId, eventId);

        final String userId = user.getUserDetails().getUid();
        final String authorization = user.getAuthToken();

        try {
            final StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
                authorization,
                serviceAuth,
                userId,
                JURISDICTION,
                getCaseType(),
                caseId,
                eventId);

            final CaseDataContent caseDataContent = ccdCaseDataContentProvider.createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseDetailsUpdater.updateCaseData(caseTask, startEventResponse).getData());

            coreCaseDataApi.submitEventForCaseWorker(
                authorization,
                serviceAuth,
                userId,
                JURISDICTION,
                getCaseType(),
                caseId,
                true,
                caseDataContent);
        } catch (FeignException e) {
            final String message = format("Submit Event Failed for Case ID: %s, Event ID: %s", caseId, eventId);
            log.info(message, e);
            log.info(e.contentUTF8());

            throw new CcdManagementException(e.status(), message, e);
        }
    }

    @Retryable(retryFor = {FeignException.class, RuntimeException.class})
    public void updateBulkCaseWithRetries(final BulkCaseTask bulkCaseTask,
                                          final String eventId,
                                          final User authorization,
                                          final String serviceAuth,
                                          final Long caseId) {

        log.info("Submit event with retry for Case ID: {}, Event ID: {}", caseId, eventId);

        final String userId = authorization.getUserDetails().getUid();

        try {
            final StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
                authorization.getAuthToken(),
                serviceAuth,
                userId,
                JURISDICTION,
                BulkActionCaseTypeConfig.getCaseType(),
                String.valueOf(caseId),
                eventId);

            final CaseDataContent caseDataContent = ccdCaseDataContentProvider.createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                bulkCaseDetailsUpdater.updateCaseData(bulkCaseTask, startEventResponse).getData());

            coreCaseDataApi.submitEventForCaseWorker(
                authorization.getAuthToken(),
                serviceAuth,
                userId,
                JURISDICTION,
                BulkActionCaseTypeConfig.getCaseType(),
                String.valueOf(caseId),
                true,
                caseDataContent);
        } catch (FeignException e) {
            final String message = format("Submit Event Failed for Case ID: %s, Event ID: %s", caseId, eventId);
            log.info(message, e);
            log.info(e.contentUTF8());

            throw new CcdManagementException(e.status(), message, e);
        }

    }

    @Retryable(retryFor = {FeignException.class, RuntimeException.class})
    public void updateBulkCaseWithRetries(final String eventId,
                                          final User authorization,
                                          final String serviceAuth,
                                          final Long caseId) {

        log.info("Submit event for Case ID: {}, Event ID: {}", caseId, eventId);
        try {
            final String userId = authorization.getUserDetails().getUid();

            final StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
                authorization.getAuthToken(),
                serviceAuth,
                userId,
                JURISDICTION,
                BulkActionCaseTypeConfig.getCaseType(),
                String.valueOf(caseId),
                eventId
            );

            final CaseDataContent caseDataContent = ccdCaseDataContentProvider.createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                startEventResponse.getCaseDetails().getData());

            coreCaseDataApi.submitEventForCaseWorker(
                authorization.getAuthToken(),
                serviceAuth,
                userId,
                JURISDICTION,
                BulkActionCaseTypeConfig.getCaseType(),
                String.valueOf(caseId),
                true,
                caseDataContent);
        } catch (final FeignException e) {
            final String message = format("Submit Event Failed for Case ID: %s, Event ID: %s", caseId, eventId);
            log.info(message, e);
            log.info(e.contentUTF8());

            throw new CcdManagementException(e.status(), message, e);
        }
    }

    @Retryable(retryFor = {FeignException.class, RuntimeException.class})
    public void updateBulkCaseWithRetries(final Function<CaseDetails, CaseDetails> updateDetailsTask,
                                          final String eventId,
                                          final User authorization,
                                          final String serviceAuth,
                                          final Long caseId) {

        log.info("Submit event for Case ID: {}, Event ID: {}", caseId, eventId);
        try {
            final String userId = authorization.getUserDetails().getUid();

            final StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
                authorization.getAuthToken(),
                serviceAuth,
                userId,
                JURISDICTION,
                BulkActionCaseTypeConfig.getCaseType(),
                String.valueOf(caseId),
                eventId
            );

            final CaseDataContent caseDataContent = ccdCaseDataContentProvider.createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                updateDetailsTask.apply(startEventResponse.getCaseDetails()));

            coreCaseDataApi.submitEventForCaseWorker(
                authorization.getAuthToken(),
                serviceAuth,
                userId,
                JURISDICTION,
                BulkActionCaseTypeConfig.getCaseType(),
                String.valueOf(caseId),
                true,
                caseDataContent);
        } catch (final FeignException e) {
            final String message = format("Submit Event Failed for Case ID: %s, Event ID: %s", caseId, eventId);
            log.info(message, e);
            log.info(e.contentUTF8());

            throw new CcdManagementException(e.status(), message, e);
        }
    }

    private Function<CaseDetails, CaseDetails> removeFailedCasesFromBulkCaseListTask(final List<Long> failedCaseIds) {

        return casedetails -> {

            final uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> internalCaseDetails =
                caseDetailsConverter.convertToBulkActionCaseDetailsFromReformModel(casedetails);

            final List<ListValue<BulkListCaseDetails>> bulkCaseDetailsListValues = internalCaseDetails.getData().getBulkListCaseDetails();

            final Predicate<ListValue<BulkListCaseDetails>> listValuePredicate = lv -> {
                Long caseId = Long.valueOf(lv.getValue().getCaseReference().getCaseReference());
                return failedCaseIds.contains(caseId);
            };

            bulkCaseDetailsListValues.removeIf(listValuePredicate);

            return caseDetailsConverter.convertToReformModelFromBulkActionCaseDetails(internalCaseDetails);
        };
    }

    public void submitBulkActionEvent(final List<Long> failedCaseIds,
                                      final Long caseId,
                                      final String eventId,
                                      final User user,
                                      final String serviceAuth) {

        updateBulkCaseWithRetries(
            removeFailedCasesFromBulkCaseListTask(failedCaseIds),
            eventId,
            user,
            serviceAuth,
            caseId
        );
    }

    public void submitBulkActionEvent(final Long caseId,
                                      final String eventId,
                                      final User user,
                                      final String serviceAuth) {

        updateBulkCaseWithRetries(
            eventId,
            user,
            serviceAuth,
            caseId
        );
    }

    public void submitBulkActionEvent(final BulkCaseTask bulkCaseTask,
                                      final Long caseId,
                                      final String eventId,
                                      final User user,
                                      final String serviceAuth) {

        updateBulkCaseWithRetries(
            bulkCaseTask,
            eventId,
            user,
            serviceAuth,
            caseId
        );
    }

    public void resetOrgAssignedUsersSupplementaryData(final String caseId,
                                                       final String authorisation,
                                                       final String serviceAuth,
                                                       final String orgId) {
        setOrgAssignedUsersSupplementaryData(caseId, authorisation, serviceAuth, orgId, "0");
    }

    public void setOrgAssignedUsersSupplementaryData(final String caseId,
                                                     final String authorisation,
                                                     final String serviceAuth,
                                                     final String orgId,
                                                     final String newValue) {

        Map<String, Map<String, Map<String, Object>>> supplementaryData = new HashMap<>();
        supplementaryData.put("supplementary_data_updates",
            singletonMap("$set",
                Map.of("orgs_assigned_users." + orgId, newValue,
                    "processed", true)));

        submitSupplementaryDataUpdateToCcd(caseId, authorisation, serviceAuth, supplementaryData);
    }

    public void incrementOrgAssignedUsersSupplementaryData(final String caseId,
                                                           final String authorisation,
                                                           final String serviceAuth,
                                                           final String orgId) {
        Map<String, Map<String, Map<String, Object>>> supplementaryData = new HashMap<>();
        supplementaryData.put("supplementary_data_updates",
            singletonMap("$inc",
                singletonMap("orgs_assigned_users." + orgId, "1")));

        submitSupplementaryDataUpdateToCcd(caseId, authorisation, serviceAuth, supplementaryData);
    }

    private void submitSupplementaryDataUpdateToCcd(final String caseId,
                                                    final String authorisation,
                                                    final String serviceAuth,
                                                    final Map<String, Map<String, Map<String, Object>>> supplementaryData) {

        try {
            coreCaseDataApi.submitSupplementaryData(authorisation,
                serviceAuth,
                caseId,
                supplementaryData);
        } catch (FeignException e) {
            log.error("Endpoint threw exception with status: {} \n cause: {}\n and message: {}\n ",
                e.status(), e.getCause(), e.getMessage());
        }
    }

    private void startAndSubmitEventForCaseworkers(final String eventId,
                                                   final String serviceAuth,
                                                   final String caseId,
                                                   final String userId,
                                                   final String authorization) {

        final StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            authorization,
            serviceAuth,
            userId,
            JURISDICTION,
            getCaseType(),
            caseId,
            eventId);

        final CaseDataContent caseDataContent = ccdCaseDataContentProvider.createCaseDataContent(
            startEventResponse,
            DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
            DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
            startEventResponse.getCaseDetails().getData());

        coreCaseDataApi.submitEventForCaseWorker(
            authorization,
            serviceAuth,
            userId,
            JURISDICTION,
            getCaseType(),
            caseId,
            true,
            caseDataContent);
    }
}
