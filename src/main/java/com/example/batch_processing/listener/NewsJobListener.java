package com.example.batch_processing.listener;

import java.sql.Timestamp;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class NewsJobListener implements JobExecutionListener, StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(NewsJobListener.class);

    private final JdbcTemplate jdbcTemplate;

    public NewsJobListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String keyword = jobExecution.getJobParameters().getString("keyword");
        Long jobExecutionId = jobExecution.getId();

        jdbcTemplate.update(
            "INSERT INTO job_log (job_execution_id, keyword, status, start_time) VALUES (?, ?, ?, ?)",
            jobExecutionId, keyword, "STARTED", Timestamp.from(Instant.now())
        );

        log.info("Job started for keyword '{}'", keyword);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        String keyword = jobExecution.getJobParameters().getString("keyword");
        Long jobExecutionId = jobExecution.getId();
        String status = jobExecution.getStatus().toString();

        jdbcTemplate.update(
            "UPDATE job_log SET status = ?, end_time = ? WHERE job_execution_id = ?",
            status, Timestamp.from(Instant.now()), jobExecutionId
        );

        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("Job '{}' completed successfully for keyword '{}'", jobName, keyword);
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            log.error("Job '{}' failed for keyword '{}': {}", jobName, keyword, jobExecution.getAllFailureExceptions());
        } else if (jobExecution.getStatus() == BatchStatus.STOPPED) {
            log.warn("Job '{}' was stopped before completion for keyword '{}'", jobName, keyword);
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String jobName = stepExecution.getJobExecution().getJobInstance().getJobName();
        String keyword = stepExecution.getJobExecution().getJobParameters().getString("keyword");
        Long jobExecutionId = stepExecution.getJobExecution().getId();
        String stepName = stepExecution.getStepName();
        long writeCount = stepExecution.getWriteCount();

        String status = stepName + ":" + stepExecution.getStatus().toString();

        if ("fetchNewsStep".equals(stepName)) {
            jdbcTemplate.update(
                "UPDATE job_log SET status = ?, articles_count = ? WHERE job_execution_id = ?",
                status, writeCount, jobExecutionId
            );
        } else {
            jdbcTemplate.update(
                "UPDATE job_log SET status = ? WHERE job_execution_id = ?",
                status, jobExecutionId
            );
        }

        log.info("Job '{}' step '{}' completed for keyword '{}' with status: {}, items written: {}",
            jobName, stepName, keyword, stepExecution.getStatus(), writeCount);

        return stepExecution.getExitStatus();
    }
}
