package com.theminesec.example.headless_xml

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.theminesec.example.headless_xml.databinding.ActivityMainBinding
import com.theminesec.sdk.headless.HeadlessActivity
import com.theminesec.sdk.headless.HeadlessSetup
import com.theminesec.sdk.headless.model.transaction.Amount
import com.theminesec.sdk.headless.model.transaction.PoiRequest
import com.theminesec.sdk.headless.model.transaction.TranType
import kotlinx.coroutines.launch
import java.util.*

class ClientMain : AppCompatActivity() {

    private val binding: ActivityMainBinding by contentView(R.layout.activity_main)

    private val launcher = registerForActivityResult(
        HeadlessActivity.contract(ClientHeadlessImpl::class.java)
    ) {
        Log.d(TAG, "onCreate: WrappedResult<Transaction>: $it}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.view = this
    }

    fun checkInitStatus() {
        val sdkInitResp = (application as ClientApp).sdkInitResp
        Log.d(TAG, "checkInitStatus: $sdkInitResp")
    }

    fun initialSetup() = lifecycleScope.launch {
        val res = HeadlessSetup.initialSetup(this@ClientMain)
        Log.d(TAG, "setupInitial: $res")
    }

    fun launchNewSale() {
        launcher.launch(
            PoiRequest.New(
                tranType = TranType.SALE,
                amount = Amount(
                    "10.00".toBigDecimal(),
                    Currency.getInstance("HKD"),
                ),
                profileId = "hkd-p1",
                forceFetchProfile = true
            )
        )
    }

    fun launchNewSale20() {
        launcher.launch(
            PoiRequest.New(
                tranType = TranType.SALE,
                amount = Amount(
                    "20.00".toBigDecimal(),
                    Currency.getInstance("HKD"),
                ),
                profileId = "hkd-limited",
                forceFetchProfile = true
            )
        )
    }
}
