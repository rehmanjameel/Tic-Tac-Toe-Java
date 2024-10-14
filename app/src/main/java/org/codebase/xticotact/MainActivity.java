package org.codebase.xticotact;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;


import org.codebase.xticotact.databinding.ActivityMainBinding;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private boolean gameActive = true;

    // Player representation
    // 0 - X
    // 1 - O
    private int activePlayer = 0;
    private int[] gameState = {2, 2, 2, 2, 2, 2, 2, 2, 2};

    // Win positions
    private final int[][] winPositions = {
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

        // Set initial status
        binding.statusTextView.setText("X's Turn");

        // Set restart button listener
        binding.restartButton.setOnClickListener(v -> gameReset());
    }

    public void playerTap(View view) {
        if (!gameActive) {
            gameReset();
            return;
        }

        ImageView img = (ImageView) view;
        int tappedImage = Integer.parseInt(img.getTag().toString());

        // If the tapped image is empty
        if (gameState[tappedImage] == 2) {
            // Mark this position
            gameState[tappedImage] = activePlayer;
            counter++;

            // Set the image and animate
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

            // Animation: Slide in from top
            img.setTranslationY(-1000f);
            img.animate().translationYBy(1000f).setDuration(300).start();

            // Check for a win or draw
            if (counter >= 5) {
                checkWin();
            }

            if (counter == 9 && gameActive) {
                // Draw scenario
                binding.statusTextView.setText("It's a Draw!");
                applyDrawAnimation();
                gameActive = false;
            }
        }
    }

    private void checkWin() {
        for (int[] winPosition : winPositions) {
            if (gameState[winPosition[0]] == gameState[winPosition[1]] &&
                    gameState[winPosition[1]] == gameState[winPosition[2]] &&
                    gameState[winPosition[0]] != 2) {

                // Somebody has won! - Find out who!
                String winnerStr = (gameState[winPosition[0]] == 0) ? "X has won!" : "O has won!";

                // Update the status bar for winner announcement
                binding.statusTextView.setText(winnerStr);
                gameActive = false;

                // Highlight winning positions
                highlightWinningCells(winPosition);

                // Apply winning animation
                applyWinAnimation();

                return;
            }
        }
    }

    private void highlightWinningCells(int[] winPosition) {
        for (int pos : winPosition) {
            ImageView img = getImageViewByTag(pos);
            if (img != null) {
                img.setBackground(ContextCompat.getDrawable(this, R.drawable.winning_cell_background));
            }
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
        for (int i = 0; i < gameState.length; i++) {
            if (gameState[i] != 2) {
                ImageView img = getImageViewByTag(i);
                if (img != null) {
                    img.startAnimation(winAnim);
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

}