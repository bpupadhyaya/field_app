package com.fieldapp.integration;

import com.fieldapp.FieldAppApplication;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

class FieldAppMainIntegrationTest {

    @Test
    void mainDelegatesToSpringApplicationRun() {
        String[] args = {"--spring.main.web-application-type=none"};
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
            FieldAppApplication.main(args);
            mocked.verify(() -> SpringApplication.run(FieldAppApplication.class, args));
        }
    }
}
