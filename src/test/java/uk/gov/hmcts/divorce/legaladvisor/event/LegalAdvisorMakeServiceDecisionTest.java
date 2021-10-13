package uk.gov.hmcts.divorce.legaladvisor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.ServiceApplicationTemplateContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServiceConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DISPENSED_AS_SERVICE_GRANTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.ORDER_TO_DISPENSE_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DISPENSE_WITH_SERVICE_GRANTED;
import static uk.gov.hmcts.divorce.legaladvisor.event.LegalAdvisorMakeServiceDecision.LEGAL_ADVISOR_SERVICE_DECISION;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class LegalAdvisorMakeServiceDecisionTest {

    @Mock
    private Clock clock;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private ServiceApplicationTemplateContent serviceApplicationTemplateContent;

    @InjectMocks
    private LegalAdvisorMakeServiceDecision makeServiceDecision;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        makeServiceDecision.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(LEGAL_ADVISOR_SERVICE_DECISION);
    }

    @Test
    void shouldUpdateStateToHoldingAndSetDecisionDateAndGenerateOrderToDispenseDocIfApplicationIsGrantedAndTypeIsDispensed() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .alternativeService(
                AlternativeService
                    .builder()
                    .deemedServiceDate(LocalDate.now(clock))
                    .serviceApplicationGranted(YES)
                    .alternativeServiceType(DISPENSED)
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();
        when(serviceApplicationTemplateContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        String documentUrl = "http://localhost:8080/4567";
        var orderToDispensedDoc = new Document(
            documentUrl,
            "deemedAsServedGranted",
            documentUrl + "/binary"
        );

        when(
            caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                ORDER_TO_DISPENSE_TEMPLATE_ID,
                ENGLISH,
                DISPENSED_AS_SERVICE_GRANTED
            ))
            .thenReturn(orderToDispensedDoc);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            makeServiceDecision.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getAlternativeService().getServiceApplicationDecisionDate())
            .isEqualTo(getExpectedLocalDate());

        assertThat(response.getState()).isEqualTo(Holding);

        var deemedOrDispensedDoc = DivorceDocument
            .builder()
            .documentLink(orderToDispensedDoc)
            .documentFileName(orderToDispensedDoc.getFilename())
            .documentType(DISPENSE_WITH_SERVICE_GRANTED)
            .build();


        assertThat(response.getData().getDocumentsGenerated())
            .extracting("value")
            .containsExactly(deemedOrDispensedDoc);
    }

    @Test
    void shouldNotUpdateStateAndSetServiceApplicationDecisionDateIfServiceApplicationIsNotGranted() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .alternativeService(
                AlternativeService
                    .builder()
                    .deemedServiceDate(LocalDate.now(clock))
                    .serviceApplicationGranted(NO)
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setState(AwaitingServiceConsideration);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            makeServiceDecision.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getAlternativeService().getServiceApplicationDecisionDate())
            .isNull();

        assertThat(response.getState()).isEqualTo(AwaitingServiceConsideration);
    }
}
