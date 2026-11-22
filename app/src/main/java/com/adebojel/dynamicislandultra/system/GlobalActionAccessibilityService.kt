package com.adebojel.dynamicislandultra.system

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * Executes only explicit global actions requested from this app's control screen.
 * It deliberately does not inspect window content, nodes, text, gestures or screenshots.
 */
class GlobalActionAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
        GlobalActionController.attach(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No event content is read or retained.
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        GlobalActionController.detach(this)
        super.onDestroy()
    }
}

object GlobalActionController {
    @Volatile
    private var connectedService: GlobalActionAccessibilityService? = null

    internal fun attach(service: GlobalActionAccessibilityService) {
        connectedService = service
    }

    internal fun detach(service: GlobalActionAccessibilityService) {
        if (connectedService === service) connectedService = null
    }

    fun perform(action: Int): Boolean = connectedService?.performGlobalAction(action) == true
    fun isConnected(): Boolean = connectedService != null
}
