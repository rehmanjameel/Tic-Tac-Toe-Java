package org.codebase.xticotact;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import android.animation.Animator;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.codebase.xticotact.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAnalytics mFirebaseAnalytics;
    private GridLayout gridLayout;
    private boolean gameActive = true;
    private MediaPlayer mediaPlayer;
    private InterstitialAd mInterstitialAd;
    private int gridSize = 3; // Change to 5 or 7 for larger boards

    private int wonCount = 0;
    // Player representation
    // 0 - X
    // 1 - O
    private int activePlayer = 0;
//    private int[] gameState = {2, 2, 2, 2, 2, 2, 2, 2, 2};
    private int[][] gameState;

    // Win positions
    private final int[][] winPositions3x3 = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // Rows
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // Columns
            {0, 4, 8}, {2, 4, 6}             // Diagonals
    };

    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // Initialize ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        gridLayout = binding.gridLayouts;

        String gameLevelType = getIntent().getStringExtra("level_type");
        if (gameLevelType!= null && gameLevelType.equals("easy_level")) {
            gridSize = 3;
        } else if (gameLevelType!= null && gameLevelType.equals("medium_level")) {
            gridSize = 5;
        } else if (gameLevelType!= null && gameLevelType.equals("hard_level")) {
            gridSize = 7;
        }

        binding.titleTextView.setShadowLayer(10, 5,5, ContextCompat.getColor(this, R.color.oColor));
        binding.statusTextView.setShadowLayer(10, 5,5, ContextCompat.getColor(this, R.color.oColor));

        initializeBoard(gridSize);
        // Set initial status
        binding.statusTextView.setText("X's Turn");

        // Set restart button listener
        binding.restartButton.setOnClickListener(v -> initializeBoard(gridSize));

        // Set gridlayout to restart the game
        binding.gridLayout.setOnClickListener(view -> initializeBoard(gridSize));

        // Load sound
        mediaPlayer = MediaPlayer.create(this, R.raw.hurrah); // Place sound in res/raw

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "name");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    private void initializeBoard(int size) {
        gridSize = size;
        gameActive = true;
        activePlayer = 0;
        counter = 0;
        binding.statusTextView.setText("X's Turn");

        gridLayout.removeAllViews();
        gridLayout.setColumnCount(gridSize);
        gridLayout.setRowCount(gridSize);

        gameState = new int[gridSize][gridSize];

        // Fixed GridLayout width and height in dp
        int totalGridWidthDp = 350;  // Full grid width including margins
        int totalGridHeightDp = 400; // Full grid height including margins
        int cellAreaWidthDp = 315;   // **Only for cells, margins will use remaining space**
        int cellAreaHeightDp = 370;  // **Height reserved for cells**
        int marginDp = 2;            // Small margin

        // Convert dp to pixels
        int totalGridWidthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, totalGridWidthDp, getResources().getDisplayMetrics());
        int totalGridHeightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, totalGridHeightDp, getResources().getDisplayMetrics());
        int cellAreaWidthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cellAreaWidthDp, getResources().getDisplayMetrics());
        int cellAreaHeightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cellAreaHeightDp, getResources().getDisplayMetrics());
        int marginPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginDp, getResources().getDisplayMetrics());

        // Apply fixed dimensions to GridLayout and center it
        ViewGroup.LayoutParams gridParams = gridLayout.getLayoutParams();
        gridParams.width = totalGridWidthPx;
        gridParams.height = totalGridHeightPx;
        gridLayout.setLayoutParams(gridParams);

        // **Fix both column & row overflow issues**:
        int totalMarginWidth = (gridSize - 1) * marginPx;
        int totalMarginHeight = (gridSize - 1) * marginPx;
        int cellWidth = (cellAreaWidthPx - totalMarginWidth) / gridSize;
        int cellHeight = (cellAreaHeightPx - totalMarginHeight) / gridSize; // Now height is also fixed properly

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                gameState[i][j] = 2;

                ImageView cell = new ImageView(this);
                cell.setAdjustViewBounds(true);
                cell.setPadding(2, 2, 2, 2);
                cell.setScaleType(ImageView.ScaleType.CENTER_CROP);
                cell.setImageResource(android.R.color.transparent);
                cell.setBackgroundColor(Color.WHITE);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellWidth;
                params.height = cellHeight;
                params.setMargins(marginPx, marginPx, marginPx, marginPx);
                cell.setLayoutParams(params);

                final int finalI = i;
                final int finalJ = j;
                cell.setOnClickListener(v -> playerTap(finalI, finalJ, cell));

                gridLayout.addView(cell);
            }
        }
    }


    private List<int[]> generateWinPositions(int gridSize) {
        List<int[]> winPositions = new ArrayList<>();

        int winCondition = (gridSize == 5) ? 4 : (gridSize == 7) ? 5
                : (gridSize == 9) ? 6 : gridSize; // 4 for 5x5, 5 for 7x7, full for 3x3

        // Rows
        for (int row = 0; row < gridSize; row++) {
            for (int start = 0; start <= gridSize - winCondition; start++) {
                int[] winRow = new int[winCondition];
                for (int i = 0; i < winCondition; i++) {
                    winRow[i] = row * gridSize + (start + i);
                }
                winPositions.add(winRow);
            }
        }

        // Columns
        for (int col = 0; col < gridSize; col++) {
            for (int start = 0; start <= gridSize - winCondition; start++) {
                int[] winColumn = new int[winCondition];
                for (int i = 0; i < winCondition; i++) {
                    winColumn[i] = (start + i) * gridSize + col;
                }
                winPositions.add(winColumn);
            }
        }

        // Primary Diagonal (\ direction)
        for (int row = 0; row <= gridSize - winCondition; row++) {
            for (int col = 0; col <= gridSize - winCondition; col++) {
                int[] primaryDiagonal = new int[winCondition];
                for (int i = 0; i < winCondition; i++) {
                    primaryDiagonal[i] = (row + i) * gridSize + (col + i);
                }
                winPositions.add(primaryDiagonal);
            }
        }

        // Secondary Diagonal (/ direction)
        for (int row = 0; row <= gridSize - winCondition; row++) {
            for (int col = winCondition - 1; col < gridSize; col++) {
                int[] secondaryDiagonal = new int[winCondition];
                for (int i = 0; i < winCondition; i++) {
                    secondaryDiagonal[i] = (row + i) * gridSize + (col - i);
                }
                winPositions.add(secondaryDiagonal);
            }
        }

        return winPositions;
    }

    // method to apply the players X or O in board
    private void playerTap(int row, int col, ImageView img) {
        if (!gameActive || gameState[row][col] != 2) return;

        // Update state
        gameState[row][col] = activePlayer;
        counter++;

        // Set Image and Toggle Player
        if (activePlayer == 0) {
            img.setImageResource(R.drawable.x);
            img.setImageTintList(ContextCompat.getColorStateList(this, R.color.xColor));
            binding.statusTextView.setText("O's Turn");
            activePlayer = 1;
        } else {
            img.setImageResource(R.drawable.o);
            img.setImageTintList(ContextCompat.getColorStateList(this, R.color.oColor));
            binding.statusTextView.setText("X's Turn");
            activePlayer = 0;
        }

        // Animation
        img.setTranslationY(-1000f);
        img.animate().translationYBy(1000f).setDuration(300).start();

        // Check for win or draw
        if (counter >= gridSize) checkWin();
        if (counter == gridSize * gridSize && gameActive) {
            binding.statusTextView.setText("It's a Draw!");
            gameActive = false;
        }
    }

    private void checkWin() {
        List<int[]> winPositions = generateWinPositions(gridSize);

        for (int[] winPosition : winPositions) {
            int firstCell = gameState[winPosition[0] / gridSize][winPosition[0] % gridSize];
            boolean win = true;

            for (int i = 1; i < winPosition.length; i++) {
                int row = winPosition[i] / gridSize;
                int col = winPosition[i] % gridSize;
                if (gameState[row][col] != firstCell || firstCell == 2) {
                    win = false;
                    break;
                }
            }

            if (win) {
                String winnerStr = (firstCell == 0) ? "X has won!" : "O has won!";
                if (winnerStr.equals("X has won!")) {
                    wonCount++;
                    if (wonCount == 1) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showInterstitialAd();
                            }
                        }, 1500);
                    }
                } else {
                    wonCount--;
                    if (wonCount == -2) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showInterstitialAd();
                            }
                        }, 1500);
                    }
                }
                binding.statusTextView.setText(winnerStr);
                gameActive = false;
                highlightWinningCells(winPosition);
                applyWinAnimation();
                showHurrahAnimation();
                return;
            }
        }
    }

    private void highlightWinningCells(int[] winPosition) {
        for (int pos : winPosition) {
            int row = pos / gridSize;
            int col = pos % gridSize;
            ImageView img = (ImageView) gridLayout.getChildAt(row * gridSize + col);
            img.setBackgroundColor(ContextCompat.getColor(this, R.color.winnerColor));
        }
    }

    private void applyWinAnimation() {
        Animation winAnim = AnimationUtils.loadAnimation(this, R.anim.win_animation);

        for (int row = 0; row < gameState.length; row++) {
            for (int col = 0; col < gameState[row].length; col++) {
                if (gameState[row][col] != 2) { // Check if cell is occupied
                    int position = row * gridSize + col; // Convert 2D index to 1D position
                    ImageView img = (ImageView) gridLayout.getChildAt(position);
                    if (img != null) {
                        img.startAnimation(winAnim);
                    }
                }
            }
        }
    }

    private void showHurrahAnimation() {
        binding.hurrahAnimation.setVisibility(View.VISIBLE);
        binding.hurrahAnimation.playAnimation();

        if (mediaPlayer != null) {
            mediaPlayer.start();
        }

        // Hide animation after completion
        binding.hurrahAnimation.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                binding.hurrahAnimation.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
    }


    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, getString(R.string.interstitial_ad_id), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        Log.d("AdMob", "Interstitial ad loaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        Log.d("AdMob", "Interstitial ad failed: " + adError.getMessage());
                        mInterstitialAd = null;
                    }
                });
    }

    private void showInterstitialAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(this);
        } else {
            Log.d("AdMob", "Interstitial ad is not ready yet.");
        }
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
        loadInterstitialAd();

        //load ads
        AdRequest adRequest = new AdRequest.Builder().build();
        binding.adView.loadAd(adRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}