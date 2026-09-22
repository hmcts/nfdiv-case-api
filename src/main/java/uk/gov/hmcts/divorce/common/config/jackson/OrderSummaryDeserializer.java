package uk.gov.hmcts.divorce.common.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderSummaryDeserializer extends StdDeserializer<OrderSummary> {

    static final long serialVersionUID = 1L;

    public OrderSummaryDeserializer() {
        super(OrderSummary.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public OrderSummary deserialize(JsonParser parser, DeserializationContext context) throws IOException {
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
