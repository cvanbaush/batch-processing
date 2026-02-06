package com.example.batch_processing.controller;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobs/news")
public class NewsJobController {

    private final JobOperator jobOperator;
    private final Job fetchAndSummarizeNewsJob;

    public NewsJobController(JobOperator jobOperator, Job fetchAndSummarizeNewsJob) {
        this.jobOperator = jobOperator;
        this.fetchAndSummarizeNewsJob = fetchAndSummarizeNewsJob;
    }

    @PostMapping
    public ResponseEntity<JobResponse> runJob(@RequestParam String keyword) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addString("keyword", keyword, true)
                .addLong("timestamp", System.currentTimeMillis(), true)
                .toJobParameters();

            JobExecution jobExecution = jobOperator.run(fetchAndSummarizeNewsJob, jobParameters);

            return ResponseEntity.ok(new JobResponse(
                jobExecution.getId(),
                jobExecution.getStatus().toString(),
                keyword
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new JobResponse(null, "FAILED", keyword, e.getMessage()));
        }
    }

    public record JobResponse(Long jobId, String status, String keyword, String error) {
        public JobResponse(Long jobId, String status, String keyword) {
            this(jobId, status, keyword, null);
        }
    }
}
