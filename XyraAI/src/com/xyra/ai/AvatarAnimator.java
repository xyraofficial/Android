package com.xyra.ai;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;

public class AvatarAnimator {
    
    private View avatarView;
    private AnimatorSet currentAnimation;
    private boolean isAnimating;
    
    public AvatarAnimator(View avatarView) {
        this.avatarView = avatarView;
        this.isAnimating = false;
    }
    
    public void startThinkingAnimation() {
        if (avatarView == null || isAnimating) return;
        
        stopAnimation();
        isAnimating = true;
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(avatarView, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(avatarView, "scaleY", 1f, 1.1f, 1f);
        ObjectAnimator rotateY = ObjectAnimator.ofFloat(avatarView, "rotationY", 0f, 10f, -10f, 0f);
        
        currentAnimation = new AnimatorSet();
        currentAnimation.playTogether(scaleX, scaleY, rotateY);
        currentAnimation.setDuration(800);
        currentAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        
        currentAnimation.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (isAnimating && avatarView != null) {
                    currentAnimation.start();
                }
            }
        });
        
        currentAnimation.start();
    }
    
    public void startTypingAnimation() {
        if (avatarView == null || isAnimating) return;
        
        stopAnimation();
        isAnimating = true;
        
        ObjectAnimator bounceY = ObjectAnimator.ofFloat(avatarView, "translationY", 0f, -15f, 0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(avatarView, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(avatarView, "scaleY", 1f, 1.05f, 1f);
        
        currentAnimation = new AnimatorSet();
        currentAnimation.playTogether(bounceY, scaleX, scaleY);
        currentAnimation.setDuration(600);
        currentAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        
        currentAnimation.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (isAnimating && avatarView != null) {
                    currentAnimation.start();
                }
            }
        });
        
        currentAnimation.start();
    }
    
    public void startWaveAnimation() {
        if (avatarView == null) return;
        
        stopAnimation();
        
        ObjectAnimator wave = ObjectAnimator.ofFloat(avatarView, "rotation", 0f, 15f, -15f, 10f, -10f, 0f);
        wave.setDuration(1000);
        wave.setInterpolator(new AccelerateDecelerateInterpolator());
        wave.start();
    }
    
    public void startBounceAnimation() {
        if (avatarView == null) return;
        
        stopAnimation();
        
        ObjectAnimator bounce = ObjectAnimator.ofFloat(avatarView, "translationY", 0f, -30f, 0f);
        bounce.setDuration(500);
        bounce.setInterpolator(new BounceInterpolator());
        bounce.start();
    }
    
    public void stopAnimation() {
        isAnimating = false;
        if (currentAnimation != null) {
            currentAnimation.cancel();
            currentAnimation = null;
        }
        if (avatarView != null) {
            avatarView.setScaleX(1f);
            avatarView.setScaleY(1f);
            avatarView.setTranslationY(0f);
            avatarView.setRotation(0f);
            avatarView.setRotationY(0f);
        }
    }
    
    public boolean isAnimating() {
        return isAnimating;
    }
}
