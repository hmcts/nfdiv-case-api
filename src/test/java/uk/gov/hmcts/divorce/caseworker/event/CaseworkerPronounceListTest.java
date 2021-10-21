package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerPronounceList.CASEWORKER_PRONOUNCE_LIST;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createBulkActionConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class CaseworkerPronounceListTest {

    @InjectMocks
    private CaseworkerPronounceList caseworkerPronounceList;

    @Test
    void shouldAddConfigurationToConfigBuilder() {

        final ConfigBuilderImpl<BulkActionCaseData, BulkActionState, UserRole> configBuilder = createBulkActionConfigBuilder();

        caseworkerPronounceList.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_PRONOUNCE_LIST);
    }

    @Test
    void shouldReturnWithNoErrorIfHasJudgePronouncedIsYesForMidEventCallback() {

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails = new CaseDetails<>();
        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .hasJudgePronounced(YES)
            .build();
        caseDetails.setData(bulkActionCaseData);

        final var result = caseworkerPronounceList.midEvent(caseDetails, caseDetails);

        assertThat(result.getErrors()).isNull();
    }

    @Test
    void shouldReturnWithErrorIfHasJudgePronouncedIsNoForMidEventCallback() {

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails = new CaseDetails<>();
        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .hasJudgePronounced(NO)
            .build();
        caseDetails.setData(bulkActionCaseData);

        final var result = caseworkerPronounceList.midEvent(caseDetails, caseDetails);

        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0)).isEqualTo("The judge must have pronounced to continue.");
    }
}