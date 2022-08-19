package uk.gov.hmcts.divorce.common.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.common.event.UpdateConditionalOrder.UPDATE_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_SERVICE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getDivorceDocumentListValue;

@ExtendWith(MockitoExtension.class)
class UpdateConditionalOrderTest {
    @InjectMocks
    private UpdateConditionalOrder updateConditionalOrder;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        updateConditionalOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(UPDATE_CONDITIONAL_ORDER);
    }

    @Test
    void shouldReturnProofOfServiceUploadDocumentsInDescendingOrderWhenNewDocumentsAreAdded() {

        final ListValue<DivorceDocument> doc1 =
            getDivorceDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003", "certificateOfCService.pdf", CERTIFICATE_OF_SERVICE);

        final ListValue<DivorceDocument> doc2 =
            getDivorceDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004", "certificateOfCService2.pdf", CERTIFICATE_OF_SERVICE);

        final var previousCaseData = caseData();
        previousCaseData.getConditionalOrder().setProofOfServiceUploadDocuments(singletonList(doc1));

        final CaseDetails<CaseData, State> previousCaseDetails = new CaseDetails<>();
        previousCaseDetails.setData(previousCaseData);
        previousCaseDetails.setId(TEST_CASE_ID);
        previousCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final var newCaseData = caseData();
        newCaseData.getConditionalOrder().setProofOfServiceUploadDocuments(List.of(doc1, doc2));

        final CaseDetails<CaseData, State> newCaseDetails = new CaseDetails<>();
        newCaseDetails.setData(newCaseData);
        newCaseDetails.setId(TEST_CASE_ID);
        newCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            updateConditionalOrder.aboutToSubmit(newCaseDetails, previousCaseDetails);

        final List<ListValue<DivorceDocument>> proofOfServiceUploadDocuments = response
            .getData()
            .getConditionalOrder()
            .getProofOfServiceUploadDocuments();

        assertThat(proofOfServiceUploadDocuments).hasSize(2);
        assertThat(proofOfServiceUploadDocuments.get(0).getValue()).isSameAs(doc2.getValue());
        assertThat(proofOfServiceUploadDocuments.get(1).getValue()).isSameAs(doc1.getValue());
    }

    @Test
    void shouldSkipSortingProofOfServiceUploadDocumentsWhenNoNewDocumentsAreAdded() {

        final ListValue<DivorceDocument> doc1 =
            getDivorceDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003", "certificateOfCService.pdf", CERTIFICATE_OF_SERVICE);

        final var previousCaseData = caseData();
        previousCaseData.getConditionalOrder().setProofOfServiceUploadDocuments(singletonList(doc1));

        final CaseDetails<CaseData, State> previousCaseDetails = new CaseDetails<>();
        previousCaseDetails.setData(previousCaseData);
        previousCaseDetails.setId(TEST_CASE_ID);
        previousCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final var newCaseData = caseData();
        newCaseData.getConditionalOrder().setProofOfServiceUploadDocuments(singletonList(doc1));

        final CaseDetails<CaseData, State> newCaseDetails = new CaseDetails<>();
        newCaseDetails.setData(newCaseData);
        newCaseDetails.setId(TEST_CASE_ID);
        newCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            updateConditionalOrder.aboutToSubmit(newCaseDetails, previousCaseDetails);

        final List<ListValue<DivorceDocument>> proofOfServiceUploadDocuments = response
            .getData()
            .getConditionalOrder()
            .getProofOfServiceUploadDocuments();

        assertThat(proofOfServiceUploadDocuments).hasSize(1);
        assertThat(proofOfServiceUploadDocuments.get(0).getValue()).isSameAs(doc1.getValue());
    }
}
