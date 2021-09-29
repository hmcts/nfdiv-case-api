package uk.gov.hmcts.divorce.document.content.provider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.exception.InvalidCcdCaseDataException;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.document.content.provider.ApplicationTemplateDataProvider.Connection;

import java.util.Set;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_DOMICILED;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_LAST_RESIDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_RESIDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_RESIDENT_SIX_MONTHS;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_RESIDENT_TWELVE_MONTHS;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.RESIDUAL_JURISDICTION;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.EMPTY;

@ExtendWith(MockitoExtension.class)
class ApplicationTemplateDataProviderTest {

    @InjectMocks
    private ApplicationTemplateDataProvider applicationTemplateDataProvider;

    @Test
    void shouldReturnListOfJurisdictionsForJointIfAllSelected() {

        final var caseId = 124872587L;
        final var application = Application.builder().build();
        application.getJurisdiction().setConnections(Set.of(
            APP_1_APP_2_RESIDENT,
            APP_1_APP_2_LAST_RESIDENT,
            APP_1_RESIDENT_TWELVE_MONTHS,
            APP_1_RESIDENT_SIX_MONTHS,
            APP_1_APP_2_DOMICILED,
            RESIDUAL_JURISDICTION));

        final var result = applicationTemplateDataProvider.deriveJointJurisdictionList(application, caseId);

        assertThat(result).containsExactly(
            new Connection("the applicants are habitually resident in England and Wales"),
            new Connection("the applicants were last habitually resident in England and Wales and one of them still resides there"),
            new Connection("one of the applicants resides in England and Wales and has resided there for at least a year immediately "
                + "prior to the presentation of the application"),
            new Connection("the applicants are domiciled and habitually resident in England and Wales and have resided there for at "
                + "least six months immediately prior to the application"),
            new Connection("the applicants are domiciled in England and Wales"),
            new Connection("the applicants are registered as civil partners of each other in England or Wales "
                + "or, in the case of a same sex couple, married each other under the law of England and Wales and it "
                + "would be in the interests of justice for the court to assume jurisdiction in this case")
        );
    }

    @Test
    void shouldThrowExceptionForJointIfNoJurisdictionConnectsSelected() {

        final var caseId = 124872587L;
        final var application = Application.builder().build();
        application.getJurisdiction().setConnections(emptySet());

        assertThatThrownBy(() -> applicationTemplateDataProvider.deriveJointJurisdictionList(application, caseId))
            .isInstanceOf(InvalidCcdCaseDataException.class)
            .hasMessage("JurisdictionConnections" + EMPTY);
    }

    @Test
    void shouldThrowExceptionForJointIfJurisdictionConnectionsIsNull() {

        final var caseId = 124872587L;
        final var application = Application.builder().build();

        assertThatThrownBy(() -> applicationTemplateDataProvider.deriveJointJurisdictionList(application, caseId))
            .isInstanceOf(InvalidCcdCaseDataException.class)
            .hasMessage("JurisdictionConnections" + EMPTY);
    }

    @Test
    void shouldReturnListOfJurisdictionsForSoleIfAllSelected() throws Exception {

        final var caseId = 124872587L;
        final var application = Application.builder().build();
        application.getJurisdiction().setConnections(Set.of(
            APP_1_APP_2_RESIDENT,
            APP_1_APP_2_LAST_RESIDENT,
            APP_1_RESIDENT_TWELVE_MONTHS,
            APP_1_RESIDENT_SIX_MONTHS,
            APP_1_APP_2_DOMICILED,
            RESIDUAL_JURISDICTION));

        final var result = applicationTemplateDataProvider.deriveSoleJurisdictionList(application, caseId);

        assertThat(result).containsExactly(
            new Connection("the applicant and respondent are habitually resident in England and Wales"),
            new Connection("the applicant and respondent were last habitually resident in England and Wales "
                + "and one of them still resides there"),
            new Connection("the applicant is habitually resident in England and Wales and has resided there "
                + "for at least a year immediately prior to the presentation of the application"),
            new Connection("the applicant is domiciled and habitually resident in England and Wales and has "
                + "resided there for at least six months immediately prior to the application"),
            new Connection("the applicant and respondent are both domiciled in England and Wales"),
            new Connection("the applicant and respondent registered as civil partners of each other in England "
                + "or Wales or, in the case of a same sex couple, married each other under the law of England and Wales "
                + "and it would be in the interests of justice for the court to assume jurisdiction in this case")
        );
    }

    @Test
    void shouldThrowExceptionForSoleIfNoJurisdictionConnectsSelected() {

        final var caseId = 124872587L;
        final var application = Application.builder().build();
        application.getJurisdiction().setConnections(emptySet());

        assertThatThrownBy(() -> applicationTemplateDataProvider.deriveSoleJurisdictionList(application, caseId))
            .isInstanceOf(InvalidCcdCaseDataException.class)
            .hasMessage("JurisdictionConnections" + EMPTY);
    }

    @Test
    void shouldThrowExceptionForSoleIfJurisdictionConnectionsIsNull() {

        final var caseId = 124872587L;
        final var application = Application.builder().build();

        assertThatThrownBy(() -> applicationTemplateDataProvider.deriveSoleJurisdictionList(application, caseId))
            .isInstanceOf(InvalidCcdCaseDataException.class)
            .hasMessage("JurisdictionConnections" + EMPTY);
    }
}