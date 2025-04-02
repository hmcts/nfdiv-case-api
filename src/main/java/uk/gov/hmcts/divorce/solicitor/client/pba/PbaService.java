package uk.gov.hmcts.divorce.solicitor.client.pba;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@Slf4j
@RequiredArgsConstructor
public class PbaService {

    private final HttpServletRequest httpServletRequest;

    private final IdamService idamService;

    private final PbaRefDataClient pbaRefDataClient;

    private final AuthTokenGenerator authTokenGenerator;

    public DynamicList populatePbaDynamicList() {
        List<DynamicListElement> pbaAccountNumbers = retrievePbaNumbers()
            .stream()
            .map(pbaNumber -> DynamicListElement.builder().label(pbaNumber).code(UUID.randomUUID()).build())
            .collect(Collectors.toList());

        return DynamicList
            .builder()
            .listItems(pbaAccountNumbers)
            .build();
    }

    private List<String> retrievePbaNumbers() {
        String solicitorAuthToken = httpServletRequest.getHeader(AUTHORIZATION);
        var solUserDetails = idamService.retrieveUser(solicitorAuthToken).getUserDetails();
        var solicitorEmail = solUserDetails.getSub();

        ResponseEntity<PbaOrganisationResponse> responseEntity =
            pbaRefDataClient.retrievePbaNumbers(solicitorAuthToken, authTokenGenerator.generate(), solicitorEmail);

        PbaOrganisationResponse pbaOrganisationResponse = Objects.requireNonNull(responseEntity.getBody());

        return pbaOrganisationResponse.getOrganisationEntityResponse().getPaymentAccount();
    }
}
