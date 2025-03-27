package com.theminesec.example.headless;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.theminesec.example.headless.databinding.CompAmountDisplayBinding;
import com.theminesec.lib.dto.common.Amount;
import com.theminesec.lib.dto.transaction.PaymentMethod;
import com.theminesec.lib.dto.transaction.WalletType;
import com.theminesec.sdk.headless.ui.AcceptanceMarksView;
import com.theminesec.sdk.headless.ui.AmountView;
import com.theminesec.sdk.headless.ui.AwaitCardIndicatorView;
import com.theminesec.sdk.headless.ui.ProgressIndicatorView;
import com.theminesec.sdk.headless.ui.component.resource.PaymentMethodKt;
import com.theminesec.sdk.headless.ui.component.resource.WalletTypeKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.math.RoundingMode;
import java.util.List;

public class ClientViewProvider implements
        AmountView,
        ProgressIndicatorView,
        AwaitCardIndicatorView,
        AcceptanceMarksView {

    private int intToDp(Context context, int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }

    @NotNull
    @Override
    public View createAmountView(
            @NotNull Context context,
            @NotNull Amount amount,
            @Nullable String description) {
        LayoutInflater inflater = LayoutInflater.from(context);
        CompAmountDisplayBinding binding = DataBindingUtil.inflate(inflater, R.layout.comp_amount_display, null, false);
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
        );
        binding.getRoot().setLayoutParams(layoutParams);
        binding.setAmount(amount.getValue().setScale(amount.getCurrency().getDefaultFractionDigits(), RoundingMode.HALF_DOWN).toString());
        binding.setDescription(description);

        return binding.getRoot();
    }

    @NotNull
    @Override
    public View createAcceptanceMarksView(
            @NotNull Context context,
            @NotNull List<? extends PaymentMethod> supportedPayments,
            boolean showWallet) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        iconLp.setMarginEnd(intToDp(context, 16));

        for (PaymentMethod pm : supportedPayments) {
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(PaymentMethodKt.getImageRes(pm));
            imageView.setLayoutParams(iconLp);
            linearLayout.addView(imageView);
        }

        if (showWallet) {
            ImageView walletImageView = new ImageView(context);
            walletImageView.setImageResource(WalletTypeKt.getImageRes(WalletType.APPLE_PAY));
            linearLayout.addView(walletImageView);
        }

        return linearLayout;
    }

    @NotNull
    @Override
    public View createAwaitCardIndicatorView(@NotNull Context context) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        TextureView textureView = new TextureView(context);

        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Uri videoUri = new Uri.Builder()
                        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                        .authority(context.getPackageName())
                        .appendPath(String.valueOf(R.raw.demo_await))
                        .build();

                try {
                    mediaPlayer.setDataSource(context, videoUri);
                    mediaPlayer.setSurface(new Surface(surface));
                    mediaPlayer.setLooping(true);
                    mediaPlayer.prepareAsync();
                    mediaPlayer.setOnPreparedListener(MediaPlayer::start);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });

        return textureView;
    }

    @NotNull
    @Override
    public View createProgressIndicatorView(@NotNull Context context) {
        CircularProgressIndicator progressIndicator = new CircularProgressIndicator(context);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        progressIndicator.setLayoutParams(layoutParams);

        progressIndicator.setIndicatorSize(intToDp(context, 200));
        progressIndicator.setTrackThickness(intToDp(context, 8));
        progressIndicator.setIndeterminate(true);
        progressIndicator.setTrackColor(Color.TRANSPARENT);
        progressIndicator.setIndicatorColor(Color.parseColor("#FFD503"));
        progressIndicator.setVisibility(View.VISIBLE);

        return progressIndicator;
    }
}
