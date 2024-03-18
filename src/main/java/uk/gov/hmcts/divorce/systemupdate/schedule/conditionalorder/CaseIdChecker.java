package uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import uk.gov.hmcts.divorce.document.print.exception.InvalidResourceException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class CaseIdChecker {

    private final AtomicReference<List<Long>> caseIds = new AtomicReference<>();

    public boolean isCaseIdValid(long caseId) {
        List<Long> ids = caseIds.get();
        if (ids == null) {
            synchronized (this) {
                ids = caseIds.get();
                if (ids == null) {
                    try {
                        ids = loadCaseIds();
                        caseIds.set(ids);
                    } catch (IOException e) {
                        throw new InvalidResourceException("Failed to load the case ids", e);
                    }
                }
            }
        }
        return ids.contains(caseId);
    }

    private List<Long> loadCaseIds() throws IOException {
        ClassPathResource resource = new ClassPathResource("relevant_ids.txt");
        String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        return Arrays.stream(content.split("\\s*,\\s*"))
            .map(String::trim) // Trim leading and trailing whitespace
            .filter(s -> !s.isEmpty()) // Filter out empty strings
            .map(Long::parseLong)
            .toList();
    }
}

