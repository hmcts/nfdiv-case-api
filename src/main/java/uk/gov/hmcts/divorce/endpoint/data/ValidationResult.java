package uk.gov.hmcts.divorce.endpoint.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationResult {

    private List<String> warnings;

    private List<String> errors;

    private ValidationStatus status;
}
