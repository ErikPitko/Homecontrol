package pitko.erik.homecontrol.sensors;

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
    public void setSensorStatus(String msg) {
        DateTime dateTime = ISODateTimeFormat.dateTime().parseDateTime(msg);
        int val;
        if ((val = Days.daysBetween(DateTime.now(), dateTime).getDays()) < 0) {
            setPostfixbyResource("days");
            super.setSensorStatus(String.valueOf(val));
        } else if ((val = Hours.hoursBetween(DateTime.now(), dateTime).getHours()) < 0) {
            setPostfixbyResource("hours");
            super.setSensorStatus(String.valueOf(val));
        } else {
            this.postfix = "min";
            super.setSensorStatus((String.valueOf(Minutes.minutesBetween(DateTime.now(), dateTime).getMinutes())));
        }
    }
}
