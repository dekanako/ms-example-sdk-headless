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
import com.theminesec.sdk.headless.HeadlessActivity
import com.theminesec.sdk.headless.HeadlessService
import com.theminesec.sdk.headless.HeadlessSetup
import com.theminesec.sdk.headless.model.WrappedResult
import com.theminesec.sdk.headless.model.transaction.*
import kotlinx.coroutines.launch
import java.lang.reflect.Type
import java.time.Instant
import java.util.*

class ClientMain : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: HelperViewModel by viewModels()
        var completedTranId: String? by mutableStateOf(null)
        val gson = GsonBuilder()
            .registerTypeAdapter(Instant::class.java, object : JsonSerializer<Instant> {
                override fun serialize(p0: Instant?, p1: Type?, p2: JsonSerializationContext?): JsonElement = JsonPrimitive(p0?.toString())
            })
            .setPrettyPrinting().create()

        val launcher = registerForActivityResult(
            HeadlessActivity.contract(ClientHeadlessImpl::class.java)
        ) {
            viewModel.writeMessage("${it.javaClass.simpleName} \n${gson.toJson(it)}")
            when (it) {
                is WrappedResult.Success -> {
                    completedTranId = it.value.tranId
                    viewModel.writeMessage("Success:\n$it}")
                }

                is WrappedResult.Failure -> {
                    viewModel.writeMessage("Failed:\n$it}")
                }
            }
        }

        setContent {
            HelperLayout {
                Button(onClick = {
                    val sdkInitResp = (application as ClientApp).sdkInitResp
                    viewModel.writeMessage("sdkInitResp: $sdkInitResp}")
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
                        PoiRequest.New(
                            tranType = TranType.SALE,
                            amount = Amount(
                                viewModel.amountStr.toBigDecimal(),
                                Currency.getInstance(viewModel.currency),
                            ),
                            profileId = "prof_01HSEDQK3ZFH7R0KASB8T1SBSN",
                            forceFetchProfile = true
                        )
                    )
                }) {
                    Text(text = "TranRequest with profile")
                }

                Button(onClick = {
                    launcher.launch(
                        PoiRequest.New(
                            tranType = TranType.SALE,
                            amount = Amount(
                                "0.69".toBigDecimal(),
                                Currency.getInstance(viewModel.currency),
                            ),
                            profileId = "prof_01HSEDQK3ZFH7R0KASB8T1SBSN",
                            forceFetchProfile = true
                        )
                    )
                }) {
                    Text(text = "Mock Declined")
                }
                Button(onClick = {
                    launcher.launch(
                        PoiRequest.New(
                            tranType = TranType.SALE,
                            amount = Amount(
                                viewModel.amountStr.toBigDecimal(),
                                Currency.getInstance(viewModel.currency),
                            ),
                            profileId = "wrong profile",
                            forceFetchProfile = true
                        )
                    )
                }) {
                    Text(text = "TranRequest with wrong profile id")
                }

                HorizontalDivider()
                Button(onClick = {
                    completedTranId?.let {
                        val action = PoiRequest.Action(ActionType.VOID, it)
                        launcher.launch(action)
                    } ?: viewModel.writeMessage("No tran ID")
                }) {
                    Text(text = "Action (after transaction) request with UI")
                }
                Button(onClick = {
                    completedTranId?.let {
                        val query = PoiRequest.Query(Referencable.TranId(it))
                        launcher.launch(query)
                    } ?: viewModel.writeMessage("No tran ID")
                }) {
                    Text(text = "Query request with UI")
                }

                HorizontalDivider()

                Button(onClick = {
                    completedTranId?.let { tranId ->
                        val action = PoiRequest.Action(ActionType.VOID, tranId)
                        lifecycleScope.launch {
                            HeadlessService.createAction(action)
                                .also { viewModel.writeMessage("createAction: $it") }
                        }
                    } ?: viewModel.writeMessage("No tran ID")
                }) {
                    Text(text = "Action request without UI")
                }
                Button(onClick = {
                    completedTranId?.let { tranId ->
                        lifecycleScope.launch {
                            val reference = Referencable.TranId(tranId)
                            HeadlessService.getTransaction(reference).also {
                                viewModel.writeMessage("getTransaction: $it")
                            }
                        }
                    } ?: viewModel.writeMessage("No tran ID")
                }) {
                    Text(text = "Query request without UI")
                }
            }
        }
    }
}
