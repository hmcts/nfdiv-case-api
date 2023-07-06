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
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.caseworker.service.notification.FinalOrderGrantedNotification;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateFinalOrder;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateFinalOrderCoverLetter;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceGeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ExpeditedFinalOrderAuthorisation;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerExpediteFinalOrder.CASEWORKER_EXPEDITE_FINAL_ORDER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.GENERAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getDivorceDocumentListValue;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getDivorceGeneralOrderListValue;

@ExtendWith(MockitoExtension.class)
class CaseworkerExpediteFinalOrderTest {

    @Mock
    private Clock clock;

    @Mock
    private GenerateFinalOrder generateFinalOrder;

    @Mock
    private GenerateFinalOrderCoverLetter generateFinalOrderCoverLetter;

    @Mock
    private FinalOrderGrantedNotification finalOrderGrantedNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private CaseworkerExpediteFinalOrder caseworkerExpediteFinalOrder;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerExpediteFinalOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_EXPEDITE_FINAL_ORDER);
    }

    @Test
    void shouldReturnErrorIfNoGeneralOrderDocuments() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerExpediteFinalOrder.aboutToStart(details);

        assertThat(response.getErrors()).isNotNull();
    }

    @Test
    void shouldPopulateGeneralOrderDocumentNamesDynamicList() {
        final CaseDetails<CaseData, State> details = getCaseDetails();

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerExpediteFinalOrder.aboutToStart(details);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getData().getDocuments().getGeneralOrderDocumentNames().getListItems()).hasSize(1);
        assertThat(response.getData().getDocuments().getGeneralOrderDocumentNames().getValue().getLabel())
            .isEqualTo("generalOrderDocumentName");
    }

    @Test
    void shouldReturnErrorIfGeneralOrderDocumentNotFound() {
        final CaseDetails<CaseData, State> details = getCaseDetailsWithSelectedGeneralOrderDocument();
        final CaseData caseData = details.getData();

        caseData.getDocuments().getGeneralOrderDocumentNames().getValue().setLabel("NA");

        setMockClock(clock);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerExpediteFinalOrder.aboutToSubmit(details, details);

        assertThat(response.getErrors()).isNotNull();
    }

    @Test
    void shouldPopulateFinalOrderGrantedDateAndSendEmail() {
        final CaseDetails<CaseData, State> details = getCaseDetailsWithSelectedGeneralOrderDocument();

        setMockClock(clock);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerExpediteFinalOrder.aboutToSubmit(details, details);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getData()
            .getFinalOrder()
            .getExpeditedFinalOrderAuthorisation()
            .getExpeditedFinalOrderGeneralOrder()
            .getGeneralOrderDocument()
            .getDocumentFileName())
            .isEqualTo("generalOrderDocumentName");
        assertThat(response.getData().getFinalOrder().getExpeditedFinalOrderAuthorisation().getExpeditedFinalOrderJudgeName())
            .isEqualTo("JudgeName");
        assertThat(response.getData().getFinalOrder().getGrantedDate()).isNotNull();
        assertThat(response.getData().getFinalOrder().getGrantedDate()).isEqualTo(getExpectedLocalDateTime());

        verify(generateFinalOrderCoverLetter).apply(details);
        verify(generateFinalOrder).apply(details);
    }

    @Test
    void shouldSendNotificationWhenSubmittedCallbackIsInvoked() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        caseworkerExpediteFinalOrder.submitted(details, details);

        verify(notificationDispatcher).send(finalOrderGrantedNotification, caseData, TEST_CASE_ID);
    }

    CaseDetails<CaseData, State> getCaseDetails() {
        final CaseData caseData = caseData();
        final DivorceDocument generalOrderDoc = getDivorceDocumentListValue(
            "http://localhost:8080/1234",
            "generalOrderDocumentName",
            GENERAL_ORDER
        ).getValue();
        final ListValue<DivorceGeneralOrder> generalOrder = getDivorceGeneralOrderListValue(
            generalOrderDoc.getDocumentLink(),
            UUID.randomUUID().toString()
        );
        caseData.setGeneralOrders(singletonList(generalOrder));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        return details;
    }

    CaseDetails<CaseData, State> getCaseDetailsWithSelectedGeneralOrderDocument() {
        final CaseDetails<CaseData, State> details = getCaseDetails();
        final CaseData caseData = details.getData();
        final String generalOrderDocumentFilename = caseData
            .getGeneralOrders().get(0).getValue().getGeneralOrderDocument().getDocumentFileName();
        caseData.setFinalOrder(
            FinalOrder.builder()
                .granted(Set.of(FinalOrder.Granted.YES))
                .dateFinalOrderEligibleFrom(LocalDate.now())
                .expeditedFinalOrderAuthorisation(ExpeditedFinalOrderAuthorisation.builder()
                    .expeditedFinalOrderJudgeName("JudgeName")
                    .build())
                .build()
        );

        final List<DynamicListElement> generalOrderDocumentNames = singletonList(DynamicListElement.builder()
            .label(generalOrderDocumentFilename)
            .code(UUID.randomUUID())
            .build());

        final DynamicList generalOrderDocumentNamesDynamicList = DynamicList
            .builder()
            .value(DynamicListElement.builder().label("generalOrderDocumentName").code(UUID.randomUUID()).build())
            .listItems(generalOrderDocumentNames)
            .build();

        caseData.getDocuments().setGeneralOrderDocumentNames(generalOrderDocumentNamesDynamicList);

        details.setData(caseData);

        return details;
    }
}
