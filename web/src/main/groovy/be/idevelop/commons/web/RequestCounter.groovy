package be.idevelop.commons.web

import javax.servlet.http.HttpServletRequest

/**
 * Counter class that counts how many times a client does a server request.
 *
 * @author steven
 */
enum RequestCounter {

    COUNTER

    private Map<RequestKey, Integer> counterMap

    private RequestCounter() {
        this.counterMap = new HashMap<RequestKey, Integer>()
    }

    def increment(HttpServletRequest request) {
        def key = new RequestKey(request)
        def value = this.counterMap[key]
        if (!value) {
            this.counterMap[key] = 0
        }
        return ++this.counterMap[key]
    }

    def reset(HttpServletRequest request) {
        def key = new RequestKey(request)
        this.counterMap[key] = 0
        return this.counterMap[key]
    }

    void clear() {
        counterMap.clear()
    }
}
