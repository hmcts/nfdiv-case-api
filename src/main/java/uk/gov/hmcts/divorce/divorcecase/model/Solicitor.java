package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.OrganisationPolicyAccess;

import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
public class Solicitor {

    @CCD(label = "Solicitor’s name")
    private String name;

    @CCD(
        label = "Solicitor’s reference number",
        hint = "This is your internal reference that your firm uses to identify the case."
    )
    private String reference;

    @CCD(
        label = "Solicitor’s Phone number",
        regex = "^[0-9 +().-]{9,}$"
    )
    private String phone;

    @CCD(
        label = "Solicitor’s Email",
        typeOverride = Email
    )
    private String email;

    @CCD(
        label = "Solicitor’s firm/ DX address",
        typeOverride = TextArea
    )
    private String address;

    @CCD(
        label = "Service by email"
    )
    private Set<Prayer> agreeToReceiveEmailsCheckbox;

    @Getter
    @AllArgsConstructor
    public enum Prayer implements HasLabel {

        @JsonProperty("Yes")
        CONFIRM("I confirm I’m willing to accept service of all correspondence and orders by email at the email address above");

        private final String label;
    }

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
