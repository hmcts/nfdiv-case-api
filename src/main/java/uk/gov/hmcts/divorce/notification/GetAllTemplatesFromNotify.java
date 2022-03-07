package uk.gov.hmcts.divorce.notification;

import lombok.extern.slf4j.Slf4j;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.Template;
import uk.gov.service.notify.TemplateList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// GetAllTemplatesFromNotify is a standalone util class which allows you to get all templates from Notify
// which contain a given search criteria.
@Slf4j
@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
public final class GetAllTemplatesFromNotify {

    private static final List<String> exclusions = List.of("OLD", "NOT_USED");

    private static final String searchCriteria = "Enter search criteria here";
    private static final String notificationType = "email";

    private static final String apiKey = "";

    private GetAllTemplatesFromNotify() {
    }

    public static void main(String[] args) {

        NotificationClient client = getNotificationClient();

        try {
            TemplateList templates = client.getAllTemplates(notificationType);

            log.info("Found " + templates.getTemplates().size() + " templates in total");

            // Uncomment if searching template values.
            searchTemplateValues(templates);

            // uncomment if searching template body contents.
            searchTemplateBody(templates);

        } catch (NotificationClientException e) {
            e.printStackTrace(System.out);
        }
    }

    public static NotificationClient getNotificationClient() {
        return new NotificationClient(apiKey);
    }

    private static void searchTemplateValues(TemplateList templates) {

        int count = 0;

        for (Template template: templates.getTemplates()) {

            Map<String, Object> personalisation = template.getPersonalisation().orElse(new HashMap<>());

            if (personalisation.containsKey(searchCriteria)) {
                log.info("************************************************************");

                if (isRetiredTemplate(template.getName())) {
                    log.info("Excluding retired template: " + template.getName());
                    continue;
                }
                printFoundTemplate(template);
                count++;
            }
        }

        log.info("\nThere were " + count + " templates found with key " + searchCriteria);
    }

    private static void searchTemplateBody(TemplateList templates) {

        int count = 0;

        for (Template template: templates.getTemplates()) {

            String body = template.getBody();

            if (body.contains(searchCriteria)) {
                log.info("************************************************************");

                if (isRetiredTemplate(template.getName())) {
                    log.info("Excluding retired template: " + template.getName());
                    continue;
                }
                printFoundTemplate(template);
                count++;
            }
        }

        log.info("\nThere were " + count + " templates found with text '" + searchCriteria + "'");
    }

    private static void printFoundTemplate(Template template) {

        log.info("Name: " + template.getName());
        log.info("ID: " + template.getId());
        log.info("Type: " + template.getTemplateType());

        // Optional<String> subject = template.getSubject();
        // subject.ifPresent(s -> log.info("Subject: " + s));
        // log.info("Body is: " + template.getBody());
    }

    private static boolean isRetiredTemplate(String str) {
        return exclusions.stream().anyMatch(str::contains);
    }
}
