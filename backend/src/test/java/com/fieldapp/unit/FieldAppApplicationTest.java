package com.fieldapp.unit;

import com.fieldapp.FieldAppApplication;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

class FieldAppApplicationTest {
    @Test
    void canInstantiateApplicationClass() {
        new FieldAppApplication();
    }

    @Test
    void mainDelegatesToSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
            FieldAppApplication.main(new String[]{"--spring.main.banner-mode=off"});
            mocked.verify(() -> SpringApplication.run(FieldAppApplication.class, new String[]{"--spring.main.banner-mode=off"}));
        }
    }
}
