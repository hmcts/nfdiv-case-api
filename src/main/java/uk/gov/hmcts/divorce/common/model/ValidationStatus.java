package uk.gov.hmcts.divorce.common.model;

import lombok.Getter;

@Getter
public enum ValidationStatus {
    FAILED("failed"),
    SUCCESS("success");

    private final String value;

    ValidationStatus(String value) {
        this.value = value;
    }

}
