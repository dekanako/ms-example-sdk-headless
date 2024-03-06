package com.theminesec.example.headless

import android.app.Application
import android.util.Log
import com.theminesec.example.headless.util.TAG
import com.theminesec.sdk.headless.HeadlessSetup
import com.theminesec.sdk.headless.model.WrappedResult
import com.theminesec.sdk.headless.model.setup.SdkInitResp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClientApp : Application() {
    private val appScope = CoroutineScope(Dispatchers.Main)
    var sdkInitResp: WrappedResult<SdkInitResp>? = null
        private set

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            val clientAppInitRes = HeadlessSetup.initSoftPos(this@ClientApp, "public-test.license")
            Log.d(TAG, "$clientAppInitRes")
            sdkInitResp = clientAppInitRes
        }
    }
}
