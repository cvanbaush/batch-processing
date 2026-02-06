package com.example.batch_processing.controller;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobs/logs")
public class JobLogController {

    private final JdbcTemplate jdbcTemplate;

    public JobLogController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<JobLogEntry> getJobLogs(@RequestParam(required = false) String keyword) {
        if (keyword != null) {
            return jdbcTemplate.query(
                "SELECT job_execution_id, keyword, status, articles_count, start_time, end_time FROM job_log WHERE keyword = ? ORDER BY start_time DESC",
                (rs, rowNum) -> new JobLogEntry(
                    rs.getLong("job_execution_id"),
                    rs.getString("keyword"),
                    rs.getString("status"),
                    rs.getInt("articles_count"),
                    rs.getTimestamp("start_time"),
                    rs.getTimestamp("end_time")
                ),
                keyword
            );
        }
        return jdbcTemplate.query(
            "SELECT job_execution_id, keyword, status, articles_count, start_time, end_time FROM job_log ORDER BY start_time DESC",
            (rs, rowNum) -> new JobLogEntry(
                rs.getLong("job_execution_id"),
                rs.getString("keyword"),
                rs.getString("status"),
                rs.getInt("articles_count"),
                rs.getTimestamp("start_time"),
                rs.getTimestamp("end_time")
            )
        );
    }

    public record JobLogEntry(
        Long jobExecutionId,
        String keyword,
        String status,
        int articlesCount,
        Timestamp startTime,
        Timestamp endTime
    ) {}
}
