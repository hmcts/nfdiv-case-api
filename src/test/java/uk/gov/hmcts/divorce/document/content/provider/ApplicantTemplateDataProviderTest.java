package uk.gov.hmcts.divorce.document.content.provider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.CHILDREN;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_APP1_CONTACT_PRIVATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_APP1_REPRESENTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_APP2_CONTACT_PRIVATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_APP2_REPRESENTED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_ADDRESS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.LINE_1_LINE_2_CITY_POSTCODE;

@ExtendWith(MockitoExtension.class)
class ApplicantTemplateDataProviderTest {

    @InjectMocks
    private ApplicantTemplateDataProvider applicantTemplateDataProvider;

    @Test
    void shouldReturnNullForJointIfNoFinancialOrder() {

        final Applicant applicant = Applicant.builder().build();

        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(applicant, false)).isNull();
    }

    @Test
    void shouldReturnNullForJointIfEmptyFinancialOrder() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrdersFor(emptySet())
            .build();

        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(applicant, false)).isNull();
    }

    @Test
    void shouldReturnNullForJointIfFinancialOrderIsNo() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(NO)
            .build();

        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(applicant, false)).isNull();
    }

    @Test
    void shouldReturnCorrectStringForJointFinancialOrderForApplicantAndChildrenForJoint() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrdersFor(Set.of(APPLICANT, CHILDREN))
            .build();

        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(applicant, false))
            .isEqualTo("applicants, and for the children of both the applicants.");
    }

    @Test
    void shouldReturnCorrectStringForJointFinancialOrderForApplicant() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrdersFor(Set.of(APPLICANT))
            .build();

        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(applicant, false))
            .isEqualTo("applicants.");
    }

    @Test
    void shouldReturnCorrectStringForJointFinancialOrderForChildren() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrdersFor(Set.of(CHILDREN))
            .build();

        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(applicant, false))
            .isEqualTo("children of both the applicants.");
    }

    @Test
    void shouldReturnNullForJointIfNoFinancialOrderInWelsh() {

        final Applicant applicant = Applicant.builder().build();

        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(applicant, false)).isNull();
    }

    @Test
    void shouldReturnNullForJointIfEmptyFinancialOrderInWelsh() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrdersFor(emptySet())
            .build();

        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(applicant, true)).isNull();
    }

    @Test
    void shouldReturnNullForJointIfFinancialOrderIsNoInWelsh() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(NO)
            .build();

        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(applicant, true)).isNull();
    }

    @Test
    void shouldReturnCorrectStringForJointFinancialOrderForApplicantAndChildrenForJointInWelsh() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrdersFor(Set.of(APPLICANT, CHILDREN))
            .build();

        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(applicant, true))
            .isEqualTo("ceiswyr, a phlant y ddau geisydd.");
    }

    @Test
    void shouldReturnCorrectStringForJointFinancialOrderForApplicantInWelsh() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrdersFor(Set.of(APPLICANT))
            .build();

        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(applicant, true))
            .isEqualTo("ceiswyr.");
    }

    @Test
    void shouldReturnCorrectStringForJointFinancialOrderForChildrenInWelsh() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrdersFor(Set.of(CHILDREN))
            .build();

        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(applicant, true))
            .isEqualTo("phlant y ddau geisydd.");
    }

    @Test
    void shouldReturnNullForJointIfEmptyFinancialOrderFor() {
        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(emptySet())).isNull();
    }

    @Test
    void shouldReturnCorrectStringIfFinancialOrderForContainsApplicantAndChildrenForJoint() {
        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(Set.of(APPLICANT, CHILDREN)))
            .isEqualTo("applicants, and for the children of both the applicants.");
    }

    @Test
    void shouldReturnCorrectStringIfFinancialOrderForContainsApplicant() {
        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(Set.of(APPLICANT)))
            .isEqualTo("applicants.");
    }

    @Test
    void shouldReturnCorrectStringIfFinancialOrderForContainsChildren() {
        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(Set.of(CHILDREN)))
            .isEqualTo("children of both the applicants.");
    }

    @Test
    void shouldReturnNullForSoleIfNoFinancialOrder() {

        final Applicant applicant = Applicant.builder().build();

        assertThat(applicantTemplateDataProvider.deriveSoleFinancialOrder(applicant)).isNull();
    }

    @Test
    void shouldReturnNullForSoleIfEmptyFinancialOrder() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrdersFor(emptySet())
            .build();

        assertThat(applicantTemplateDataProvider.deriveSoleFinancialOrder(applicant)).isNull();
    }

    @Test
    void shouldReturnNullForSoleIfFinancialOrderIsNo() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(NO)
            .build();

        assertThat(applicantTemplateDataProvider.deriveSoleFinancialOrder(applicant)).isNull();
    }

    @Test
    void shouldReturnCorrectStringForSoleFinancialOrderForApplicantAndChildrenForJoint() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrdersFor(Set.of(APPLICANT, CHILDREN))
            .build();

        assertThat(applicantTemplateDataProvider.deriveSoleFinancialOrder(applicant))
            .isEqualTo("applicant, and for the children of the applicant and the respondent.");
    }

    @Test
    void shouldReturnCorrectStringForSoleFinancialOrderForApplicant() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrdersFor(Set.of(APPLICANT))
            .build();

        assertThat(applicantTemplateDataProvider.deriveSoleFinancialOrder(applicant))
            .isEqualTo("applicant.");
    }

    @Test
    void shouldReturnCorrectStringForSoleFinancialOrderForChildren() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrdersFor(Set.of(CHILDREN))
            .build();

        assertThat(applicantTemplateDataProvider.deriveSoleFinancialOrder(applicant))
            .isEqualTo("children of the applicant and the respondent.");
    }

    @Test
    void shouldReturnWelshContentForSoleFinancialOrderForApplicantAndChildrenForJoint() {

        final Applicant applicant = Applicant.builder()
            .languagePreferenceWelsh(YES)
            .financialOrder(YES)
            .financialOrdersFor(Set.of(APPLICANT, CHILDREN))
            .build();

        assertThat(applicantTemplateDataProvider.deriveSoleFinancialOrder(applicant))
            .isEqualTo("y ceisydd a phlant y ceisydd a'r atebydd.");
    }

    @Test
    void shouldReturnWelshContentForSoleFinancialOrderForApplicant() {

        final Applicant applicant = Applicant.builder()
            .languagePreferenceWelsh(YES)
            .financialOrder(YES)
            .financialOrdersFor(Set.of(APPLICANT))
            .build();

        assertThat(applicantTemplateDataProvider.deriveSoleFinancialOrder(applicant))
            .isEqualTo("y ceisydd.");
    }

    @Test
    void shouldReturnWelshContentForSoleFinancialOrderForChildren() {

        final Applicant applicant = Applicant.builder()
            .languagePreferenceWelsh(YES)
            .financialOrder(YES)
            .financialOrdersFor(Set.of(CHILDREN))
            .build();

        assertThat(applicantTemplateDataProvider.deriveSoleFinancialOrder(applicant))
            .isEqualTo("plant y ceisydd a'r atebydd.");
    }

    @Test
    public void shouldMapApplicantContactDetailsWhenApplicantContactIsNotPrivateAndIsRepresented() {
        Applicant applicant1 = buildApplicant(YES, ContactDetailsType.PUBLIC);
        Applicant applicant2 = buildApplicant(YES, ContactDetailsType.PUBLIC);

        Map<String, Object> templateContent = new HashMap<>();

        applicantTemplateDataProvider.mapContactDetails(applicant1, applicant2, templateContent);

        assertThat(templateContent).contains(
            entry(APPLICANT_1_EMAIL, "sol@gm.com"),
            entry(APPLICANT_1_POSTAL_ADDRESS, "sol address"),
            entry(APPLICANT_2_EMAIL, "sol@gm.com"),
            entry(APPLICANT_2_POSTAL_ADDRESS, "sol address"),
            entry(IS_APP1_CONTACT_PRIVATE, false),
            entry(IS_APP1_REPRESENTED, true),
            entry(IS_APP2_CONTACT_PRIVATE, false),
            entry(IS_APP2_REPRESENTED, true)
        );
    }

    @Test
    public void shouldMapSolicitorContactDetailsWhenApplicantContactIsPrivateAndIsRepresented() {
        Applicant applicant1 = buildApplicant(YES, ContactDetailsType.PRIVATE);
        Applicant applicant2 = buildApplicant(YES, ContactDetailsType.PRIVATE);

        Map<String, Object> templateContent = new HashMap<>();

        applicantTemplateDataProvider.mapContactDetails(applicant1, applicant2, templateContent);

        assertThat(templateContent).contains(
            entry(APPLICANT_1_EMAIL, "sol@gm.com"),
            entry(APPLICANT_1_POSTAL_ADDRESS, "sol address"),
            entry(APPLICANT_2_EMAIL, "sol@gm.com"),
            entry(APPLICANT_2_POSTAL_ADDRESS, "sol address"),
            entry(IS_APP1_CONTACT_PRIVATE, true),
            entry(IS_APP1_REPRESENTED, true),
            entry(IS_APP2_CONTACT_PRIVATE, true),
            entry(IS_APP2_REPRESENTED, true)
        );
    }

    @Test
    public void shouldMapSolicitorContactDetailsWhenApplicantContactIsPrivateAndIsNotRepresented() {
        Applicant applicant1 = buildApplicant(NO, ContactDetailsType.PRIVATE);
        Applicant applicant2 = buildApplicant(YES, ContactDetailsType.PRIVATE);

        Map<String, Object> templateContent = new HashMap<>();

        applicantTemplateDataProvider.mapContactDetails(applicant1, applicant2, templateContent);

        assertThat(templateContent).contains(
            entry(APPLICANT_1_EMAIL, null),
            entry(APPLICANT_1_POSTAL_ADDRESS, null),
            entry(APPLICANT_2_EMAIL, "sol@gm.com"),
            entry(APPLICANT_2_POSTAL_ADDRESS, "sol address"),
            entry(IS_APP1_CONTACT_PRIVATE, true),
            entry(IS_APP1_REPRESENTED, false),
            entry(IS_APP2_CONTACT_PRIVATE, true),
            entry(IS_APP2_REPRESENTED, true)
        );
    }

    @Test
    public void shouldMapApplicantContactDetailsWhenApplicantContactIsNotPrivateAndIsNotRepresented() {
        Applicant applicant1 = buildApplicant(NO, ContactDetailsType.PUBLIC);
        Applicant applicant2 = buildApplicant(NO, ContactDetailsType.PUBLIC);

        Map<String, Object> templateContent = new HashMap<>();

        applicantTemplateDataProvider.mapContactDetails(applicant1, applicant2, templateContent);

        assertThat(templateContent).contains(
            entry(APPLICANT_1_EMAIL, "app@gm.com"),
            entry(APPLICANT_1_POSTAL_ADDRESS, LINE_1_LINE_2_CITY_POSTCODE),
            entry(APPLICANT_2_EMAIL, "app@gm.com"),
            entry(APPLICANT_2_POSTAL_ADDRESS, LINE_1_LINE_2_CITY_POSTCODE),
            entry(IS_APP1_CONTACT_PRIVATE, false),
            entry(IS_APP1_REPRESENTED, false),
            entry(IS_APP2_CONTACT_PRIVATE, false),
            entry(IS_APP2_REPRESENTED, false)
        );
    }

    private Applicant buildApplicant(YesOrNo isRepresented, ContactDetailsType contactDetailsType) {
        return Applicant.builder()
            .contactDetailsType(contactDetailsType)
            .email("app@gm.com")
            .address(APPLICANT_ADDRESS)
            .solicitorRepresented(isRepresented)
            .solicitor(Solicitor.builder().email("sol@gm.com").address("sol address").build())
            .build();
    }
}
