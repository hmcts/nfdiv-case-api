package uk.gov.hmcts.divorce.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.Template;
import uk.gov.service.notify.TemplateList;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private static final Set<String> emailTemplateIdsFromApplicationYaml = Set.of(
        "00c04fec-dc80-4ec9-82f1-f6eab43d656c",
        "01c401de-a71d-466c-b279-5bb9759a8cc4",
        "03d15445-c91d-44b0-a8b0-a7755864381e",
        "040e2529-50d7-4b1b-a083-016349edce63",
        "086715a5-6a1b-4d70-8a30-0fc50b62535e",
        "0936ca51-ab0d-4e97-b95b-9d306bbe74f9",
        "0b0b9648-3551-45f0-ac23-ebfc15c01633",
        "0babadb5-acba-4735-ba87-b7a666949efb",
        "0c8de3fa-2294-41d0-8060-3de20bc23712",
        "0d8c59f1-aea3-4245-8bca-eb0d5558f0fd",
        "0e70b66c-dba8-4b7d-8f2e-9f1d05614310",
        "1162cc6f-3d04-4d1d-8240-4754c5242803",
        "171a6120-4e41-4d71-99d7-90d10e2f5b9c",
        "1845c608-0642-48e9-b91d-839b53ac9b9e",
        "199df8f7-f237-405a-ada7-094149ee2830",
        "1a133c4b-6853-47a6-bbc7-3a3b6baa130a",
        "1bd19b93-132f-43a6-97ba-ca71c39ea3bc",
        "1e70a276-237a-44f1-bbb5-8e9eb1a0ac3a",
        "1f4cd3f9-1092-4703-826c-0685131e6dff",
        "1fb44d0d-c743-4bff-9e5c-6a62d3711517",
        "239fc7ea-c717-40b6-8e6b-483e12af493c",
        "248d19a5-b00b-4c57-a283-10054558003c",
        "24d6bb85-82de-49a5-ae97-aec15ecc0ee8",
        "26aaa755-2cb1-422c-be80-08ff15f9be27",
        "27e89abf-c793-4891-97ce-f54cd9ac0803",
        "28139c85-180d-456c-b40c-36c4e04afa5d",
        "29c1cdb1-bf0c-4f27-bb52-b99f040e8f53",
        "2b44b8b2-8a2e-4029-974e-bd14458b1f0b",
        "2b606152-85f4-43fc-9e2f-48edfd15f077",
        "2b7cc04e-2c21-4c06-9ae4-e7109a3efde4",
        "2d79f93a-aabc-470a-b5f5-333059787a8a",
        "325a1603-499c-4a96-af50-92931d4b68ff",
        "32778ad1-0315-41cc-927b-2c008be35435",
        "33b20df5-0967-490b-ac94-daa1569468d7",
        "34116ad2-305f-4db8-852f-9d7652c2ae41",
        "351edcb3-5193-4883-9095-19ef6617fa8a",
        "355c1651-7909-4886-80aa-1cc59f9e888d",
        "356a4b59-fbaf-4367-9b47-dbd05340b073",
        "35dde12d-cc37-4ccd-b712-788cba8590ce",
        "360465c3-2da5-403a-8163-5487cc214aa7",
        "38d403a8-a3a2-4cbf-bfd6-de406583047c",
        "39aac458-5f31-442e-bee7-0f97bb584d98",
        "39def49e-2d2d-489e-b715-3103e27d5977",
        "3a12c67b-da4a-4f56-acff-71bc7f4efa24",
        "3aff9dc0-0188-42f6-a0be-fc5daa404d9a",
        "3e8df6d1-3426-490c-8d8f-4816065eb5b8",
        "3ecccc69-81e7-4bcd-8316-0547fd30795f",
        "3f2e50a1-b4ae-4466-bb8c-eac55563aa02",
        "3f42a757-f635-4f58-be27-d65af450a2ee",
        "40951ec2-011d-4ae0-8b61-ef457e0cf2a1",
        "42784df1-bf2e-4a8a-b82b-4cc7825d4d3a",
        "444fbd4b-5c40-4e42-99a0-761cd07933a6",
        "44724f77-1ab4-44c7-9b0d-229d25059f05",
        "471ea2f0-9be6-40bf-93e5-148f66fb9b56",
        "4789bd02-0d9b-40cc-ab08-4790b0480323",
        "488c2dd6-d38f-4869-b716-0743be52c6cb",
        "4c5ce6c2-2a4c-447a-b887-0a5c08f65025",
        "4d0e2dca-1af3-446d-997d-70b46f99bd73",
        "4fa1278a-8c8c-49da-b1ee-9d289f5e2436",
        "508ec880-2585-411f-bc2b-4316217fb5d8",
        "544c4297-e1c5-44bc-8f02-5661494beb4f",
        "57f59d4a-6332-4361-bf41-5092cafb8047",
        "59ab58b7-b102-4b17-b023-226ce399668b",
        "5f9d4cbe-623e-4066-ae87-76f9afeae884",
        "60e1d3e6-48b9-48de-8c13-173c620c665e",
        "61332826-713e-49a7-ba37-563cb9261c65",
        "61a8ec4f-b0cf-45d9-a419-29cae2c811c7",
        "63b41176-67eb-4ad7-9e3d-fa8aabd77c6a",
        "68b87fab-f99f-49c2-a3df-04415d003eb9",
        "68e04e81-0dd3-4837-9459-050eaf362abe",
        "6a6eb8ca-17e8-49a7-adf6-6f3cd4e1b546",
        "6ad4cf12-90b0-4663-878a-0e2f627e4959",
        "6c9270fc-82c4-4355-b0e4-b88a778ef5db",
        "6caef42a-91d7-40e5-b440-cbb48daf1415",
        "6db9a294-1613-4926-867a-4302cb0ad6dc",
        "6dcfc4fa-bacc-455b-be66-0c0b429c4d41",
        "6e9526c5-9380-4bf1-879a-b8e401e39a6f",
        "700d4e14-7eae-4219-b2e6-be01c31e071f",
        "705321e1-adda-4dc8-b46d-e5519418821a",
        "70dd90fe-e702-4daa-8f69-4ab41518d9a6",
        "71337384-3fdb-4eb8-8adb-be5e4e59857a",
        "72a3f5f8-598f-420b-8041-3f6541e48779",
        "7636dc7a-039d-4963-805d-33f539a0b48d",
        "7795bcbb-d13a-466f-b74b-ef2948712083",
        "7cddc0bf-5b2f-4b4a-b4b6-28cfc22d1cd5",
        "7d86d77b-125e-4ea6-ac09-d9aa0b755714",
        "7d8db5c5-4b9c-4221-8fed-8c542d45a6af",
        "7e5978a9-4ceb-45ca-9a41-b3f2822a0824",
        "7e64d645-8973-4bac-95ca-49e54c423655",
        "7e72dfe4-d414-4597-9d72-9ae5d91e8376",
        "80ec944d-2791-42ec-9551-1f76da6f7eb4",
        "83592af9-52ad-4592-a8e2-7aea0ffc7069",
        "83a57815-f1a2-4ac8-92b4-5597b9bd5b38",
        "8415ea9a-fc60-4eb2-82fe-0925731342ba",
        "8b532824-0b6b-4970-95be-3a659f257aa8",
        "8cc94ef3-2507-41cd-8267-8c9857d41505",
        "8dc68381-4e1d-4b8c-9c6a-8c9ac4147df0",
        "8de7c96b-8582-4df4-aa74-89b33ad8454d",
        "8e73b725-e81c-4c94-80ca-46630cc1b117",
        "8eb89fa9-815a-4414-9d9c-f57e5a288d77",
        "8f022f2d-0e8e-4e89-b944-7cfe74a68056",
        "8fa6008b-1541-44bc-9ddd-45fa4cd3222c",
        "90474194-594c-45ee-b2e9-217599d37cf3",
        "91f58fa8-4784-4837-a286-f3199131c486",
        "92191d4c-6078-4859-ad4d-8320d49b262d",
        "92440b46-66f9-4b15-bf28-047b5cc8202d",
        "9473fd7a-cd5c-433a-aad5-e0a23eda0174",
        "952beacb-0e73-4fde-94d4-60cfcbec2d63",
        "979e15bb-5033-4038-9560-5468e3d45f81",
        "979f2885-7856-45d9-81a5-3c6b2979f906",
        "97a51838-789b-4c18-b4b8-9b4ff32e831f",
        "98960794-a1a9-4761-a495-2eca16f4c81e",
        "99b0b24c-a54d-486a-a69a-4348eb2e66a4",
        "9a89d8c0-55a5-4534-a9c5-77ff4ed956ad",
        "9b6ffba8-fc6f-4e57-a660-17765e8c9bd0",
        "9bd841f2-0989-45c5-a08b-348ecd09711f",
        "9d456d7f-2173-4d55-93bb-dbaa4f1cd7be",
        "a11a1030-1fc2-438a-94b9-b49addb1cc1e",
        "a311523f-be00-47de-ba8d-8afd1e3ccd38",
        "a72d5f64-ac56-49c8-99f2-047a96d1fd87",
        "a7fa0582-1b75-4451-88ec-19db06f68563",
        "aa5ae136-7004-46f6-81ad-606242f4a501",
        "ab76d32c-ee27-4b4b-aeaa-9b781bd0dfe2",
        "aba8cda5-779e-4943-8fe6-13b7f6a03cf3",
        "abd903c6-7897-4257-a970-0d10b17c5edd",
        "ac1727ae-458d-48eb-be0a-0bdc6a8c3517",
        "adbb43fa-b7d2-4184-8bd9-4b5632f57b7a",
        "aed239de-f5e3-46c6-994b-5bbf0aa43441",
        "af3c1d06-0485-4443-b544-005348f15c4e",
        "b1590f77-3cdb-4f80-abae-4f4f13f06e26",
        "b22ad794-a768-4b1a-86a8-3bf5c713ea2b",
        "b734941e-37d8-47b8-aef8-6c51412bcbc1",
        "b823559f-e421-432f-bb3f-051cdb3db007",
        "b95dc623-7508-4310-9f99-6057a6242c2a",
        "b993799e-3e27-4792-9694-4ff1770cb1cf",
        "c283f530-c962-4d78-a876-0b7af7d718b1",
        "c407bd28-fd6c-40d6-b320-dd4acb135ec0",
        "c40abaaf-dc6a-4dc0-815c-a411822d988b",
        "cc370122-aedd-4073-a89f-93ab395e5017",
        "cdee584c-db6a-4c3a-b2eb-5956940cab58",
        "d050d274-c0d0-4ce3-bb31-4a9f3ad1b7f1",
        "d15a318f-404b-4c29-8a01-1febee5bf2d5",
        "d35bfca9-d752-465e-a76c-022c56abdbcc",
        "d3d73525-b455-456e-843a-0638554f08ff",
        "d5e65a52-3132-422d-ac8b-0d285650d475",
        "d5f8728e-3eba-4bc5-b3db-80f6685a4941",
        "d64630fa-42a0-4a8e-9d9d-903549a0207f",
        "d6863262-3fa5-4a52-a5a4-3f1660728294",
        "d6b94713-02d8-4851-916f-6bb927a68b00",
        "d7e14a2c-ede8-4f90-8f6f-f7f09ff920c4",
        "d7e4af3e-53b4-4372-ac28-296e43182a21",
        "d87e0405-15bb-405b-b277-27cec5444106",
        "db7f376b-ab61-4316-b59f-83fe570b8f23",
        "dc1a3d51-3e93-4f22-b268-ab0f86676f1f",
        "ddb52f9e-64d4-43a9-817d-405db378d4c5",
        "e0398a62-8900-48d0-baf8-5d73040089ae",
        "e414d40e-de04-4aa9-b85c-fc939417f0fd",
        "e7704d69-cf1d-4015-88ba-b998bab99008",
        "e872dbc4-e1f9-454c-830c-3e86ccb03251",
        "ec30a7ef-8e51-45ae-a018-c5ff2622db40",
        "ee403b64-1df6-4630-a233-881e85be0395",
        "ef1aa11d-bd54-47a4-8e69-25d6962d80c9",
        "f2371067-a968-43bd-93b8-a490165b2e9a",
        "f4971c37-45ca-470a-9ae4-d64cd96b73ff",
        "f56f0271-e40b-4895-87bf-92c47682be44",
        "f75b9f49-509f-4e07-b87d-ab3297e5bd19",
        "f7929730-decd-41fd-b596-de71699f6c7e",
        "f7b63c3d-d687-401d-8e4e-14e1aa0c77b5",
        "fb12d7f8-5951-48c8-9fd0-062829f49df7",
        "fe1a2a4c-68ac-4887-8557-7be338399a34");

    public static void main(String[] args) {

        NotificationClient client = getNotificationClient();

        try {
            TemplateList templates = client.getAllTemplates(notificationType);

            log.info("Found " + templates.getTemplates().size() + " templates in total");

            // Uncomment if searching template values.
            searchTemplateValues(templates);

            // uncomment if searching template body contents.
            searchTemplateBody(templates);

            final Set<Template> interestingTemplates = filterTemplates(templates);
            interestingTemplates.forEach(GetAllTemplatesFromNotify::writeTemplate);

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


    private static Set<Template> filterTemplates(TemplateList templates) {
        return templates.getTemplates().stream().filter(t -> GetAllTemplatesFromNotify.emailTemplateIdsFromApplicationYaml.contains(t.getId().toString())).collect(Collectors.toSet());
    }

    private static void writeTemplate(Template template){
        String path = template.getId().toString() + ".json";
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            mapper.writeValue(new File(path), template);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
