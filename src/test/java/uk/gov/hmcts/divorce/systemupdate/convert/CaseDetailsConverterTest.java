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
    void shouldConvertToReformModelCaseDetailsFromCaseDetails() {

        final long id = 456L;
        final int lockedBy = 5;
        final int securityLevel = 5;
        final CaseData caseData = CaseData.builder()
            .application(Application.builder().createdDate(LOCAL_DATE).build())
            .build();
        final String callbackResponseStatus = "Status";

        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        caseDetails.setId(id);
        caseDetails.setJurisdiction(JURISDICTION);
        caseDetails.setCaseTypeId(CASE_TYPE);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);
        caseDetails.setLastModified(LOCAL_DATE_TIME);
        caseDetails.setState(Submitted);
        caseDetails.setLockedBy(lockedBy);
        caseDetails.setSecurityLevel(securityLevel);
        caseDetails.setData(caseData);
        caseDetails.setSecurityClassification(PUBLIC);
        caseDetails.setCallbackResponseStatus(callbackResponseStatus);

        final CaseDetails reformModelCaseDetails = caseDetailsConverter.convertToReformModelFromCaseDetails(caseDetails);

        assertThat(reformModelCaseDetails.getId()).isEqualTo(id);
        assertThat(reformModelCaseDetails.getJurisdiction()).isEqualTo(JURISDICTION);
        assertThat(reformModelCaseDetails.getCaseTypeId()).isEqualTo(CASE_TYPE);
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
    void shouldConvertToCaseDetailsFromReformModelCaseDetails() {

        final long id = 456L;
        final int lockedBy = 5;
        final int securityLevel = 5;
        final String callbackResponseStatus = "Status";

        final CaseDetails reformCaseDetails = CaseDetails.builder()
            .id(456L)
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .createdDate(LOCAL_DATE_TIME)
            .lastModified(LOCAL_DATE_TIME)
            .state(Submitted.getName())
            .lockedBy(lockedBy)
            .securityLevel(securityLevel)
            .data(Map.of("createdDate", LOCAL_DATE))
            .securityClassification(PUBLIC)
            .callbackResponseStatus(callbackResponseStatus)
            .build();

        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails =
            caseDetailsConverter.convertToCaseDetailsFromReformModel(reformCaseDetails);

        assertThat(caseDetails.getId()).isEqualTo(id);
        assertThat(caseDetails.getJurisdiction()).isEqualTo(JURISDICTION);
        assertThat(caseDetails.getCaseTypeId()).isEqualTo(CASE_TYPE);
        assertThat(caseDetails.getCreatedDate()).isEqualTo(LOCAL_DATE_TIME);
        assertThat(caseDetails.getLastModified()).isEqualTo(LOCAL_DATE_TIME);
        assertThat(caseDetails.getState()).isEqualTo(Submitted);
        assertThat(caseDetails.getLockedBy()).isEqualTo(lockedBy);
        assertThat(caseDetails.getSecurityLevel()).isEqualTo(securityLevel);
        assertThat(caseDetails.getData().getApplication().getCreatedDate()).isEqualTo(LOCAL_DATE);
        assertThat(caseDetails.getSecurityClassification()).isEqualTo(PUBLIC);
        assertThat(caseDetails.getCallbackResponseStatus()).isEqualTo(callbackResponseStatus);
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

    @Test
    void shouldConvertToBulkActionCaseDetailsFromReformModel() {

        final long id = 456L;
        final int lockedBy = 5;
        final int securityLevel = 5;
        final String callbackResponseStatus = "Status";

        final CaseDetails reformCaseDetails = CaseDetails.builder()
            .id(456L)
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .createdDate(LOCAL_DATE_TIME)
            .lastModified(LOCAL_DATE_TIME)
            .state(Created.getName())
            .lockedBy(lockedBy)
            .securityLevel(securityLevel)
            .data(Map.of("caseTitle", "test"))
            .securityClassification(PUBLIC)
            .callbackResponseStatus(callbackResponseStatus)
            .build();

        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> caseDetails =
            caseDetailsConverter.convertToBulkActionCaseDetailsFromReformModel(reformCaseDetails);

        assertThat(caseDetails.getId()).isEqualTo(id);
        assertThat(caseDetails.getJurisdiction()).isEqualTo(JURISDICTION);
        assertThat(caseDetails.getCaseTypeId()).isEqualTo(CASE_TYPE);
        assertThat(caseDetails.getCreatedDate()).isEqualTo(LOCAL_DATE_TIME);
        assertThat(caseDetails.getLastModified()).isEqualTo(LOCAL_DATE_TIME);
        assertThat(caseDetails.getState()).isEqualTo(Created);
        assertThat(caseDetails.getLockedBy()).isEqualTo(lockedBy);
        assertThat(caseDetails.getSecurityLevel()).isEqualTo(securityLevel);
        assertThat(caseDetails.getData().getCaseTitle()).isEqualTo("test");
        assertThat(caseDetails.getSecurityClassification()).isEqualTo(PUBLIC);
        assertThat(caseDetails.getCallbackResponseStatus()).isEqualTo(callbackResponseStatus);
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
