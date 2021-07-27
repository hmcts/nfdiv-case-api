package uk.gov.hmcts.divorce.common.ccd;

public interface CcdPageConfiguration {
    String NEVER_SHOW = "divorceOrDissolution=\"NEVER_SHOW\"";

    void addTo(final PageBuilder pageBuilder);
}
