package uk.gov.hmcts.divorce.notification;

import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

public final class SendEmailFromNotify {

    private static final String apiKey = "TODO use UK_GOV_NOTIFY_API_KEY from .aat-env";
    private static final String EMAIL_ADDRESS = "hmctstest@mailinator.com";

    private static final String NEW_UNIVERSAL_TEMPLATE_ID = "94125fa9-7d37-44e5-aa45-1ce92b6d0547";
    private static final String JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW_TEMPLATE_ID ="d7e4af3e-53b4-4372-ac28-296e43182a21";

    private SendEmailFromNotify() {
    }

    public static void main(String[] args) {

        Map<String, Object> data = new HashMap<>();
        data.put("isReminder", "no");
        data.put("isDivorce", "yes");
        data.put("isDissolution", "no");
        data.put("create account link", "https://nfdiv-apply-for-divorce.aat.platform.hmcts.net/applicant2");
        data.put("reference number", "1689-5874-3896-7384");
        data.put("date of response", "2 August 2023");
        data.put("partner", "husband");
        data.put("access code", "3ZZYAKV7");

        sendEmail(JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW_TEMPLATE_ID, data);

        data.remove("isReminder");
        data.put("isReminder", false);
        data.remove("isDivorce");
        data.put("isDivorce", true);
        data.remove("isDissolution");
        data.put("isDissolution", false);

        Map<String, String> templateVars = prepareTemplateVars(data);

        sendEmail(NEW_UNIVERSAL_TEMPLATE_ID, templateVars);
    }

    private static Map<String, String> prepareTemplateVars(final Map data) {
        String subjectStringTemplate =
            "{{#isReminder}}Reminder:{{/isReminder}}  Application {{#isDivorce}}for your divorce{{/isDivorce}}{{#isDissolution}}to end your civil partnership{{/isDissolution}}";
        String bodyStringTemplate =
            "Your {{partner}} has created {{#isDivorce}}a divorce application{{/isDivorce}}{{#isDissolution}}an application to end your civil partnership{{/isDissolution}}. They have indicated they want to submit it jointly with you. If you want to join this application you need to review and confirm the information by {{date of response}}. This means you will be applying jointly. \r\n\r\n1. Create your {{#isDivorce}}divorce account{{/isDivorce}}{{#isDissolution}}account to end your civil partnership{{/isDissolution}} and sign in {{create account link}}\r\n2. Enter your unique reference number: {{reference number}}\r\n2. Enter your access code: {{access code}}\r\n\r\nIf you do not want to join the application, then you can either ignore this notification or contact your {{partner}}, if it's safe to do so. \r\n\r\nIf you do not join this application then your {{partner}} can still submit a sole application. This would mean you would have to respond to the {{#isDivorce}}divorce{{/isDivorce}} application instead of joining it. You can also make a sole application, if you want.\r\n\r\nDivorce and Dissolution\r\nHM Courts & Tribunals Service\r\ndivorcecase@justice.gov.uk\r\n0300 303 0642 (Monday to Friday, 8am to 6pm)\r\n\r\nYour feedback helps improve this service for others: https://www.smartsurvey.co.uk/s/Divorce_Feedback/?pageurl=/email";

        Template subjectTemplate = Mustache.compiler().compile(subjectStringTemplate);
        Template bodyTemplate = Mustache.compiler().compile(bodyStringTemplate);

        final String subject = subjectTemplate.execute(data);
        final String body = bodyTemplate.execute(data);

        Map<String, String> templateVars = new HashMap<>();
        templateVars.put("subject", subject);
        templateVars.put("body", body);
        return templateVars;
    }

    private static NotificationClient getNotificationClient() {
        return new NotificationClient(apiKey);
    }

    private static void sendEmail(String notifyTemplateId, Map templateVars){
        NotificationClient client = getNotificationClient();

        try {
            client.sendEmail(
                notifyTemplateId,
                EMAIL_ADDRESS,
                templateVars,
                UUID.randomUUID().toString());
        } catch (NotificationClientException e) {
            throw new RuntimeException(e);
        }
    }
}
