package com.example.batch_processing.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.batch_processing.model.NewsSummary;

@RestController
@RequestMapping("/summaries")
public class SummaryController {

    private final JdbcTemplate jdbcTemplate;

    public SummaryController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<NewsSummary> getSummaries(@RequestParam(required = false) String keyword) {
        if (keyword != null) {
            return jdbcTemplate.query(
                "SELECT keyword, summary, processed_date FROM news_summary WHERE keyword = ? ORDER BY processed_date DESC",
                (rs, rowNum) -> new NewsSummary(
                    rs.getString("keyword"),
                    rs.getString("summary"),
                    rs.getObject("processed_date", LocalDate.class)
                ),
                keyword
            );
        }
        return jdbcTemplate.query(
            "SELECT keyword, summary, processed_date FROM news_summary ORDER BY processed_date DESC",
            (rs, rowNum) -> new NewsSummary(
                rs.getString("keyword"),
                rs.getString("summary"),
                rs.getObject("processed_date", LocalDate.class)
            )
        );
    }
}
