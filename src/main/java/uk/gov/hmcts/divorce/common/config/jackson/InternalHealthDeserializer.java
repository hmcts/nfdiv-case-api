package uk.gov.hmcts.divorce.common.config.jackson;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;
import uk.gov.hmcts.reform.ccd.document.am.healthcheck.InternalHealth;

import java.io.IOException;

public class InternalHealthDeserializer extends StdDeserializer<InternalHealth> {
    static final long serialVersionUID = 1L;

    public InternalHealthDeserializer() {
        this(null);
    }

    protected InternalHealthDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public InternalHealth deserialize(JsonParser parser, DeserializationContext context) {
        JsonNode node = parser.readValueAsTree();
        return new InternalHealth(node.findValue("status").textValue());
    }
}
