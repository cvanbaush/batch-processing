CREATE TABLE IF NOT EXISTS news_summary (
    id INTEGER IDENTITY PRIMARY KEY,
    keyword VARCHAR(255) NOT NULL,
    summary CLOB NOT NULL,
    processed_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS job_log (
    id INTEGER IDENTITY PRIMARY KEY,
    job_execution_id BIGINT NOT NULL,
    keyword VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    articles_count INTEGER DEFAULT 0,
    start_time TIMESTAMP,
    end_time TIMESTAMP
);
