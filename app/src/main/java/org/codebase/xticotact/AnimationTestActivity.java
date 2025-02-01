package org.codebase.xticotact;

import android.animation.Animator;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.codebase.xticotact.databinding.ActivityAnimationTestBinding;

public class AnimationTestActivity extends AppCompatActivity {

    private ActivityAnimationTestBinding binding;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnimationTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Load sound
        mediaPlayer = MediaPlayer.create(this, R.raw.hurrah); // Place sound in res/raw

        binding.showHurrahBtn.setOnClickListener(view -> showHurrahAnimation());
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

//    private void showHurrahEffect() {
//        // Play Sound
//        if (mediaPlayer != null) {
//            mediaPlayer.start();
//        }
//
//        Party party = new Party(
//                270, // Confetti shoots upwards
//                360, // Full circle spread
//                10f, // Minimum speed of confetti
//                30f, // Maximum speed of confetti
//                0.9f, // Slows down confetti
//                List.of(Size.Companion.getSMALL(), Size.Companion.getMEDIUM(), Size.Companion.getLARGE()), // Different confetti sizes
//                Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def), // Confetti colors
//                Arrays.asList(Shape.Square.INSTANCE, Shape.Circle.INSTANCE), // Confetti shapes
//                2000, // Time before confetti disappears
//                true,
//                new Position.Relative(0.5, 1.0), // Center-bottom position
//                0,
//                new Rotation(),
//                new EmitterConfig(new Emitter(100, TimeUnit.MILLISECONDS)).max(300)
//
//
//        );
//
//        // Run on UI thread to avoid crashes
//        runOnUiThread(() -> binding.konfettiView.start(party));
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}