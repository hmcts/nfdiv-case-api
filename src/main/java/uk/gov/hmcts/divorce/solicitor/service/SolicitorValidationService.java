package uk.gov.hmcts.divorce.solicitor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;

@Slf4j
@RequiredArgsConstructor
@Service
public class SolicitorValidationService {

    private final CcdAccessService ccdAccessService;
    private final OrganisationClient organisationClient;

}
