package net.lab.zenminder;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.widget.TextView;

class HtmlView extends TextView {
    public HtmlView(Context context) {
        super(context);
        convertToHtml();
    }

    public HtmlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        convertToHtml();
    }

    public HtmlView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        convertToHtml();
    }

    private void convertToHtml() {
        setMovementMethod(LinkMovementMethod.getInstance());
        setText(Html.fromHtml(getText().toString()));
    }
}
