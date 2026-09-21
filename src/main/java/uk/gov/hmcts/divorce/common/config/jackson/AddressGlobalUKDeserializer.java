package uk.gov.hmcts.divorce.common.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;

import java.io.IOException;

public class AddressGlobalUKDeserializer extends StdDeserializer<AddressGlobalUK> {
    static final long serialVersionUID = 1L;

    public AddressGlobalUKDeserializer() {
        super(AddressGlobalUK.class);
    }

    @Override
    public AddressGlobalUK deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.readValueAsTree();

        return AddressGlobalUK.builder()
            .addressLine1(text(node, "AddressLine1"))
            .addressLine2(text(node, "AddressLine2"))
            .addressLine3(text(node, "AddressLine3"))
            .postTown(text(node, "PostTown"))
            .county(text(node, "County"))
            .postCode(text(node, "PostCode"))
            .country(text(node, "Country"))
            .build();
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }
}
