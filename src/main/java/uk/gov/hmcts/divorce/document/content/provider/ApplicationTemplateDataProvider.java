package uk.gov.hmcts.divorce.document.content.provider;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.exception.InvalidCcdCaseDataException;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Character.toLowerCase;
import static java.util.Optional.ofNullable;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_DOMICILED;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_LAST_RESIDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_RESIDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_DOMICILED;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_RESIDENT_JOINT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_RESIDENT_SIX_MONTHS;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_RESIDENT_TWELVE_MONTHS;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_2_DOMICILED;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_2_RESIDENT_JOINT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_2_RESIDENT_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.RESIDUAL_JURISDICTION_CP;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.RESIDUAL_JURISDICTION_D;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.EMPTY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COUNTRY_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PLACE_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;


@Component
@Slf4j
public class ApplicationTemplateDataProvider {

    private static final List<JurisdictionConnections> JURISDICTION_ORDER = Arrays.asList(
        APP_1_APP_2_RESIDENT,
        APP_1_APP_2_LAST_RESIDENT,
        APP_2_RESIDENT_SOLE,
        APP_2_RESIDENT_JOINT,
        APP_1_RESIDENT_TWELVE_MONTHS,
        APP_1_RESIDENT_SIX_MONTHS,
        APP_1_APP_2_DOMICILED,
        APP_1_DOMICILED,
        APP_2_DOMICILED,
        RESIDUAL_JURISDICTION_CP,
        RESIDUAL_JURISDICTION_D,
        APP_1_RESIDENT_JOINT
    );

    public List<Connection> deriveJurisdictionList(final Application application, final Long caseId) {

        final Set<JurisdictionConnections> connections = application.getJurisdiction().getConnections();

        if (isEmpty(connections)) {
            final String errorMessage = "JurisdictionConnections" + EMPTY;
            log.info("{}, for case id {} ", errorMessage, caseId);
            throw new InvalidCcdCaseDataException(errorMessage);
        }

        return JURISDICTION_ORDER.stream()
            .filter(connections::contains)
            .map(jurisdictionConnection ->
                new Connection(toLowerCase(jurisdictionConnection.getLabel().charAt(0)) + jurisdictionConnection.getLabel().substring(1)))
            .collect(Collectors.toList());
    }

    public List<Connection> deriveJurisdictionList(final Application application, final Long caseId,
                                                   final LanguagePreference languagePreference) {

        final Set<JurisdictionConnections> connections = application.getJurisdiction().getConnections();

        if (isEmpty(connections)) {
            final String errorMessage = "JurisdictionConnections" + EMPTY;
            log.info("{}, for case id {} ", errorMessage, caseId);
            throw new InvalidCcdCaseDataException(errorMessage);
        }

        return JURISDICTION_ORDER.stream()
            .filter(connections::contains)
            .map(jurisdictionConnection -> {
                String label = WELSH.equals(languagePreference) && StringUtils.isNotBlank(jurisdictionConnection.getLabelCy())
                    ? jurisdictionConnection.getLabelCy() : jurisdictionConnection.getLabel();
                return new Connection(toLowerCase(label.charAt(0)) + label.substring(1));
            })
            .collect(Collectors.toList());
    }

    public void mapMarriageDetails(Map<String, Object> templateContent, final Application application) {
        templateContent.put(PLACE_OF_MARRIAGE, application.getMarriageDetails().getPlaceOfMarriage());
        templateContent.put(COUNTRY_OF_MARRIAGE, application.getMarriageDetails().getCountryOfMarriage());
        templateContent.put(MARRIAGE_DATE,
            ofNullable(application.getMarriageDetails().getDate())
                .map(marriageDate -> marriageDate.format(DATE_TIME_FORMATTER))
                .orElse(null));
    }

    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Connection {
        private String description;
    }
}
