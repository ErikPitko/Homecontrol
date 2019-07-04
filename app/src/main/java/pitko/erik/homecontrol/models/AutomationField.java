package pitko.erik.homecontrol.models;

import android.widget.EditText;

public class AutomationField {
    private String topic;
    private EditText editText;

    public AutomationField(String topic, EditText editText) {
        this.topic = topic;
        this.editText = editText;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public EditText getEditText() {
        return editText;
    }

    public void setEditText(EditText editText) {
        this.editText = editText;
    }
}
