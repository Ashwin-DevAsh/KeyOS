package tech.DevAsh.keyOS.Helpers.KioskHelpers

import android.app.usage.UsageEvents
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Browser
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import tech.DevAsh.KeyOS.Services.WindowChangeDetectingService
import java.util.ArrayList
import java.util.HashMap

object WebBlocker {

    var context:Context?=null

    fun block(event:AccessibilityEvent,_context: Context){
        context=_context
        val parentNodeInfo = event.source ?: return
        val packageName = event.packageName.toString()
        var browserConfig: SupportedBrowserConfig? = null
        for (supportedConfig in getSupportedBrowsers()) {
            if (supportedConfig.packageName == packageName) {
                browserConfig = supportedConfig
            }
        }
        if (browserConfig == null) {
            return
        }
        val capturedUrl = captureUrl(parentNodeInfo, browserConfig)
        parentNodeInfo.recycle()

        //we can't find a url. Browser either was updated or opened page without url text field
        if (capturedUrl == null) {
            return
        }

        val eventTime = event.eventTime
        val detectionId = "$packageName, and url $capturedUrl"
        val lastRecordedTime = if (previousUrlDetections.containsKey(
                        detectionId)) previousUrlDetections[detectionId]!! else 0.toLong()
        //some kind of redirect throttling
        if (eventTime - lastRecordedTime > 2000) {
            previousUrlDetections[detectionId] = eventTime
            analyzeCapturedUrl(capturedUrl, browserConfig.packageName)
        }
    }

    private fun analyzeCapturedUrl(capturedUrl: String, browserPackage: String) {
        val redirectUrl = "https://www.google.com"
        if (capturedUrl.contains("facebook.com")) {
            performRedirect(redirectUrl, browserPackage)
        }
    }

    private fun performRedirect(redirectUrl: String, browserPackage: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl))
            intent.setPackage(browserPackage)
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, browserPackage)
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
           context?. startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl))
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(i)
        }
    }

    private fun packageNames(): Array<String> {
        val packageNames: MutableList<String> = ArrayList()
        for (config in getSupportedBrowsers()) {
            packageNames.add(config.packageName)
        }
        return packageNames.toTypedArray()
    }

    private class SupportedBrowserConfig(var packageName: String, var addressBarId: String)

    /** @return a list of supported browser configs
     * This list could be instead obtained from remote server to support future browser updates without updating an app
     */
    private fun getSupportedBrowsers(): List<SupportedBrowserConfig> {
        val browsers: MutableList<SupportedBrowserConfig> = ArrayList()
        browsers.add(SupportedBrowserConfig("com.android.chrome", "com.android.chrome:id/url_bar"))
        browsers.add(SupportedBrowserConfig("org.mozilla.firefox", "org.mozilla.firefox:id/url_bar_title"))
        browsers.add(SupportedBrowserConfig("com.brave.browser", "com.brave.browser:id/url_bar"))
        browsers.add(SupportedBrowserConfig("com.opera.browser", "com.opera.browser:id/url_field"))
        browsers.add(SupportedBrowserConfig("com.duckduckgo.mobile.android", "com.duckduckgo.mobile.android:id/omnibarTextInput"))
        return browsers
    }

    private val previousUrlDetections: HashMap<String, Long> = HashMap()


    private fun captureUrl(info: AccessibilityNodeInfo, config: SupportedBrowserConfig): String? {
        val nodes = info.findAccessibilityNodeInfosByViewId(config.addressBarId)
        if (nodes == null || nodes.size <= 0) {
            return null
        }
        val addressBarNodeInfo = nodes[0]
        var url: String? = null
        if (addressBarNodeInfo.text != null) {
            url = addressBarNodeInfo.text.toString()
        }
        addressBarNodeInfo.recycle()
        return url
    }

}