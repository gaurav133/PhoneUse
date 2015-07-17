/*
 * Copyright (C) 2013 47 Degrees, LLC
 * http://47deg.com
 * hello@47deg.com
 *
 * Copyright 2012 Roman Nurik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asgj.android.appusage.ui.widgets.listview;

import static com.nineoldandroids.view.ViewHelper.setAlpha;
import static com.nineoldandroids.view.ViewHelper.setTranslationX;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.graphics.Rect;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ListView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

/**
 * Touch listener impl for the SwipeListView
 */
public class SwipeListViewTouchListener implements View.OnTouchListener {


    private int swipeMode = SwipeListView.SWIPE_MODE_RIGHT;

    private int swipeFrontView = 0;
    private int swipeBackView = 0;

    private Rect rect = new Rect();

    // Cached ViewConfiguration and system-wide constant values
    private int slop;
    private int minFlingVelocity;
    private int maxFlingVelocity;
    private long configShortAnimationTime;
    private long animationTime;

    private float leftOffset = 0;
    private float rightOffset = 0;

    private int swipeDrawableChecked = 0;
    private int swipeDrawableUnchecked = 0;

    // Fixed properties
    private SwipeListView swipeListView;
    private int viewWidth = 1; // 1 and not 0 to prevent dividing by zero

    private List<PendingDismissData> pendingDismisses = new ArrayList<PendingDismissData>();
    private int dismissAnimationRefCount = 0;

    private float downX;
    private boolean swiping;
    private boolean swipingRight;
    private VelocityTracker velocityTracker;
    private int downPosition;
    private View frontView;
    private View backView;
    private boolean paused;

    private int swipeCurrentAction = SwipeListView.SWIPE_ACTION_REVEAL;

    private int swipeActionLeft = SwipeListView.SWIPE_ACTION_REVEAL;
    private int swipeActionRight = SwipeListView.SWIPE_ACTION_REVEAL;

    private boolean listViewMoving;
    private List<Boolean> checked = new ArrayList<Boolean>();
    private int oldSwipeActionRight;
    private int oldSwipeActionLeft;
    
    public OnItemSwiped mSwipeListener = null;
    
    public void setOnItemSwiped(OnItemSwiped swipeListener){
    	mSwipeListener = swipeListener;
    }
    
    public interface OnItemSwiped {
    	public void onItemSwiped(int position);
    }

    /**
     * Constructor
     *
     * @param swipeListView  SwipeListView
     * @param swipeFrontView front view Identifier
     * @param swipeBackView  back view Identifier
     */
    public SwipeListViewTouchListener(SwipeListView swipeListView, int swipeFrontView, int swipeBackView) {
        this.swipeFrontView = swipeFrontView;
        this.swipeBackView = swipeBackView;
        ViewConfiguration vc = ViewConfiguration.get(swipeListView.getContext());
        slop = vc.getScaledTouchSlop();
        minFlingVelocity = vc.getScaledMinimumFlingVelocity();
        maxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        configShortAnimationTime = swipeListView.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
        animationTime = configShortAnimationTime;
        this.swipeListView = swipeListView;
    }

    /**
     * Sets current item's parent view
     *
     * @param parentView Parent view
     */

    /**
     * Sets current item's front view
     *
     * @param frontView Front view
     */
    private void setFrontView(View frontView, final int childPosition) {
        this.frontView = frontView;
   
}

    /**
     * Set current item's back view
     *
     * @param backView
     */
    private void setBackView(View backView) {
        this.backView = backView;
    }

    /**
     * @return true if the list is in motion
     */
    public boolean isListViewMoving() {
        return listViewMoving;
    }

    /**
     * Sets animation time when the user drops the cell
     *
     * @param animationTime milliseconds
     */
    public void setAnimationTime(long animationTime) {
        if (animationTime > 0) {
            this.animationTime = animationTime;
        } else {
            this.animationTime = configShortAnimationTime;
        }
    }

    /**
     * Sets the right offset
     *
     * @param rightOffset Offset
     */
    public void setRightOffset(float rightOffset) {
        this.rightOffset = rightOffset;
    }

    /**
     * Set the left offset
     *
     * @param leftOffset Offset
     */
    public void setLeftOffset(float leftOffset) {
        this.leftOffset = leftOffset;
    }

    /**
     * Sets the swipe mode
     *
     * @param swipeMode
     */
    public void setSwipeMode(int swipeMode) {
        this.swipeMode = swipeMode;
    }

    /**
     * Check is swiping is enabled
     *
     * @return
     */
    protected boolean isSwipeEnabled() {
        return true;
    }

    /**
     * Return action on left
     *
     * @return Action
     */
    public int getSwipeActionLeft() {
        return swipeActionLeft;
    }

    /**
     * Set action on left
     *
     * @param swipeActionLeft Action
     */
    public void setSwipeActionLeft(int swipeActionLeft) {
        this.swipeActionLeft = swipeActionLeft;
    }

    /**
     * Return action on right
     *
     * @return Action
     */
    public int getSwipeActionRight() {
        return swipeActionRight;
    }

    /**
     * Set action on right
     *
     * @param swipeActionRight Action
     */
    public void setSwipeActionRight(int swipeActionRight) {
        this.swipeActionRight = swipeActionRight;
    }

    /**
     * Set drawable checked (only SWIPE_ACTION_CHOICE)
     *
     * @param swipeDrawableChecked drawable
     */
    protected void setSwipeDrawableChecked(int swipeDrawableChecked) {
        this.swipeDrawableChecked = swipeDrawableChecked;
    }

    /**
     * Set drawable unchecked (only SWIPE_ACTION_CHOICE)
     *
     * @param swipeDrawableUnchecked drawable
     */
    protected void setSwipeDrawableUnchecked(int swipeDrawableUnchecked) {
        this.swipeDrawableUnchecked = swipeDrawableUnchecked;
    }

    /**
     * Adds new items when adapter is modified
     */

    /**
     * Open item
     *
     * @param position Position of list
     */

    /**
     * Close item
     *
     * @param position Position of list
     */
//    protected void closeAnimate(int position) {
//        if (swipeListView != null) {
//            int firstVisibleChildPosition = swipeListView.getFirstVisiblePosition();
//            final View childContainer = swipeListView.getChildAt(position - firstVisibleChildPosition);
//            if (childContainer != null) {
//                final View child = childContainer.findViewById(swipeFrontView);
//
//                if (child != null) {
//                    closeAnimate(child, position);
//                }
//            }
//        }
//    }

    /**
     * Swap choice state in item
     *
     * @param position position of list
//     */
//    private void swapChoiceState(int position) {
//        int lastCount = getCountSelected();
//        boolean lastChecked = checked.get(position);
//        checked.set(position, !lastChecked);
//        int count = lastChecked ? lastCount - 1 : lastCount + 1;
//        if (lastCount == 0 && count == 1) {
////            swipeListView.onChoiceStarted();
//            closeOpenedItems();
//            setActionsTo(SwipeListView.SWIPE_ACTION_REVEAL);
//        }
//        if (lastCount == 1 && count == 0) {
////            swipeListView.onChoiceEnded();
//            returnOldActions();
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            swipeListView.setItemChecked(position, !lastChecked);
//        }
////        swipeListView.onChoiceChanged(position, !lastChecked);
//        reloadChoiceStateInView(frontView, position);
//    }

    /**
     * Unselected choice state in item
     */

    /**
     * Dismiss an item.
     * @param position is the position of the item to delete.
     * @return 0 if the item is not visible. Otherwise return the height of the cell to dismiss.
     */

    /**
     * Draw cell for display if item is selected or not
     *
     * @param frontView view to draw
     * @param position  position in list
     */
    protected void reloadChoiceStateInView(View frontView, int position) {
        if (isChecked(position)) {
            if (swipeDrawableChecked > 0) frontView.setBackgroundResource(swipeDrawableChecked);
        } else {
            if (swipeDrawableUnchecked > 0) frontView.setBackgroundResource(swipeDrawableUnchecked);
        }
    }

    /**
     * Reset the state of front view when the it's recycled by ListView
     *
     * @param frontView view to re-draw
     */
    protected void reloadSwipeStateInView(View frontView, int position) {
        setTranslationX(frontView, swipeListView.getWidth());
    }

    /**
     * Get if item is selected
     *
     * @param position position in list
     * @return
     */
    protected boolean isChecked(int position) {
        return position < checked.size() && checked.get(position);
    }

    /**
     * Count selected
     *
     * @return
     */
    protected int getCountSelected() {
        int count = 0;
        for (int i = 0; i < checked.size(); i++) {
            if (checked.get(i)) {
                count++;
            }
        }
        if(SwipeListView.DEBUG){
            Log.d(SwipeListView.TAG, "selected: " + count);
        }
        return count;
    }

    /**
     * Get positions selected
     *
     * @return
     */
    protected List<Integer> getPositionsSelected() {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < checked.size(); i++) {
            if (checked.get(i)) {
                list.add(i);
            }
        }
        return list;
    }


    /**
     * Close item
     *
     * @param view     affected view
     * @param position Position of list
     */
//    private void closeAnimate(View view, int position) {
//            generateRevealAnimate(view, true, false, position);
//    }

    /**
     * Create animation
     *
     * @param view      affected view
     * @param swap      If state should change. If "false" returns to the original position
     * @param swapRight If swap is true, this parameter tells if move is to the right or left
     * @param position  Position of list
     */
    private void generateAnimate(final View view, final boolean swap, final boolean swapRight, final int position,final float deltaX) {
        if(SwipeListView.DEBUG){
            Log.d(SwipeListView.TAG, "swap: " + swap + " - swapRight: " + swapRight + " - position: " + position);
        }
        if (swipeCurrentAction == SwipeListView.SWIPE_ACTION_REVEAL) {
            generateRevealAnimate(view, swap, swapRight, position,deltaX);
        }
    }


    /**
     * Create reveal animation
     *
     * @param view      affected view
     * @param swap      If will change state. If "false" returns to the original position
     * @param swapRight If swap is true, this parameter tells if movement is toward right or left
     * @param position  list position
     */
    private void generateRevealAnimate(final View view, final boolean swap, final boolean swapRight, final int position,final float deltaX) {
        int moveTo = 0;
       
            if (swap) {
                moveTo = swapRight ? (int) (viewWidth - rightOffset) : (int) (-viewWidth + leftOffset);
            }

        animate(view)
                .translationX(viewWidth)
                .setDuration(400)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                    	if(mSwipeListener != null){
                    		mSwipeListener.onItemSwiped(downPosition);
                    	}
                    	
                        resetCell();
                    }
                });
    }

    private void resetCell() {
        if (downPosition != ListView.INVALID_POSITION) {
            if (swipeCurrentAction == SwipeListView.SWIPE_ACTION_REVEAL) {
                backView.setVisibility(View.VISIBLE);
            }
            frontView.setTranslationX(0);
            frontView.setClickable(true);
            frontView.setLongClickable(true);
            frontView = null;
            backView = null;
            downPosition = ListView.INVALID_POSITION;
        }
    }

    /**
     * Set enabled
     *
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        paused = !enabled;
    }

    
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (!isSwipeEnabled()) {
            return false;
        }

        if (viewWidth < 2) {
            viewWidth = swipeListView.getWidth();
        }

        switch (MotionEventCompat.getActionMasked(motionEvent)) {
            case MotionEvent.ACTION_DOWN: {
                if (paused && downPosition != ListView.INVALID_POSITION) {
                    return false;
                }
                swipeCurrentAction = SwipeListView.SWIPE_ACTION_REVEAL;

                int childCount = swipeListView.getChildCount();
                int[] listViewCoords = new int[2];
                swipeListView.getLocationOnScreen(listViewCoords);
                int x = (int) motionEvent.getRawX() - listViewCoords[0];
                int y = (int) motionEvent.getRawY() - listViewCoords[1];
                View child;
                for (int i = 0; i < childCount; i++) {
                    child = swipeListView.getChildAt(i);
                    child.getHitRect(rect);

                    int childPosition = swipeListView.getPositionForView(child);

                    // dont allow swiping if this is on the header or footer or IGNORE_ITEM_VIEW_TYPE or enabled is false on the adapter
                    boolean allowSwipe = swipeListView.getAdapter().isEnabled(childPosition) && swipeListView.getAdapter().getItemViewType(childPosition) >= 0;

                    if (allowSwipe && rect.contains(x, y)) {
                        setFrontView(child.findViewById(swipeFrontView), childPosition);

                        downX = motionEvent.getRawX();
                        downPosition = childPosition;

                        frontView.setClickable(false);
                        frontView.setLongClickable(false);

                        velocityTracker = VelocityTracker.obtain();
                        velocityTracker.addMovement(motionEvent);
                        if (swipeBackView > 0) {
                            setBackView(child.findViewById(swipeBackView));
                        }
                        break;
                    }
                }
                view.onTouchEvent(motionEvent);
                return true;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (velocityTracker == null || !swiping || downPosition == ListView.INVALID_POSITION) {
                    break;
                }

                float deltaX = motionEvent.getRawX() - downX;
                velocityTracker.addMovement(motionEvent);
                velocityTracker.computeCurrentVelocity(1000);
                float velocityX = Math.abs(velocityTracker.getXVelocity());
                float velocityY = Math.abs(velocityTracker.getYVelocity());
                boolean swap = false;
                boolean swapRight = false;
                if (minFlingVelocity <= velocityX && velocityX <= maxFlingVelocity && velocityY * 2 < velocityX) {
                    swapRight = velocityTracker.getXVelocity() > 0;
                    if(SwipeListView.DEBUG){
                        Log.d(SwipeListView.TAG, "swapRight: " + swapRight + " - swipingRight: " + swipingRight);
                    }
                    if (swapRight != swipingRight && swipeActionLeft != swipeActionRight) {
                        swap = false;
                    } else if (swapRight) {
                        swap = false;
                    } else {
                        swap = true;
                    }
                } else if (Math.abs(deltaX) > viewWidth / 2) {
                    swap = true;
                    swapRight = deltaX > 0;
                }


                generateAnimate(frontView, swap, swapRight, downPosition,deltaX);

                velocityTracker.recycle();
                velocityTracker = null;
                downX = 0;
                swiping = false;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (velocityTracker == null || paused || downPosition == ListView.INVALID_POSITION) {
                    break;
                }

                velocityTracker.addMovement(motionEvent);
                velocityTracker.computeCurrentVelocity(1000);
                float velocityX = Math.abs(velocityTracker.getXVelocity());
                float velocityY = Math.abs(velocityTracker.getYVelocity());
                float deltaX = motionEvent.getRawX() - downX;
                float deltaMode = Math.abs(deltaX);

                if (deltaMode > slop && swipeCurrentAction == SwipeListView.SWIPE_ACTION_REVEAL && velocityY < velocityX) {
                    swiping = true;
                    swipingRight = (deltaX > 0);
                    if(SwipeListView.DEBUG){
                        Log.d(SwipeListView.TAG, "deltaX: " + deltaX + " - swipingRight: " + swipingRight);
                    }
                    swipeCurrentAction = SwipeListView.SWIPE_ACTION_REVEAL;
                    swipeListView.requestDisallowInterceptTouchEvent(true);
                    MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (MotionEventCompat.getActionIndex(motionEvent) << MotionEventCompat.ACTION_POINTER_INDEX_SHIFT));
                    swipeListView.onTouchEvent(cancelEvent);
                    if (swipeCurrentAction == SwipeListView.SWIPE_ACTION_REVEAL) {
                        backView.setVisibility(View.VISIBLE);
                    }
                }
                break;
            }
        }
        return false;
    }


    protected void returnOldActions() {
        swipeActionRight = oldSwipeActionRight;
        swipeActionLeft = oldSwipeActionLeft;
    }

    /**
     * Moves the view
     *
     * @param deltaX delta
     */
    public void move(float deltaX) {
//        swipeListView.onMove(downPosition, deltaX);
        float posX = ViewHelper.getX(frontView);
            posX += true ? -viewWidth + rightOffset : viewWidth - leftOffset;
        if (posX > 0 && !swipingRight) {
            if(SwipeListView.DEBUG){
                Log.d(SwipeListView.TAG, "change to right");
            }
            swipingRight = !swipingRight;
            swipeCurrentAction = swipeActionRight;
            if (false) {
                backView.setVisibility(View.GONE);
            } else {
                backView.setVisibility(View.VISIBLE);
            }
        }
        if (posX < 0 && swipingRight) {
            if(SwipeListView.DEBUG){
                Log.d(SwipeListView.TAG, "change to left");
            }
            swipingRight = !swipingRight;
            swipeCurrentAction = swipeActionLeft;
                backView.setVisibility(View.VISIBLE);
        }
            setTranslationX(frontView, deltaX);
    }

    /**
     * Class that saves pending dismiss data
     */
    class PendingDismissData implements Comparable<PendingDismissData> {
        public int position;
        public View view;

        public PendingDismissData(int position, View view) {
            this.position = position;
            this.view = view;
        }

        @Override
        public int compareTo(PendingDismissData other) {
            // Sort by descending position
            return other.position - position;
        }
    }

    /**
     * Perform dismiss action
     *
     * @param dismissView     View
     * @param dismissPosition Position of list
     */
    protected void performDismiss(final View dismissView, final int dismissPosition, boolean doPendingDismiss) {
        enableDisableViewGroup((ViewGroup) dismissView, false);
        final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
        final int originalHeight = dismissView.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(animationTime);

        if (doPendingDismiss) {
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    --dismissAnimationRefCount;
                    if (dismissAnimationRefCount == 0) {
                        removePendingDismisses(originalHeight);
                    }
                }
            });
        }

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                enableDisableViewGroup((ViewGroup) dismissView, true);
            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                dismissView.setLayoutParams(lp);
            }
        });

        pendingDismisses.add(new PendingDismissData(dismissPosition, dismissView));
        animator.start();
    }

    /**
     * Remove all pending dismisses.
     */
    protected void resetPendingDismisses() {
        pendingDismisses.clear();
    }

    /**
     * Will call {@link #removePendingDismisses(int)} in animationTime + 100 ms.
     * @param originalHeight will be used to rest the cells height.
     */
    protected void handlerPendingDismisses(final int originalHeight) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                removePendingDismisses(originalHeight);
            }
        }, animationTime + 100);
    }

    /**
     * Will delete all pending dismisses.
     * Will call callback onDismiss for all pending dismisses.
     * Will reset all cell height to originalHeight.
     * @param originalHeight is the height of the cell before animation.
     */
    private void removePendingDismisses(int originalHeight) {
        // No active animations, process all pending dismisses.
        // Sort by descending position
        Collections.sort(pendingDismisses);

        int[] dismissPositions = new int[pendingDismisses.size()];
        for (int i = pendingDismisses.size() - 1; i >= 0; i--) {
            dismissPositions[i] = pendingDismisses.get(i).position;
        }
//        swipeListView.onDismiss(dismissPositions);

        ViewGroup.LayoutParams lp;
        for (PendingDismissData pendingDismiss : pendingDismisses) {
            // Reset view presentation
            if (pendingDismiss.view != null) {
                setAlpha(pendingDismiss.view, 1f);
                setTranslationX(pendingDismiss.view, 0);
                lp = pendingDismiss.view.getLayoutParams();
                lp.height = originalHeight;
                pendingDismiss.view.setLayoutParams(lp);
            }
        }

        resetPendingDismisses();

    }

    public static void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = viewGroup.getChildAt(i);
            view.setEnabled(enabled);
            if (view instanceof ViewGroup) {
                enableDisableViewGroup((ViewGroup) view, enabled);
            }
        }
    }

}
