package uk.gov.hmcts.divorce.legaladvisor.service.printer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;

@ExtendWith(MockitoExtension.class)
public class MissingDocumentsValidationTest {

    @InjectMocks
    private MissingDocumentsValidation missingDocumentsValidation = MissingDocumentsValidation.builder().build();

    @Test
    void shouldReturnDefaultInstanceOfMissingDocumentsValidationClass() {
        assertThat(missingDocumentsValidation.message)
            .isEqualTo("Warning Message When Insufficient Documents Are Found");
        assertThat(missingDocumentsValidation.documentTypeList)
            .isEqualTo(List.of(COVERSHEET));
        assertThat(missingDocumentsValidation.expectedDocumentsSize)
            .isEqualTo(1);
    }
}
