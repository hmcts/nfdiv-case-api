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
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.util.Map;
import java.util.Set;

// Simple indexer replicating logstash functionality
// but saving up to ~1GB of RAM.
@Component
@Slf4j
public class ESIndexer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate db;

    @Value("${es.host}")
    private String esHost;

    @Value("${es.search.enabled}")
    private boolean searchEnabled;

    @Override
    public void run(String... args) throws Exception {
        if (searchEnabled) {
            var t = new Thread(this::index);
            t.setDaemon(true);
            t.setUncaughtExceptionHandler(this.failFast);
            t.setName("****NFD ElasticSearch indexer");
            t.start();
        }
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private void index() {

        try {
        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
            new HttpHost(esHost, 9200)));
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
                        var r = client.bulk(request, RequestOptions.DEFAULT);
                        if (r.hasFailures()) {
                            throw new RuntimeException("**** Cftlib elasticsearch indexing error(s): "
                                + r.buildFailureMessage());
                        }
                    }
                    c.commit();
                }
            }
        } catch (Exception e) {

            log.info("failed to index", e);
        }
    }

    public void filter(Map<String, Map<String, Object>> map, String... forKeys) {
        if (null != map) {
            var keyset = Set.of(forKeys);
            map.keySet().removeIf(k -> !keyset.contains(k));
        }
    }

    public static final Thread.UncaughtExceptionHandler failFast = (thread, exception) -> {
        exception.printStackTrace();
        System.out.println("*** Cftlib thread " + thread.getName() + " terminated with an unhandled exception ***");
        System.out.println("Logs are available in build/cftlib/logs");
        System.out.println("For further support visit https://moj.enterprise.slack.com/archives/C033F1GDD6Z");
        Runtime.getRuntime().halt(-1);
    };
}

