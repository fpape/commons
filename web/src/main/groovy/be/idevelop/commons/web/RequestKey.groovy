package be.idevelop.commons.web

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

/**
 * Key for the {@link TimedRequestCache}.
 *
 * @author steven
 */
class RequestKey implements Comparable<RequestKey> {

    static final String KEY = 'RequestKey'

    def internalValue

    RequestKey(HttpServletRequest request) {
        internalValue = request.getAttribute(KEY)
        if (!internalValue) {
            internalValue = "${request.remoteAddr}#${getUserAgent(request)}#${getSessionId(request)}"
            request.setAttribute(KEY, internalValue)
        }
    }

    def getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader('User-Agent');
        return userAgent ?: ''
    }

    def getSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session ? session.id : ''
    }

    @Override
    int compareTo(RequestKey t) {
        assert (t != null)

        return internalValue.compareTo(t.internalValue)
    }

    boolean equals(o) {
        if (this.is(o)) {
            return true
        } else if (getClass() != o.class) {
            return false
        } else {
            return internalValue == ((RequestKey) o).internalValue
        }
    }

    int hashCode() {
        return (internalValue != null ? internalValue.hashCode() : 0)
    }


    @Override
    public String toString() {
        return "RequestKey{${internalValue}}"
    }


}
