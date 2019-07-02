package pitko.erik.homecontrol.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.util.Date;

import pitko.erik.homecontrol.R;

public class FragmentSingleGraph extends Fragment {
    private String text;
    private TextView txtView;
    private GraphView graph;
    private LineGraphSeries<DataPoint> series;

    public void setText(String text) {
        this.text = text;
        if (txtView != null)
            txtView.setText(text);
    }

    public void addSeries(LineGraphSeries<DataPoint> series) {
        this.series = series;
        if (this.graph != null)
            this.graph.addSeries(series);
    }

    public void setBounds(Date min, Date max) {
        this.graph.getViewport().setMinX(min.getTime());
        this.graph.getViewport().setMaxX(max.getTime());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_singlegraph,
                container, false);
        txtView = view.findViewById(R.id.textView);
        graph = view.findViewById(R.id.graph);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(false);

        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity(), DateFormat.getTimeInstance(DateFormat.SHORT)));
        graph.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space

        setText(text);
        if (this.series != null) {
            this.graph.addSeries(this.series);
        }
        return view;
    }


}
