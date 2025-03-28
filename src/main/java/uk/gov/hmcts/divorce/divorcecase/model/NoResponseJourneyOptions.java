package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.Applicant2Access;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccessOnlyAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoResponseJourneyOptions {

    @CCD(
            label = "Are these details for your partner correct and up to date?",
            typeOverride = FixedRadioList,
            typeParameterOverride = "NoResponseCheckContactDetails"
    )
    private NoResponseCheckContactDetails noResponseCheckContactDetails;

    @CCD(
            label = "Can you prove that your partner has received the papers?",
            access = {DefaultAccess.class}
    )
    private YesOrNo noResponsePartnerHasReceivedPapers;
}
