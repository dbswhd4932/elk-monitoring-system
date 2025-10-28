import static net.grinder.script.Grinder.grinder
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import net.grinder.script.GTest
import net.grinder.script.Grinder
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

import org.ngrinder.http.HTTPRequest
import org.ngrinder.http.HTTPRequestControl
import org.ngrinder.http.HTTPResponse
import org.ngrinder.http.cookie.Cookie
import org.ngrinder.http.cookie.CookieManager

/**
 * Week 3-4: Redis 캐시 적용 API 테스트
 * 목표: TPS 2000+, 평균 응답시간 50ms 이하
 */
@RunWith(GrinderRunner)
class ProductWithCacheTest {

    public static GTest test
    public static HTTPRequest request
    public static Map<String, String> headers = [:]

    @BeforeProcess
    public static void beforeProcess() {
        HTTPRequestControl.setConnectionTimeout(300000)
        test = new GTest(1, "Product API - WITH Redis Cache")
        request = new HTTPRequest()
        grinder.logger.info("=== Redis Cache Performance Test ===")
    }

    @BeforeThread
    public void beforeThread() {
        test.record(this, "testProductWithCache")
        grinder.statistics.delayReports = true
        grinder.logger.info("=== Test Thread Started ===")
    }

    @Before
    public void before() {
        request.setHeaders(headers)
    }

    @Test
    public void testProductWithCache() {
        // 1~100 사이 랜덤 상품 ID 조회
        def productId = (Math.random() * 100 + 1).toInteger()

        HTTPResponse response = request.GET(
            "http://host.docker.internal:8080/api/v3/products/cache/" + productId
        )

        if (response.statusCode == 301 || response.statusCode == 302) {
            grinder.logger.warn("Warning: Redirect response code {}.", response.statusCode)
        } else {
            assertThat(response.statusCode, is(200))
        }
    }
}
