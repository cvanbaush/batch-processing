# News Batch Processing Service

A Spring Batch application that fetches news articles from NewsAPI, saves them as JSON Lines files, and generates AI-powered summaries using OpenAI.

## Features

- Fetch news articles by keyword from NewsAPI
- Save articles to organized JSON Lines files (`data/articles/{keyword}/{keyword}-{date}.jsonl`)
- Generate AI summaries using OpenAI GPT-4o-mini
- Store summaries in HSQLDB database
- Track job execution history with detailed logging

## Prerequisites

- Java 17+
- Maven 3.8+
- NewsAPI key (https://newsapi.org)
- OpenAI API key (https://platform.openai.com)

## Configuration

Set the following environment variables:

```bash
export NEWSAPI_API_KEY=your_newsapi_key
export OPENAI_API_KEY=your_openai_key
```

Or create a `.env` file in the project root (for VS Code integration).

## Running the Application

```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`.

## API Endpoints

### Trigger News Job

Fetches articles for a keyword, saves them to file, and generates an AI summary.

```
POST /jobs/news?keyword={keyword}
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| keyword | string | Yes | Search term for news articles |

**Response:**
```json
{
  "jobId": 1,
  "status": "COMPLETED",
  "keyword": "nvidia",
  "error": null
}
```

**Example:**
```bash
curl -X POST "http://localhost:8080/jobs/news?keyword=nvidia"
```

---

### Get Summaries

Retrieves AI-generated news summaries.

```
GET /summaries
GET /summaries?keyword={keyword}
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| keyword | string | No | Filter summaries by keyword |

**Response:**
```json
[
  {
    "keyword": "nvidia",
    "summary": "Recent reports indicate that Nvidia's RTX 50 Super graphics card has been delayed...",
    "processedDate": "2026-02-06"
  }
]
```

**Example:**
```bash
curl "http://localhost:8080/summaries?keyword=nvidia"
```

---

### Get Job Logs

Retrieves job execution history.

```
GET /jobs/logs
GET /jobs/logs?keyword={keyword}
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| keyword | string | No | Filter logs by keyword |

**Response:**
```json
[
  {
    "jobExecutionId": 1,
    "keyword": "nvidia",
    "status": "COMPLETED",
    "articlesCount": 5,
    "startTime": "2026-02-06T19:32:52.585Z",
    "endTime": "2026-02-06T19:32:55.063Z"
  }
]
```

**Example:**
```bash
curl "http://localhost:8080/jobs/logs"
```

## Data Storage

- **Articles**: `data/articles/{keyword}/{keyword}-{date}.jsonl`
- **Database**: `data/newsdb` (HSQLDB file-based database)

## Running Tests

```bash
./mvnw test
```
