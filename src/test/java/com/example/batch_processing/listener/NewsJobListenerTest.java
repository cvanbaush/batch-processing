package com.example.batch_processing.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NewsJobListenerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private NewsJobListener listener;

    @BeforeEach
    void setUp() {
        listener = new NewsJobListener(jdbcTemplate);
    }

    private JobExecution createJobExecution(String keyword) {
        JobParameters params = new JobParametersBuilder()
            .addString("keyword", keyword)
            .toJobParameters();
        JobInstance jobInstance = new JobInstance(1L, "fetchAndSummarizeNewsJob");
        return new JobExecution(1L, jobInstance, params);
    }

    @Test
    void beforeJob_insertsJobLogEntry() {
        JobExecution jobExecution = createJobExecution("nvidia");

        listener.beforeJob(jobExecution);

        verify(jdbcTemplate).update(
            eq("INSERT INTO job_log (job_execution_id, keyword, status, start_time) VALUES (?, ?, ?, ?)"),
            eq(1L),
            eq("nvidia"),
            eq("STARTED"),
            any(Timestamp.class)
        );
    }

    @Test
    void afterJob_updatesJobLogWithCompletedStatus() {
        JobExecution jobExecution = createJobExecution("nvidia");
        jobExecution.setStatus(BatchStatus.COMPLETED);

        listener.afterJob(jobExecution);

        verify(jdbcTemplate).update(
            eq("UPDATE job_log SET status = ?, end_time = ? WHERE job_execution_id = ?"),
            eq("COMPLETED"),
            any(Timestamp.class),
            eq(1L)
        );
    }

    @Test
    void afterJob_updatesJobLogWithFailedStatus() {
        JobExecution jobExecution = createJobExecution("nvidia");
        jobExecution.setStatus(BatchStatus.FAILED);

        listener.afterJob(jobExecution);

        verify(jdbcTemplate).update(
            eq("UPDATE job_log SET status = ?, end_time = ? WHERE job_execution_id = ?"),
            eq("FAILED"),
            any(Timestamp.class),
            eq(1L)
        );
    }

    @Test
    void afterStep_updatesFetchNewsStepWithArticlesCount() {
        JobExecution jobExecution = createJobExecution("nvidia");
        StepExecution stepExecution = new StepExecution("fetchNewsStep", jobExecution);
        stepExecution.setWriteCount(5);
        stepExecution.setStatus(BatchStatus.COMPLETED);
        stepExecution.setExitStatus(ExitStatus.COMPLETED);

        ExitStatus result = listener.afterStep(stepExecution);

        verify(jdbcTemplate).update(
            eq("UPDATE job_log SET status = ?, articles_count = ? WHERE job_execution_id = ?"),
            eq("fetchNewsStep:COMPLETED"),
            eq(5L),
            eq(1L)
        );
        assertThat(result).isEqualTo(ExitStatus.COMPLETED);
    }

    @Test
    void afterStep_updatesOtherStepsWithoutArticlesCount() {
        JobExecution jobExecution = createJobExecution("nvidia");
        StepExecution stepExecution = new StepExecution("summarizeNewsStep", jobExecution);
        stepExecution.setStatus(BatchStatus.COMPLETED);

        listener.afterStep(stepExecution);

        verify(jdbcTemplate).update(
            eq("UPDATE job_log SET status = ? WHERE job_execution_id = ?"),
            eq("summarizeNewsStep:COMPLETED"),
            eq(1L)
        );
    }
}
