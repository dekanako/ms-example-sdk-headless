package com.theminesec.example.headless;

import android.graphics.Color;
import com.theminesec.sdk.headless.ui.ThemeProvider;
import org.jetbrains.annotations.NotNull;

public class ClientThemeProvider extends ThemeProvider {
    @NotNull
    @Override
    public HeadlessColors provideColors(boolean darkTheme) {
        return new HeadlessColorsLight().copy(
                // primary
                Color.parseColor("#FFD503"),
                // primaryForeground
                Color.parseColor("#F0FDF4"),
                // secondary
                Color.parseColor("#0A307A"),
                // secondaryForeground
                Color.parseColor("#EDF2FD"),
                // background
                Color.parseColor("#FFFFFF"),
                // foreground
                Color.parseColor("#333333"),
                // muted
                Color.parseColor("#F1F5F9"),
                // mutedForeground
                Color.parseColor("#4E3C2E"),
                // accent
                Color.parseColor("#E2E8F0"),
                // accentForeground
                Color.parseColor("#334155"),
                // approval
                Color.parseColor("#29D657"),
                // approvalForeground
                Color.parseColor("#F0FDF4"),
                // error
                Color.parseColor("#EF4444"),
                // errorForeground
                Color.parseColor("#FEF2F2"),
                // ring
                Color.parseColor("#CBD5E1")
        );
    }
}
