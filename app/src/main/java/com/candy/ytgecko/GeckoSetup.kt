package com.candy.ytgecko

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.json.JSONException
import org.json.JSONObject
import org.mozilla.geckoview.*

private const val EXTENSION_LOCATION = "resource://android/assets/messaging/"
val geckoSession = GeckoSession()
var isFullScreen: Boolean = false
var canGeckoGoBack: Boolean = false
var currentURL: String = "https://m.youtube.com"
var mediaArtist: String = "Artist"
var mediaTitle: String = "Title of Track"

fun contentOverrideSetup(myActivity: Activity){

    geckoSession.contentDelegate = object : GeckoSession.ContentDelegate{
        override fun onFirstContentfulPaint(session: GeckoSession) {
            binding.blackPage.visibility = View.INVISIBLE
            super.onFirstContentfulPaint(session)
        }
        override fun onFullScreen(session: GeckoSession, fullScreen: Boolean) {
            if(fullScreen){
                WindowCompat.setDecorFitsSystemWindows(myActivity.window, false)
                val windowInsetsController =
                    ViewCompat.getWindowInsetsController(myActivity.window.decorView) ?: return
                windowInsetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                isFullScreen = true
                super.onFullScreen(session, fullScreen)
            }else{
                WindowCompat.setDecorFitsSystemWindows(myActivity.window, true)
                val windowInsetsController =
                    ViewCompat.getWindowInsetsController(myActivity.window.decorView) ?: return
                windowInsetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
                super.onFullScreen(session, fullScreen)
                session.exitFullScreen()
                isFullScreen = false
            }
        }

        override fun onContextMenu(
            session: GeckoSession,
            screenX: Int,
            screenY: Int,
            element: GeckoSession.ContentDelegate.ContextElement
        ) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type="text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, currentURL)
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Sharing URL")
            myActivity.startActivity(Intent.createChooser(shareIntent,"Share URL"))
            super.onContextMenu(session, screenX, screenY, element)
        }
    }
}

var mediaStore: MediaSession =object: MediaSession(geckoSession){}
fun mediaOverride(context: Context, myActivity: Activity){
    geckoSession.mediaSessionDelegate = object : MediaSession.Delegate{
        override fun onActivated(session: GeckoSession, mediaSession: MediaSession) {
            notificationBuilder(context, 1)
            mediaStore = mediaSession
            super.onActivated(session, mediaSession)
        }

        override fun onPause(session: GeckoSession, mediaSession: MediaSession) {
            myActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            mediaStore = mediaSession
            notificationBuilder(context, 2)
            super.onPause(session, mediaSession)
        }

        override fun onPlay(session: GeckoSession, mediaSession: MediaSession) {
            mediaStore = mediaSession
            notificationBuilder(context, 1)
            myActivity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            super.onPlay(session, mediaSession)
        }

        override fun onDeactivated(session: GeckoSession, mediaSession: MediaSession) {
            hideNotification(context)
            super.onDeactivated(session, mediaSession)
        }

        override fun onMetadata(
            session: GeckoSession,
            mediaSession: MediaSession,
            meta: MediaSession.Metadata
        ) {
            if(meta.title != null){ mediaTitle = meta.title!! }
            if(meta.artist != null){ mediaArtist = meta.artist!! }
            notificationBuilder(context, 1)
            super.onMetadata(session, mediaSession, meta)
        }

        override fun onStop(session: GeckoSession, mediaSession: MediaSession) {
            myActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            hideNotification(context)
            super.onStop(session, mediaSession)
        }

    }
}

fun navOverrideSetup(myActivity: Activity){
    geckoSession.navigationDelegate = object : GeckoSession.NavigationDelegate{
        override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
            canGeckoGoBack = canGoBack
        }

        override fun onLoadRequest(
            session: GeckoSession,
            request: GeckoSession.NavigationDelegate.LoadRequest):
                GeckoResult<AllowOrDeny> {
            return if (request.uri.contains("youtube")) {
                GeckoResult.allow()
            } else {
                GeckoResult.deny()
            }
        }

        override fun onLocationChange(session: GeckoSession, url: String?) {
            currentURL = if (url == null) {
                geckoSession.loadUri("https://m.youtube.com")
                "https://m.youtube.com"
            }else{
                url
            }

            if (url == "https://m.youtube.com" || url == "https://m.youtube.com/"){
                myActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else{
                myActivity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }

            super.onLocationChange(session, url)
        }
    }
}

fun setUpGecko(context: Context){
    val runtime = GeckoRuntime.create(context)

    val messageDelegate: WebExtension.MessageDelegate = object : WebExtension.MessageDelegate {
        override fun onMessage(
            nativeApp: String,
            message: Any,
            sender: WebExtension.MessageSender
        ): GeckoResult<Any>? {
            if (message is JSONObject) {
                try {
                    if (message.has("type") && "WPAManifest" == message.getString("type")) {
                        val manifest = message.getJSONObject("manifest")
                        Log.d("MessageDelegate", "Found WPA manifest: $manifest")
                    }
                } catch (ex: JSONException) {
                    Log.e("MessageDelegate", "Invalid manifest", ex)
                }
            }
            return null
        }
    }

    runtime
        .webExtensionController
        .ensureBuiltIn(EXTENSION_LOCATION, "messaging@example.com")
        .accept( // Set delegate that will receive messages coming from this extension.
            { extension ->
                extension?.let {
                    geckoSession
                        .webExtensionController
                        .setMessageDelegate(it, messageDelegate, "browser")
                }
            }
        )
        { e -> Log.e("MessageDelegate", "Error registering extension", e) }


    geckoSession.open(runtime)
    val seT = geckoSession.settings
    seT.useTrackingProtection = true
    seT.suspendMediaWhenInactive = false
    binding.geckoview.setSession(geckoSession)
}
