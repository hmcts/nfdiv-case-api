package uk.gov.hmcts.divorce.common.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.ccd.sdk.type.PreviousOrganisationCollectionItem;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

public class OrganisationPolicyDeserializer extends StdDeserializer<OrganisationPolicy<?>> {

    static final long serialVersionUID = 1L;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public OrganisationPolicyDeserializer() {
        super((Class) OrganisationPolicy.class);
    }

    @Override
    public OrganisationPolicy<?> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.readValueAsTree();
        JsonNode organisationNode = node.get("Organisation");
        Organisation organisation = organisationNode == null || organisationNode.isNull()
            ? null
            : new Organisation(
                text(organisationNode, "OrganisationID"),
                text(organisationNode, "OrganisationName")
            );

        JsonNode previousOrganisationsNode = node.get("PreviousOrganisations");
        Set<PreviousOrganisationCollectionItem> previousOrganisations =
            previousOrganisationsNode == null || previousOrganisationsNode.isNull()
                ? null
                : ((ObjectMapper) parser.getCodec()).convertValue(
                    previousOrganisationsNode,
                    new TypeReference<>() {
                    }
                );

        String prepopulateValue = text(node, "PrepopulateToUsersOrganisation");
        YesOrNo prepopulate = prepopulateValue == null
            ? null
            : YesOrNo.valueOf(prepopulateValue.toUpperCase(Locale.ROOT));

        return new OrganisationPolicy<>(
            organisation,
            previousOrganisations,
            text(node, "OrgPolicyReference"),
            prepopulate,
            UserRole.fromString(text(node, "OrgPolicyCaseAssignedRole"))
        );
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }
}
