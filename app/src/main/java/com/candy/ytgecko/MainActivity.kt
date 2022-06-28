package com.candy.ytgecko

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import com.candy.ytgecko.databinding.ActivityMainBinding

lateinit var binding: ActivityMainBinding
var loadPage = "https://m.youtube.com"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        when (intent?.action) {
            Intent.ACTION_VIEW -> {
                loadPage = intent.data.toString()
            }
        }

        contentOverrideSetup(this)
        navOverrideSetup(this)
        mediaOverride(this, this)
        setUpGecko(this)
        geckoSession.loadUri(loadPage)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(isFullScreen){
            geckoSession.exitFullScreen()
            return true
        }else if (keyCode == KeyEvent.KEYCODE_BACK && canGeckoGoBack) {
            geckoSession.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        hideNotification(this)
        super.onDestroy()
    }

}