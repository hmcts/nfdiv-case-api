package uk.gov.hmcts.divorce.caseworker.event;

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
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.payment.model.PaymentStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static java.time.ZoneId.systemDefault;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUploadDocumentsAndSubmit.CASEWORKER_UPLOAD_DOCUMENTS_AND_SUBMIT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getDivorceDocumentListValue;

@ExtendWith(MockitoExtension.class)
class CaseworkerUploadDocumentsAndSubmitTest {

    @Mock
    private Clock clock;

    @InjectMocks
    private CaseworkerUploadDocumentsAndSubmit caseworkerUploadDocumentsAndSubmit;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerUploadDocumentsAndSubmit.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_UPLOAD_DOCUMENTS_AND_SUBMIT);
    }

    @Test
    void shouldBlankDocumentUploadComplete() {
        final var caseData = CaseData.builder().build();
        caseData.getApplication().setDocumentUploadComplete(NO);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final var response =
            caseworkerUploadDocumentsAndSubmit.aboutToStart(caseDetails);

        assertThat(response.getData().getApplication().getDocumentUploadComplete()).isNull();
    }

    @Test
    void shouldRemainInAwaitingDocumentsStateIfDocumentUploadNotComplete() {

        final var caseData = CaseData.builder().build();
        caseData.getApplication().setDocumentUploadComplete(NO);
        caseData.getApplication().setApplicant1WantsToHavePapersServedAnotherWay(YES);
        caseData.getApplication().setApplicant1CannotUploadSupportingDocument(Set.of(DocumentType.CORRESPONDENCE));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingDocuments);

        final var response =
            caseworkerUploadDocumentsAndSubmit.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(AwaitingDocuments);
        assertThat(response.getData().getApplication().getApplicant1WantsToHavePapersServedAnotherWay()).isNull();
        assertThat(response.getData().getApplication().getApplicant1CannotUploadSupportingDocument()).isNull();
    }

    @Test
    void shouldTransitionFromAwaitingDocumentsToSubmittedIfDocumentUploadIsComplete() {

        final var instant = Instant.now();
        final var zoneId = systemDefault();
        final var expectedDateTime = LocalDateTime.ofInstant(instant, zoneId);
        final var orderSummary = OrderSummary.builder().paymentTotal("55000").build();

        final var payment = new ListValue<>(null, Payment
            .builder()
            .amount(55000)
            .status(PaymentStatus.SUCCESS)
            .build());

        final var application = Application.builder()
            .applicationFeeOrderSummary(orderSummary)
            .solSignStatementOfTruth(YES)
            .documentUploadComplete(YES)
            .applicant1WantsToHavePapersServedAnotherWay(YES)
            .applicant1CannotUploadSupportingDocument(Set.of(DocumentType.CORRESPONDENCE))
            .applicationPayments(singletonList(payment))
            .build();

        final var caseData = CaseData.builder()
            .application(application)
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingDocuments);

        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(zoneId);

        final var response =
            caseworkerUploadDocumentsAndSubmit.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(Submitted);
        assertThat(response.getData().getApplication().getDateSubmitted()).isEqualTo(expectedDateTime);
    }

    @Test
    void shouldReturnApp1UploadedDocumentsInDescendingOrderWhenNewDocumentsAreAdded() {

        final ListValue<DivorceDocument> doc1 =
            getDivorceDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003", "co_granted.pdf", CONDITIONAL_ORDER_GRANTED);

        final ListValue<DivorceDocument> doc2 =
            getDivorceDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004", "co_application.pdf", CONDITIONAL_ORDER_APPLICATION);

        final var previousCaseData = caseData();
        previousCaseData.setApplicant1DocumentsUploaded(singletonList(doc1));

        final CaseDetails<CaseData, State> previousCaseDetails = caseDetails(previousCaseData);

        final var newCaseData = caseData();
        newCaseData.getApplication().setDocumentUploadComplete(NO);
        newCaseData.setApplicant1DocumentsUploaded(List.of(doc1, doc2));

        final CaseDetails<CaseData, State> newCaseDetails = caseDetails(newCaseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUploadDocumentsAndSubmit.aboutToSubmit(newCaseDetails, previousCaseDetails);

        assertThat(response.getData().getApplicant1DocumentsUploaded().size()).isEqualTo(2);
        assertThat(response.getData().getApplicant1DocumentsUploaded().get(0).getValue()).isSameAs(doc2.getValue());
        assertThat(response.getData().getApplicant1DocumentsUploaded().get(1).getValue()).isSameAs(doc1.getValue());
    }

    private CaseDetails<CaseData, State> caseDetails(CaseData caseData) {
        final CaseDetails<CaseData, State> previousCaseDetails = new CaseDetails<>();
        previousCaseDetails.setData(caseData);
        previousCaseDetails.setId(TEST_CASE_ID);
        previousCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        return previousCaseDetails;
    }
}
