package pitko.erik.homecontrol.fragments;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pitko.erik.homecontrol.R;
import pitko.erik.homecontrol.graphs.Graph;

public class FragmentSingleGraph extends Fragment {
    private final Graph parent;
    private String text;
    private TextView txtView;
    private LineChart chart;
    private LineData series;
    private RadioGroup.OnCheckedChangeListener graphTimePeriodListener;
    private List<Long> dayLimitLines;
    private Graph.TimePeriod currentTimePeriod;

    public FragmentSingleGraph(Graph parent) {
        this.parent = parent;
        this.dayLimitLines = new ArrayList<>();
        createGroupOnClick();
    }

    public void setText(String text) {
        this.text = text;
        if (txtView != null)
            txtView.setText(text);
    }

    public LineChart getChart() {
        return chart;
    }

    public Graph.TimePeriod getCurrentTimePeriod() {
        return currentTimePeriod;
    }

    public void addDayLimitLineValue(long value) {
        dayLimitLines.add(value);
    }

    private void addLimitLines(AxisBase axis, String label, int colorId) {
        if (chart != null) {
            for (Long val : this.dayLimitLines) {
                LimitLine ll1 = new LimitLine(val, label);
                ll1.setLineWidth(1f);
                ll1.enableDashedLine(10f, 10f, 0f);
                ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
                ll1.setLineColor(colorId);

                axis.addLimitLine(ll1);
            }
        }
    }

    public void addSeries(LineDataSet series) {
        series.setAxisDependency(YAxis.AxisDependency.LEFT);
        series.setColor(ColorTemplate.getHoloBlue());
        series.setValueTextColor(Color.BLUE);
        series.setLineWidth(1.5f);
        series.setDrawValues(true);
        series.enableDashedHighlightLine(10f, 3f, 0f);
//        circle
        series.setDrawCircles(true);
        series.setDrawCircleHole(true);
        series.setCircleRadius(1f);
        series.setCircleHoleRadius(0.5f);
        series.setCircleColor(Color.BLUE);
//        fill
        series.setHighLightColor(Color.rgb(244, 117, 117));
        series.setDrawFilled(true);
        Drawable drawable = getResources().getDrawable(R.drawable.fade_blue);
        series.setFillDrawable(drawable);

        series.setMode(LineDataSet.Mode.LINEAR);

        this.series = new LineData(series);

        if (this.chart != null) {
            this.chart.getXAxis().removeAllLimitLines();
//            reset zoom
            this.chart.zoomToCenter(1 / this.chart.getScaleX(), 1 / this.chart.getScaleY());
            this.chart.setData(this.series);
        }
        chart.animateX(1000);
        addLimitLines(chart.getXAxis(), "", R.color.transparentGray);
        this.dayLimitLines.clear();
    }

    private void createGroupOnClick() {
        graphTimePeriodListener = (group, id) -> {
            String pattern;
            this.chart.getXAxis().setGranularityEnabled(true);
            switch (id) {
                case R.id.g_week:
                    pattern = "E HH'h'";
//                    set granularity for 1 hour
                    this.chart.getXAxis().setGranularity(60 * 60 * 1000 + 1);
                    this.currentTimePeriod = Graph.TimePeriod.WEEK;
                    break;
                case R.id.g_month:
                    pattern = "[w] E";
//                    set granularity for 1 day
                    this.chart.getXAxis().setGranularity(24 * 60 * 60 * 1000 + 1);
                    this.currentTimePeriod = Graph.TimePeriod.MONTH;
                    break;
                default:
                    pattern = "HH:mm";
                    this.chart.getXAxis().setGranularityEnabled(false);
                    this.currentTimePeriod = Graph.TimePeriod.DAY;
            }
            parent.loadChartData(this.currentTimePeriod);
            chart.getXAxis().setValueFormatter(new ValueFormatter() {
                private final SimpleDateFormat mFormat = new SimpleDateFormat(pattern, Locale.getDefault());

                public String getFormattedValue(float value) {
                    return mFormat.format(new Date((long) value));
                }
            });
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_singlegraph,
                container, false);
        txtView = view.findViewById(R.id.textView);
        RadioGroup graphTimePeriodGroup = view.findViewById(R.id.g_group);
        graphTimePeriodGroup.setOnCheckedChangeListener(graphTimePeriodListener);

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
        chart.setHighlightPerTapEnabled(false);
        // set an alternative background color
        chart.setViewPortOffsets(0f, 0f, 0f, 0f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xAxis.setTextSize(11f);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(true);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setCenterAxisLabels(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

            public String getFormattedValue(float value) {
                return mFormat.format(new Date((long) value));
            }
        });

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(false);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setXOffset(-0.3f);
        leftAxis.setYOffset(-5f);
        setText(text);
        return view;
    }
}
