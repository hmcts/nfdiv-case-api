package uk.gov.hmcts.divorce.print.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Letter {
    private final String name;
    private final byte[] data;
    private final int count;
}
