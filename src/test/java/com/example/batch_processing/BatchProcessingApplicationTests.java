package com.example.batch_processing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
	"newsapi.base-url=https://newsapi.org",
	"newsapi.api-key=test-key",
	"newsapi.page-size=10",
	"openai.api-key=test-key",
	"spring.batch.job.enabled=false",
	"spring.datasource.url=jdbc:hsqldb:mem:testdb"
})
class BatchProcessingApplicationTests {

	@Test
	void contextLoads() {
	}

}
