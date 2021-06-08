package uk.gov.hmcts.divorce.print.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Document {
    final String name;
    final byte[] data;
    final int count;
}
