package uk.gov.hmcts.divorce.solicitor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import uk.gov.hmcts.divorce.solicitor.event.page.SolAboutTheSolicitor;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorUpdateApplicationService;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorUpdateApplication.SOLICITOR_UPDATE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getDivorceDocumentListValue;

@ExtendWith(MockitoExtension.class)
class SolicitorUpdateApplicationTest {

    @Mock
    private SolicitorUpdateApplicationService solicitorUpdateApplicationService;

    @Mock
    private SolAboutTheSolicitor solAboutTheSolicitor;

    @InjectMocks
    private SolicitorUpdateApplication solicitorUpdateApplication;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorUpdateApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_UPDATE);
    }

    @Test
    void shouldCallSolicitorUpdateApplicationAndReturnExpectedCaseData() {

        final var caseData = caseData();
        final var expectedResult = CaseData.builder().build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);
        details.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> expectedDetails = new CaseDetails<>();
        expectedDetails.setData(expectedResult);
        expectedDetails.setId(TEST_CASE_ID);
        expectedDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(solicitorUpdateApplicationService.aboutToSubmit(details)).thenReturn(expectedDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorUpdateApplication.aboutToSubmit(details, details);

        assertThat(response.getData()).isEqualTo(expectedResult);

        verify(solicitorUpdateApplicationService).aboutToSubmit(details);
        verifyNoMoreInteractions(solicitorUpdateApplicationService);
    }

    @Test
    void shouldReturnApp1UploadedDocumentsInDescendingOrderWhenNewDocumentsAreAdded() {

        final ListValue<DivorceDocument> doc1 =
            getDivorceDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003", "co_granted.pdf", CONDITIONAL_ORDER_GRANTED);

        final ListValue<DivorceDocument> doc2 =
            getDivorceDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004", "co_application.pdf", CONDITIONAL_ORDER_APPLICATION);

        final var previousCaseData = caseData();
        previousCaseData.setApplicant1DocumentsUploaded(singletonList(doc1));

        final CaseDetails<CaseData, State> previousCaseDetails = new CaseDetails<>();
        previousCaseDetails.setData(previousCaseData);
        previousCaseDetails.setId(TEST_CASE_ID);
        previousCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final var newCaseData = caseData();
        newCaseData.setApplicant1DocumentsUploaded(List.of(doc1, doc2));

        final CaseDetails<CaseData, State> newCaseDetails = new CaseDetails<>();
        newCaseDetails.setData(newCaseData);
        newCaseDetails.setId(TEST_CASE_ID);
        newCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(solicitorUpdateApplicationService.aboutToSubmit(newCaseDetails)).thenReturn(newCaseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorUpdateApplication.aboutToSubmit(newCaseDetails, previousCaseDetails);

        assertThat(response.getData().getApplicant1DocumentsUploaded().size()).isEqualTo(2);
        assertThat(response.getData().getApplicant1DocumentsUploaded().get(0).getValue()).isSameAs(doc2.getValue());
        assertThat(response.getData().getApplicant1DocumentsUploaded().get(1).getValue()).isSameAs(doc1.getValue());

        verify(solicitorUpdateApplicationService).aboutToSubmit(newCaseDetails);
        verifyNoMoreInteractions(solicitorUpdateApplicationService);
    }

    @Test
    void shouldSkipSortingApp1UploadedDocumentsWhenNoNewDocumentsAreAdded() {

        final ListValue<DivorceDocument> doc1 =
            getDivorceDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003", "co_granted.pdf", CONDITIONAL_ORDER_GRANTED);

        final var previousCaseData = caseData();
        previousCaseData.setApplicant1DocumentsUploaded(singletonList(doc1));

        final CaseDetails<CaseData, State> previousCaseDetails = new CaseDetails<>();
        previousCaseDetails.setData(previousCaseData);
        previousCaseDetails.setId(TEST_CASE_ID);
        previousCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final var newCaseData = caseData();
        newCaseData.setApplicant1DocumentsUploaded(singletonList(doc1));

        final CaseDetails<CaseData, State> newCaseDetails = new CaseDetails<>();
        newCaseDetails.setData(newCaseData);
        newCaseDetails.setId(TEST_CASE_ID);
        newCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(solicitorUpdateApplicationService.aboutToSubmit(newCaseDetails)).thenReturn(newCaseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorUpdateApplication.aboutToSubmit(newCaseDetails, previousCaseDetails);

        assertThat(response.getData().getApplicant1DocumentsUploaded().size()).isEqualTo(1);
        assertThat(response.getData().getApplicant1DocumentsUploaded().get(0).getValue()).isSameAs(doc1.getValue());

        verify(solicitorUpdateApplicationService).aboutToSubmit(newCaseDetails);
        verifyNoMoreInteractions(solicitorUpdateApplicationService);
    }
}
