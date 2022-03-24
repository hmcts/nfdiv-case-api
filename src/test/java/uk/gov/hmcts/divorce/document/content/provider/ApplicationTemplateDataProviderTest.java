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
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.RESIDUAL_JURISDICTION_CP;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.EMPTY;

@ExtendWith(MockitoExtension.class)
class ApplicationTemplateDataProviderTest {

    @InjectMocks
    private ApplicationTemplateDataProvider applicationTemplateDataProvider;

    @Test
    void shouldReturnListOfJurisdictionsIfAllSelected() throws Exception {

        final var caseId = 124872587L;
        final var application = Application.builder().build();
        application.getJurisdiction().setConnections(Set.of(
            APP_1_APP_2_RESIDENT,
            APP_1_APP_2_LAST_RESIDENT,
            APP_1_RESIDENT_TWELVE_MONTHS,
            APP_1_RESIDENT_SIX_MONTHS,
            APP_1_APP_2_DOMICILED,
            RESIDUAL_JURISDICTION_CP));

        final var result = applicationTemplateDataProvider.deriveJurisdictionList(application, caseId);

        assertThat(result).containsExactly(
            new Connection("both parties to the marriage or civil partnership are habitually resident in England and Wales"),
            new Connection("both parties to the marriage or civil partnership were last habitually resident in England and "
                + "Wales and one of them continues to reside there"),
            new Connection("the applicant is habitually resident in England and Wales and has resided there for at least "
                + "one year immediately before the application was made"),
            new Connection("the applicant is domiciled and habitually resident in England and Wales and has resided there for "
                + "at least six months immediately before the application was made"),
            new Connection("both parties to the marriage or civil partnership are domiciled in England and Wales"),
            new Connection("the parties registered as civil partners of each other in England or Wales and it would be in the "
                + "interest of justice for the court to assume jurisdiction in this case")
        );
    }

    @Test
    void shouldThrowExceptionIfNoJurisdictionConnectsSelected() {

        final var caseId = 124872587L;
        final var application = Application.builder().build();
        application.getJurisdiction().setConnections(emptySet());

        assertThatThrownBy(() -> applicationTemplateDataProvider.deriveJurisdictionList(application, caseId))
            .isInstanceOf(InvalidCcdCaseDataException.class)
            .hasMessage("JurisdictionConnections" + EMPTY);
    }

    @Test
    void shouldThrowExceptionIfJurisdictionConnectionsIsNull() {

        final var caseId = 124872587L;
        final var application = Application.builder().build();

        assertThatThrownBy(() -> applicationTemplateDataProvider.deriveJurisdictionList(application, caseId))
            .isInstanceOf(InvalidCcdCaseDataException.class)
            .hasMessage("JurisdictionConnections" + EMPTY);
    }
}
