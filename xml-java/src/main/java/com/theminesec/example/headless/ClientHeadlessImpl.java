package com.theminesec.example.headless;

import com.theminesec.sdk.headless.HeadlessActivity;
import com.theminesec.sdk.headless.ui.ThemeProvider;
import com.theminesec.sdk.headless.ui.UiProvider;
import org.jetbrains.annotations.NotNull;

public class ClientHeadlessImpl extends HeadlessActivity {
    @NotNull
    @Override
    public ThemeProvider provideTheme() {
        return new ClientThemeProvider();
    }

    private final ClientViewProvider viewProvider = new ClientViewProvider();

    @NotNull
    @Override
    public UiProvider provideUi() {
        return new UiProvider(
                viewProvider, // amountView
                viewProvider, // progressIndicatorView
                viewProvider, // awaitCardIndicatorView
                viewProvider, // acceptanceMarksView
                null, // signatureScreenView
                null  // uiStateDisplayView
        );
    }
}
