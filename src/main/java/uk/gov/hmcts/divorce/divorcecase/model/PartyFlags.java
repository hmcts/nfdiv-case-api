package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;
import uk.gov.hmcts.ccd.sdk.type.*;
import uk.gov.hmcts.divorce.divorcecase.model.access.*;
import uk.gov.hmcts.divorce.document.model.*;

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.divorce.document.DocumentUtil.getConfidentialDocumentType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PartyFlags {
    @CCD(access = {CaseworkerAccess.class},
        label = "Applicant/Applicant1 Flags")
    private Flags applicant1Flags;

    @CCD(access = {CaseworkerAccess.class},
        label = "Respondent/Applicant2 Flags")
    private Flags applicant2Flags;

    @CCD(access = {CaseworkerAccess.class},
        label = "Applicant/Applicant1 Solicitor Flags")
    private Flags applicant1SolicitorFlags;

    @CCD(access = {CaseworkerAccess.class},
        label = "Respondent/Applicant2 Solicitor Flags")
    private Flags applicant2SolicitorFlags;

    /* To support external flags in the future, new flags for parties can be added here.
        Group id should be used for internal and external flags for a particular case party.
     */

    private String applicant1GroupId;
    private String applicant2GroupId;
    private String applicant1SolicitorGroupId;
    private String applicant2SolicitorGroupId;
}
