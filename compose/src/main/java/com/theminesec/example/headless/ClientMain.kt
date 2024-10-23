package com.theminesec.example.headless

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.gson.*
import com.theminesec.example.headless.exampleHelper.HelperLayout
import com.theminesec.example.headless.exampleHelper.HelperViewModel
import com.theminesec.example.headless.exampleHelper.component.Button
import com.theminesec.lib.dto.common.Amount
import com.theminesec.lib.dto.poi.CvmSignatureMode
import com.theminesec.lib.dto.poi.PoiRequest
import com.theminesec.lib.dto.poi.Referencable
import com.theminesec.lib.dto.transaction.PaymentMethod
import com.theminesec.lib.dto.transaction.TranType
import com.theminesec.sdk.headless.HeadlessActivity
import com.theminesec.sdk.headless.HeadlessService
import com.theminesec.sdk.headless.HeadlessSetup
import com.theminesec.sdk.headless.model.WrappedResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.lang.reflect.Type
import java.math.BigDecimal
import java.time.Instant
import java.util.*

class ClientMain : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: HelperViewModel by viewModels()
        var completedSaleTranId: String? by mutableStateOf(null)
        var completedSalePosReference: String? by mutableStateOf(null)
        var completedSaleRequestId: String? by mutableStateOf(null)
        var completedRefundTranId: String? by mutableStateOf(null)
        val gson = GsonBuilder()
            .registerTypeAdapter(Instant::class.java, object : JsonSerializer<Instant> {
                override fun serialize(p0: Instant?, p1: Type?, p2: JsonSerializationContext?): JsonElement = JsonPrimitive(p0?.toString())
            })
            .setPrettyPrinting().create()

        lifecycleScope.launch {
            val status = (application as ClientApp).sdkInitStatus.first()
            viewModel.writeMessage("SDK Init: $status")
        }

        val launcher = registerForActivityResult(
            HeadlessActivity.contract(ClientHeadlessImpl::class.java)
        ) {
            viewModel.writeMessage("${it.javaClass.simpleName} \n${gson.toJson(it)}")
            viewModel.resetRandomPosReference()
            when (it) {
                is WrappedResult.Success -> {
                    if (it.value.tranType == TranType.SALE) {
                        completedSaleTranId = it.value.tranId
                        completedSalePosReference = it.value.posReference
                        completedSaleRequestId = it.value.actions.firstOrNull()?.requestId
                    }
                    if (it.value.tranType == TranType.REFUND) {
                        completedRefundTranId = it.value.tranId
                    }
                }

                is WrappedResult.Failure -> {
                    viewModel.writeMessage("Failed")
                }
            }
        }

        setContent {
            HelperLayout {
                Button(onClick = {
                    lifecycleScope.launch {
                        val status = (application as ClientApp).sdkInitStatus.first()
                        viewModel.writeMessage("SDK Init: $status")
                    }
                }) {
                    Text(text = "SDK init status")
                }

                Button(onClick = {
                    lifecycleScope.launch {
                        val res = HeadlessSetup.initialSetup(this@ClientMain)
                        viewModel.writeMessage("initialSetup: $res")
                    }
                }) {
                    Text(text = "Initial setups (download Keys)")
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        label = { Text(text = "Currency") },
                        value = viewModel.currency,
                        onValueChange = {},
                        enabled = false
                    )
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        label = { Text(text = "Amount") },
                        value = viewModel.amountStr,
                        onValueChange = viewModel::handleInputAmt,
                        isError = viewModel.amountStr.isEmpty(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "POS message ID") },
                    value = viewModel.posReference,
                    onValueChange = { viewModel.posReference = it },
                )
                TextButton(
                    onClick = { viewModel.resetRandomPosReference() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(text = "Set random POS message ID")
                }

                Button(onClick = {
                    launcher.launch(
                        PoiRequest.ActionNew(
                            tranType = TranType.SALE,
                            amount = Amount(
                                viewModel.amountStr.toBigDecimal(),
                                Currency.getInstance(viewModel.currency),
                            ),
                            profileId = "prof_01HYYPGVE7VB901M40SVPHTQ0V",
                            preferredAcceptanceTag = "SME",
                            forcePaymentMethod = listOf(PaymentMethod.VISA, PaymentMethod.MASTERCARD),
                            description = "description 123",
                            posReference = viewModel.posReference,
                            forceFetchProfile = true,
                            cvmSignatureMode = CvmSignatureMode.ELECTRONIC_SIGNATURE,
                            tapToOwnDevice = false,
                        )
                    )
                }) {
                    Text(text = "PoiRequest.ActionNew")
                }

                HorizontalDivider()
                Text(text = "With UI\nActivity result launcher", style = MaterialTheme.typography.titleLarge)

                Text(text = "Last SALE: $completedSaleTranId")
                Text(text = "Last REFUND: $completedRefundTranId")

                Button(onClick = {
                    completedSaleTranId?.let {
                        val action = PoiRequest.ActionVoid(it)
                        launcher.launch(action)
                    } ?: viewModel.writeMessage("No tran ID")
                }) {
                    Text(text = "PoiRequest.ActionVoid")
                }
                Button(onClick = {
                    completedSaleTranId?.let {
                        val action = PoiRequest.ActionLinkedRefund(it)
                        launcher.launch(action)
                    } ?: viewModel.writeMessage("No tran ID")
                }) {
                    Text(text = "PoiRequest.ActionLinkedRefund (full amt)")
                }
                Button(onClick = {
                    completedSaleTranId?.let {
                        val action = PoiRequest.ActionLinkedRefund(it, Amount(BigDecimal("0.5"), Currency.getInstance(viewModel.currency)))
                        launcher.launch(action)
                    } ?: viewModel.writeMessage("No tran ID")
                }) {
                    Text(text = "PoiRequest.ActionLinkedRefund (partial amt)")
                }
                Button(onClick = {
                    completedRefundTranId?.let {
                        val action = PoiRequest.ActionVoid(it)
                        launcher.launch(action)
                    } ?: viewModel.writeMessage("No tran ID")
                }) {
                    Text(text = "PoiRequest.ActionVoid (using refund tran id)")
                }
                Button(onClick = {
                    completedSaleTranId?.let {
                        val query = PoiRequest.Query(Referencable.TranId(it))
                        launcher.launch(query)
                    } ?: viewModel.writeMessage("No tran ID")
                }) {
                    Text(text = "PoiRequest.Query (TranId)")
                }
                Button(onClick = {
                    completedSalePosReference?.let {
                        val query = PoiRequest.Query(Referencable.PosReference(it))
                        launcher.launch(query)
                    } ?: viewModel.writeMessage("No pos ref")
                }) {
                    Text(text = "PoiRequest.Query (PosReference)")
                }
                Button(onClick = {
                    completedSaleRequestId?.let {
                        val query = PoiRequest.Query(Referencable.RequestId(it))
                        launcher.launch(query)
                    } ?: viewModel.writeMessage("No poi req id")
                }) {
                    Text(text = "PoiRequest.Query (RequestId)")
                }

                HorizontalDivider()
                Text(text = "Without UI\nSuspended API", style = MaterialTheme.typography.titleLarge)

                Button(onClick = {
                    completedSaleTranId?.let {
                        val action = PoiRequest.ActionVoid(it)
                        lifecycleScope.launch {
                            HeadlessService.createAction(action).also { viewModel.writeMessage("createAction: $it") }
                        }
                    } ?: viewModel.writeMessage("No tran ID")
                }) {
                    Text(text = "PoiRequest.ActionVoid")
                }
                Button(onClick = {
                    completedSaleTranId?.let {
                        val action = PoiRequest.ActionLinkedRefund(it)
                        lifecycleScope.launch {
                            HeadlessService.createAction(action).also { viewModel.writeMessage("createAction: $it") }
                        }
                        launcher.launch(action)
                    } ?: viewModel.writeMessage("No tran ID")
                }) {
                    Text(text = "PoiRequest.ActionLinkedRefund (full amt)")
                }
                Button(onClick = {
                    completedSaleTranId?.let {
                        val action = PoiRequest.ActionLinkedRefund(it, Amount(BigDecimal("0.5"), Currency.getInstance(viewModel.currency)))
                        lifecycleScope.launch {
                            HeadlessService.createAction(action).also { viewModel.writeMessage("createAction: $it") }
                        }
                    } ?: viewModel.writeMessage("No tran ID")
                }) {
                    Text(text = "PoiRequest.ActionLinkedRefund (partial amt)")
                }
                Button(onClick = {
                    completedSaleTranId?.let {
                        val query = Referencable.TranId(it)
                        lifecycleScope.launch {
                            HeadlessService.getTransaction(query).also { viewModel.writeMessage("queryTransaction: $it") }
                        }
                    } ?: viewModel.writeMessage("No tran ID")
                }) {
                    Text(text = "PoiRequest.Query (TranId)")
                }
                Button(onClick = {
                    completedSalePosReference?.let {
                        val query = Referencable.PosReference(it)
                        lifecycleScope.launch {
                            HeadlessService.getTransaction(query).also { viewModel.writeMessage("queryTransaction: $it") }
                        }
                    } ?: viewModel.writeMessage("No pos ref")
                }) {
                    Text(text = "PoiRequest.Query (PosReference)")
                }
                Button(onClick = {
                    completedSaleRequestId?.let {
                        val query = Referencable.RequestId(it)
                        lifecycleScope.launch {
                            HeadlessService.getTransaction(query).also { viewModel.writeMessage("queryTransaction: $it") }
                        }
                    } ?: viewModel.writeMessage("No req id")
                }) {
                    Text(text = "PoiRequest.Query (RequestId)")
                }
            }
        }
    }
}
