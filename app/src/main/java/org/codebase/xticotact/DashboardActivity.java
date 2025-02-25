package org.codebase.xticotact;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
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

    private String levelText = "";
    private String personText = "";

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
//            binding.easyLayout.setBackgroundResource(R.drawable.level_selction_bg);
            handleCardClick(binding.easyLayout);
            levelText = "easy_level";
//            startActivity(new Intent(this, MainActivity.class)
//                    .putExtra("level_type", "easy_level"));
        });

        binding.mediumLevelCard.setOnClickListener(view -> {
            handleCardClick(binding.mediumLayout);
            levelText = "medium_level";

//            startActivity(new Intent(this, MainActivity.class)
//                    .putExtra("level_type", "medium_level"));
        });

        binding.hardLevelCard.setOnClickListener(view -> {
            handleCardClick(binding.hardLayout);
            levelText = "hard_level";

        });

        binding.p2rCard.setOnClickListener(view -> {
            handlePersonClick(binding.p2rLayout);
            personText = "p2r";
        });

        binding.p2pCard.setOnClickListener(view -> {
            handlePersonClick(binding.p2pLayout);
            personText = "p2p";
        });
        
        binding.playGame.setOnClickListener(view -> {

            startActivity(new Intent(this, MainActivity.class)
                    .putExtra("level_type", levelText)
                    .putExtra("person_type", personText));
        });
    }

    private void handleCardClick(LinearLayout selectedCard) {
        // Reset all cards and texts
        resetCards();

        // Highlight the selected card and show the corresponding text
        selectedCard.setBackgroundResource(R.drawable.level_selction_bg); // Change to your desired color
//        selectedText.setVisibility(View.VISIBLE);
    }

    private void handlePersonClick(LinearLayout selectedCard) {
        // Reset all cards and texts
        resetPersonCards();

        // Highlight the selected card and show the corresponding text
        selectedCard.setBackgroundResource(R.drawable.level_selction_bg); // Change to your desired color
//        selectedText.setVisibility(View.VISIBLE);
    }

    private void resetCards() {
        // Reset all cards to white
        binding.easyLayout.setBackgroundResource(R.drawable.level_card_bg);;
        binding.mediumLayout.setBackgroundResource(R.drawable.level_card_bg);
        binding.hardLayout.setBackgroundResource(R.drawable.level_card_bg);

        // Hide all text views
//        text1.setVisibility(View.GONE);
//        text2.setVisibility(View.GONE);
//        text3.setVisibility(View.GONE);
    }

    private void resetPersonCards() {
        // Reset all cards to white
        binding.p2rLayout.setBackgroundResource(R.drawable.level_card_bg);;
        binding.p2pLayout.setBackgroundResource(R.drawable.level_card_bg);

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