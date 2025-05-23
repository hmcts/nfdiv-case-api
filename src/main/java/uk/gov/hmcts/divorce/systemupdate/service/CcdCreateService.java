package uk.gov.hmcts.divorce.systemupdate.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig.getCaseType;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CreateBulkList.CREATE_BULK_LIST;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.JURISDICTION;

@Service
@Slf4j
@RequiredArgsConstructor
public class CcdCreateService {

    private static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY = "No Fault Divorce case submission event";
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION = "Submitting No Fault Divorce Case Event";

    private final CoreCaseDataApi coreCaseDataApi;

    private final CcdCaseDataContentProvider ccdCaseDataContentProvider;

    private final CaseDetailsConverter caseDetailsConverter;

    public uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> createBulkCase(
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> caseDetails,
        final User user,
        final String serviceAuth) {

        final String userId = user.getUserDetails().getUid();
        final String authorization = user.getAuthToken();

        try {

            final StartEventResponse startEventResponse = coreCaseDataApi.startForCaseworker(
                authorization,
                serviceAuth,
                userId,
                JURISDICTION,
                getCaseType(),
                CREATE_BULK_LIST
            );

            final CaseDataContent caseDataContent = ccdCaseDataContentProvider.createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseDetails.getData());

            final CaseDetails bulkCaseDetails = coreCaseDataApi.submitForCaseworker(
                authorization,
                serviceAuth,
                userId,
                JURISDICTION,
                getCaseType(),
                true,
                caseDataContent);

            return caseDetailsConverter.convertToBulkActionCaseDetailsFromReformModel(bulkCaseDetails);

        } catch (final FeignException e) {
            throw new CcdManagementException(e.status(), "Bulk case creation failed", e);
        }
    }
}
