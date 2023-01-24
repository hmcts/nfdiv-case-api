package uk.gov.hmcts.divorce.legaladvisor.service.printer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class MissingDocumentsValidationTest {

    @InjectMocks
    private MissingDocumentsValidation missingDocumentsValidation = MissingDocumentsValidation.builder().build();

    @Test
    void ShouldReturnDefaultInstanceOfMissingDocumentsValidationClass() {
        assertThat(missingDocumentsValidation.message)
            .isEqualTo("");
        assertThat(missingDocumentsValidation.documentTypeList)
            .isEqualTo(List.of());
        assertThat(missingDocumentsValidation.expectedDocumentsSize)
            .isEqualTo(0);
    }
}
