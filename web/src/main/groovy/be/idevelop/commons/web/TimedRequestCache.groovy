package be.idevelop.commons.web

import be.idevelop.commons.collections.BinarySearchTree
import be.idevelop.commons.collections.TimedCollectionWrapper
import javax.servlet.http.HttpServletRequest

/**
 * Cache for keeping requests for a certain time.
 *
 * @author steven
 */
enum TimedRequestCache {

    CACHE

    private TimedCollectionWrapper timedCache
    private synchronized initialized = false;

    TimedRequestCache() {
        timedCache = new TimedCollectionWrapper(new BinarySearchTree())
    }

    def init(millisToRemainInCache) {
        initialized = true
        timedCache.millisToRemainInCollection = millisToRemainInCache
    }

    def contains(HttpServletRequest request) {
        checkInitialized()

        return timedCache.contains(new TimedRequestCacheKey(request))
    }

    def put(HttpServletRequest request) {
        checkInitialized()

        def key = new TimedRequestCacheKey(request)
        timedCache.add(key)
    }

    def checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException('Should be initialized first')
        }
    }

    void clear() {
        timedCache.clear()
    }
}

