package lrstudios.games.ego.lib;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;


public class DetailedSeekBar extends RelativeLayout implements SeekBar.OnSeekBarChangeListener {
    private static Formatter _defaultFormatter;

    private TextView _txt_title;
    private TextView _txt_descriptionLeft;
    private TextView _txt_descriptionRight;
    private TextView _txt_value;
    private SeekBar _seekBar;

    private Formatter _formatter;


    public DetailedSeekBar(Context context) {
        this(context, null);
    }

    public DetailedSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DetailedSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        LayoutInflater.from(context).inflate(R.layout.detailed_seek_bar, this, true);

        _txt_title = (TextView) findViewById(R.id._dsb_txt_title);
        _txt_descriptionLeft = (TextView) findViewById(R.id._dsb_txt_description_left);
        _txt_descriptionRight = (TextView) findViewById(R.id._dsb_txt_description_right);
        _txt_value = (TextView) findViewById(R.id._dsb_txt_value);
        _seekBar = (SeekBar) findViewById(R.id._dsb_seek_bar);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DetailedSeekBar, defStyle, 0);
        String title = a.getString(R.styleable.DetailedSeekBar_barTitle);
        String descriptionLeft = a.getString(R.styleable.DetailedSeekBar_descriptionLeft);
        String descriptionRight = a.getString(R.styleable.DetailedSeekBar_descriptionRight);
        a.recycle();

        _txt_title.setText(title != null ? title : "");
        _txt_descriptionLeft.setText(descriptionLeft != null ? descriptionLeft : "");
        _txt_descriptionRight.setText(descriptionRight != null ? descriptionRight : "");

        _seekBar.setMax(10000);
        _seekBar.setOnSeekBarChangeListener(this);
        _seekBar.setProgress(0);
    }

    public void setTitle(String title) {
        _txt_title.setText(title);
    }

    public void setDescriptionLeft(String text) {
        _txt_descriptionLeft.setText(text);
    }

    public void setDescriptionRight(String text) {
        _txt_descriptionRight.setText(text);
    }

    public int getProgress() {
        return _seekBar.getProgress();
    }

    public void setProgress(int value) {
        _seekBar.setProgress(value);
    }

    public void setMax(int max) {
        _seekBar.setMax(max);
    }

    public void setFormatter(Formatter formatter) {
        _formatter = formatter;
        _updateProgressText();
    }

    private Formatter getFormatter() {
        return (_formatter != null) ? _formatter : getDefaultFormatter();
    }

    private void _updateProgressText() {
        _txt_value.setText(getFormatter().format(getProgress()));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        _updateProgressText();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }


    private static Formatter getDefaultFormatter() {
        if (_defaultFormatter == null) {
            _defaultFormatter = new Formatter() {
                @Override
                public CharSequence format(int progress) {
                    return Integer.toString(progress);
                }
            };
        }
        return _defaultFormatter;
    }


    public static abstract class Formatter {
        public abstract CharSequence format(int progress);
    }
}
