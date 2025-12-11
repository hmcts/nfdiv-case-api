package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.systemupdate.schedule.TaskHelper;
import uk.gov.hmcts.divorce.testutil.ConfigTestUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemRectifyBulkListTest {

    @Mock
    private TaskHelper taskHelper;

    @InjectMocks
    private SystemRectifyBulkList event; // your CCDConfig class

    private BulkActionCaseData data;
    private CaseDetails<BulkActionCaseData, BulkActionState> details;

    @BeforeEach
    void setUp() {
        data = new BulkActionCaseData();
        data.setBulkListCaseDetails(List.of(
            lv(caseRef("1234-5678-9012-3456")),
            lv(caseRef("1111-2222-3333-4444")),
            lv(caseRef("9999-8888-7777-6666"))
        ));

        details = new CaseDetails<>();
        details.setId(1758254429226124L); // bulk id under test
        details.setData(data);
        details.setState(BulkActionState.Created);
    }

    @Test
    void configure_executesWithoutError() {
        ConfigBuilderImpl<BulkActionCaseData, BulkActionState, UserRole> builder =
            ConfigTestUtil.createBulkActionConfigBuilder();

        SystemRectifyBulkList event1 = new SystemRectifyBulkList(new TaskHelper());

        assertDoesNotThrow(() -> event1.configure(builder));

    }

    @Test
    void aboutToSubmit_removesOnlyListedCasesForThisBulk_preservesOrder() throws Exception {
        // CSV provides removals for this bulk + another bulk to ensure scoping
        TaskHelper.BulkRectifySpec thisBulk = new TaskHelper.BulkRectifySpec(
            1758254429226124L,
            List.of(1234567890123456L, 9999888877776666L) // digits-only
        );
        TaskHelper.BulkRectifySpec otherBulk = new TaskHelper.BulkRectifySpec(
            1758261653985127L,
            List.of(1111222233334444L)
        );

        when(taskHelper.loadRectifyBatches("rectify-bulk.csv"))
            .thenReturn(List.of(thisBulk, otherBulk));

        var resp = event.aboutToSubmit(details, null);
        var out = resp.getData().getBulkListCaseDetails();

        // Only 1111-2222-3333-4444 should remain (order preserved among remaining)
        assertEquals(1, out.size());
        assertEquals("1111-2222-3333-4444", out.get(0).getValue().getCaseReference().getCaseReference());
    }

    @Test
    void aboutToSubmit_noMatch_noChange() throws Exception {
        TaskHelper.BulkRectifySpec otherBulkOnly = new TaskHelper.BulkRectifySpec(
            1758261653985127L,
            List.of(1234567890123456L)
        );

        when(taskHelper.loadRectifyBatches("rectify-bulk.csv"))
            .thenReturn(List.of(otherBulkOnly));

        var resp = event.aboutToSubmit(details, null);
        var out = resp.getData().getBulkListCaseDetails();

        // Nothing removed
        assertEquals(3, out.size());
        assertEquals("1234-5678-9012-3456", out.get(0).getValue().getCaseReference().getCaseReference());
        assertEquals("1111-2222-3333-4444", out.get(1).getValue().getCaseReference().getCaseReference());
        assertEquals("9999-8888-7777-6666", out.get(2).getValue().getCaseReference().getCaseReference());
    }

    private static ListValue<BulkListCaseDetails> lv(CaseLink link) {
        BulkListCaseDetails value = BulkListCaseDetails.builder()
            .caseReference(link)
            .build();
        return ListValue.<BulkListCaseDetails>builder().value(value).build();
    }

    private static CaseLink caseRef(String hyphenated) {
        return CaseLink.builder().caseReference(hyphenated).build();
    }

    @Test
    void aboutToSubmit_handlesCsvReadFailure_gracefully() throws Exception {
        when(taskHelper.loadRectifyBatches("rectify-bulk.csv"))
            .thenThrow(new RuntimeException("Broken CSV file"));

        List<String> originalRefs = data.getBulkListCaseDetails().stream()
            .map(lv -> lv.getValue().getCaseReference().getCaseReference())
            .toList();

        var resp = event.aboutToSubmit(details, null);

        List<String> afterRefs = resp.getData().getBulkListCaseDetails().stream()
            .map(lv -> lv.getValue().getCaseReference().getCaseReference())
            .toList();

        assertEquals(originalRefs, afterRefs);
    }

}
