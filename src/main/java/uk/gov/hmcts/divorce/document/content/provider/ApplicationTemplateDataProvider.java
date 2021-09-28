package uk.gov.hmcts.divorce.document.content.provider;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Map.entry;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_DOMICILED;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_LAST_RESIDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_RESIDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_RESIDENT_SIX_MONTHS;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_RESIDENT_TWELVE_MONTHS;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.RESIDUAL_JURISDICTION;

@Component
public class ApplicationTemplateDataProvider {

    private static final List<JurisdictionConnections> JURISDICTION_ORDER = Arrays.asList(
        APP_1_APP_2_RESIDENT,
        APP_1_APP_2_LAST_RESIDENT,
        APP_1_RESIDENT_TWELVE_MONTHS,
        APP_1_RESIDENT_SIX_MONTHS,
        APP_1_APP_2_DOMICILED,
        RESIDUAL_JURISDICTION
    );

    private static final Map<JurisdictionConnections, String> JOINT_DESCRIPTIONS = Map.ofEntries(
        entry(APP_1_APP_2_RESIDENT, "the applicants are habitually resident in England and Wales"),
        entry(APP_1_APP_2_LAST_RESIDENT, "the applicants were last habitually resident in England and Wales "
            + "and one of them still resides there"),
        entry(APP_1_RESIDENT_TWELVE_MONTHS, "one of the applicants resides in England and Wales and has resided "
            + "there for at least a year immediately prior to the presentation of the application"),
        entry(APP_1_RESIDENT_SIX_MONTHS, "the applicants are domiciled and habitually resident in England and "
            + "Wales and have resided there for at least six months immediately prior to the application"),
        entry(APP_1_APP_2_DOMICILED, "the applicants are domiciled in England and Wales"),
        entry(RESIDUAL_JURISDICTION, "the applicants are registered as civil partners of each other in "
            + "England or Wales or, in the case of a same sex couple, married each other under the law of England "
            + "and Wales and it would be in the interests of justice for the court to assume jurisdiction in this "
            + "case")
    );

    private static final Map<JurisdictionConnections, String> SOLE_DESCRIPTIONS = Map.ofEntries(
        entry(APP_1_APP_2_RESIDENT, "the applicant and respondent are habitually resident in "
            + "England and Wales"),
        entry(APP_1_APP_2_LAST_RESIDENT, "the applicant and respondent were last habitually resident "
            + "in England and Wales and one of them still resides there"),
        entry(APP_1_RESIDENT_TWELVE_MONTHS, "the applicant is habitually resident in England and Wales "
            + "and has resided there for at least a year immediately prior to the presentation of the application"),
        entry(APP_1_RESIDENT_SIX_MONTHS, "the applicant is domiciled and habitually resident in England "
            + "and Wales and has resided there for at least six months immediately prior to the application"),
        entry(APP_1_APP_2_DOMICILED, "the applicant and respondent are both domiciled in England and Wales"),
        entry(RESIDUAL_JURISDICTION, "the applicant and respondent registered as civil partners "
            + "of each other in England or Wales or, in the case of a same sex couple, married each other under "
            + "the law of England and Wales and it would be in the interests of justice for the court to assume "
            + "jurisdiction in this case")
    );

    public List<Connection> deriveJointJurisdictionList(final Application application) {
        return deriveJurisdictionList(application, JOINT_DESCRIPTIONS);
    }

    public List<Connection> deriveSoleJurisdictionList(final Application application) {
        return deriveJurisdictionList(application, SOLE_DESCRIPTIONS);
    }

    private List<Connection> deriveJurisdictionList(final Application application,
                                                    final Map<JurisdictionConnections, String> descriptionMapping) {

        final Set<JurisdictionConnections> connections = application.getJurisdiction().getConnections();

        if (null == connections) {
            return emptyList();
        }

        return JURISDICTION_ORDER.stream()
            .filter(connections::contains)
            .map(jurisdictionConnection -> new Connection(descriptionMapping.get(jurisdictionConnection)))
            .collect(Collectors.toList());
    }

    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Connection {
        private String description;
    }
}
