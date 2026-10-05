package com.internal.tasktracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    // Cap page size so a huge pageSize cannot be used to exhaust memory.
    private static final int MAX_PAGE_SIZE = 100;

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("/api/tasks")
    public ResponseEntity<?> searchTasks(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        // Normalize query input. Locale.ROOT avoids locale-dependent casing
        // (e.g. Turkish dotted/dotless I) breaking the LIKE comparison.
        String query = q == null ? "" : q.trim();

        // Escape LIKE special characters so input is matched literally —
        // otherwise '%' or '_' act as wildcards (q=% returned every task).
        String escaped = query.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        String searchTerm = "%" + escaped + "%";

        // Parse status filter. An unknown value is a client error (400),
        // not an unhandled IllegalArgumentException surfacing as a 500.
        String normalizedStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                normalizedStatus = TaskStatus.valueOf(status.toUpperCase(Locale.ROOT)).name();
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "INVALID_STATUS",
                        "message", "Unknown status: '" + status
                                + "'. Valid values: OPEN, IN_PROGRESS, DONE"));
            }
        }

        // Clamp pagination into sane bounds. Previously page=0 or a negative
        // pageSize reached List.subList() and threw IndexOutOfBoundsException.
        int safePage = Math.max(1, page);
        int safePageSize = Math.min(Math.max(1, pageSize), MAX_PAGE_SIZE);

        log.debug("searchTasks q=\"{}\" status={} page={} pageSize={}",
                query, normalizedStatus, safePage, safePageSize);

        // Paginate in the database; only one page of rows leaves the DB.
        List<Task> pageResults = taskRepository.searchTasks(
                searchTerm, normalizedStatus, safePageSize, (safePage - 1) * safePageSize);
        long total = taskRepository.countTasks(searchTerm, normalizedStatus);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", pageResults);
        response.put("total", total);
        response.put("page", safePage);
        response.put("pageSize", safePageSize);

        return ResponseEntity.ok(response);
    }
}
