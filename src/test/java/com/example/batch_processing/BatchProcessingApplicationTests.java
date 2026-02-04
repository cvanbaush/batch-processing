package com.example.batch_processing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
	"newsapi.baseUrl=https://newsapi.org",
	"newsapi.apiKey=test-key",
	"newsapi.pageSize=10",
	"spring.batch.job.enabled=false"
})
class BatchProcessingApplicationTests {

	@Test
	void contextLoads() {
	}

}
