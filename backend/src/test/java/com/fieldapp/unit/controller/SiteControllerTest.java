package com.fieldapp.unit.controller;

import com.fieldapp.controller.SiteController;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SiteControllerTest {
    @Test
    void navReturnsConfiguredMenuGroups() {
        SiteController controller = new SiteController();
        Object out = controller.nav();

        assertThat(out).isInstanceOf(List.class);
        List<?> groups = (List<?>) out;
        assertThat(groups).hasSize(5);
        @SuppressWarnings("unchecked")
        Map<String, Object> first = (Map<String, Object>) groups.get(0);
        assertThat(first).containsKeys("key", "items");

        @SuppressWarnings("unchecked")
        Map<String, Object> digital = (Map<String, Object>) groups.stream()
                .map(Map.class::cast)
                .filter(g -> "digital".equals(g.get("key")))
                .findFirst()
                .orElseThrow();
        assertThat(digital.get("items")).isEqualTo(List.of("Digital Tools"));
    }
}
