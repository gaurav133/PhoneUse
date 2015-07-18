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


import android.animation.Animator.AnimatorListener;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;

/**
 * Touch listener impl for the SwipeListView
 */
public class SwipeListViewTouchListener implements View.OnTouchListener {


    private int swipeFrontView = 0;
    private int swipeBackView = 0;

    private Rect rect = new Rect();

    // Cached ViewConfiguration and system-wide constant values
    private int slop;
    private int minFlingVelocity;
    private int maxFlingVelocity;

    // Fixed properties
    private SwipeListView swipeListView;
    private int viewWidth = 1; // 1 and not 0 to prevent dividing by zero


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
        this.swipeListView = swipeListView;
    }

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
        
        view.animate()
                .translationX(viewWidth)
                .setDuration(400)
                .setListener(new AnimatorListener() {
                	
                	@Override
                	public void onAnimationCancel(
                			android.animation.Animator animation) {
                		// TODO Auto-generated method stub
                		
                	}
                	
                	@Override
                	public void onAnimationStart(
                			android.animation.Animator animation) {
                		// TODO Auto-generated method stub
                		
                	}
                	
                	@Override
                	public void onAnimationEnd(
                			android.animation.Animator animation) {
                    	if(mSwipeListener != null){
                    		mSwipeListener.onItemSwiped(downPosition);
                    	}
                    	
                        resetCell();
                    }
                	
                	@Override
                	public void onAnimationRepeat(
                			android.animation.Animator animation) {
                		// TODO Auto-generated method stub
                		
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

                if (deltaX > slop && swipeCurrentAction == SwipeListView.SWIPE_ACTION_REVEAL && velocityY < velocityX) {
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





}
