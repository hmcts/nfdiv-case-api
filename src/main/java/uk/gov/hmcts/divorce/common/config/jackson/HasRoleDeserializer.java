package uk.gov.hmcts.divorce.common.config.jackson;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.Arrays;

public class HasRoleDeserializer extends StdDeserializer<HasRole> {
    static final long serialVersionUID = 1L;

    public HasRoleDeserializer() {
        this(null);
    }

    protected HasRoleDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public HasRole deserialize(JsonParser parser, DeserializationContext context) {
        JsonNode node = parser.readValueAsTree();

        return Arrays
            .stream(UserRole.values())
            .filter(r -> r.getRole().equals(node.asText()))
            .findFirst()
            .get();
    }
}
