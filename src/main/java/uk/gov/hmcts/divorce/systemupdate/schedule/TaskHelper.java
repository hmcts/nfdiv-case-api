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
        ClassPathResource resource = new ClassPathResource(csvFile);
        String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        // normalise newlines; split into lines
        String[] lines = content.replace("\r\n", "\n").replace("\r", "\n").split("\n");

        List<BulkRectifySpec> out = new ArrayList<>();
        for (String rawLine : lines) {
            parseRectifyLine(rawLine).ifPresent(out::add);   // ← all the branching lives in the helper
        }
        return out;
    }

    private java.util.Optional<BulkRectifySpec> parseRectifyLine(final String rawLine) {
        final String line = rawLine == null ? "" : rawLine.trim();
        if (line.isEmpty() || line.startsWith("#")) {
            return java.util.Optional.empty();
        }

        final StringTokenizer tok = new StringTokenizer(line, ",");
        if (!tok.hasMoreTokens()) {
            return java.util.Optional.empty();
        }

        final Long bulkRef = parseId(tok.nextToken());
        if (bulkRef == null) {
            log.warn("Skipping line with invalid bulk ref: {}", line);
            return java.util.Optional.empty();
        }

        final List<Long> caseRefs = new ArrayList<>();
        while (tok.hasMoreTokens()) {
            final Long id = parseId(tok.nextToken());
            if (id != null) {
                caseRefs.add(id);
            }
        }

        if (caseRefs.isEmpty()) {
            log.warn("No valid case refs for bulk {} – skipping line", bulkRef);
            return java.util.Optional.empty();
        }

        return java.util.Optional.of(new BulkRectifySpec(bulkRef, caseRefs));
    }

    private Long parseId(String token) {
        if (token == null) {
            return null;
        }
        String digits = token.trim().replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(digits);
        } catch (NumberFormatException nfe) {
            log.warn("Invalid numeric id token: {}", token);
            return null;
        }
    }

    public record BulkRectifySpec(long bulkRef, List<Long> caseRefs) {
        public BulkRectifySpec {
            caseRefs = List.copyOf(caseRefs); // ensure immutability
        }
    }
}
