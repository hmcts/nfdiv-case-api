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
}
