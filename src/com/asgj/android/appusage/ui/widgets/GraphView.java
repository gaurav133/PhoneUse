package com.asgj.android.appusage.ui.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.view.View;

public class GraphView extends View {

	private Paint paint;
	private float[] mIntervalValues;
	private float[] mIntervalStartTime;
	private float[] mIntervalEndTime;
	private String[] mHorizontalLabels;
	private String[] mVerticalLabels;
	private String mGraphTitle;
	private static float mHorizontalGapParameter = 40;
	private static float mVertialGapParameter = 75;
	private static int[] mColorList = new int[] { Color.RED, Color.BLUE,
			Color.GREEN, Color.CYAN };

	/**
	 * 
	 * @param context context that used this view
	 * @param values list of duration of intervals to be shown on list
	 * @param title  title of the graph
	 * @param horlabels arrays of labels shown on x-axis
	 * @param verlabels arrays of label shown on y- axis
	 * @param startime  arrays of start time of intervals
	 * @param endtime   arrays of end time of intervals
	 */
	public GraphView(Context context, float[] values, String title,
			String[] horlabels, String[] verlabels, float[] startime,
			float[] endtime) {
		super(context);
		this.mIntervalValues = values;
		this.mIntervalStartTime = startime;
		this.mIntervalEndTime = endtime;
		this.mGraphTitle = title;
		this.mHorizontalLabels = horlabels;
		this.mVerticalLabels = verlabels;
		paint = new Paint();
		this.setHorizontalScrollBarEnabled(true);
		this.setVerticalScrollBarEnabled(true);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension((int) (mHorizontalGapParameter * (mHorizontalLabels.length + 1) + 100),
				(int) (mVertialGapParameter * (mVerticalLabels.length + 1) + 200));
	}

	@Override
	protected void onDraw(Canvas canvas) {

		paint.setTextAlign(Align.LEFT);
		paint.setColor(Color.BLUE);
		paint.setStrokeWidth(3f);
		paint.setTextSize(50f);
		canvas.drawText(mGraphTitle, mHorizontalGapParameter, mHorizontalGapParameter * 2, paint);
		paint.setTextSize(20f);
		paint.setStrokeWidth(1f);
		for (int i = 0; i < mHorizontalLabels.length; i++) {
			paint.setColor(Color.DKGRAY);
			canvas.drawLine(mHorizontalGapParameter, getHeight() - mHorizontalGapParameter, mHorizontalGapParameter
					* (mHorizontalLabels.length), getHeight() - mHorizontalGapParameter, paint);
			paint.setColor(Color.BLACK);
			canvas.drawText(mHorizontalLabels[i], (i * mHorizontalGapParameter) + mHorizontalGapParameter,
					getHeight() - mHorizontalGapParameter + 30, paint);
		}

		for (int i = 0; i < mVerticalLabels.length; i++) {
			paint.setColor(Color.DKGRAY);
			canvas.drawLine(mHorizontalGapParameter, getHeight() - mHorizontalGapParameter, mHorizontalGapParameter,
					getHeight() - mHorizontalGapParameter - (mVertialGapParameter * (mVerticalLabels.length)),
					paint);
			paint.setTextAlign(Align.CENTER);
			paint.setColor(Color.BLACK);
			canvas.drawText(mVerticalLabels[i], mHorizontalGapParameter - 20, getHeight() - mHorizontalGapParameter
					- (mVertialGapParameter * (i + 1)), paint);
		}

		for (int i = 0; i < mIntervalValues.length; i++) {
			if (mIntervalValues[i] == 0)
				continue;
			paint.setColor(mColorList[(i + 1) % 4]);
			canvas.drawRect(((mIntervalStartTime[i] * mHorizontalGapParameter) + mHorizontalGapParameter),
					(getHeight() - mHorizontalGapParameter - (mIntervalValues[i] * mVertialGapParameter)),
					((mIntervalEndTime[i] * mHorizontalGapParameter) + mHorizontalGapParameter),
					(getHeight() - mHorizontalGapParameter), paint);
		}

	}
}
