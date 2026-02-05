package com.example.batch_processing.model;

import java.time.LocalDate;

public record NewsSummary(
    String keyword,
    String summary,
    LocalDate processedDate
) {}
