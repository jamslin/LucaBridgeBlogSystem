package com.lucabridge.blog;

import com.lucabridge.blog.entity.Event;
import com.lucabridge.blog.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;

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

    @Autowired
    EventRepository eventRepository;

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
                .andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    void postDetail_fallsBackToZhHantWhenTranslationMissing() throws Exception {
        mockMvc.perform(get("/api/posts/coastal-cleanup-tsing-lung-tau-2025").param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fallback").value(true));
    }

    @Test
    void postDetail_404sForUnknownSlug() throws Exception {
        mockMvc.perform(get("/api/posts/does-not-exist"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void duplicateEventSlug_returnsConflict() throws Exception {
        Event event = eventRepository.findAll().get(0);
        String request = """
                {"slug":"%s","translations":[{"lang":"zh-Hant","title":"Duplicate","bodyMarkdown":"Body"}]}
                """.formatted(event.getSlug());

        mockMvc.perform(post("/api/admin/events").contentType(APPLICATION_JSON).content(request))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void eventWithoutStart_canBeDraftedButNotPublished() throws Exception {
        String request = """
                {"slug":"hardening-no-start","translations":[{"lang":"zh-Hant","title":"Draft","bodyMarkdown":"Body"}]}
                """;
        String id = mockMvc.perform(post("/api/admin/events").contentType(APPLICATION_JSON).content(request))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        mockMvc.perform(post("/api/admin/events/{id}/publish", Long.valueOf(id)))
                .andExpect(status().isBadRequest());
    }
}
