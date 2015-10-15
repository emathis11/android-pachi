package lrstudios.games.ego.lib.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import lrstudios.games.ego.lib.R;


public class HoloDialog extends Dialog {

    protected HoloDialog(Context context) {
        super(context);
    }

    protected HoloDialog(Context context, int theme) {
        super(context, theme);
    }

    protected HoloDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    public static final class Builder {
        private Context _context;

        private String _title;
        private String _message;
        private View _view;
        private int _iconResId;

        private String _positiveButton;
        private String _negativeButton;
        private String _neutralButton;
        private DialogInterface.OnClickListener _positiveButtonClickListener;
        private DialogInterface.OnClickListener _negativeButtonClickListener;
        private DialogInterface.OnClickListener _neutralButtonClickListener;


        public Builder(Context context) {
            _context = context;
        }

        public Builder setTitle(int titleResId) {
            _title = _context.getString(titleResId);
            return this;
        }

        public Builder setTitle(String title) {
            _title = title;
            return this;
        }

        public Builder setMessage(int messageResId) {
            _message = _context.getString(messageResId);
            return this;
        }

        public Builder setMessage(String message) {
            _message = message;
            return this;
        }

        public Builder setIcon(int resId) {
            _iconResId = resId;
            return this;
        }

        public Builder setPositiveButton(int stringResId, DialogInterface.OnClickListener clickListener) {
            _positiveButton = _context.getString(stringResId);
            _positiveButtonClickListener = clickListener;
            return this;
        }

        public Builder setNegativeButton(int stringResId, DialogInterface.OnClickListener clickListener) {
            _negativeButton = _context.getString(stringResId);
            _negativeButtonClickListener = clickListener;
            return this;
        }

        public Builder setNeutralButton(int stringResId, DialogInterface.OnClickListener clickListener) {
            _neutralButton = _context.getString(stringResId);
            _neutralButtonClickListener = clickListener;
            return this;
        }

        public Builder setView(View view) {
            _view = view;
            return this;
        }

        public Dialog show() {
            View dialogView = LayoutInflater.from(_context).inflate(R.layout.alert_dialog_holo, null);

            if (_title == null && _iconResId == 0) {
                dialogView.findViewById(R.id.topPanel).setVisibility(View.GONE);
                dialogView.findViewById(R.id.titleDivider).setVisibility(View.GONE);
            }
            if (_message == null)
                dialogView.findViewById(R.id.contentPanel).setVisibility(View.GONE);
            if (_view == null)
                dialogView.findViewById(R.id.customPanel).setVisibility(View.GONE);
            if (_negativeButton == null && _neutralButton == null && _positiveButton == null)
                dialogView.findViewById(R.id.buttonPanel).setVisibility(View.GONE);

            if (_title != null)
                ((TextView) dialogView.findViewById(R.id.alertTitle)).setText(_title);
            if (_iconResId > 0)
                ((ImageView) dialogView.findViewById(R.id.icon)).setImageDrawable(_context.getResources().getDrawable(_iconResId));
            if (_message != null)
                ((TextView) dialogView.findViewById(R.id.message)).setText(_message);
            if (_view != null)
                ((ViewGroup) dialogView.findViewById(R.id.custom)).addView(_view);

            Button btn1 = (Button) dialogView.findViewById(R.id.button1);
            Button btn2 = (Button) dialogView.findViewById(R.id.button2);
            Button btn3 = (Button) dialogView.findViewById(R.id.button3);

            if (_positiveButton != null) {
                btn1.setText(_positiveButton);
            }
            else {
                btn1.setVisibility(View.GONE);
            }

            if (_negativeButton != null) {
                btn2.setText(_negativeButton);
            }
            else {
                btn2.setVisibility(View.GONE);
            }

            if (_neutralButton != null) {
                btn3.setText(_neutralButton);
            }
            else {
                btn3.setVisibility(View.GONE);
            }

            HoloDialog dialog = new HoloDialog(_context, R.style.HoloDialog);
            dialog.setContentView(dialogView);
            dialog.show();

            btn1.setOnClickListener(new DialogButtonClickListener(dialog, _positiveButtonClickListener, DialogInterface.BUTTON_POSITIVE));
            btn2.setOnClickListener(new DialogButtonClickListener(dialog, _negativeButtonClickListener, DialogInterface.BUTTON_NEGATIVE));
            btn3.setOnClickListener(new DialogButtonClickListener(dialog, _neutralButtonClickListener, DialogInterface.BUTTON_NEUTRAL));

            return dialog;
        }
    }

    private static final class DialogButtonClickListener implements View.OnClickListener {
        private Dialog _dialog;
        private DialogInterface.OnClickListener _listener;
        private int _buttonId;

        private DialogButtonClickListener(Dialog dialog, DialogInterface.OnClickListener listener, int buttonId) {
            _dialog = dialog;
            _listener = listener;
            _buttonId = buttonId;
        }

        @Override
        public void onClick(View v) {
            if (_listener != null)
                _listener.onClick(_dialog, _buttonId);
            _dialog.dismiss();
        }
    }
}
