package be.idevelop.commons.web

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

/**
 * Key for the {@link TimedRequestCache}.
 *
 * @author steven
 */
class TimedRequestCacheKey implements Comparable<TimedRequestCacheKey> {

    def internalValue

    TimedRequestCacheKey(HttpServletRequest request) {
        internalValue = "${request.remoteAddr}#${getUserAgent(request)}#${getSessionId(request)}"
    }

    def getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent ?: ''
    }

    def getSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session ? session.id : ''
    }

    @Override
    int compareTo(TimedRequestCacheKey t) {
        assert (t != null)

        return internalValue.compareTo(t.internalValue)
    }

    @Override
    public String toString() {
        return "TimedRequestCacheKey{${internalValue}}"
    }


}
