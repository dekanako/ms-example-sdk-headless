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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.theminesec.example.headless.exampleHelper.HelperLayout
import com.theminesec.example.headless.exampleHelper.HelperViewModel
import com.theminesec.example.headless.exampleHelper.component.Button
import com.theminesec.sdk.headless.HeadlessActivity
import com.theminesec.sdk.headless.HeadlessService
import com.theminesec.sdk.headless.HeadlessSetup
import com.theminesec.sdk.headless.model.transaction.*
import kotlinx.coroutines.launch
import ulid.ULID
import java.util.*

class ClientMain : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: HelperViewModel by viewModels()

        val launcher = registerForActivityResult(
            HeadlessActivity.contract(ClientHeadlessImpl::class.java)
        ) {
            viewModel.writeMessage("WrappedResult<Transaction>: \n$it}")
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
                            profileId = "hkd-p1",
                            forceFetchProfile = false
                        )
                    )
                }) {
                    Text(text = "TranRequest with profile 1 (Full method)")
                }
                Button(onClick = {
                    launcher.launch(
                        PoiRequest.New(
                            tranType = TranType.SALE,
                            amount = Amount(
                                viewModel.amountStr.toBigDecimal(),
                                Currency.getInstance(viewModel.currency),
                            ),
                            profileId = "hkd-limited-payment",
                            forceFetchProfile = true
                        )
                    )
                }) {
                    Text(text = "TranRequest with profile 2 (VMA)")
                }

                HorizontalDivider()
                Button(onClick = {
                    val action = PoiRequest.Action(ActionType.VOID, ULID.randomULID())
                    launcher.launch(action)
                }) {
                    Text(text = "Action (after transaction) request with UI")
                }
                Button(onClick = {
                    val query = PoiRequest.Query(Referencable.TranId(ULID.randomULID()))
                    launcher.launch(query)
                }) {
                    Text(text = "Query request with UI")
                }

                HorizontalDivider()

                Button(onClick = {
                    val action = PoiRequest.Action(ActionType.VOID, ULID.randomULID())
                    lifecycleScope.launch {
                        HeadlessService.createAction(action)
                            .also { viewModel.writeMessage("createAction: $it") }
                    }
                }) {
                    Text(text = "Action request without UI")
                }
                Button(onClick = {
                    lifecycleScope.launch {
                        val reference = Referencable.TranId(ULID.randomULID())
                        HeadlessService.getTransaction(reference)
                            .also { viewModel.writeMessage("getTransaction: $it") }
                    }
                }) {
                    Text(text = "Query request without UI")
                }
            }
        }
    }
}
