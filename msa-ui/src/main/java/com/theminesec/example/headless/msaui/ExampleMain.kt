package com.theminesec.example.headless.msaui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.theminesec.lib.dto.common.Amount
import com.theminesec.lib.dto.common.toCurrency
import com.theminesec.lib.dto.poi.CvmSignatureMode
import com.theminesec.lib.dto.poi.PoiRequest
import com.theminesec.lib.dto.transaction.PaymentMethod
import com.theminesec.lib.dto.transaction.TranType
import com.theminesec.sdk.headless.HeadlessActivity
import com.theminesec.sdk.headless.HeadlessSetup
import com.theminesec.sdk.headless.model.WrappedResult
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Clock

class ExampleMain : ComponentActivity() {
    private val json by lazy {
        Json { prettyPrint = true }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // init
        var isInitializing by mutableStateOf(false)
        var sdkInitStatus: String? by mutableStateOf(null)

        lifecycleScope.launch {
            isInitializing = true
            HeadlessSetup.initSoftPos(application, "test.license").let {
                sdkInitStatus = it.toString()
            }
            isInitializing = false
        }

        // setup
        var isSettingUp by mutableStateOf(false)
        var setupStatus: String? by mutableStateOf(null)

        // txn
        val profileId = "prof_01HYYPGVE7VB901M40SVPHTQ0V"
        var tranStatus: String? by mutableStateOf(null)
        var wholeThing: String? by mutableStateOf(null)
        val launcher = registerForActivityResult(
            HeadlessActivity.contract(ExampleHeadlessImpl::class.java)
        ) { activityResult ->
            when (activityResult) {
                is WrappedResult.Success -> {
                    val result = activityResult.value
                    Log.d(TAG, "result success: $result")
                    tranStatus = result.tranStatus.name
                    wholeThing = json.encodeToString(result)
                }

                is WrappedResult.Failure -> {
                    Log.d(TAG, "result failed: $activityResult")
                    wholeThing = json.encodeToString(activityResult)
                }
            }
        }

        setContent {
            val localContext = LocalContext.current

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, Color.Gray, MaterialTheme.shapes.extraSmall)
                            .padding(4.dp)
                    ) {
                        Text(text = "init")
                        Button(
                            onClick = {
                                lifecycleScope.launch {
                                    isInitializing = true
                                    HeadlessSetup.initSoftPos(application, "test.license").let {
                                        sdkInitStatus = it.toString()
                                    }
                                    isInitializing = false
                                }
                            },
                            enabled = !isInitializing,
                        ) {
                            Text(text = "init sdk")
                        }

                        Text(text = "$sdkInitStatus")
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, Color.Gray, MaterialTheme.shapes.extraSmall)
                            .padding(4.dp)
                    ) {
                        Text(text = "setup")
                        Button(
                            onClick = {
                                lifecycleScope.launch {
                                    isSettingUp = true
                                    HeadlessSetup.initialSetup(localContext).let {
                                        setupStatus = it.toString()
                                    }
                                    isSettingUp = false
                                }
                            }, enabled = !isSettingUp
                        ) {
                            Text(text = "setups (download keys)")
                        }
                        Text(text = "$setupStatus")
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, Color.Gray, MaterialTheme.shapes.extraSmall)
                            .padding(4.dp)
                    ) {
                        Text(text = "txn status: $tranStatus")

                        Button(onClick = {
                            launcher.launch(
                                PoiRequest.ActionNew(
                                    tranType = TranType.SALE,
                                    amount = Amount(
                                        "1.0".toBigDecimal(),
                                        "USD".toCurrency(),
                                    ),
                                    profileId = profileId,
                                    // below are optional params
                                    tapToOwnDevice = true, // for turning on an indicator for mastercard in message to acquirer
                                    //forceFetchProfile = true, // force get profile from remote, ignoring local cache
                                    forcePaymentMethod = listOf(
                                        PaymentMethod.VISA,
                                        PaymentMethod.MASTERCARD
                                    ), // if you like to explicitly turn off some method regardless from the remote profile
                                    posReference = Clock.systemUTC().millis()
                                        .toString(), // your sys's unique identifier
                                )
                            )
                        }) {
                            Text(text = "txn request ($1)")
                        }

                        Button(onClick = {
                            launcher.launch(
                                PoiRequest.ActionNew(
                                    tranType = TranType.SALE,
                                    amount = Amount(
                                        "101.0".toBigDecimal(),
                                        "USD".toCurrency(),
                                    ),
                                    profileId = profileId,
                                    posReference = Clock.systemUTC().millis().toString(),
                                    cvmSignatureMode = CvmSignatureMode.ELECTRONIC_SIGNATURE,
                                    forceFetchProfile = true,
                                )
                            )
                        }) {
                            Text(text = "txn request ($101) with e signature")
                        }

                        Text(text = "$wholeThing")
                    }
                }
            }
        }
    }
}
