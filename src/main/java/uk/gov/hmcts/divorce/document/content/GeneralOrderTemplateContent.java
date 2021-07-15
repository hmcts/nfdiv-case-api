package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Component
@Slf4j
public class GeneralOrderTemplateContent {

    @Value("${court.locations.serviceCentre.serviceCentreName}")
    private String serviceCentre;

    @Value("${court.locations.serviceCentre.centreName}")
    private String centreName;

    @Value("${court.locations.serviceCentre.poBox}")
    private String poBox;

    @Value("${court.locations.serviceCentre.town}")
    private String town;

    @Value("${court.locations.serviceCentre.postCode}")
    private String postcode;

    @Value("${court.locations.serviceCentre.email}")
    private String email;

    @Value("${court.locations.serviceCentre.phoneNumber}")
    private String phoneNumber;

    private static final DateTimeFormatter TEMPLATE_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy");

    public Supplier<Map<String, Object>> apply(final CaseData caseData,
                                               final Long ccdCaseReference) {

        return () -> {
            Map<String, Object> templateData = new HashMap<>();

            log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

            var generalOrder = caseData.getGeneralOrder();
            templateData.put("generalOrderDate", generalOrder.getGeneralOrderDate().format(TEMPLATE_DATE_FORMAT));
            templateData.put("caseReference", ccdCaseReference);
            templateData.put("petitionerFullName", caseData.getApplication().getMarriageDetails().getApplicant1Name());
            templateData.put("respondentFullName", caseData.getApplication().getMarriageDetails().getApplicant2Name());
            templateData.put("generalOrderRecitals", generalOrder.getGeneralOrderRecitals());
            templateData.put("judgeType", generalOrder.getGeneralOrderJudgeType().getLabel());
            templateData.put("judgeName", generalOrder.getGeneralOrderJudgeName());
            templateData.put("generalOrderDetails", generalOrder.getGeneralOrderDetails());

            var ctscContactDetails = CtscContactDetails
                .builder()
                .centreName(centreName)
                .emailAddress(email)
                .serviceCentre(serviceCentre)
                .poBox(poBox)
                .town(town)
                .postcode(postcode)
                .phoneNumber(phoneNumber)
                .build();

            templateData.put("ctscContactDetails", ctscContactDetails);

            return templateData;
        };
    }


}
