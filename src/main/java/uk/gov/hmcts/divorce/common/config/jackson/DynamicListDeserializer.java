package uk.gov.hmcts.divorce.common.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DynamicListDeserializer extends StdDeserializer<DynamicList> {

    static final long serialVersionUID = 1L;

    public DynamicListDeserializer() {
        super(DynamicList.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public DynamicList deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        Map<String, Object> dynamicList = parser.readValueAs(Map.class);
        DynamicListElement value = createElement((Map<String, Object>) dynamicList.get("value"));
        List<Map<String, Object>> itemData = (List<Map<String, Object>>) dynamicList.get("list_items");
        List<DynamicListElement> items = itemData == null
            ? null
            : itemData.stream().map(DynamicListDeserializer::createElement).toList();
        return new DynamicList(value, items);
    }

    private static DynamicListElement createElement(Map<String, Object> element) {
        if (element == null) {
            return null;
        }
        String code = (String) element.get("code");
        return new DynamicListElement(code == null ? null : UUID.fromString(code), (String) element.get("label"));
    }
}
