package com.fieldapp.controller;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SuperAdminControllerTest {
    @Test
    void topologyReturnsCloudRows() {
        SuperAdminController controller = new SuperAdminController();
        Object out = controller.topology();

        assertThat(out).isInstanceOf(List.class);
        assertThat((List<?>) out).hasSize(3);
    }

    @Test
    void aiReturnsEnabledConfig() {
        SuperAdminController controller = new SuperAdminController();
        Object out = controller.ai();

        assertThat(out).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> ai = (Map<String, Object>) out;
        assertThat(ai).containsEntry("enabled", true);
    }
}
