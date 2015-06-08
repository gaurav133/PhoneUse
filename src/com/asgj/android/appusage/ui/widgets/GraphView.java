package com.asgj.android.appusage.ui.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.view.View;

public class GraphView extends View {

	private Paint paint;
	private float[] values;
	private float[] startTime;
	private float[] endtime;
	private String[] horlabels;
	private String[] verlabels;
	private String title;
	private static float horstart = 40;
	private static float vertStart = 75;
	private static int[] mColorList = new int[] { Color.RED, Color.BLUE,
			Color.GREEN, Color.CYAN };

	public GraphView(Context context, float[] values, String title,
			String[] horlabels, String[] verlabels, float[] startime,
			float[] endtime) {
		super(context);
		this.values = values;
		this.startTime = startime;
		this.endtime = endtime;
		this.title = title;
		this.horlabels = horlabels;
		this.verlabels = verlabels;
		paint = new Paint();
		this.setHorizontalScrollBarEnabled(true);
		this.setVerticalScrollBarEnabled(true);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension((int) (horstart * (horlabels.length + 1) + 100),
				(int) (vertStart * (verlabels.length + 1) + 200));
	}

	@Override
	protected void onDraw(Canvas canvas) {

		paint.setTextAlign(Align.LEFT);
		paint.setColor(Color.BLUE);
		paint.setStrokeWidth(3f);
		paint.setTextSize(50f);
		canvas.drawText(title, horstart, horstart * 2, paint);
		paint.setTextSize(20f);
		paint.setStrokeWidth(1f);
		for (int i = 0; i < horlabels.length; i++) {
			paint.setColor(Color.DKGRAY);
			canvas.drawLine(horstart, getHeight() - horstart, horstart
					* (horlabels.length), getHeight() - horstart, paint);
			paint.setColor(Color.BLACK);
			canvas.drawText(horlabels[i], (i * horstart) + horstart,
					getHeight() - horstart + 30, paint);
		}

		for (int i = 0; i < verlabels.length; i++) {
			paint.setColor(Color.DKGRAY);
			canvas.drawLine(horstart, getHeight() - horstart, horstart,
					getHeight() - horstart - (vertStart * (verlabels.length)),
					paint);
			paint.setTextAlign(Align.CENTER);
			paint.setColor(Color.BLACK);
			canvas.drawText(verlabels[i], horstart - 20, getHeight() - horstart
					- (vertStart * (i + 1)), paint);
		}

		for (int i = 0; i < values.length; i++) {
			if (values[i] == 0)
				continue;
			paint.setColor(mColorList[(i + 1) % 4]);
			canvas.drawRect(((startTime[i] * horstart) + horstart),
					(getHeight() - horstart - (values[i] * vertStart)),
					((endtime[i] * horstart) + horstart),
					(getHeight() - horstart), paint);
		}

	}
}
