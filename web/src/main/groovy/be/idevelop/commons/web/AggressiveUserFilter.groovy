package be.idevelop.commons.web

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.util.AntPathMatcher
import static be.idevelop.commons.web.PunishedCache.JAIL
import static be.idevelop.commons.web.RequestCounter.COUNTER
import static be.idevelop.commons.web.TimedRequestCache.CACHE
import javax.servlet.*

/**
 * Servlet filter that blocks users that are requesting too many pages per minute.
 * I.e. requesting a page without respecting a certain time between requests.
 *
 * @author steven
 */
class AggressiveUserFilter implements Filter {

    static final String MILLIS_BETWEEN_CALLS = "MILLIS_BETWEEN_CALLS"

    private static final int DEFAULT_MILLIS_BETWEEN_CALLS = 2000

    private int millisBetweenCalls

    static final String MAX_TOO_FAST_REQUESTS = "MAX_TOO_FAST_REQUESTS"

    private static final int DEFAULT_MAX_TOO_FAST_REQUESTS = 10

    private int maxTooFastRequests

    static final String URL_PATTERN = "URL_PATTERN"

    private static final String DEFAULT_URL_PATTERN = '/*'

    private String urlPattern

    static final String EXCLUDE_URL_PATTERN = "EXCLUDE_URL_PATTERN"

    private static final String DEFAULT_EXCLUDE_URL_PATTERN = ''

    private String excludeUrlPattern

    private AntPathMatcher antPathMatcher = new AntPathMatcher()

    void init(FilterConfig filterConfig) throws ServletException {
        initMillisBetweenCalls(filterConfig)
        initMaxTooFastRequests(filterConfig)
        initUrlPattern(filterConfig)

        CACHE.init(millisBetweenCalls)
    }

    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            if (shouldFilter(request.servletPath)) {
                if (JAIL.isPunished(request)) {
                    showTheClientSomeManners(response)
                    return
                }
                if (CACHE.contains(request)) {
                    if (shouldBePunished(request)) {
                        JAIL.punish(request)
                        showTheClientSomeManners(response)
                        return
                    }
                }
                CACHE.put(request)
            }
            chain.doFilter(request, response)
        }
    }

    boolean shouldFilter(String path) {
        return isIncluded(path) && !isExcluded(path);
    }

    private boolean isIncluded(String path) {
        return match(urlPattern, path)
    }

    private boolean isExcluded(String path) {
        return match(excludeUrlPattern, path)
    }

    private boolean match(String pattern, String path) {
        def splitPattern = pattern.split(',').collect {it -> it.trim()}
        return match(splitPattern, path)
    }

    private boolean match(List<String> patterns, String path) {
        for (String pattern: patterns) {
            if (antPathMatcher.match(pattern, path)) {
                return true
            }
        }
        return false
    }

    private showTheClientSomeManners(HttpServletResponse response) {
        response.sendError(429, 'Too many requests')
        response.flushBuffer()
    }

    boolean shouldBePunished(HttpServletRequest request) {
        if (COUNTER.increment(request) == this.maxTooFastRequests) {
            COUNTER.reset(request)
            return true
        }
        return false
    }

    void destroy() {
        CACHE.clear()
        COUNTER.clear()
        JAIL.clear()
    }

    private void initMillisBetweenCalls(FilterConfig filterConfig) {
        if (filterConfig.getInitParameter(MILLIS_BETWEEN_CALLS) != null) {
            try {
                this.millisBetweenCalls = new Integer(filterConfig.getInitParameter(MILLIS_BETWEEN_CALLS))
                return;
            } catch (NumberFormatException ignored) {
            }
        }
        this.millisBetweenCalls = DEFAULT_MILLIS_BETWEEN_CALLS;
    }

    private void initMaxTooFastRequests(FilterConfig filterConfig) {
        if (filterConfig.getInitParameter(MAX_TOO_FAST_REQUESTS) != null) {
            try {
                this.maxTooFastRequests = new Integer(filterConfig.getInitParameter(MAX_TOO_FAST_REQUESTS))
                return;
            } catch (NumberFormatException ignored) {
            }
        }
        this.maxTooFastRequests = DEFAULT_MAX_TOO_FAST_REQUESTS
    }

    private void initUrlPattern(FilterConfig filterConfig) {
        if (filterConfig.getInitParameter(URL_PATTERN) != null) {
            this.urlPattern = filterConfig.getInitParameter(URL_PATTERN)
        } else {
            this.urlPattern = DEFAULT_URL_PATTERN
        }
        if (filterConfig.getInitParameter(EXCLUDE_URL_PATTERN) != null) {
            this.excludeUrlPattern = filterConfig.getInitParameter(EXCLUDE_URL_PATTERN)
        } else {
            this.excludeUrlPattern = DEFAULT_EXCLUDE_URL_PATTERN
        }
    }
}