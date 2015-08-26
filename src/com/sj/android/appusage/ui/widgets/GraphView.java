package com.sj.android.appusage.ui.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.view.View;

import com.sj.android.appusage.R;

public class GraphView extends View {

	private Paint paint;
	private float[] mIntervalValues;
	private float[] mIntervalStartTime;
	private float[] mIntervalEndTime;
	private String[] mHorizontalLabels;
	private String[] mVerticalLabels;
	private String mGraphTitle;
	private String mHorizontalLabelName = null;
	private String mVerticalLabelName = null;
	private float mHorizontalGapParameter;
	private float mVertialGapParameter;
	private Context mContext = null;
	private int[] mColorList = new int[] { Color.RED, Color.BLUE,
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
			float[] endtime,String horizontalLabelName,String verticalLabelName) {
		super(context);
		this.mContext = context;
		this.mIntervalValues = values;
		this.mIntervalStartTime = startime;
		this.mIntervalEndTime = endtime;
		this.mGraphTitle = title;
		this.mHorizontalLabels = horlabels;
		this.mVerticalLabels = verlabels;
		this.mHorizontalLabelName = horizontalLabelName;
		this.mVerticalLabelName = verticalLabelName;
		this.mHorizontalGapParameter = context.getResources().getDimension(R.dimen.graph_view_horizontal_parameter);
		this.mVertialGapParameter = context.getResources().getDimension(R.dimen.graph_view_vertical_parameter);
		paint = new Paint();
		this.setHorizontalScrollBarEnabled(true);
		this.setVerticalScrollBarEnabled(true);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension((int) (1.4 * mHorizontalGapParameter * (mHorizontalLabels.length)),
				(int) (mVertialGapParameter * (mVerticalLabels.length) + (mContext.getResources().getDimension(R.dimen.graph_view_extra_measure_height))));
	}

	@Override
	protected void onDraw(Canvas canvas) {

		paint.setTextAlign(Align.LEFT);
		paint.setColor(Color.BLUE);
		paint.setStrokeWidth(mContext.getResources().getDimension(R.dimen.graph_view_stroke_width_large));
		paint.setTextSize(mContext.getResources().getDimension(R.dimen.graph_view_stroke_text_size_large));
		//Graph title
		canvas.drawText(mGraphTitle, mHorizontalGapParameter * 3, mHorizontalGapParameter * 2, paint);
		
		//graph vertical label nam and horizontal label name
		paint.setStrokeWidth(mContext.getResources().getDimension(R.dimen.graph_view_stroke_width_medium));
		paint.setTextSize(mContext.getResources().getDimension(R.dimen.graph_view_stroke_text_size_medium));
		canvas.drawText(mVerticalLabelName, mHorizontalGapParameter, mHorizontalGapParameter * 3, paint);
		canvas.drawText(mHorizontalLabelName, mHorizontalGapParameter
				* (mHorizontalLabels.length+ 1), getHeight() - mHorizontalGapParameter, paint);
		
		
		paint.setTextSize(mContext.getResources().getDimension(R.dimen.graph_view_strpke_text_size_small));
		paint.setStrokeWidth(mContext.getResources().getDimension(R.dimen.graph_view_stroke_width_small));
		
		//horizental line and its labels
		for (int i = 0; i < mHorizontalLabels.length; i++) {
			paint.setColor(Color.DKGRAY);
			canvas.drawLine(mHorizontalGapParameter, getHeight() - mHorizontalGapParameter, mHorizontalGapParameter
					* (mHorizontalLabels.length), getHeight() - mHorizontalGapParameter, paint);
			paint.setColor(Color.BLACK);
			canvas.drawText(mHorizontalLabels[i], (i * mHorizontalGapParameter) + mHorizontalGapParameter,
					getHeight() - mHorizontalGapParameter + (mContext.getResources().getDimension(R.dimen.graph_view_bottom_margin_horizontal_label)), paint);
		}

		//vertical line and its labels
		for (int i = 0; i < mVerticalLabels.length; i++) {
			paint.setColor(Color.DKGRAY);
			canvas.drawLine(mHorizontalGapParameter, getHeight() - mHorizontalGapParameter, mHorizontalGapParameter,
					getHeight() - mHorizontalGapParameter - (mVertialGapParameter * (mVerticalLabels.length)),
					paint);
			paint.setTextAlign(Align.CENTER);
			paint.setColor(Color.BLACK);
			canvas.drawText(mVerticalLabels[i], mHorizontalGapParameter - (mContext.getResources().getDimension(R.dimen.graph_view_left_margin_vertical_label)), getHeight() - mHorizontalGapParameter
					- (mVertialGapParameter * (i + 1)), paint);
		}

		//intervals with different colors
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
