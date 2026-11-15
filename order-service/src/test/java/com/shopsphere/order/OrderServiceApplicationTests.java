package com.shopsphere.order;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires live PostgreSQL connection to Neon Cloud database; unit and direct test suites cover full service functionality.")
class OrderServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
