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
 * Week 3-4: Redis 캐시 성능 비교 테스트
 *
 * 테스트 시나리오:
 * - 캐시 적용 API와 캐시 미적용 API를 동시에 테스트
 * - 성능 차이 측정 (TPS, 응답시간)
 */
@RunWith(GrinderRunner)
class ProductCacheCompareTest {

    public static GTest testWithCache
    public static GTest testWithoutCache
    public static HTTPRequest request
    public static Map<String, String> headers = [:]

    @BeforeProcess
    public static void beforeProcess() {
        HTTPRequestControl.setConnectionTimeout(300000)

        // 테스트 1: 캐시 적용 API
        testWithCache = new GTest(1, "Product API - WITH Cache")

        // 테스트 2: 캐시 미적용 API
        testWithoutCache = new GTest(2, "Product API - WITHOUT Cache")

        request = new HTTPRequest()
        grinder.logger.info("=== Cache Performance Comparison Test Started ===")
    }

    @BeforeThread
    public void beforeThread() {
        testWithCache.record(this, "testProductWithCache")
        testWithoutCache.record(this, "testProductWithoutCache")
        grinder.statistics.delayReports = true
        grinder.logger.info("=== Test Thread Started ===")
    }

    @Before
    public void before() {
        request.setHeaders(headers)
    }

    /**
     * 테스트 1: Redis 캐시 적용 API
     * 기대 성능: TPS 2000+, 평균 응답시간 50ms 이하
     */
    @Test
    public void testProductWithCache() {
        def productId = (Math.random() * 100 + 1).toInteger()

        HTTPResponse response = request.GET(
            "http://host.docker.internal:8080/api/v3/products/cache/" + productId
        )

        assertThat(response.statusCode, is(200))

        // 응답 헤더 확인
        def cacheType = response.getHeader("X-Cache-Type")
        grinder.logger.debug("Cache Type: {}, Product ID: {}", cacheType, productId)
    }

    /**
     * 테스트 2: 캐시 미적용 API (DB 직접 조회)
     * 기대 성능: TPS 500, 평균 응답시간 200ms
     */
    @Test
    public void testProductWithoutCache() {
        def productId = (Math.random() * 100 + 1).toInteger()

        HTTPResponse response = request.GET(
            "http://host.docker.internal:8080/api/v3/products/no-cache/" + productId
        )

        assertThat(response.statusCode, is(200))

        // 응답 헤더 확인
        def cacheType = response.getHeader("X-Cache-Type")
        grinder.logger.debug("Cache Type: {}, Product ID: {}", cacheType, productId)
    }
}
