package com.rahul.roboadvisor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdvisorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointReturnsUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.version").exists());
    }

    @Test
    void adviceEndpointReturnsAllocation() throws Exception {
        mockMvc.perform(get("/api/advice?age=30&risk=MEDIUM&amount=5000&years=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equityPercent").value(70))
                .andExpect(jsonPath("$.debtPercent").value(20))
                .andExpect(jsonPath("$.goldPercent").value(10));
    }

    @Test
    void badInputReturns400() throws Exception {
        mockMvc.perform(get("/api/advice?age=5&amount=5000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void configEndpointReturnsSettings() throws Exception {
        mockMvc.perform(get("/api/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("RoboAdvisor Lite"))
                .andExpect(jsonPath("$.environment").value("local"))
                .andExpect(jsonPath("$.themeColor").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void homePageIsServed() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk());
    }
}
