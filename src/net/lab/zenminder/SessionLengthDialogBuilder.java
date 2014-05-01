package net.lab.zenminder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;

// dialog to edit whole numbers
public abstract class SessionLengthDialogBuilder extends AlertDialog.Builder {
    private EditText mEditor;

    public SessionLengthDialogBuilder(Context context, int value) {
        super(context);

        View view = View.inflate(
                context,
                R.layout.session_length_dialog,
                null);
        mEditor = (EditText) view.findViewById(R.id.dialog_edittext);
        mEditor.setText(String.valueOf(value));

        setIcon(R.drawable.ic_launcher);
        setTitle(R.string.session_length_dialog_title);
        setView(view);
        setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int button) {
                onOK(Integer.parseInt(mEditor.getText().toString()));
            }});
        setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int button) { }});
    }

    // override to do something with the edited value
    public abstract void onOK(int value);
}
