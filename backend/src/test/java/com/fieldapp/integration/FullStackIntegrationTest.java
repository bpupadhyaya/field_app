package com.fieldapp.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fieldapp.model.Device;
import com.fieldapp.repo.DeviceRepo;
import com.fieldapp.repo.SnapshotRecordRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FullStackIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeviceRepo deviceRepo;

    @Autowired
    private SnapshotRecordRepo snapshotRecordRepo;

    @Test
    void authAndPublicSurfacesWork() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));

        mockMvc.perform(get("/api/"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void roleBoundEndpointsEnforceAccess() throws Exception {
        String adminToken = loginAndGetToken("admin", "admin123");
        String managerToken = loginAndGetToken("manager1", "manager123");
        String userToken = loginAndGetToken("user1", "user123");
        String sadminToken = loginAndGetToken("sadmin", "sadmin123");

        mockMvc.perform(get("/api/site/navigation")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/superadmin/cloud-topology")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/superadmin/cloud-topology")
                        .header("Authorization", "Bearer " + sadminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/superadmin/ai-management")
                        .header("Authorization", "Bearer " + sadminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/runtime")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/runtime")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/whoami")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"));
    }

    @Test
    void deviceOperationsAndRestrictionsWork() throws Exception {
        String adminToken = loginAndGetToken("admin", "admin123");
        String sadminToken = loginAndGetToken("sadmin", "sadmin123");
        String userToken = loginAndGetToken("user1", "user123");
        String managerToken = loginAndGetToken("manager1", "manager123");
        String superOnlyUsername = "superonly_" + UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"%s",
                                  "password":"sadmin123",
                                  "displayName":"Super Only",
                                  "email":"%s@field.local",
                                  "roles":["SUPER_ADMIN"]
                                }
                                """.formatted(superOnlyUsername, superOnlyUsername)))
                .andExpect(status().isOk());
        String superOnlyToken = loginAndGetToken(superOnlyUsername, "sadmin123");

        Device device = deviceRepo.findAll().getFirst();
        Long deviceId = device.getId();
        String zoneId = device.getZoneId();

        mockMvc.perform(get("/api/devices")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/devices")
                        .queryParam("q", "tractor")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/devices")
                        .queryParam("q", "   ")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/devices/" + deviceId + "/health")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/devices/" + deviceId + "/essential")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        for (String range : new String[]{"24h", "7d", "30d", "90d"}) {
            mockMvc.perform(get("/api/devices/" + deviceId + "/price-history")
                            .queryParam("range", range)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/devices/" + deviceId + "/control")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"command\":\"lane_navigation\",\"zoneId\":\"WRONG\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Command accepted: lane_navigation"));

        mockMvc.perform(post("/api/devices/" + deviceId + "/control")
                        .header("Authorization", "Bearer " + sadminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"command\":\"lane_navigation\",\"zoneId\":\"WRONG\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/devices/" + deviceId + "/control")
                        .header("Authorization", "Bearer " + superOnlyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"command\":\"lane_navigation\",\"zoneId\":\"WRONG\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/devices/" + deviceId + "/control")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"command\":\"lane_navigation\",\"zoneId\":\"WRONG\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/devices/" + deviceId + "/control")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"command\":\"loading\",\"zoneId\":\"" + zoneId + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/devices/" + deviceId + "/control")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"command\":\"loading\",\"zoneId\":\"WRONG\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User control restricted to assigned zone"));

        mockMvc.perform(post("/api/devices/" + deviceId + "/control")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"command\":\"loading\",\"zoneId\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User control restricted to assigned zone"));

        mockMvc.perform(post("/api/devices/" + deviceId + "/control")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"command\":\"steer_outside_zone\",\"zoneId\":\"" + zoneId + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User cannot steer device outside zone"));

        mockMvc.perform(post("/api/devices/" + deviceId + "/control")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"command\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));

        mockMvc.perform(patch("/api/devices/" + deviceId + "/price")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\": " + BigDecimal.valueOf(77777) + "}"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/devices/" + deviceId + "/price")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\": " + BigDecimal.valueOf(88888) + "}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Unexpected server error"));
    }

    @Test
    void userAndAdminWorkflowsCoverSuccessAndFailurePaths() throws Exception {
        String adminToken = loginAndGetToken("admin", "admin123");
        String managerToken = loginAndGetToken("manager1", "manager123");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users")
                        .queryParam("q", "admin")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/users")
                        .queryParam("q", "Admin User")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());

        String username = "intuser_" + UUID.randomUUID().toString().substring(0, 8);
        String createBody = """
                {
                  "username":"%s",
                  "password":"user123",
                  "displayName":"Integration User",
                  "email":"%s@field.local",
                  "roles":["USER"]
                }
                """.formatted(username, username);

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username already exists"));

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"badrole","password":"x","displayName":"x","email":"x@field.local","roles":[""]}"""))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"badrole2","password":"x","displayName":"x","email":"x@field.local","roles":["NOPE"]}"""))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"roleprefix%s","password":"x","displayName":"Role Prefix","email":"roleprefix%s@field.local","userType":"DEALER","roles":["ROLE_USER"]}"""
                                .formatted(System.nanoTime(), System.nanoTime())))
                .andExpect(status().isOk());

        String snapshotResponse = mockMvc.perform(post("/api/admin/snapshots")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode snapshotJson = objectMapper.readTree(snapshotResponse);
        long snapshotId = snapshotJson.get("id").asLong();
        assertThat(snapshotId).isPositive();

        mockMvc.perform(get("/api/admin/snapshots")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/snapshots/" + snapshotId + "/restore")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        long missingId = snapshotRecordRepo.findAll().stream().mapToLong(s -> s.getId()).max().orElse(1L) + 1000;
        mockMvc.perform(post("/api/admin/snapshots/" + missingId + "/restore")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/admin/snapshots/cleanup")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"keepMinCount\":5,\"maxAgeDays\":30}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/traffic")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/audit")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String body = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("token").asText();
    }
}
