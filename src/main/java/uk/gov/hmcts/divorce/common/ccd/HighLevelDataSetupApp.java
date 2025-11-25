package uk.gov.hmcts.divorce.common.ccd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.befta.dse.ccd.CcdEnvironment;
import uk.gov.hmcts.befta.dse.ccd.CcdRoleConfig;
import uk.gov.hmcts.befta.dse.ccd.DataLoaderToDefinitionStore;
import uk.gov.hmcts.befta.exception.ImportException;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig;
import uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce;

import java.util.List;
import java.util.Locale;
import java.util.Set;


public class HighLevelDataSetupApp extends DataLoaderToDefinitionStore {

    private static final Logger logger = LoggerFactory.getLogger(HighLevelDataSetupApp.class);

    private static final CcdRoleConfig[] CCD_ROLES_NEEDED_FOR_NFD = {
        new CcdRoleConfig("caseworker-divorce-courtadmin_beta", "PUBLIC"),
        new CcdRoleConfig("caseworker-divorce-superuser", "PUBLIC"),
        new CcdRoleConfig("caseworker-divorce-courtadmin-la", "PUBLIC"),
        new CcdRoleConfig("caseworker-divorce-courtadmin", "PUBLIC"),
        new CcdRoleConfig("caseworker-divorce-solicitor", "PUBLIC"),
        new CcdRoleConfig("caseworker-divorce-judge", "PUBLIC"),
        new CcdRoleConfig("caseworker-divorce-pcqextractor", "PUBLIC"),
        new CcdRoleConfig("caseworker-divorce-systemupdate", "PUBLIC"),
        new CcdRoleConfig("caseworker-divorce-bulkscan", "PUBLIC"),
        new CcdRoleConfig("caseworker-caa", "PUBLIC"),
        new CcdRoleConfig("caseworker-approver", "PUBLIC"),
        new CcdRoleConfig("citizen", "PUBLIC"),
        new CcdRoleConfig("caseworker-divorce", "PUBLIC"),
        new CcdRoleConfig("caseworker", "PUBLIC"),
        new CcdRoleConfig("caseworker-divorce-rparobot", "PUBLIC"),
        new CcdRoleConfig("payments", "PUBLIC"),
        new CcdRoleConfig("pui-case-manager", "PUBLIC"),
        new CcdRoleConfig("pui-finance-manager", "PUBLIC"),
        new CcdRoleConfig("pui-organisation-manager", "PUBLIC"),
        new CcdRoleConfig("pui-user-manager", "PUBLIC"),
        new CcdRoleConfig("TTL_profile", "PUBLIC")
    };

    private final CcdEnvironment environment;

    private static final Set<Integer> TOLERABLE_EXCEPTIONS = Set.of(
        HttpStatus.GATEWAY_TIMEOUT.value(), HttpStatus.CONFLICT.value()
    );

    public HighLevelDataSetupApp(CcdEnvironment dataSetupEnvironment) {
        super(dataSetupEnvironment);
        environment = dataSetupEnvironment;
    }

    public static void main(String[] args) throws Throwable {
        main(HighLevelDataSetupApp.class, args);
    }

    @Override
    protected boolean shouldTolerateDataSetupFailure(Throwable e) {
        if (e instanceof ImportException importException) {
            return TOLERABLE_EXCEPTIONS.contains(importException.getHttpStatusCode());
        }

        var env = getDataSetupEnvironment();

        return CcdEnvironment.PERFTEST == env || CcdEnvironment.DEMO == env || CcdEnvironment.ITHC == env;
    }

    @Override
    public void addCcdRoles() {
        for (CcdRoleConfig roleConfig : CCD_ROLES_NEEDED_FOR_NFD) {
            try {
                logger.info("\n\nAdding CCD Role {}.", roleConfig);
                addCcdRole(roleConfig);
                logger.info("\n\nAdded CCD Role {}.", roleConfig);
            } catch (Exception e) {
                logger.error("\n\nCouldn't add CCD Role {} - Exception: {} .\n\n", roleConfig, e);
                if (!shouldTolerateDataSetupFailure()) {
                    throw e;
                }
            }
        }
    }

    @Override
    protected List<String> getAllDefinitionFilesToLoadAt(String definitionsPath) {
        String environmentName = environment.name().toLowerCase(Locale.UK);
        return List.of(
            "build/ccd-config/ccd-" + NoFaultDivorce.getCaseType() + "-" + environmentName + ".xlsx",
            "build/ccd-config/ccd-" + BulkActionCaseTypeConfig.getCaseType() + "-" + environmentName + ".xlsx"
        );
    }
}
