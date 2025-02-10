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
    private int gridSize = 5; // Change to 5 or 7 for larger boards

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

        initializeBoard(gridSize);
        // Set initial status
        binding.statusTextView.setText("X's Turn");

        // Set restart button listener
        binding.restartButton.setOnClickListener(v -> initializeBoard(gridSize));

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

        // Fixed width and height in pixels (350dp x 400dp)
        int gridWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350, getResources().getDisplayMetrics());
        int gridHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 400, getResources().getDisplayMetrics());

        // Calculate cell size based on fixed GridLayout size
        int cellSize = Math.min(gridWidth, gridHeight) / gridSize;

        // Apply fixed dimensions to GridLayout
        ViewGroup.LayoutParams gridParams = gridLayout.getLayoutParams();
        gridParams.width = gridWidth;
        gridParams.height = gridHeight;
        gridLayout.setLayoutParams(gridParams);

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                gameState[i][j] = 2;

                ImageView cell = new ImageView(this);
                cell.setAdjustViewBounds(true);
                cell.setScaleType(ImageView.ScaleType.CENTER_CROP);
                cell.setImageResource(android.R.color.transparent);
                cell.setBackgroundColor(Color.WHITE);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize;
                params.height = cellSize;
                params.setMargins(1, 1, 1, 1);
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

        // Rows
        for (int row = 0; row < gridSize; row++) {
            int[] winRow = new int[gridSize];
            for (int col = 0; col < gridSize; col++) {
                winRow[col] = row * gridSize + col;
            }
            winPositions.add(winRow);
        }

        // Columns
        for (int col = 0; col < gridSize; col++) {
            int[] winColumn = new int[gridSize];
            for (int row = 0; row < gridSize; row++) {
                winColumn[row] = row * gridSize + col;
            }
            winPositions.add(winColumn);
        }

        // Primary Diagonal
        int[] primaryDiagonal = new int[gridSize];
        for (int i = 0; i < gridSize; i++) {
            primaryDiagonal[i] = i * gridSize + i;
        }
        winPositions.add(primaryDiagonal);

        // Secondary Diagonal
        int[] secondaryDiagonal = new int[gridSize];
        for (int i = 0; i < gridSize; i++) {
            secondaryDiagonal[i] = i * gridSize + (gridSize - 1 - i);
        }
        winPositions.add(secondaryDiagonal);

        return winPositions;
    }


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
                binding.statusTextView.setText(winnerStr);
                gameActive = false;
                highlightWinningCells(winPosition);
                applyWinAnimation();
                return;
            }
        }
    }


//    public void playerTap(View view) {
//        if (!gameActive) {
//            gameReset();
//            return;
//        }
//
//        ImageView img = (ImageView) view;
//        int tappedImage = Integer.parseInt(img.getTag().toString());
//
//        // If the tapped image is empty
//        if (gameState[tappedImage] == 2) {
//            // Mark this position
//            gameState[tappedImage] = activePlayer;
//            counter++;
//
//            // Set the image and animate
//            if (activePlayer == 0) {
//                img.setImageResource(R.drawable.x);
//                img.setImageTintList(ContextCompat.getColorStateList(this, R.color.xColor));
//                binding.statusTextView.setText("O's Turn");
//                activePlayer = 1;
//            } else {
//                img.setImageResource(R.drawable.o);
//                img.setImageTintList(ContextCompat.getColorStateList(this, R.color.oColor));
//                binding.statusTextView.setText("X's Turn");
//                activePlayer = 0;
//            }
//
//            // Animation: Slide in from top
//            img.setTranslationY(-1000f);
//            img.animate().translationYBy(1000f).setDuration(300).start();
//
//            // Check for a win or draw
//            if (counter >= 5) {
//                checkWin();
//            }
//
//            if (counter == 9 && gameActive) {
//                // Draw scenario
//                binding.statusTextView.setText("It's a Draw!");
//                applyDrawAnimation();
//                gameActive = false;
//            }
//        }
//    }
//
//    private void checkWin() {
//        for (int[] winPosition : winPositions) {
//            if (gameState[winPosition[0]] == gameState[winPosition[1]] &&
//                    gameState[winPosition[1]] == gameState[winPosition[2]] &&
//                    gameState[winPosition[0]] != 2) {
//
//                // Somebody has won! - Find out who!
//                String winnerStr = (gameState[winPosition[0]] == 0) ? "X has won!" : "O has won!";
//                showHurrahAnimation();
//
//                if (winnerStr.equals("X has won!")) {
//                    wonCount++;
////                    Toast.makeText(this, "x : "+ wonCount, Toast.LENGTH_SHORT).show();
//                    if (wonCount == 2) {
//                        wonCount = 0;
//                        new Handler().postDelayed(this::showInterstitialAd, 2000);
//
//                    }
//                } else {
//                    wonCount--;
//                    if (wonCount == -2) {
//                        wonCount = 0;
//                        new Handler().postDelayed(this::showInterstitialAd, 2000);
//                    }
//                }
//
//
//                // Update the status bar for winner announcement
//                binding.statusTextView.setText(winnerStr);
//                gameActive = false;
//
//                // Highlight winning positions
//                highlightWinningCells(winPosition);
//
//                // Apply winning animation
//                applyWinAnimation();
//
//                return;
//            }
//        }
//    }

    private void highlightWinningCells(int[] winPosition) {
        for (int pos : winPosition) {
            int row = pos / gridSize;
            int col = pos % gridSize;
            ImageView img = (ImageView) gridLayout.getChildAt(row * gridSize + col);
            img.setBackgroundColor(ContextCompat.getColor(this, R.color.winnerColor));
        }
    }


    private ImageView getImageViewByTag(int tag) {
        switch (tag) {
            case 0:
                return binding.imageView0;
            case 1:
                return binding.imageView1;
            case 2:
                return binding.imageView2;
            case 3:
                return binding.imageView3;
            case 4:
                return binding.imageView4;
            case 5:
                return binding.imageView5;
            case 6:
                return binding.imageView6;
            case 7:
                return binding.imageView7;
            case 8:
                return binding.imageView8;
            default:
                return null;
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

    private void applyDrawAnimation() {
        Animation drawAnim = AnimationUtils.loadAnimation(this, R.anim.draw_animation);
        binding.gridLayout.startAnimation(drawAnim);
    }

    private void gameReset() {
        // Set game state back to the default
        gameActive = true;
        activePlayer = 0;

        // Stop the draw animation if it is running
        binding.gridLayout.clearAnimation();  // This will stop the blinking animation

        // Reset all game positions to empty (2 means empty)
        Arrays.fill(gameState, 2);

        // Reset the counter to track turns
        counter = 0;

        // Stop any blinking animations on the ImageViews and reset background colors
        resetImageViews();

        // Reset all the images in the grid to be empty
        binding.imageView0.setImageResource(0);
        binding.imageView1.setImageResource(0);
        binding.imageView2.setImageResource(0);
        binding.imageView3.setImageResource(0);
        binding.imageView4.setImageResource(0);
        binding.imageView5.setImageResource(0);
        binding.imageView6.setImageResource(0);
        binding.imageView7.setImageResource(0);
        binding.imageView8.setImageResource(0);

        // Update the status text to show it's X's turn
        binding.statusTextView.setText("X's Turn - Tap to play");

        // Ensure the grid layout stays visible after resetting
        binding.gridLayout.setVisibility(View.VISIBLE);
    }

    private void resetImageViews() {
        // Reset all image views by stopping animations and setting background color to white
        ImageView[] imageViews = {
                binding.imageView0, binding.imageView1, binding.imageView2,
                binding.imageView3, binding.imageView4, binding.imageView5,
                binding.imageView6, binding.imageView7, binding.imageView8
        };

        for (ImageView imageView : imageViews) {
            // Stop any ongoing animations
            imageView.clearAnimation();

            // Reset the background color to white
            imageView.setBackgroundColor(getResources().getColor(android.R.color.white));
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