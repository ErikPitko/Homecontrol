package pitko.erik.homecontrol.fragments;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import pitko.erik.homecontrol.R;

public class FragmentSingleGraph extends Fragment {
    private String text;
    private TextView txtView;

    private LineChart chart;
    private LineData series;

    public void setText(String text) {
        this.text = text;
        if (txtView != null)
            txtView.setText(text);
    }

    public void addSeries(LineDataSet series) {
        series.setAxisDependency(YAxis.AxisDependency.LEFT);
        series.setColor(ColorTemplate.getHoloBlue());
        series.setValueTextColor(ColorTemplate.getHoloBlue());
        series.setLineWidth(1.5f);
        series.setDrawCircles(false);
        series.setDrawValues(false);
        series.setFillAlpha(65);
        series.setFillColor(ColorTemplate.getHoloBlue());
        series.setHighLightColor(Color.rgb(244, 117, 117));
        series.enableDashedHighlightLine(10f, 5f, 0f);
//        fill
        series.setDrawFilled(true);
        Drawable drawable = getResources().getDrawable(R.drawable.fade_blue);
        series.setFillDrawable(drawable);

        this.series = new LineData(series);

        if (this.chart != null)
            this.chart.setData(this.series);
        chart.animateX(1000);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_singlegraph,
                container, false);
        txtView = view.findViewById(R.id.textView);
        chart = view.findViewById(R.id.graph);

        // no description text
        chart.getDescription().setEnabled(false);

        chart.getLegend().setEnabled(false);

        // enable touch gestures
        chart.setTouchEnabled(true);

        chart.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setHighlightPerDragEnabled(false);
        chart.setHighlightPerTapEnabled(true);


        // set an alternative background color
        chart.setViewPortOffsets(0f, 0f, 0f, 0f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xAxis.setTextSize(10f);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(1f); // one hour
        xAxis.setValueFormatter(new ValueFormatter() {

            private final SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

            public String getFormattedValue(float value) {

                return mFormat.format(new Date((long)value));
            }
        });

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(false);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setYOffset(-9f);

        setText(text);
        return view;
    }
}
