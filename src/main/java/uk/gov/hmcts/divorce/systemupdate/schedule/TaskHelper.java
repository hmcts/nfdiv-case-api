package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

@Component
@Slf4j
public class TaskHelper {

    public List<Long> loadCaseIds(String csvFile) throws IOException {
        ClassPathResource resource = new ClassPathResource(csvFile);
        String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        StringTokenizer tokenizer = new StringTokenizer(content, ",");
        List<Long> idList = new ArrayList<>();

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim(); // Trim whitespace from each token
            idList.add(Long.valueOf(token));
        }

        return idList;
    }

    public void logError(String message, Long arg, Exception e) {
        log.error(message, arg, e);
    }


    /** CSV format: one line per bulk case -> "bulkRef, caseRef1, caseRef2, ..." */
    public List<BulkRectifySpec> loadRectifyBatches(final String csvFile) throws IOException {
        try {
            ClassPathResource resource = new ClassPathResource(csvFile);
            String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            String[] lines = content.replace("\r\n", "\n").replace("\r", "\n").split("\n");
            List<BulkRectifySpec> out = new ArrayList<>();
            for (String rawLine : lines) {
                parseLine(rawLine, out);
            }
            return out;
        } catch (Exception e) {
            return List.of();
        }
    }

    private void parseLine(String rawLine, List<BulkRectifySpec> out) {
        try {
            if (rawLine == null || rawLine.trim().isEmpty() || rawLine.trim().startsWith("#")) {
                return;
            }
            StringTokenizer tok = new StringTokenizer(rawLine, ",");
            long bulkRef = Long.parseLong(tok.nextToken().trim().replaceAll("\\D", ""));
            List<Long> caseRefs = new ArrayList<>();
            while (tok.hasMoreTokens()) {
                String t = tok.nextToken().trim().replaceAll("\\D", "");
                if (!t.isEmpty()) {
                    caseRefs.add(Long.parseLong(t));
                }
            }
            if (!caseRefs.isEmpty()) {
                out.add(new BulkRectifySpec(bulkRef, caseRefs));
            }
        } catch (Exception ex) {
            // skip any line with errors
            logError("Skipping entry with error", null, ex);
        }
    }

    public record BulkRectifySpec(long bulkRef, List<Long> caseRefs) {
        public BulkRectifySpec {
            caseRefs = List.copyOf(caseRefs);
        }
    }
}
