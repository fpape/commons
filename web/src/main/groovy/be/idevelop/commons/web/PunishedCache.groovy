package be.idevelop.commons.web

import be.idevelop.commons.collections.BinarySearchTree
import be.idevelop.commons.collections.TimedCollectionWrapper
import javax.servlet.http.HttpServletRequest

/**
 * This is the jail for clients that have been requesting too much on a short time.
 * They'll stay here for a configurable amount of time, by default 30 seconds.
 *
 * @author steven
 */
enum PunishedCache {

    JAIL

    private Integer defaultPunishedTimeInMillis = 30000

    private TimedCollectionWrapper<RequestKey> timedCache

    private PunishedCache() {
        timedCache = new TimedCollectionWrapper<RequestKey>(new BinarySearchTree<RequestKey>())
        timedCache.millisToRemainInCollection = defaultPunishedTimeInMillis
    }

    boolean isPunished(HttpServletRequest request) {
        return timedCache.contains(new RequestKey(request))
    }

    def punish(HttpServletRequest request) {
        timedCache.add(new RequestKey(request))
    }

    def setPunishedTimeInMillis(Integer timeToLive) {
        timedCache.millisToRemainInCollection = timeToLive
    }

    def clear() {
        timedCache.clear()
    }
}
