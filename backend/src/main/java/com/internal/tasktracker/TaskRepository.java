package com.internal.tasktracker;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // NOTE on the parentheses: AND binds tighter than OR in SQL, so
    //   archived = FALSE AND title LIKE :term OR description LIKE :term AND (...)
    // used to parse as
    //   (archived AND title-match) OR (description-match AND status-match)
    // which leaked archived tasks via the description branch and ignored the
    // status filter whenever the title matched. Grouping the OR makes
    // archived and status apply to the whole predicate.
    // Note the ESCAPE clause: the controller escapes %, _ and \ in user
    // input so LIKE treats them literally (see TaskController).
    String SEARCH_FILTER = "FROM tasks WHERE archived = FALSE "
            + "AND (LOWER(title) LIKE :term ESCAPE '\\' OR LOWER(description) LIKE :term ESCAPE '\\') "
            + "AND (:status IS NULL OR status = :status)";

    // Paginate in the database (LIMIT/OFFSET) instead of fetching every
    // matching row into memory and slicing it with subList().
    @Query(value = "SELECT * " + SEARCH_FILTER
                 + " ORDER BY created_at DESC LIMIT :limit OFFSET :offset",
           nativeQuery = true)
    List<Task> searchTasks(@Param("term") String term,
                           @Param("status") String status,
                           @Param("limit") int limit,
                           @Param("offset") int offset);

    @Query(value = "SELECT COUNT(*) " + SEARCH_FILTER, nativeQuery = true)
    long countTasks(@Param("term") String term, @Param("status") String status);
}
