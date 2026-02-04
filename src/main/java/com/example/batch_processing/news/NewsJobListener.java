package com.example.batch_processing.news;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class NewsJobListener implements JobExecutionListener, StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(NewsJobListener.class);

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        String keyword = jobExecution.getJobParameters().getString("keyword");
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
        log.info("Job '{}' step '{}' completed for keyword '{}' with status: {}, items written: {}",
            jobName,
            stepExecution.getStepName(),
            keyword,
            stepExecution.getStatus(),
            stepExecution.getWriteCount());
        return stepExecution.getExitStatus();
    }
}
