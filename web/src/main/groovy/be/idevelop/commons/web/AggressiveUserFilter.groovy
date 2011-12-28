package be.idevelop.commons.web

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.util.AntPathMatcher
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

    static final String URL_PATTERN = "URL_PATTERN"

    private static final String DEFAULT_URL_PATTERN = '/*'

    private static final int DEFAULT_MILLIS_BETWEEN_CALLS = 1000

    private int millisBetweenCalls

    private String urlPattern

    private static final AntPathMatcher matcher = new AntPathMatcher()

    void init(FilterConfig filterConfig) throws ServletException {
        initMillisBetweenCalls(filterConfig)
        initUrlPattern(filterConfig)

        CACHE.init(millisBetweenCalls)
    }

    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            if (matchesUrlPattern(request.requestURI, request.getContextPath())) {
                if (CACHE.contains(request)) {
                    response.sendError(429, 'Too many requests')
                    response.flushBuffer()
                } else {
                    CACHE.put(request)
                    chain.doFilter(request, response)
                }
            }
        }
    }

    void destroy() {
        CACHE.clear()
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

    def initUrlPattern(FilterConfig filterConfig) {
        String pattern = filterConfig.getInitParameter(URL_PATTERN)
        pattern = pattern ?: DEFAULT_URL_PATTERN
        if (matcher.isPattern(pattern)) {
            this.urlPattern = pattern
        } else {
            throw new IllegalArgumentException("Invalid ANT path pattern")
        }
    }

    boolean matchesUrlPattern(String requestUri, String contextRoot) {
        def path = requestUri
        if (path && !path.empty && contextRoot && !contextRoot.empty) {
            path = path.substring(contextRoot.length())
        }
        path = !path.startsWith('/') ? "/${path}" : path
        matcher.match(urlPattern, path)
    }

}