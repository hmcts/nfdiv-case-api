package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.CitizenGeneralApplicationSubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class ExpireUnpaidSearchGovRecordsApplicationsTest {

    @Mock
    private CitizenGeneralApplicationSubmissionService generalApplicationSubmissionService;

    @InjectMocks
    private ExpireUnpaidSearchGovRecordsApplications expireUnpaidSearchGovRecordsApplications;

    @Test
    void shouldLeaveCaseDetailsUnchangedIfNoApplicationExists() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        when(generalApplicationSubmissionService.findActiveGeneralApplication(caseData, caseData.getApplicant1()))
            .thenReturn(Optional.empty());

        final CaseDetails<CaseData, State> result = expireUnpaidSearchGovRecordsApplications.apply(caseDetails);

        assertThat(result).isEqualTo(caseDetails);
    }

    @Test
    void shouldLeaveCaseDetailsUnchangedIfActiveGenAppIsNotSearchGovRecords() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseData.getApplicant1().setGeneralAppServiceRequest(TEST_SERVICE_REFERENCE);

        when(generalApplicationSubmissionService.findActiveGeneralApplication(caseData, caseData.getApplicant1()))
            .thenReturn(Optional.of(
                GeneralApplication.builder()
                    .generalApplicationType(GeneralApplicationType.DEEMED_SERVICE)
                    .build()
            ));

        final CaseDetails<CaseData, State> result = expireUnpaidSearchGovRecordsApplications.apply(caseDetails);

        assertThat(result.getData().getApplicant1().getGeneralAppServiceRequest()).isEqualTo(TEST_SERVICE_REFERENCE);
    }

    @Test
    void shouldBlankOutServiceRequestIfSearchGovRecordsAppIsInProgress() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseData.getApplicant1().setGeneralAppServiceRequest(TEST_SERVICE_REFERENCE);

        when(generalApplicationSubmissionService.findActiveGeneralApplication(caseData, caseData.getApplicant1()))
            .thenReturn(Optional.of(
                GeneralApplication.builder()
                    .generalApplicationType(GeneralApplicationType.DISCLOSURE_VIA_DWP)
                    .build()
            ));

        final CaseDetails<CaseData, State> result = expireUnpaidSearchGovRecordsApplications.apply(caseDetails);

        assertThat(result.getData().getApplicant1().getGeneralAppServiceRequest()).isEqualTo(null);
    }
}
