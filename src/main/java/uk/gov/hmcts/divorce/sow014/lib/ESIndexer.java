package uk.gov.hmcts.divorce.sow014.lib;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.Map;
import java.util.Set;

// Simple indexer replicating logstash functionality
// but saving up to ~1GB of RAM.
@Component
@Slf4j
public class ESIndexer {

    private JdbcTemplate db;

    private String esHost;

    private boolean searchEnabled;

    @Autowired
    @SneakyThrows
    public ESIndexer(@Value("${es.search.enabled}") boolean searchEnabled,
                     @Value("${es.host}") String esHost,
                     JdbcTemplate db) {
        this.searchEnabled = searchEnabled;
        this.esHost = esHost;
        this.db = db;

        log.info("Initializing ES Indexer");
        log.info("searchEnabled {}", searchEnabled);

        if (searchEnabled) {
            var t = new Thread(this::index);
            t.setDaemon(true);
            t.setUncaughtExceptionHandler(this.failFast);
            t.setName("****NFD ElasticSearch indexer");
            t.start();
            log.info("ES Indexer started");
        }
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private void index() {

        log.info("Starting ES Indexer");
        try(RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
            new HttpHost(esHost))))
        {
        log.info("es client {}", client.getLowLevelClient().getHttpClient());
        try (Connection c = db.getDataSource().getConnection()) {
            c.setAutoCommit(false);
            while (true) {
                Thread.sleep(250);

                // Replicates the behaviour of the previous logstash configuration.
                // https://github.com/hmcts/rse-cft-lib/blob/94aa0edeb0e1a4337a411ed8e6e20f170ed30bae/cftlib/lib/runtime/compose/logstash/logstash_conf.in#L3
                var results = c.prepareStatement("""
                    with updated as (
                      delete from es_queue es where id in (select id from es_queue limit 2000)
                      returning id
                    )
                      select reference as id, case_type_id, index_id, row_to_json(row)::jsonb as row
                      from (
                        select
                          now() as "@timestamp",
                          version::text as "@version",
                          cd.case_type_id,
                          cd.created_date,
                          ce.data,
                          ce.data_classification,
                          jurisdiction,
                          cd.reference,
                          ce.created_date as last_modified,
                          last_state_modified_date,
                          supplementary_data,
                          lower(cd.case_type_id) || '_cases' as index_id,
                          cd.state,
                          cd.security_classification
                       from updated
                        join case_event ce using(id)
                        join case_data cd on cd.reference = ce.case_reference
                    ) row
                    """).executeQuery();

                BulkRequest request = new BulkRequest();
                while (results.next()) {
                    var row = results.getString("row");
                    request.add(new IndexRequest(results.getString("index_id"))
                        .id(results.getString("id"))
                        .source(row, XContentType.JSON));

                    // Replicate CCD's globalsearch logstash setup.
                    // Where cases define a 'SearchCriteria' field we index certain fields into CCD's central
                    // 'global search' index.
                    // https://github.com/hmcts/cnp-flux-config/blob/master/apps/ccd/ccd-logstash/ccd-logstash.yaml#L99-L175
                    var mapper = new ObjectMapper();
                    Map<String, Object> map = mapper.readValue(row, new TypeReference<>() {
                    });
                    var data = (Map<String, Map<String, Object>>) map.get("data");
                    if (data.containsKey("SearchCriteria")) {
                        filter(data, "SearchCriteria", "caseManagementLocation", "CaseAccessCategory",
                            "caseNameHmctsInternal", "caseManagementCategory");
                        filter((Map<String, Map<String, Object>>) map.get("supplementary_data"), "HMCTSServiceId");
                        filter((Map<String, Map<String, Object>>) map.get("data_classification"), "SearchCriteria",
                            "CaseAccessCategory", "caseManagementLocation", "caseNameHmctsInternal",
                            "caseManagementCategory");
                        map.remove("last_state_modified_date");
                        map.remove("last_modified");
                        map.remove("created_date");
                        map.put("index_id", "global_search");

                        request.add(new IndexRequest("global_search")
                            .id(results.getString("id"))
                            .source(mapper.writeValueAsString(map), XContentType.JSON));
                    }
                }
                if (request.numberOfActions() > 0) {
                    log.info("trying to update index with request {}", request);

                    var r = client.bulk(request, RequestOptions.DEFAULT);
                    if (r.hasFailures()) {
                        log.info("failed to index, **** Cftlib elasticsearch indexing error(s): {}", r.buildFailureMessage());
                    } else {
                        log.info("index updated with status {}", r.status());
                    }
                }
                c.commit();
            }
        }
        } catch (Exception e) {
            //Do nothing
            log.info("failed to index", e.getMessage());
        }
    }

    public void filter(Map<String, Map<String, Object>> map, String... forKeys) {
        if (null != map) {
            var keyset = Set.of(forKeys);
            map.keySet().removeIf(k -> !keyset.contains(k));
        }
    }

    public static final Thread.UncaughtExceptionHandler failFast = (thread, exception) -> {
        log.info("stack trace {}", exception.getMessage());
        log.info("*** Cftlib thread " + thread.getName() + " terminated with an unhandled exception ***");
        log.info("Logs are available in build/cftlib/logs");
        log.info("For further support visit https://moj.enterprise.slack.com/archives/C033F1GDD6Z");
//        Runtime.getRuntime().halt(-1);
    };
}

