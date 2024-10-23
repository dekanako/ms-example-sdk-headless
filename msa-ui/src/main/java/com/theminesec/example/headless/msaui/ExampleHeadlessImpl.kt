package com.theminesec.example.headless.msaui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.theminesec.lib.dto.poi.PoiRequest
import com.theminesec.lib.dto.transaction.PaymentMethod
import com.theminesec.lib.dto.transaction.WalletType
import com.theminesec.sdk.headless.HeadlessActivity
import com.theminesec.sdk.headless.extension.toDisplayString
import com.theminesec.sdk.headless.ui.*
import com.theminesec.sdk.headless.ui.component.AwaitCardIndicator
import com.theminesec.sdk.headless.ui.component.ProcessingIndicator
import com.theminesec.sdk.headless.ui.component.resource.Icon
import com.theminesec.sdk.headless.ui.component.resource.getStringRes
import com.theminesec.sdk.headless.ui.theme.HeadlessTheme
import com.theminesec.sdk.headless.ui.theme.baseFontFeature
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

class ExampleHeadlessImpl : HeadlessActivity() {
    // provide colors
    override fun provideTheme() = ExampleThemeProvider

    // provide screens
    override val experimentalScreenProvider = true
    override val screenProvider = MsaScreenProvider
}

object ExampleThemeProvider : ThemeProvider() {
    override fun provideColors(darkTheme: Boolean) =
        if (darkTheme) HeadlessColorsDark().copy(
            primary = Color(0xFF00EBED).toArgb(),
            primaryForeground = Color(0xFFE4F8EE).toArgb(),
            secondaryForeground = Color(0xFF004f5C).toArgb(),
            approval = Color(0xFF004f5C).toArgb(),
            approvalForeground = Color(0xFFbfffde).toArgb(),
        )
        else HeadlessColorsLight().copy(
            primary = Color(0xFF004f5C).toArgb(),
            primaryForeground = Color(0xFFFFFFFF).toArgb(),
            secondaryForeground = Color(0xFF004f5C).toArgb(),
            approval = Color(0xFF004f5C).toArgb(),
            approvalForeground = Color(0xFFbfffde).toArgb(),
        )

    override fun provideVerticalBias() = VerticalBias(0.45F)
}

object MsaScreenProvider : ScreenProvider() {
    private val shellPadding
        @Composable
        get() = PaddingValues(HeadlessTheme.spacing.md)

    @Composable
    override fun PreparationScreen(
        poiRequest: PoiRequest.ActionNew,
        preparingFlow: Flow<UiState.Preparing>,
        countdownFlow: StateFlow<Int>
    ) {
        val uiState by preparingFlow.collectAsStateWithLifecycle(UiState.Preparing.Idle)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(shellPadding),
        ) {
            // render animation first to lay it in the back
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(modifier = Modifier.size(280.dp), contentAlignment = Alignment.Center) {
                    ProcessingIndicator()
                }
                Spacer(Modifier.height(HeadlessTheme.spacing.lg))
                CustomStateDisplay(uiState)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
            ) {
                CustomTopBar(countdownFlow)
                CustomAmountDisplay(poiRequest)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                CustomCopyright()
            }
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
        val uiState by awaitingFlow.collectAsStateWithLifecycle(UiState.Preparing.Idle)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(shellPadding),
        ) {
            // render animation first to lay it in the back
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AwaitCardIndicator()
                Spacer(Modifier.height(HeadlessTheme.spacing.lg))
                CustomStateDisplay(uiState)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
            ) {
                CustomTopBar(countdownFlow, onAbort)
                CustomAmountDisplay(poiRequest)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(HeadlessTheme.spacing.xs2),
                    horizontalArrangement = Arrangement.spacedBy(
                        HeadlessTheme.spacing.xs,
                        Alignment.CenterHorizontally
                    ),
                ) { supportedMethods.forEach { it.Icon() } }
                Spacer(modifier = Modifier.size(HeadlessTheme.spacing.xs2))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(HeadlessTheme.spacing.xs2),
                    horizontalArrangement = Arrangement.spacedBy(
                        HeadlessTheme.spacing.xs,
                        Alignment.CenterHorizontally
                    ),
                ) { WalletType.entries.forEach { it.Icon() } }
                Spacer(modifier = Modifier.size(HeadlessTheme.spacing.lg))
                CustomCopyright()
            }
        }
    }

    @Composable
    override fun ProcessingScreen(
        poiRequest: PoiRequest,
        processingFlow: Flow<UiState.Processing>,
        countdownFlow: StateFlow<Int>
    ) {
        val uiState by processingFlow.collectAsStateWithLifecycle(UiState.Preparing.Idle)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(shellPadding),
        ) {
            // render animation first to lay it in the back
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(modifier = Modifier.size(280.dp), contentAlignment = Alignment.Center) {
                    ProcessingIndicator()
                }
                Spacer(Modifier.height(HeadlessTheme.spacing.lg))
                CustomStateDisplay(uiState)
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
            ) {
                CustomTopBar(countdownFlow)
                if (poiRequest is PoiRequest.ActionNew) {
                    CustomAmountDisplay(poiRequest)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                CustomCopyright()
            }
        }
    }

    @Composable
    fun CustomTopBar(
        countdownFlow: StateFlow<Int>,
        onAbort: (() -> Unit)? = null,
    ) {
        val countdownSec by countdownFlow.collectAsStateWithLifecycle()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            onAbort?.let {
                IconButton(
                    modifier = Modifier
                        .size(56.dp)
                        .requiredSize(56.dp)
                        .offset(x = (-16).dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = HeadlessTheme.headlessColors.mutedForeground.toComposeColor(),
                    ),
                    onClick = it,
                ) {
                    Icon(
                        painter = painterResource(id = com.theminesec.sdk.headless.R.drawable.ico_close),
                        contentDescription = stringResource(com.theminesec.sdk.headless.R.string.button_abort)
                    )
                }
            }
            Spacer(Modifier.weight(1f, true))
            Text(
                text = stringResource(com.theminesec.sdk.headless.R.string.var_countdown_second, countdownSec),
                style = HeadlessTheme.typography.bodyLarge,
                color = HeadlessTheme.headlessColors.primary.toComposeColor()
            )
        }
    }

    @Composable
    fun CustomAmountDisplay(poiRequest: PoiRequest.ActionNew) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(HeadlessTheme.spacing.sm))
            Text(
                stringResource(poiRequest.tranType.getStringRes()),
                style = HeadlessTheme.typography.titleSmall,
                color = HeadlessTheme.headlessColors.mutedForeground.toComposeColor(),
            )
            Text(
                text = poiRequest.amount.toDisplayString(),
                style = HeadlessTheme.typography.headlineLarge.copy(fontFeatureSettings = "$baseFontFeature,tnum"),
            )
            poiRequest.description?.let {
                Text(it)
            }
        }
    }

    @Composable
    fun CustomStateDisplay(uiState: UiState) {
        Text(
            text = uiState.getTitle(),
            textAlign = TextAlign.Center,
            style = HeadlessTheme.typography.titleLarge,
        )
        Spacer(Modifier.size(HeadlessTheme.spacing.xs2))
        Text(
            text = uiState.getDescription(),
            textAlign = TextAlign.Center,
            style = HeadlessTheme.typography.bodyMedium,
            color = HeadlessTheme.headlessColors.accentForeground.toComposeColor()
        )
    }

    @Composable
    fun CustomCopyright() {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = stringResource(R.string.var_copyright, LocalDate.now().year),
            style = HeadlessTheme.typography.bodySmall.copy(fontFeatureSettings = "$baseFontFeature,tnum"),
            color = HeadlessTheme.headlessColors.mutedForeground.toComposeColor(),
        )
    }
}
