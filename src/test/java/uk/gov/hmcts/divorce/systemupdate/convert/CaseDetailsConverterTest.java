package uk.gov.hmcts.divorce.systemupdate.convert;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig.CASE_TYPE;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig.JURISDICTION;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Created;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.reform.ccd.client.model.Classification.PUBLIC;

@ExtendWith(MockitoExtension.class)
class CaseDetailsConverterTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @InjectMocks
    private CaseDetailsConverter caseDetailsConverter;

    @Test
    void shouldConvertCaseDetailsToReformModelCaseDetails() {

        final long id = 456L;
        final String jurisdiction = "NFD";
        final String caseTypeId = "case type id";
        final int lockedBy = 5;
        final int securityLevel = 5;
        final CaseData caseData = CaseData.builder()
            .application(Application.builder().createdDate(LOCAL_DATE).build())
            .build();
        final String callbackResponseStatus = "Status";

        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        caseDetails.setId(id);
        caseDetails.setJurisdiction(jurisdiction);
        caseDetails.setCaseTypeId(caseTypeId);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);
        caseDetails.setLastModified(LOCAL_DATE_TIME);
        caseDetails.setState(Submitted);
        caseDetails.setLockedBy(lockedBy);
        caseDetails.setSecurityLevel(securityLevel);
        caseDetails.setData(caseData);
        caseDetails.setSecurityClassification(PUBLIC);
        caseDetails.setCallbackResponseStatus(callbackResponseStatus);

        final CaseDetails reformModelCaseDetails = caseDetailsConverter.convertToReformModel(caseDetails);

        assertThat(reformModelCaseDetails.getId()).isEqualTo(id);
        assertThat(reformModelCaseDetails.getJurisdiction()).isEqualTo(jurisdiction);
        assertThat(reformModelCaseDetails.getCaseTypeId()).isEqualTo(caseTypeId);
        assertThat(reformModelCaseDetails.getCreatedDate()).isEqualTo(LOCAL_DATE_TIME);
        assertThat(reformModelCaseDetails.getLastModified()).isEqualTo(LOCAL_DATE_TIME);
        assertThat(reformModelCaseDetails.getState()).isEqualTo(Submitted.getName());
        assertThat(reformModelCaseDetails.getLockedBy()).isEqualTo(lockedBy);
        assertThat(reformModelCaseDetails.getSecurityLevel()).isEqualTo(securityLevel);
        assertThat(reformModelCaseDetails.getData()).isEqualTo(expectedData(caseData));
        assertThat(reformModelCaseDetails.getSecurityClassification()).isEqualTo(PUBLIC);
        assertThat(reformModelCaseDetails.getCallbackResponseStatus()).isEqualTo(callbackResponseStatus);
    }

    @Test
    void shouldConvertToReformModelFromBulkActionCaseDetails() {

        final long id = 456L;
        final int lockedBy = 5;
        final int securityLevel = 5;
        final var caseData = BulkActionCaseData.builder()
            .caseTitle("test")
            .build();

        final String callbackResponseStatus = "Status";

        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> caseDetails =
            new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        caseDetails.setId(id);
        caseDetails.setJurisdiction(JURISDICTION);
        caseDetails.setCaseTypeId(CASE_TYPE);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);
        caseDetails.setLastModified(LOCAL_DATE_TIME);
        caseDetails.setState(Created);
        caseDetails.setLockedBy(lockedBy);
        caseDetails.setSecurityLevel(securityLevel);
        caseDetails.setData(caseData);
        caseDetails.setSecurityClassification(PUBLIC);
        caseDetails.setCallbackResponseStatus(callbackResponseStatus);

        final CaseDetails reformModelCaseDetails = caseDetailsConverter.convertToReformModelFromBulkActionCaseDetails(caseDetails);

        assertThat(reformModelCaseDetails.getId()).isEqualTo(id);
        assertThat(reformModelCaseDetails.getJurisdiction()).isEqualTo(JURISDICTION);
        assertThat(reformModelCaseDetails.getCaseTypeId()).isEqualTo(CASE_TYPE);
        assertThat(reformModelCaseDetails.getCreatedDate()).isEqualTo(LOCAL_DATE_TIME);
        assertThat(reformModelCaseDetails.getLastModified()).isEqualTo(LOCAL_DATE_TIME);
        assertThat(reformModelCaseDetails.getState()).isEqualTo(Created.getName());
        assertThat(reformModelCaseDetails.getLockedBy()).isEqualTo(lockedBy);
        assertThat(reformModelCaseDetails.getSecurityLevel()).isEqualTo(securityLevel);
        assertThat(reformModelCaseDetails.getData()).isEqualTo(expectedBulkCaseData(caseData));
        assertThat(reformModelCaseDetails.getSecurityClassification()).isEqualTo(PUBLIC);
        assertThat(reformModelCaseDetails.getCallbackResponseStatus()).isEqualTo(callbackResponseStatus);
    }


    private Map<Object, Object> expectedData(final CaseData caseData) {
        return objectMapper.convertValue(caseData, new TypeReference<>() {
        });
    }

    private Map<Object, Object> expectedBulkCaseData(final BulkActionCaseData caseData) {
        return objectMapper.convertValue(caseData, new TypeReference<>() {
        });
    }
}
