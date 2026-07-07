package com.lucabridge.blog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke test for the public read path (Coding Plan Phase 1: "Integration tests (Testcontainers Postgres)").
 * Flyway runs db/migration (schema) + db/seed (demo data) against a real Postgres container.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class PostApiIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("lucabridge")
            .withUsername("lucabridge")
            .withPassword("lucabridge");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.locations", () -> "classpath:db/migration,classpath:db/seed");
        registry.add("spring.profiles.active", () -> "dev");
    }

    @Autowired
    MockMvc mockMvc;

    @Test
    void categoriesEndpoint_returnsLocalizedFourMissionCategories() throws Exception {
        mockMvc.perform(get("/api/categories").param("lang", "zh-Hant"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void postsEndpoint_returnsPublishedSeedPosts() throws Exception {
        mockMvc.perform(get("/api/posts").param("lang", "zh-Hant"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(4));
    }

    @Test
    void postDetail_fallsBackToZhHantWhenTranslationMissing() throws Exception {
        // 'campus-recycling-workshop' only has a zh-Hant translation seeded.
        mockMvc.perform(get("/api/posts/campus-recycling-workshop").param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fallback").value(true));
    }

    @Test
    void postDetail_404sForUnknownSlug() throws Exception {
        mockMvc.perform(get("/api/posts/does-not-exist"))
                .andExpect(status().isNotFound());
    }
}
