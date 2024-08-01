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
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.service.notification.FinalOrderGrantedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceGeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrderAuthorisation;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentGenerator;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGrantFinalOrder.CASEWORKER_GRANT_FINAL_ORDER;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGrantFinalOrder.ERROR_CASE_NOT_ELIGIBLE;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGrantFinalOrder.ERROR_NO_CO_GRANTED_DATE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerGrantFinalOrderTest {

    @Mock
    private Clock clock;

    @Mock
    private DocumentGenerator documentGenerator;

    @Mock
    private FinalOrderGrantedNotification finalOrderGrantedNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private CaseworkerGrantFinalOrder caseworkerGrantFinalOrder;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerGrantFinalOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_GRANT_FINAL_ORDER);
    }

    @Test
    public void shouldPopulateDynamicListWithGeneralOrderWhenFinalOrderIsOverdue() {
        final CaseData caseData = caseData();
        caseData.setConditionalOrder(ConditionalOrder.builder().grantedDate(LocalDate.now()).build());
        caseData.setFinalOrder(FinalOrder.builder()
                .isFinalOrderOverdue(YesOrNo.YES)
                .dateFinalOrderEligibleFrom(LocalDate.now())
            .build());
        caseData.setGeneralOrders(List.of(
            ListValue.<DivorceGeneralOrder>builder()
                .id(UUID.randomUUID().toString())
                .value(DivorceGeneralOrder.builder()
                    .generalOrderDocument(DivorceDocument.builder()
                        .documentType(DocumentType.GENERAL_ORDER)
                        .documentFileName("generalOrder1")
                        .build())
                    .build())

                .build(),
            ListValue.<DivorceGeneralOrder>builder()
                .id(UUID.randomUUID().toString())
                .value(DivorceGeneralOrder.builder()
                    .generalOrderDocument(DivorceDocument.builder()
                        .documentType(DocumentType.GENERAL_ORDER)
                        .documentFileName("generalOrder2")
                        .build())
                    .build())

                .build()
        ));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerGrantFinalOrder.aboutToStart(details);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getData().getDocuments().getGeneralOrderDocumentNames().getListItems()
            .stream().map(DynamicListElement::getLabel)).containsAll(List.of("generalOrder1", "generalOrder2"));
    }

    @Test
    public void shouldNotPopulateDynamicListWithGeneralOrderWhenFinalOrderIsNotOverdue() {
        final CaseData caseData = caseData();
        caseData.setConditionalOrder(ConditionalOrder.builder().grantedDate(LocalDate.now()).build());
        caseData.setFinalOrder(FinalOrder.builder()
            .dateFinalOrderEligibleFrom(LocalDate.now())
            .build());
        caseData.setGeneralOrders(List.of(
            ListValue.<DivorceGeneralOrder>builder()
                .id(UUID.randomUUID().toString())
                .value(DivorceGeneralOrder.builder()
                    .generalOrderDocument(DivorceDocument.builder()
                        .documentType(DocumentType.GENERAL_ORDER)
                        .documentFileName("generalOrder1")
                        .build())
                    .build())

                .build(),
            ListValue.<DivorceGeneralOrder>builder()
                .id(UUID.randomUUID().toString())
                .value(DivorceGeneralOrder.builder()
                    .generalOrderDocument(DivorceDocument.builder()
                        .documentType(DocumentType.GENERAL_ORDER)
                        .documentFileName("generalOrder2")
                        .build())
                    .build())

                .build()
        ));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerGrantFinalOrder.aboutToStart(details);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getData().getDocuments().getGeneralOrderDocumentNames()).isNull();
    }

    @Test
    public void shouldReturnErrorWhenCOGrantedDateIsNotSet() {
        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerGrantFinalOrder.aboutToStart(details);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors())
            .isEqualTo(Collections.singletonList(ERROR_NO_CO_GRANTED_DATE));
    }

    @Test
    void shouldPopulateFinalOrderGrantedDateAndSendEmailIfFinalOrderIsEligible() {
        final CaseData caseData = caseData();
        caseData.setFinalOrder(
            FinalOrder.builder()
                .granted(Set.of(FinalOrder.Granted.YES))
                .dateFinalOrderEligibleFrom(LocalDate.now())
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        setMockClock(clock);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerGrantFinalOrder.aboutToSubmit(details, details);

        assertThat(response.getData().getFinalOrder().getGrantedDate()).isNotNull();
        assertThat(response.getData().getFinalOrder().getGrantedDate()).isEqualTo(getExpectedLocalDateTime());

        verify(documentGenerator).generateAndStoreCaseDocument(eq(FINAL_ORDER_GRANTED),
            eq(FINAL_ORDER_TEMPLATE_ID),
            eq(FINAL_ORDER_DOCUMENT_NAME),
            any(),
            anyLong());
    }

    @Test
    void shouldGenerateFinalOrderGrantedCoverLetterIfApplicantOrRespondentIsOffline() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(YesOrNo.YES);
        caseData.getApplicant2().setOffline(YesOrNo.YES);
        caseData.getApplicant2().setEmail("");
        caseData.setFinalOrder(
            FinalOrder.builder()
                .granted(Set.of(FinalOrder.Granted.YES))
                .dateFinalOrderEligibleFrom(LocalDate.now())
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        setMockClock(clock);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerGrantFinalOrder.aboutToSubmit(details, details);

        assertThat(response.getData().getFinalOrder().getGrantedDate()).isNotNull();
        assertThat(response.getData().getFinalOrder().getGrantedDate()).isEqualTo(getExpectedLocalDateTime());

        verify(documentGenerator).generateAndStoreCaseDocument(eq(FINAL_ORDER_GRANTED),
            eq(FINAL_ORDER_TEMPLATE_ID),
            eq(FINAL_ORDER_DOCUMENT_NAME),
            any(),
            anyLong());
    }

    @Test
    void shouldReturnErrorsIfDateFinalOrderEligibleFromIsInFuture() {
        final CaseData caseData = caseData();
        caseData.setFinalOrder(
            FinalOrder.builder()
                .granted(Set.of(FinalOrder.Granted.YES))
                .dateFinalOrderEligibleFrom(LocalDate.now().plusDays(1))
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        setMockClock(clock);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerGrantFinalOrder.aboutToSubmit(details, details);

        assertThat(response.getData().getFinalOrder().getGrantedDate()).isNull();
        assertThat(response.getErrors()).contains(ERROR_CASE_NOT_ELIGIBLE);

        verifyNoInteractions(documentGenerator);
    }

    @Test
    public void shouldSetGeneralOrderGrantingFinalOrderWhenFinalOrderIsOverdue() {
        final CaseData caseData = caseData();
        caseData.setFinalOrder(
            FinalOrder.builder()
                .isFinalOrderOverdue(YesOrNo.YES)
                .granted(Set.of(FinalOrder.Granted.YES))
                .dateFinalOrderEligibleFrom(LocalDate.now())
                .build()
        );

        caseData.getFinalOrder().setOverdueFinalOrderAuthorisation(
            FinalOrderAuthorisation.builder()
                .finalOrderJudgeName("The Judge")
                .build()
        );

        caseData.setGeneralOrders(
            List.of(
                ListValue.<DivorceGeneralOrder>builder()
                    .value(DivorceGeneralOrder.builder()
                        .generalOrderDocument(DivorceDocument.builder()
                            .documentFileName("generalOrder1")
                            .build())
                        .build())
                    .build()
            )
        );

        caseData.getDocuments().setGeneralOrderDocumentNames(
            DynamicList.builder()
                .value(DynamicListElement.builder()
                    .label("generalOrder1")
                    .build())
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        setMockClock(clock);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerGrantFinalOrder.aboutToSubmit(details, details);

        assertThat(response.getData().getFinalOrder().getGrantedDate()).isNotNull();
        assertThat(response.getData().getFinalOrder().getGrantedDate()).isEqualTo(getExpectedLocalDateTime());
        assertThat(response.getData().getFinalOrder()
            .getOverdueFinalOrderAuthorisation()
            .getFinalOrderGeneralOrder()
            .getGeneralOrderDocument()
            .getDocumentFileName()).isEqualTo("generalOrder1");

    }

    @Test
    void shouldSendNotificationWhenSubmittedCallbackIsInvoked() {
        final CaseData caseData = caseData();
        caseData.setFinalOrder(
            FinalOrder.builder()
                .granted(Set.of(FinalOrder.Granted.YES))
                .dateFinalOrderEligibleFrom(LocalDate.now())
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);
        setMockClock(clock);

        caseworkerGrantFinalOrder.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(finalOrderGrantedNotification, caseData, TEST_CASE_ID);
    }
}
