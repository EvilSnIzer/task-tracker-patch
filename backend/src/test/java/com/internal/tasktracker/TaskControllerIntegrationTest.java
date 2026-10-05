package com.internal.tasktracker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end tests for GET /api/tasks against the seeded H2 data.
 * Each test pins the behaviour of one of the bugs fixed in this patch.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void blankSearchReturnsOnlyNonArchivedTasks() throws Exception {
        // 49 rows seeded, 2 archived. The AND/OR precedence bug made this 49.
        mvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(47))
                .andExpect(jsonPath("$.items", hasSize(10)))
                .andExpect(jsonPath("$.items[*].archived", everyItem(is(false))));
    }

    @Test
    void statusFilterIsApplied() throws Exception {
        // Was 47 (status silently ignored for title matches).
        mvc.perform(get("/api/tasks?status=OPEN"))
                .andExpect(jsonPath("$.total").value(31))
                .andExpect(jsonPath("$.items[*].status", everyItem(is("OPEN"))));
    }

    @Test
    void searchIsCaseInsensitiveAndExcludesArchived() throws Exception {
        // Uppercase term must still match; archived ids 20/21 must not leak.
        mvc.perform(get("/api/tasks?q=API&pageSize=50"))
                .andExpect(jsonPath("$.total").value(8))
                .andExpect(jsonPath("$.items[*].title",
                        not(hasItem("Legacy API cleanup"))))
                .andExpect(jsonPath("$.items[*].archived", everyItem(is(false))));
    }

    @Test
    void searchAndStatusCompose() throws Exception {
        mvc.perform(get("/api/tasks?q=api&status=IN_PROGRESS"))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.items[*].status", everyItem(is("IN_PROGRESS"))));
    }

    @Test
    void unknownStatusReturns400Not500() throws Exception {
        mvc.perform(get("/api/tasks?status=BOGUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_STATUS"));
    }

    @Test
    void paginationParamsAreClamped() throws Exception {
        mvc.perform(get("/api/tasks?page=0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1));

        mvc.perform(get("/api/tasks?pageSize=100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize").value(100));

        mvc.perform(get("/api/tasks?page=99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.total").value(47));
    }

    @Test
    void likeWildcardsAreMatchedLiterally() throws Exception {
        // No seeded task contains a literal '%' or '_'; before the escape
        // fix, q=% acted as a wildcard and returned all 47 tasks.
        mvc.perform(get("/api/tasks?q=%"))
                .andExpect(jsonPath("$.total").value(0));
        mvc.perform(get("/api/tasks?q=_"))
                .andExpect(jsonPath("$.total").value(0));
    }
}
