package uk.gov.hmcts.divorce.common.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.io.IOException;
import java.time.LocalDate;

public class DivorceDocumentDeserializer extends StdDeserializer<DivorceDocument> {

    static final long serialVersionUID = 1L;

    public DivorceDocumentDeserializer() {
        super(DivorceDocument.class);
    }

    @Override
    public DivorceDocument deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode document = parser.getCodec().readTree(parser);
        JsonNode link = document.get("documentLink");
        JsonNode dateAdded = document.get("documentDateAdded");
        JsonNode type = document.get("documentType");

        return new DivorceDocument(
            text(document, "documentEmailContent"),
            link == null || link.isNull() ? null : parser.getCodec().treeToValue(link, Document.class),
            dateAdded == null || dateAdded.isNull() ? null : LocalDate.parse(dateAdded.asText()),
            text(document, "documentComment"),
            text(document, "documentFileName"),
            type == null || type.isNull() ? null : parser.getCodec().treeToValue(type, DocumentType.class)
        );
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }
}
