package be.idevelop.commons.web

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
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

    void init(FilterConfig filterConfig) throws ServletException {
        initMillisBetweenCalls(filterConfig)
        initMaxTooFastRequests(filterConfig)

        CACHE.init(millisBetweenCalls)
    }

    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            if (JAIL.isPunished(request)) {
                showTheClientSoManners(response)
                return
            }
            if (CACHE.contains(request)) {
                if (shouldBePunished(request)) {
                    JAIL.punish(request)
                    showTheClientSoManners(response)
                    return
                }
            }
            CACHE.put(request)
            chain.doFilter(request, response)
        }
    }

    private showTheClientSoManners(HttpServletResponse response) {
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

}