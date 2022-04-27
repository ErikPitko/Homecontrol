package pitko.erik.homecontrol.sensors;

import android.graphics.Color;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.format.ISODateTimeFormat;


public class TimeSensor extends Sensor {
    public TimeSensor(String topic, String sensorText, String layout, String postfix) {
        super(topic, sensorText, layout, postfix);
    }

    @Override
    public void setSensorStatus(String msg, Integer color) {
        DateTime dateTime = ISODateTimeFormat.dateTime().parseDateTime(msg);
        int val;
        if ((val = Days.daysBetween(DateTime.now(), dateTime).getDays()) < 0) {
            setPostfixbyResource("days");
            super.setSensorStatus(String.valueOf(val), Color.RED);
        } else if ((val = Hours.hoursBetween(DateTime.now(), dateTime).getHours()) < 0) {
            setPostfixbyResource("hours");
            super.setSensorStatus(String.valueOf(val), Color.RED);
        } else {
            val = Minutes.minutesBetween(DateTime.now(), dateTime).getMinutes();
            this.postfix = "min";
            if (val == 0)
                super.setSensorStatus((String.valueOf(val)), Color.GREEN);
            else
                super.setSensorStatus((String.valueOf(val)), Color.RED);
        }
    }
}
