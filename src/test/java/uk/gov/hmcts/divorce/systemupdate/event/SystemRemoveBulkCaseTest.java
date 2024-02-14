package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemoveBulkCase.SYSTEM_REMOVE_BULK_CASE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithOrderSummary;

@ExtendWith(SpringExtension.class)
public class SystemRemoveBulkCaseTest {

    @InjectMocks
    private SystemRemoveBulkCase systemRemoveBulkCase;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemRemoveBulkCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_REMOVE_BULK_CASE);
    }

    @Test
    public void shouldUnlinkCaseFromTheBulkCaseAndChangeStateIfInIncorrectState() {
        LocalDateTime dateTime = LocalDateTime.of(2022, 8, 10, 10, 0);

        ConditionalOrder conditionalOrder = ConditionalOrder.builder()
            .granted(YesOrNo.YES)
            .grantedDate(dateTime.minusDays(10).toLocalDate())
            .decisionDate(dateTime.minusDays(8).toLocalDate())
            .dateAndTimeOfHearing(dateTime)
            .pronouncementJudge("Judge")
            .court(ConditionalOrderCourt.BIRMINGHAM)
            .certificateOfEntitlementDocument(new DivorceDocument())
            .build();

        final CaseData caseData = caseDataWithOrderSummary();
        caseData.setBulkListCaseReferenceLink(CaseLink.builder()
                .caseReference("1")
            .build());
        caseData.setConditionalOrder(conditionalOrder);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);
        details.setState(AwaitingPronouncement);

        final AboutToStartOrSubmitResponse<CaseData, State> response = systemRemoveBulkCase.aboutToSubmit(details, details);

        ConditionalOrder updatedConditionalOrder = response.getData().getConditionalOrder();

        assertThat(updatedConditionalOrder.getGranted()).isEqualTo(YesOrNo.YES);
        assertThat(updatedConditionalOrder.getGrantedDate()).isEqualTo(dateTime.minusDays(10).toLocalDate());
        assertThat(updatedConditionalOrder.getDecisionDate()).isEqualTo(dateTime.minusDays(8).toLocalDate());
        assertThat(updatedConditionalOrder.getCertificateOfEntitlementDocument()).isNull();
        assertThat(updatedConditionalOrder.getCourt()).isNull();
        assertThat(updatedConditionalOrder.getPronouncementJudge()).isNull();
        assertThat(updatedConditionalOrder.getDateAndTimeOfHearing()).isNull();
        assertThat(response.getData().getBulkListCaseReferenceLink()).isNull();
        assertThat(response.getState()).isEqualTo(AwaitingPronouncement);
    }

    @Test
    public void shouldUnlinkCaseFromTheBulkCaseAndRetainStateIfInCorrectState() {
        LocalDateTime dateTime = LocalDateTime.of(2022, 8, 10, 10, 0);

        ConditionalOrder conditionalOrder = ConditionalOrder.builder()
            .granted(YesOrNo.YES)
            .grantedDate(dateTime.minusDays(10).toLocalDate())
            .decisionDate(dateTime.minusDays(8).toLocalDate())
            .dateAndTimeOfHearing(dateTime)
            .pronouncementJudge("Judge")
            .court(ConditionalOrderCourt.BIRMINGHAM)
            .certificateOfEntitlementDocument(new DivorceDocument())
            .build();

        final CaseData caseData = caseDataWithOrderSummary();
        caseData.setBulkListCaseReferenceLink(CaseLink.builder()
            .caseReference("1")
            .build());
        caseData.setConditionalOrder(conditionalOrder);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);
        details.setState(ConditionalOrderPronounced);

        final AboutToStartOrSubmitResponse<CaseData, State> response = systemRemoveBulkCase.aboutToSubmit(details, details);

        ConditionalOrder updatedConditionalOrder = response.getData().getConditionalOrder();

        assertThat(updatedConditionalOrder.getGranted()).isEqualTo(YesOrNo.YES);
        assertThat(updatedConditionalOrder.getGrantedDate()).isEqualTo(dateTime.minusDays(10).toLocalDate());
        assertThat(updatedConditionalOrder.getDecisionDate()).isEqualTo(dateTime.minusDays(8).toLocalDate());
        assertThat(updatedConditionalOrder.getCertificateOfEntitlementDocument()).isNull();
        assertThat(updatedConditionalOrder.getCourt()).isNull();
        assertThat(updatedConditionalOrder.getPronouncementJudge()).isNull();
        assertThat(updatedConditionalOrder.getDateAndTimeOfHearing()).isNull();
        assertThat(response.getData().getBulkListCaseReferenceLink()).isNull();
        assertThat(response.getState()).isEqualTo(ConditionalOrderPronounced);
    }
}
