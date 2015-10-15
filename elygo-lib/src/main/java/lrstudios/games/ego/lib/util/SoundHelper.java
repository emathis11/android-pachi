package lrstudios.games.ego.lib.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import lrstudios.games.ego.lib.R;


public class SoundHelper {
    private static final String TAG = SoundHelper.class.getSimpleName();

    private static final int MAX_STREAMS = 4;

    private Context _context;
    private SoundPool _soundPool;
    private boolean _enabled = true;

    private int _sound_stone;
    private int _sound_pass;


    public SoundHelper(Context context) {
        _context = context;

        _soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        _sound_stone = _soundPool.load(context, R.raw.stone, 1);
        _sound_pass = _soundPool.load(context, R.raw.pass, 1);
    }

    public void release() {
        _soundPool.release();
        _soundPool = null;
    }

    public void playStoneSound() {
        playSound(_sound_stone);
    }

    public void playPassSound() {
        playSound(_sound_pass);
    }

    public void setSoundsEnabled(boolean enable) {
        _enabled = enable;
    }

    private void playSound(int soundId) {
        if (_enabled && _soundPool != null)
            _soundPool.play(soundId, 0.99f, 0.99f, 0, 0, 1.0f);
    }
}
