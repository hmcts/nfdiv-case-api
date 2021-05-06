package uk.gov.hmcts.divorce.common.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.payment.model.Payment;

import java.io.IOException;
import java.util.List;

public class PaymentListValueDeserializer extends JsonDeserializer<List<ListValue<Payment>>> {

    @Override
    public List<ListValue<Payment>> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
//        JavaType type = mapper.getTypeFactory().constructParametricType(List.class, ListValue.class, Payment.class);

//        return parser.readValueAs(new TypeReference<List<ListValue<Payment>>>() {});
//
        return mapper.readValue(parser, new TypeReference<List<ListValue<Payment>>>() {});
    }
}
