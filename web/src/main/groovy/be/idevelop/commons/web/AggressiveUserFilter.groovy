package be.idevelop.commons.web

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import static be.idevelop.commons.web.TimedRequestCache.CACHE
import javax.servlet.*

/**
 * Servlet filter that blocks users that are requesting too many pages per minute.
 * I.e. requesting a page without respecting a certain time between requests.
 *
 * @author steven
 */
class AggressiveUserFilter implements Filter {

    public static final String MILLIS_BETWEEN_CALLS = "MILLIS_BETWEEN_CALLS"

    private static final int DEFAULT_MILLIS_BETWEEN_CALLS = 1000;

    private int millisBetweenCalls;

    void init(FilterConfig filterConfig) throws ServletException {
        initMillisBetweenCalls(filterConfig)

        CACHE.init(millisBetweenCalls)
    }

    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            if (CACHE.contains(request)) {
                response.sendError(429, 'Too many requests')
                response.flushBuffer()
            } else {
                CACHE.put(request)
                chain.doFilter(request, response)
            }
        }
    }

    void destroy() {
        CACHE.clear()
    }

    private void initMillisBetweenCalls(FilterConfig filterConfig) {
        if (filterConfig.getInitParameter(MILLIS_BETWEEN_CALLS) != null) {
            try {
                this.millisBetweenCalls = new Integer(filterConfig.getInitParameter(MILLIS_BETWEEN_CALLS));
                return;
            } catch (NumberFormatException ignored) {
            }
        }
        this.millisBetweenCalls = DEFAULT_MILLIS_BETWEEN_CALLS;
    }

}