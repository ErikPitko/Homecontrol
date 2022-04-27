package pitko.erik.homecontrol.models;

public class SensorShared {
    private boolean valueHidden;
    private boolean chartHidden;

    public SensorShared(String topic) {
        this.valueHidden = false;
        this.chartHidden = false;
    }

    public boolean isValueHidden() {
        return valueHidden;
    }

    public void setValueHidden(boolean valueHidden) {
        this.valueHidden = valueHidden;
    }

    public boolean isChartHidden() {
        return chartHidden;
    }

    public void setChartHidden(boolean chartHidden) {
        this.chartHidden = chartHidden;
    }
}
