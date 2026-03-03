package com.fieldapp.controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/superadmin")
public class SuperAdminController {
    @GetMapping("/cloud-topology")
    public Object topology() {
        return List.of(
                Map.of("cloud", "AWS", "region", "us-east-1", "zone", "use1-az1", "status", "RUNNING"),
                Map.of("cloud", "GCP", "region", "us-central1", "zone", "us-central1-a", "status", "READY"),
                Map.of("cloud", "Azure", "region", "eastus", "zone", "1", "status", "READY")
        );
    }

    @GetMapping("/ai-management")
    public Object ai() {
        return Map.of("enabled", true, "providers", List.of("OpenAI", "Vertex AI", "Azure OpenAI"), "notes", "Pluggable AI management surface");
    }
}
