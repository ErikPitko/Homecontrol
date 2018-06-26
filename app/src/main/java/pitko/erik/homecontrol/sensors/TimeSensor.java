package pitko.erik.homecontrol.sensors;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.format.ISODateTimeFormat;


public class TimeSensor extends Sensor {
    public TimeSensor(String topic, String sensorText, String layout, String postfix) {
        super(topic, sensorText, layout, postfix);
    }

    @Override
    public void setSensorStatus(String msg) {
        DateTime dateTime = ISODateTimeFormat.dateTime().parseDateTime(msg);
        super.setSensorStatus((String.valueOf(Minutes.minutesBetween(DateTime.now(), dateTime).getMinutes())));
    }
}
