package com.example.batch_processing.news;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobs/news")
public class NewsJobController {

    private final JobLauncher jobLauncher;
    private final Job fetchNewsJob;

    public NewsJobController(JobLauncher jobLauncher, Job fetchNewsJob) {
        this.jobLauncher = jobLauncher;
        this.fetchNewsJob = fetchNewsJob;
    }

    @PostMapping
    public ResponseEntity<JobResponse> runJob(@RequestParam String keyword) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addString("keyword", keyword)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(fetchNewsJob, jobParameters);

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
