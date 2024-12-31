package uk.gov.hmcts.divorce.payment.model.callback;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonNaming(SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentCallbackDto {

    private String id;

    private BigDecimal amount;

    private String description;

    private String reference;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "GMT")
    private Date dateCreated;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "GMT")
    private Date dateUpdated;

    private String currency;

    private String ccdCaseNumber;

    private String channel;

    private OnlinePaymentMethod method;

    private String paymentReference;

    private String externalProvider;

    private String externalReference;

    private String siteId;

    private String status;

    private List<FeeDto> fees;

    @JsonProperty("_links")
    private LinksDto links;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(SnakeCaseStrategy.class)
    @JsonInclude(NON_NULL)
    public static class LinksDto {
        private LinkDto nextUrl;
        private LinkDto self;
        private LinkDto cancel;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(NON_NULL)
    public static class LinkDto {
        private String href;
        private String method;
    }
}
