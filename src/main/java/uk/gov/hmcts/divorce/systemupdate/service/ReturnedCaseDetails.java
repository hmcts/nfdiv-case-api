package uk.gov.hmcts.divorce.systemupdate.service;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.LocalDateTime;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnedCaseDetails {

    @Valid
    @JsonProperty(value = "case_data")
    private final CaseData data;

    @JsonProperty
    private final State state;

    @JsonProperty(value = "last_modified")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSSSSS][.SSS][.SS][.S]")
    private final LocalDateTime lastModified;

    @JsonProperty(value = "last_state_modified_date")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSSSSS][.SSS][.SS][.S]")
    private final LocalDateTime lastStateModifiedDate;
    @NotNull
    private final Long id;
}
