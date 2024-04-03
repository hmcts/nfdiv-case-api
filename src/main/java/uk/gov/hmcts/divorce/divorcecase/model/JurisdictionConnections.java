package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
public enum JurisdictionConnections implements HasLabel {

    @JsonProperty("A")
    @JsonPropertyDescription("APP_1_APP_2_RESIDENT")
    APP_1_APP_2_RESIDENT("Both parties to the marriage or civil partnership are habitually resident in England and Wales",
        "Mae'r ddau barti i'r briodas neu bartneriaeth sifil yn preswylio'n arferol yng Nghymru neu Loegr."),

    @JsonProperty("B")
    @JsonPropertyDescription("APP_1_APP_2_LAST_RESIDENT")
    APP_1_APP_2_LAST_RESIDENT("Both parties to the marriage or civil partnership were last habitually resident in England and Wales"
        + " and one of them continues to reside there",
        "Roedd y ddau barti i'r briodas neu bartneriaeth sifil yn preswylio'n arferol ddiwethaf yng Nghymru neu Loegr ac mae un "
            + "ohonynt yn parhau i breswylio yno."),

    @JsonProperty("C")
    @JsonPropertyDescription("APP_2_RESIDENT_SOLE")
    APP_2_RESIDENT_SOLE("The respondent is habitually resident in England and Wales",
        "Mae'r atebydd yn preswylio'n arferol yng Nghymru neu Lloegr."),

    @JsonProperty("C2")
    @JsonPropertyDescription("APP_2_RESIDENT_JOINT")
    APP_2_RESIDENT_JOINT("This is a joint application and applicant 2 is habitually resident in England and Wales",
        "Mae Ceisydd 2 yn preswylio'n arferol yng Nghymru neu Lloegr."),

    @JsonProperty("D")
    @JsonPropertyDescription("APP_1_RESIDENT_TWELVE_MONTHS")
    APP_1_RESIDENT_TWELVE_MONTHS("The applicant is habitually resident in England and Wales and has resided there for at least "
        + "one year immediately before the application was made",
        "Mae'r ceisydd yn preswylio'n arferol yng Nghymru neu Loegr ac wedi preswylio yno am o leiaf blwyddyn yn union cyn "
            + "gwneud y cais"),

    @JsonProperty("E")
    @JsonPropertyDescription("APP_1_RESIDENT_SIX_MONTHS")
    APP_1_RESIDENT_SIX_MONTHS("The applicant is domiciled and habitually resident in England and Wales and has resided there for "
        + "at least six months immediately before the application was made",
        "Mae domisil y ceisydd yng Nghymru neu Loegr, mae'n preswylio'n arferol yno ac mae wedi preswylio yno am o leiaf chwe mis "
            + "yn union cyn cyflwyno'r cais"),

    @JsonProperty("F")
    @JsonPropertyDescription("APP_1_APP_2_DOMICILED")
    APP_1_APP_2_DOMICILED("Both parties to the marriage or civil partnership are domiciled in England and Wales",
        "Mae domisil y ddau barti i'r briodas neu  bartneriaeth sifil yng Nghymru neu Loegr"),

    @JsonProperty("G")
    @JsonPropertyDescription("APP_1_DOMICILED")
    APP_1_DOMICILED("The applicant or applicant 1 is domiciled in England and Wales",
        "Mae domisil y ceisydd neu geisydd 1 yng Nghymru a Lloegr"),

    @JsonProperty("H")
    @JsonPropertyDescription("APP_2_DOMICILED")
    APP_2_DOMICILED("Applicant 2 (in a joint) or the respondent (in a sole) is domiciled in England and Wales",
        "Mae Ceisydd 2 (mewn cais ar y cyd) neu’r atebydd (mewn cais unigol) â’i ddomisil yng Nghymru a Lloegr"),

    @JsonProperty("I")
    @JsonPropertyDescription("RESIDUAL_JURISDICTION_CP")
    RESIDUAL_JURISDICTION_CP("The parties registered as civil partners of each other in England or Wales and it would be in the "
        + "interest of justice for the court to assume jurisdiction in this case",
        "Mi wnaeth y partïon gofrestru fel partneriaid sifil i'w gilydd yng Nghymru neu Loegr, a byddai er budd cyfiawnder i'r "
            + "llys ysgwyddo awdurdodaeth yn yr achos hwn"),

    @JsonProperty("I2")
    @JsonPropertyDescription("RESIDUAL_JURISDICTION_D")
    RESIDUAL_JURISDICTION_D("The parties married each other under the law of England and Wales and it would be in the interests "
        + "of justice for the court to assume jurisdiction in this case. (Applies to same-sex married couples only)",
        "Mi wnaeth y partïon briodi ei gilydd o dan gyfraith Cymru a Lloegr, a byddai er budd cyfiawnder i'r llys ysgwyddo "
            + "awdurdodaeth yn yr achos hwn."),

    @JsonProperty("J")
    @JsonPropertyDescription("APP_1_RESIDENT_JOINT")
    APP_1_RESIDENT_JOINT("This is a joint application and applicant 1 is habitually resident in England and Wales.",
        "Mae Ceisydd 1 yn preswylio'n arferol yng Nghymru neu Lloegr.");

    private final String label;
    private final String labelCy;

    JurisdictionConnections(String label, String labelCy) {
        this.label = label;
        this.labelCy = labelCy;
    }
}
