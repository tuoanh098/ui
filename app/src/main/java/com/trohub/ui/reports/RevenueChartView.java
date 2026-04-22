package com.trohub.ui.reports;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RevenueChartView extends View {
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Entry> entries = new ArrayList<>();

    public RevenueChartView(Context context) {
        super(context);
        init();
    }

    public RevenueChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RevenueChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        axisPaint.setColor(Color.rgb(228, 231, 236));
        axisPaint.setStrokeWidth(dp(1));

        barPaint.setColor(Color.rgb(21, 101, 192));

        textPaint.setColor(Color.rgb(102, 112, 133));
        textPaint.setTextSize(dp(11));

        emptyPaint.setColor(Color.rgb(102, 112, 133));
        emptyPaint.setTextSize(dp(14));
        emptyPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setEntries(List<Entry> data) {
        entries.clear();
        if (data != null) entries.addAll(data);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int left = dp(36);
        int right = dp(12);
        int top = dp(18);
        int bottom = dp(42);

        if (entries.isEmpty()) {
            canvas.drawText("Không có dữ liệu biểu đồ", width / 2f, height / 2f, emptyPaint);
            return;
        }

        double max = 0;
        for (Entry entry : entries) {
            if (entry.value > max) max = entry.value;
        }
        if (max <= 0) max = 1;

        float chartWidth = width - left - right;
        float chartHeight = height - top - bottom;

        canvas.drawLine(left, top, left, top + chartHeight, axisPaint);
        canvas.drawLine(left, top + chartHeight, left + chartWidth, top + chartHeight, axisPaint);

        int visibleCount = Math.min(entries.size(), 8);
        float slot = chartWidth / visibleCount;
        float barWidth = Math.max(dp(16), slot * 0.56f);

        textPaint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < visibleCount; i++) {
            Entry entry = entries.get(i);
            float centerX = left + slot * i + slot / 2f;
            float barHeight = (float) (entry.value / max * chartHeight);
            float barLeft = centerX - barWidth / 2f;
            float barTop = top + chartHeight - barHeight;
            RectF rect = new RectF(barLeft, barTop, barLeft + barWidth, top + chartHeight);
            canvas.drawRoundRect(rect, dp(4), dp(4), barPaint);

            canvas.drawText(shortMoney(entry.value), centerX, Math.max(top + dp(10), barTop - dp(5)), textPaint);
            canvas.drawText(shortLabel(entry.label), centerX, top + chartHeight + dp(18), textPaint);
        }
    }

    private String shortMoney(double value) {
        if (value >= 1_000_000) return String.format(Locale.US, "%.1ftr", value / 1_000_000d);
        if (value >= 1_000) return String.format(Locale.US, "%.0fk", value / 1_000d);
        return String.valueOf(Math.round(value));
    }

    private String shortLabel(String label) {
        if (label == null || label.trim().isEmpty()) return "N/A";
        String value = label.trim();
        return value.length() <= 8 ? value : value.substring(0, 8);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    public static class Entry {
        final String label;
        final double value;

        public Entry(String label, double value) {
            this.label = label;
            this.value = value;
        }
    }
}
