package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionPageBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.systemupdate.schedule.TaskHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Created;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Pronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemRectifyBulkList implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {

    public static final String SYSTEM_RECTIFY_BULK_LIST = "system-rectify-bulk-list";
    private static final String CSV_FILE = "rectify-bulk.csv"; // same file your Runnable reads

    private final TaskHelper taskHelper;

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> builder) {
        new BulkActionPageBuilder(builder
            .event(SYSTEM_RECTIFY_BULK_LIST)
            .forStates(Created, Listed, Pronounced)
            .name("System Rectify Bulk List")
            .description("Remove CSV-listed cases from this bulk list only (child cases unchanged)")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE, SUPER_USER)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR));
    }

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> aboutToSubmit(
        final CaseDetails<BulkActionCaseData, BulkActionState> details,
        final CaseDetails<BulkActionCaseData, BulkActionState> before
    ) {
        final long bulkId = details.getId();
        final BulkActionCaseData data = details.getData();

        // 1) Look up the line for THIS bulk in the CSV
        final Set<Long> toRemove = findToRemoveForBulk(bulkId);

        if (toRemove.isEmpty() || data.getBulkListCaseDetails() == null) {
            log.info("Rectify: no removals for bulk {}", bulkId);
            return AboutToStartOrSubmitResponse.<BulkActionCaseData, BulkActionState>builder()
                .data(data)
                .state(details.getState())
                .build();
        }

        // 2) Filter bulkListCaseDetails (preserve order)
        final List<ListValue<BulkListCaseDetails>> filtered = data.getBulkListCaseDetails().stream()
            .filter(lv -> {
                final BulkListCaseDetails v = lv.getValue();
                if (v == null || v.getCaseReference() == null) {
                    return true;
                }
                final CaseLink link = v.getCaseReference();
                return link.getCaseReference() == null
                    || !toRemove.contains(digitsOnly(link.getCaseReference()));
            })
            .toList();

        data.setBulkListCaseDetails(filtered);

        log.info("Rectify: removed {} case(s) from bulk {}: {} ", toRemove.size(), bulkId, toRemove);


        return AboutToStartOrSubmitResponse.<BulkActionCaseData, BulkActionState>builder()
            .data(data)
            .state(details.getState())
            .build();
    }

    private Set<Long> findToRemoveForBulk(final long bulkId) {
        try {
            final List<TaskHelper.BulkRectifySpec> specs = taskHelper.loadRectifyBatches(CSV_FILE);
            for (TaskHelper.BulkRectifySpec spec : specs) {
                if (spec.bulkRef() == bulkId) {
                    return new HashSet<>(spec.caseRefs()); // already digits-only Longs
                }
            }
        } catch (Exception e) {
            log.warn("Rectify: failed reading CSV {} for bulk {}", CSV_FILE, bulkId, e);
        }
        return Set.of();
    }

    private static Long digitsOnly(final String ref) {
        if (ref == null) {
            return null;
        }
        final String digits = ref.replaceAll("\\D", "");
        return digits.isEmpty() ? null : Long.parseLong(digits);
    }
}
