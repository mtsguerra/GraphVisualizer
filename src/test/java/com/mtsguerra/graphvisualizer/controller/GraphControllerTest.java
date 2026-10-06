package com.mtsguerra.graphvisualizer.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GraphControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sampleGraphRoundTripsThroughDijkstra() throws Exception {
        String sample = mockMvc.perform(get("/api/sample"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(post("/api/dijkstra").contentType(MediaType.APPLICATION_JSON).content(sample))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algorithm").value("dijkstra"))
                .andExpect(jsonPath("$.path").value(org.hamcrest.Matchers.contains(0, 2, 1, 3, 4, 5)))
                .andExpect(jsonPath("$.totalDistance").value(13.0))
                .andExpect(jsonPath("$.steps[0].action").value("exploring"))
                .andExpect(jsonPath("$.steps[0].edge").doesNotExist());
    }

    @Test
    void bfsAcceptsMinimalJson() throws Exception {
        String body = """
                {
                  "nodes": [{"id": 1, "label": "A", "x": 0, "y": 0}, {"id": 2, "label": "B", "x": 10, "y": 0}],
                  "edges": [{"from": 1, "to": 2}],
                  "startNode": 1
                }
                """;

        mockMvc.perform(post("/api/bfs").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").doesNotExist())
                .andExpect(jsonPath("$.steps[2].nodeId").value(2))
                .andExpect(jsonPath("$.steps[2].action").value("exploring"))
                .andExpect(jsonPath("$.steps[2].edge.weight").value(1.0));
    }

    @Test
    void invalidGraphReturns400() throws Exception {
        String body = """
                {"nodes": [{"id": 1, "label": "A", "x": 0, "y": 0}], "edges": [], "startNode": 1}
                """;

        mockMvc.perform(post("/api/astar").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("endNode is required"));
    }

    @Test
    void corsAllowsFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/bfs")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));

        mockMvc.perform(options("/api/bfs")
                        .header("Origin", "http://evil.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
