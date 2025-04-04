package uk.gov.hmcts.divorce.systemupdate.schedule;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.caseworker.service.notification.StateReportNotification;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralApplicationReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralConsiderationComplete;
import static uk.gov.hmcts.divorce.divorcecase.model.State.NewPaperCase;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;

@Component
@Slf4j
@RequiredArgsConstructor
public class SystemGenerateCurrentStateCountsReport implements Runnable {
    public static final String REPORT_CSV = "Report.csv";
    private final CcdSearchService ccdSearchService;

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    private final StateReportNotification reportNotificationService;

    private static final String ROW_HEADER = "State,Last State Modified Date,Count";
    private static final String ROW_DELIMITER = "\n";
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void run() {
        log.info("SystemGenerateCurrentStateCountsReport scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query = boolQuery()
                .must(termsQuery("state.keyword", List.of(Submitted.name(),AwaitingHWFDecision.name(),
                    OfflineDocumentReceived.name(), NewPaperCase.name(), FinalOrderRequested.name(),
                    GeneralApplicationReceived.name(), GeneralConsiderationComplete.name())));

            Map<String, Map<String, Long>> mapByStateAndLastStateModifiedDate =
                ccdSearchService.countAllCasesByStateAndLastModifiedDate(query, user, serviceAuth);
            String reportName = LocalDateTime.now().format(dateFormatter) + REPORT_CSV;
            ImmutableList.Builder<String> preparedData =
                prepareReportData(mapByStateAndLastStateModifiedDate, reportName);

            reportNotificationService.send(preparedData, reportName);
            log.info("Results are {}", mapByStateAndLastStateModifiedDate.toString());
            log.info("SystemGenerateCurrentStateCountsReport task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("SystemGenerateCurrentStateCountsReport stopped after search error", e);
        } catch (final CcdConflictException | NotificationClientException | IOException e) {
            log.info("SystemGenerateCurrentStateCountsReport stopping "
                + "due to conflict with another running task"
            );
        }
    }

    public ImmutableList.Builder<String> prepareReportData(
        Map<String, Map<String, Long>> mapByStateAndLastStateModifiedDate, String reportName) {
        ImmutableList.Builder<String> fileData = new ImmutableList.Builder<>();
        int rowCount = 0;

        try {
            fileData.add(ROW_HEADER + ROW_DELIMITER);

            for (Map.Entry<String, Map<String, Long>> stateEntry : mapByStateAndLastStateModifiedDate.entrySet()) {
                String state = stateEntry.getKey();
                var caseCountsSortedByDate = stateEntry.getValue().entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .toList();

                for (Map.Entry<String, Long> dateCaseCount : caseCountsSortedByDate) {
                    String lastStateModifiedDate = dateCaseCount.getKey();
                    Long count = dateCaseCount.getValue();

                    fileData.add(state + "," + lastStateModifiedDate + "," + count + ROW_DELIMITER);
                    rowCount++;
                }
            }

            fileData.add("Total rows: " + rowCount + ROW_DELIMITER);

            log.info("Successfully prepared SystemGenerateCurrentStateCountsReport for {}, rowCount: {}", reportName, rowCount);

        } catch (Exception e) {
            // Log error and continue
            log.error("Error occurred while preparing SystemGenerateCurrentStateCountsReport for {}, rowCount:{}, exception: {}",
                reportName, rowCount, e.getMessage());
        }

        return fileData;
    }


}

