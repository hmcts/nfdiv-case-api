package uk.gov.hmcts.divorce.noticeofchange.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@Builder
@Data
@ComplexType
public class PreviousOrganisationCollectionItem {

    private String id;
    private PreviousOrganisation value;
}
