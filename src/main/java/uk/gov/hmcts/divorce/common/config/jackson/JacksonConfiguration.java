package uk.gov.hmcts.divorce.common.config.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer;
import uk.gov.hmcts.divorce.divorcecase.model.BailiffServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.DeemedServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.DispenseWithServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.Jurisdiction;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.NoResponseJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.NoticeOfChange;
import uk.gov.hmcts.divorce.divorcecase.model.PaperFormDetails;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseDraft;
import uk.gov.hmcts.divorce.divorcecase.model.SearchGovRecordsJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.SolicitorService;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.reform.ccd.document.am.healthcheck.InternalHealth;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.fasterxml.jackson.core.JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT;
import static com.fasterxml.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS;
import static com.fasterxml.jackson.databind.MapperFeature.INFER_BUILDER_TYPE_BINDINGS;

@Configuration
public class JacksonConfiguration {

    @Bean
    public JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer() {
        return builder -> {
            tools.jackson.databind.module.SimpleModule addressModule =
                new tools.jackson.databind.module.SimpleModule();
            addressModule.addDeserializer(AddressGlobalUK.class, new AddressGlobalUKDeserializer());
            addressModule.addDeserializer(DynamicList.class, new DynamicListDeserializer());
            addressModule.addDeserializer(OrganisationPolicy.class, new OrganisationPolicyDeserializer());
            addressModule.addDeserializer(OrderSummary.class, new OrderSummaryDeserializer());
            addressModule.addSerializer(OrganisationPolicy.class, new OrganisationPolicySerializer());

            builder
                .addModule(addressModule)
                .addMixIn(AlternativeServiceJourneyOptions.class, UpperCamelCaseMixin.class)
                .addMixIn(Applicant.class, UpperCamelCaseMixin.class)
                .addMixIn(ApplicantPrayer.class, UpperCamelCaseMixin.class)
                .addMixIn(BailiffServiceJourneyOptions.class, UpperCamelCaseMixin.class)
                .addMixIn(ConditionalOrder.class, UpperCamelCaseMixin.class)
                .addMixIn(ConditionalOrderQuestions.class, UpperCamelCaseMixin.class)
                .addMixIn(DeemedServiceJourneyOptions.class, UpperCamelCaseMixin.class)
                .addMixIn(DispenseWithServiceJourneyOptions.class, UpperCamelCaseMixin.class)
                .addMixIn(FeeDetails.class, UpperCamelCaseMixin.class)
                .addMixIn(HelpWithFees.class, UpperCamelCaseMixin.class)
                .addMixIn(InterimApplicationOptions.class, UpperCamelCaseMixin.class)
                .addMixIn(Jurisdiction.class, UpperCamelCaseMixin.class)
                .addMixIn(LabelContent.class, UpperCamelCaseMixin.class)
                .addMixIn(MarriageDetails.class, UpperCamelCaseMixin.class)
                .addMixIn(NoResponseJourneyOptions.class, UpperCamelCaseMixin.class)
                .addMixIn(NoticeOfChange.class, UpperCamelCaseMixin.class)
                .addMixIn(Organisation.class, UpperCamelCaseMixin.class)
                .addMixIn(OrganisationPolicy.class, UpperCamelCaseMixin.class)
                .addMixIn(PaperFormDetails.class, UpperCamelCaseMixin.class)
                .addMixIn(RequestForInformationResponseDraft.class, UpperCamelCaseMixin.class)
                .addMixIn(SearchGovRecordsJourneyOptions.class, UpperCamelCaseMixin.class)
                .addMixIn(Solicitor.class, UpperCamelCaseMixin.class)
                .addMixIn(SolicitorService.class, UpperCamelCaseMixin.class);
        };
    }

    @Primary
    @Bean
    public ObjectMapper getMapper() {
        final ObjectMapper mapper = JsonMapper.builder()
            .configure(ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            .enable(INFER_BUILDER_TYPE_BINDINGS)
            .disable(AUTO_CLOSE_JSON_CONTENT)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build();

        SimpleModule deserialization = new SimpleModule();
        deserialization.addDeserializer(HasRole.class, new HasRoleDeserializer());
        deserialization.addDeserializer(InternalHealth.class, new InternalHealthDeserializer());
        deserialization.addDeserializer(AddressGlobalUK.class, new Jackson2AddressGlobalUKDeserializer());
        deserialization.addDeserializer(DynamicList.class, new Jackson2DynamicListDeserializer());
        deserialization.addDeserializer(OrderSummary.class, new Jackson2OrderSummaryDeserializer());
        deserialization.addDeserializer(DivorceDocument.class, new DivorceDocumentDeserializer());
        deserialization.addDeserializer(OrganisationPolicy.class, new Jackson2OrganisationPolicyDeserializer());
        mapper.registerModule(deserialization);

        JavaTimeModule datetime = new JavaTimeModule();
        datetime.addSerializer(LocalDateSerializer.INSTANCE);
        mapper.registerModule(datetime);

        mapper.registerModule(new ParameterNamesModule());

        return mapper;
    }

    @tools.jackson.databind.annotation.JsonNaming(
        tools.jackson.databind.PropertyNamingStrategies.UpperCamelCaseStrategy.class
    )
    private abstract static class UpperCamelCaseMixin {
    }

    private static class AddressGlobalUKDeserializer
        extends tools.jackson.databind.deser.std.StdDeserializer<AddressGlobalUK> {

        AddressGlobalUKDeserializer() {
            super(AddressGlobalUK.class);
        }

        @Override
        @SuppressWarnings("unchecked")
        public AddressGlobalUK deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
            Map<String, Object> address = parser.readValueAs(Map.class);
            return new AddressGlobalUK(
                (String) address.get("AddressLine1"),
                (String) address.get("AddressLine2"),
                (String) address.get("AddressLine3"),
                (String) address.get("PostTown"),
                (String) address.get("County"),
                (String) address.get("PostCode"),
                (String) address.get("Country")
            );
        }
    }

    private static class OrganisationPolicyDeserializer
        extends tools.jackson.databind.deser.std.StdDeserializer<OrganisationPolicy<?>> {

        @SuppressWarnings({"rawtypes", "unchecked"})
        OrganisationPolicyDeserializer() {
            super((Class) OrganisationPolicy.class);
        }

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        public OrganisationPolicy<?> deserialize(JsonParser parser, DeserializationContext context)
            throws JacksonException {

            Map<String, Object> policy = parser.readValueAs(Map.class);
            return createOrganisationPolicy(policy);
        }
    }

    private static class OrderSummaryDeserializer
        extends tools.jackson.databind.deser.std.StdDeserializer<OrderSummary> {

        OrderSummaryDeserializer() {
            super(OrderSummary.class);
        }

        @Override
        @SuppressWarnings("unchecked")
        public OrderSummary deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
            Map<String, Object> summary = parser.readValueAs(Map.class);
            List<Map<String, Object>> feeItems = (List<Map<String, Object>>) summary.get("Fees");
            List<ListValue<Fee>> fees = null;
            if (feeItems != null) {
                fees = new ArrayList<>();
                for (Map<String, Object> feeItem : feeItems) {
                    Map<String, Object> feeData = (Map<String, Object>) feeItem.get("value");
                    Fee fee = new Fee(
                        (String) feeData.get("FeeAmount"),
                        (String) feeData.get("FeeCode"),
                        (String) feeData.get("FeeDescription"),
                        (String) feeData.get("FeeVersion")
                    );
                    fees.add(new ListValue<>((String) feeItem.get("id"), fee));
                }
            }
            return new OrderSummary(
                (String) summary.get("PaymentReference"),
                fees,
                (String) summary.get("PaymentTotal")
            );
        }
    }

    private static class DynamicListDeserializer
        extends tools.jackson.databind.deser.std.StdDeserializer<DynamicList> {

        DynamicListDeserializer() {
            super(DynamicList.class);
        }

        @Override
        @SuppressWarnings("unchecked")
        public DynamicList deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
            Map<String, Object> dynamicList = parser.readValueAs(Map.class);
            DynamicListElement value = createDynamicListElement((Map<String, Object>) dynamicList.get("value"));
            List<Map<String, Object>> itemData = (List<Map<String, Object>>) dynamicList.get("list_items");
            List<DynamicListElement> items = itemData == null
                ? null
                : itemData.stream().map(JacksonConfiguration::createDynamicListElement).toList();
            return new DynamicList(value, items);
        }
    }

    private static class Jackson2OrganisationPolicyDeserializer extends StdDeserializer<OrganisationPolicy<?>> {

        @SuppressWarnings({"rawtypes", "unchecked"})
        Jackson2OrganisationPolicyDeserializer() {
            super((Class) OrganisationPolicy.class);
        }

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        public OrganisationPolicy<?> deserialize(
            com.fasterxml.jackson.core.JsonParser parser,
            com.fasterxml.jackson.databind.DeserializationContext context
        ) throws IOException {

            Map<String, Object> policy = parser.readValueAs(Map.class);
            return createOrganisationPolicy(policy);
        }
    }

    private static class Jackson2AddressGlobalUKDeserializer extends StdDeserializer<AddressGlobalUK> {

        Jackson2AddressGlobalUKDeserializer() {
            super(AddressGlobalUK.class);
        }

        @Override
        @SuppressWarnings("unchecked")
        public AddressGlobalUK deserialize(
            com.fasterxml.jackson.core.JsonParser parser,
            com.fasterxml.jackson.databind.DeserializationContext context
        ) throws IOException {
            Map<String, Object> address = parser.readValueAs(Map.class);
            return new AddressGlobalUK(
                (String) address.get("AddressLine1"),
                (String) address.get("AddressLine2"),
                (String) address.get("AddressLine3"),
                (String) address.get("PostTown"),
                (String) address.get("County"),
                (String) address.get("PostCode"),
                (String) address.get("Country")
            );
        }
    }

    private static class Jackson2OrderSummaryDeserializer extends StdDeserializer<OrderSummary> {

        Jackson2OrderSummaryDeserializer() {
            super(OrderSummary.class);
        }

        @Override
        @SuppressWarnings("unchecked")
        public OrderSummary deserialize(
            com.fasterxml.jackson.core.JsonParser parser,
            com.fasterxml.jackson.databind.DeserializationContext context
        ) throws IOException {
            Map<String, Object> summary = parser.readValueAs(Map.class);
            List<Map<String, Object>> feeItems = (List<Map<String, Object>>) summary.get("Fees");
            List<ListValue<Fee>> fees = null;
            if (feeItems != null) {
                fees = new ArrayList<>();
                for (Map<String, Object> feeItem : feeItems) {
                    Map<String, Object> feeData = (Map<String, Object>) feeItem.get("value");
                    Fee fee = new Fee(
                        (String) feeData.get("FeeAmount"),
                        (String) feeData.get("FeeCode"),
                        (String) feeData.get("FeeDescription"),
                        (String) feeData.get("FeeVersion")
                    );
                    fees.add(new ListValue<>((String) feeItem.get("id"), fee));
                }
            }
            return new OrderSummary(
                (String) summary.get("PaymentReference"),
                fees,
                (String) summary.get("PaymentTotal")
            );
        }
    }

    private static class Jackson2DynamicListDeserializer extends StdDeserializer<DynamicList> {

        Jackson2DynamicListDeserializer() {
            super(DynamicList.class);
        }

        @Override
        @SuppressWarnings("unchecked")
        public DynamicList deserialize(
            com.fasterxml.jackson.core.JsonParser parser,
            com.fasterxml.jackson.databind.DeserializationContext context
        ) throws IOException {
            Map<String, Object> dynamicList = parser.readValueAs(Map.class);
            DynamicListElement value = createDynamicListElement((Map<String, Object>) dynamicList.get("value"));
            List<Map<String, Object>> itemData = (List<Map<String, Object>>) dynamicList.get("list_items");
            List<DynamicListElement> items = itemData == null
                ? null
                : itemData.stream().map(JacksonConfiguration::createDynamicListElement).toList();
            return new DynamicList(value, items);
        }
    }

    private static class DivorceDocumentDeserializer extends StdDeserializer<DivorceDocument> {

        DivorceDocumentDeserializer() {
            super(DivorceDocument.class);
        }

        @Override
        public DivorceDocument deserialize(
            com.fasterxml.jackson.core.JsonParser parser,
            com.fasterxml.jackson.databind.DeserializationContext context
        ) throws IOException {

            JsonNode document = parser.getCodec().readTree(parser);
            JsonNode link = document.get("documentLink");
            JsonNode dateAdded = document.get("documentDateAdded");
            JsonNode type = document.get("documentType");

            return new DivorceDocument(
                textValue(document, "documentEmailContent"),
                link == null ? null : parser.getCodec().treeToValue(link, Document.class),
                dateAdded == null ? null : LocalDate.parse(dateAdded.asText()),
                textValue(document, "documentComment"),
                textValue(document, "documentFileName"),
                type == null ? null : parser.getCodec().treeToValue(type, DocumentType.class)
            );
        }

        private String textValue(JsonNode node, String fieldName) {
            JsonNode value = node.get(fieldName);
            return value == null || value.isNull() ? null : value.asText();
        }
    }

    private static class OrganisationPolicySerializer extends ValueSerializer<OrganisationPolicy> {

        @Override
        public void serialize(
            OrganisationPolicy policy,
            JsonGenerator generator,
            SerializationContext context
        ) throws JacksonException {

            generator.writeStartObject(policy);
            Organisation organisation = policy.getOrganisation();
            if (organisation != null) {
                generator.writeName("Organisation").writeStartObject(organisation);
                writeStringProperty(generator, "OrganisationID", organisation.getOrganisationId());
                writeStringProperty(generator, "OrganisationName", organisation.getOrganisationName());
                generator.writeEndObject();
            }
            if (policy.getPreviousOrganisations() != null) {
                generator.writePOJOProperty("PreviousOrganisations", policy.getPreviousOrganisations());
            }
            writeStringProperty(generator, "OrgPolicyReference", policy.getOrgPolicyReference());
            if (policy.getPrepopulateToUsersOrganisation() != null) {
                generator.writeStringProperty(
                    "PrepopulateToUsersOrganisation",
                    policy.getPrepopulateToUsersOrganisation().getValue()
                );
            }
            if (policy.getOrgPolicyCaseAssignedRole() != null) {
                generator.writeStringProperty(
                    "OrgPolicyCaseAssignedRole",
                    policy.getOrgPolicyCaseAssignedRole().getRole()
                );
            }
            generator.writeEndObject();
        }

        private void writeStringProperty(JsonGenerator generator, String name, String value) throws JacksonException {
            if (value != null) {
                generator.writeStringProperty(name, value);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static OrganisationPolicy<?> createOrganisationPolicy(Map<String, Object> policy) {
        Map<String, Object> organisationData = (Map<String, Object>) policy.get("Organisation");
        Organisation organisation = organisationData == null
            ? null
            : new Organisation(
                (String) organisationData.get("OrganisationID"),
                (String) organisationData.get("OrganisationName")
            );
        String prepopulateValue = (String) policy.get("PrepopulateToUsersOrganisation");
        YesOrNo prepopulate = prepopulateValue == null
            ? null
            : YesOrNo.valueOf(prepopulateValue.toUpperCase());
        UserRole assignedRole = UserRole.fromString((String) policy.get("OrgPolicyCaseAssignedRole"));

        return new OrganisationPolicy(
            organisation,
            null,
            (String) policy.get("OrgPolicyReference"),
            prepopulate,
            assignedRole
        );
    }

    private static DynamicListElement createDynamicListElement(Map<String, Object> element) {
        if (element == null) {
            return null;
        }
        String code = (String) element.get("code");
        return new DynamicListElement(code == null ? null : UUID.fromString(code), (String) element.get("label"));
    }
}
