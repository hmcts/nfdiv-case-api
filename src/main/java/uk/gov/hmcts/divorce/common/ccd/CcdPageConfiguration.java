package uk.gov.hmcts.divorce.common.ccd;

public interface CcdPageConfiguration {
    String ALWAYS_SHOW = "";
    String NEVER_SHOW = "divorceOrDissolution=\"NEVER_SHOW\"";
    String NO_DEFAULT_VALUE = null;

    void addTo(final PageBuilder pageBuilder);
}
