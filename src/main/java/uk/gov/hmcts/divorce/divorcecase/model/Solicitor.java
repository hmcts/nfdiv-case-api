package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.OrganisationPolicyAccess;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
public class Solicitor {

    @CCD(
        label = "Solicitor’s name",
        access = {DefaultAccess.class}
    )
    private String name;

    @CCD(
        label = "Solicitor’s reference number",
        access = {DefaultAccess.class}
    )
    private String reference;

    @CCD(
        label = "Solicitor’s Phone number",
        regex = "^[0-9 +().-]{9,}$",
        access = {DefaultAccess.class}
    )
    private String phone;

    @CCD(
        label = "Solicitor’s Email",
        typeOverride = Email,
        access = {DefaultAccess.class}
    )
    private String email;

    @CCD(
        label = "Solicitor’s firm/ DX address",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String address;

    @CCD(
        label = "I confirm I am willing to accept service of all correspondence and orders by email at the email address "
            + "stated above.",
        access = {DefaultAccess.class}
    )
    private YesOrNo agreeToReceiveEmails;

    @CCD(
        label = "Firm address/DX address",
        access = {OrganisationPolicyAccess.class}
    )
    private OrganisationPolicy<UserRole> organisationPolicy;

    @JsonIgnore
    public boolean hasOrgId() {
        if (null != organisationPolicy && null != organisationPolicy.getOrganisation()) {
            return !isNullOrEmpty(organisationPolicy.getOrganisation().getOrganisationId());
        }
        return false;
    }
}
