package com.shopsmart;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: boots the whole Spring context against an in-memory H2 database
 * (the "test" profile). If any bean is mis-wired - a missing dependency, a bad
 * repository query method name, a security mis-config - this test fails to load
 * and tells us immediately. Run it with:  mvn test  (or in NetBeans, Test File).
 */
@SpringBootTest
@ActiveProfiles("test")
class ShopSmartApplicationTests {

    @Test
    void contextLoads() {
        // Intentionally empty: success means the application context started.
    }
}
