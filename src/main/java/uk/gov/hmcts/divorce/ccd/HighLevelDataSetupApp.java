package uk.gov.hmcts.divorce.ccd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.befta.dse.ccd.CcdEnvironment;
import uk.gov.hmcts.befta.dse.ccd.CcdRoleConfig;
import uk.gov.hmcts.befta.dse.ccd.DataLoaderToDefinitionStore;

public class HighLevelDataSetupApp extends DataLoaderToDefinitionStore {

    private static final Logger logger = LoggerFactory.getLogger(HighLevelDataSetupApp.class);

    private static final CcdRoleConfig[] CCD_ROLES_NEEDED_FOR_NFD = {
        new CcdRoleConfig("caseworker-divorce-courtadmin_beta", "PUBLIC"),
        new CcdRoleConfig("caseworker-divorce-superuser", "PUBLIC"),
        new CcdRoleConfig("caseworker-divorce-courtadmin-la", "PUBLIC"),
        new CcdRoleConfig("caseworker-divorce-courtadmin", "PUBLIC"),
        new CcdRoleConfig("caseworker-divorce-solicitor", "PUBLIC"),
        new CcdRoleConfig("caseworker-divorce-pcqextractor", "PUBLIC"),
        new CcdRoleConfig("caseworker-divorce-systemupdate", "PUBLIC"),
        new CcdRoleConfig("caseworker-divorce-bulkscan", "PUBLIC"),
        new CcdRoleConfig("caseworker-caa", "PUBLIC"),
        new CcdRoleConfig("citizen", "PUBLIC")

    };

    public HighLevelDataSetupApp(CcdEnvironment dataSetupEnvironment) {
        super(dataSetupEnvironment,"build/definitions");
    }

    public static void main(String[] args) throws Throwable {
        main(HighLevelDataSetupApp.class, args);
    }

    @Override
    protected boolean shouldTolerateDataSetupFailure() {
        return true;
    }

    @Override
    protected void doLoadTestData() {
        addCcdRoles();
        super.importDefinitions();
    }

    public void addCcdRoles() {
        for (CcdRoleConfig roleConfig : CCD_ROLES_NEEDED_FOR_NFD) {
            try {
                logger.info("\n\nAdding CCD Role {}.", roleConfig);
                addCcdRole(roleConfig);
                logger.info("\n\nAdded CCD Role {}.", roleConfig);
            } catch (Exception e) {
                logger.error("\n\nCouldn't add CCD Role {} - Exception: {}.\n\n", roleConfig, e);
                if (!shouldTolerateDataSetupFailure()) {
                    throw e;
                }
            }
        }
    }
}
