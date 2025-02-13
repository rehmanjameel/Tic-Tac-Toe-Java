package org.codebase.xticotact;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.material.card.MaterialCardView;

import org.codebase.xticotact.databinding.ActivityDashboardBinding;

public class DashboardActivity extends AppCompatActivity {

    ActivityDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.easyText.setShadowLayer(10, 5,5, ContextCompat.getColor(this, R.color.white));
        binding.mediumText.setShadowLayer(10, 5,5, ContextCompat.getColor(this, R.color.oColor));
        binding.hardText.setShadowLayer(10, 5,5, ContextCompat.getColor(this, R.color.winnerColor));
        binding.titleTextView.setShadowLayer(10, 5,5, ContextCompat.getColor(this, R.color.oColor));

        binding.easyLevelCard.setOnClickListener(view -> {
            startActivity(new Intent(this, MainActivity.class)
                    .putExtra("level_type", "easy_level"));
        });

        binding.mediumLevelCard.setOnClickListener(view -> {
            startActivity(new Intent(this, MainActivity.class)
                    .putExtra("level_type", "medium_level"));
        });

        binding.hardLevelCard.setOnClickListener(view -> {
            startActivity(new Intent(this, MainActivity.class)
                    .putExtra("level_type", "hard_level"));
        });
    }

    private void handleCardClick(MaterialCardView selectedCard, TextView selectedText) {
        // Reset all cards and texts
        resetCards();

        // Highlight the selected card and show the corresponding text
        selectedCard.setBackgroundResource(R.drawable.level_card_bg); // Change to your desired color
        selectedText.setVisibility(View.VISIBLE);
    }

    private void resetCards() {
        // Reset all cards to white
        binding.easyLevelCard.setBackgroundResource(R.drawable.level_card_bg);;
        binding.mediumLevelCard.setBackgroundResource(R.drawable.level_card_bg);
        binding.hardLevelCard.setBackgroundResource(R.drawable.level_card_bg);

        // Hide all text views
//        text1.setVisibility(View.GONE);
//        text2.setVisibility(View.GONE);
//        text3.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Restrict ads to 13+ audience
        RequestConfiguration requestConfiguration = new RequestConfiguration.Builder()
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_T)
                .build();

        MobileAds.initialize(this, initializationStatus -> {});
        MobileAds.setRequestConfiguration(requestConfiguration);
//        loadInterstitialAd();

        //load ads
        AdRequest adRequest = new AdRequest.Builder().build();
        binding.adView.loadAd(adRequest);
    }
}