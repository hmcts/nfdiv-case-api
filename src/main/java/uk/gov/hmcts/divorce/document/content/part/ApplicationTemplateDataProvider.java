package uk.gov.hmcts.divorce.document.content.part;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_DOMICILED;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_LAST_RESIDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_RESIDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_RESIDENT_SIX_MONTHS;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_RESIDENT_TWELVE_MONTHS;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.RESIDUAL_JURISDICTION;

@Component
public class ApplicationTemplateDataProvider {

    public List<Connection> deriveJointJurisdictionList(final Application application) {

        final Set<JurisdictionConnections> connections = application.getJurisdiction().getConnections();
        final ArrayList<Connection> jurisdictionList = new ArrayList<>();

        if (null != connections) {

            if (connections.contains(APP_1_APP_2_RESIDENT)) {
                jurisdictionList.add(new Connection("the applicants are habitually resident in England and Wales"));
            }

            if (connections.contains(APP_1_APP_2_LAST_RESIDENT)) {
                jurisdictionList.add(new Connection("the applicants were last habitually resident in England and Wales "
                    + "and one of them still resides there"));
            }

            //TODO: check this is the correct connection for joint
            if (connections.contains(APP_1_RESIDENT_TWELVE_MONTHS)) {
                jurisdictionList.add(new Connection("one of the applicants resides in England and Wales and has resided "
                    + "there for at least a year immediately prior to the presentation of the application"));
            }

            //TODO: check this is the correct connection for joint
            if (connections.contains(APP_1_RESIDENT_SIX_MONTHS)) {
                jurisdictionList.add(new Connection("the applicants are domiciled and habitually resident in England and "
                    + "Wales and have resided there for at least six months immediately prior to the application"));
            }

            if (connections.contains(APP_1_APP_2_DOMICILED)) {
                jurisdictionList.add(new Connection("the applicants are domiciled in England and Wales"));
            }

            if (connections.contains(RESIDUAL_JURISDICTION)) {
                jurisdictionList.add(new Connection("the applicants are registered as civil partners of each other in "
                    + "England or Wales or, in the case of a same sex couple, married each other under the law of England "
                    + "and Wales and it would be in the interests of justice for the court to assume jurisdiction in this "
                    + "case"));
            }
        }

        return jurisdictionList;
    }

    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Connection {
        private String description;
    }
}
