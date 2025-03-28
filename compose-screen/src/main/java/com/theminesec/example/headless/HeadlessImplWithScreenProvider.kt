package com.theminesec.example.headless

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.theminesec.lib.dto.common.Amount
import com.theminesec.lib.dto.poi.PoiRequest
import com.theminesec.lib.dto.transaction.PaymentMethod
import com.theminesec.sdk.headless.HeadlessActivity
import com.theminesec.sdk.headless.extension.toDisplayString
import com.theminesec.sdk.headless.ui.ScreenProvider
import com.theminesec.sdk.headless.ui.UiState
import com.theminesec.sdk.headless.ui.component.SignaturePad
import com.theminesec.sdk.headless.ui.component.SignatureState
import com.theminesec.sdk.headless.ui.component.resource.Icon
import com.theminesec.sdk.headless.ui.theme.HeadlessTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

class HeadlessImplWithScreenProvider : HeadlessActivity() {
    override val experimentalScreenProvider = true
    override val screenProvider = object : ScreenProvider() {
        @OptIn(ExperimentalCoroutinesApi::class)
        @Composable
        override fun PreparationScreen(
            poiRequest: PoiRequest.ActionNew,
            preparingFlow: Flow<UiState.Preparing>,
            countdownFlow: StateFlow<Int>
        ) {
            val countdownSec by countdownFlow.collectAsStateWithLifecycle()
            val uiState by preparingFlow.collectAsStateWithLifecycle(UiState.Preparing.Idle)

            Shell {
                Title(text = "Preparation Screen")
                Desc(text = "-> getting profile, checking nfc etc")
                Text(text = "countdown: $countdownSec")
                Text(text = "uiState: $uiState")
                AmountText(poiRequest.amount)
                PoiRequestText(poiRequest)
            }
        }

        @OptIn(ExperimentalLayoutApi::class)
        @Composable
        override fun AwaitingCardScreen(
            poiRequest: PoiRequest.ActionNew,
            awaitingFlow: Flow<UiState.Awaiting>,
            supportedMethods: List<PaymentMethod>,
            countdownFlow: StateFlow<Int>,
            onAbort: () -> Unit
        ) {
            val countdownSec by countdownFlow.collectAsStateWithLifecycle()
            val uiState by awaitingFlow.collectAsStateWithLifecycle(UiState.Preparing.Idle)
            Shell {
                Title(text = "Await Card Screen")
                Desc(text = "-> awaiting card tapping")
                Text(text = "countdown: $countdownSec")
                Text(text = "uiState: $uiState")
                AmountText(poiRequest.amount)
                PoiRequestText(poiRequest)
                Text(text = "supportedMethods: $supportedMethods")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(HeadlessTheme.spacing.xs2),
                    horizontalArrangement = Arrangement.spacedBy(
                        HeadlessTheme.spacing.xs,
                        Alignment.CenterHorizontally
                    )
                ) {
                    supportedMethods.forEach { it.Icon() }
                }
                Button(onClick = onAbort) {
                    Text(text = "Abort")
                }
            }
        }

        @Composable
        override fun ProcessingScreen(
            poiRequest: PoiRequest,
            processingFlow: Flow<UiState.Processing>,
            countdownFlow: StateFlow<Int>
        ) {
            val countdownSec by countdownFlow.collectAsStateWithLifecycle()
            val uiState by processingFlow.collectAsStateWithLifecycle(UiState.Preparing.Idle)
            Shell {
                Title("Processing Screen")
                Desc("-> processing transaction")
                Text(text = "countdown: $countdownSec")
                Text(text = "uiState: $uiState")
                if (poiRequest is PoiRequest.ActionNew) {
                    AmountText(poiRequest.amount)
                }
                PoiRequestText(poiRequest)
            }
        }

        @Composable
        override fun SignatureScreen(
            poiRequest: PoiRequest.ActionNew,
            signatureState: SignatureState,
            onSignatureConfirm: () -> Unit,
            displayPmAndLast4: Pair<PaymentMethod, String>,
            approvalCode: String?
        ) {
            Shell {
                Title("Signature Screen")
                Desc("-> only show when CVM is signature, and from the PoiRequest `cvmSignatureMode = CvmSignatureMode.ELECTRONIC_SIGNATURE`")
                AmountText(poiRequest.amount)
                Text(text = "displayPmAndLast4: $displayPmAndLast4")
                Text(text = "approvalCode: $approvalCode")
                PoiRequestText(poiRequest)
                SignaturePad(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f)
                        .border(
                            border = BorderStroke(1.dp, Color.Gray),
                            shape = RoundedCornerShape(4.dp)
                        ),
                    state = signatureState
                )
                Button(onClick = {
                    signatureState.clearSignatureLines()
                }) {
                    Text(text = "Clear Signature")
                }
                Button(onClick = onSignatureConfirm) {
                    Text(text = "Confirm Signature")
                }
            }
        }
    }
}

@Composable
private fun Shell(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .border(1.dp, Color.Red),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        content()
    }
}

@Composable
private fun Title(text: String) {
    Text(text = text, fontSize = 18.sp)
}

@Composable
private fun Desc(text: String) {
    Text(text = text, fontSize = 12.sp)
}

@Composable
private fun PoiRequestText(poiRequest: PoiRequest) {
    Text(text = "poiRequest: $poiRequest", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
}

@Composable
private fun AmountText(amount: Amount) {
    Text(text = "amount: ${amount.toDisplayString(Locale("en"), showCurrency = true, useSymbol = false)}")
}
