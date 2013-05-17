package net.lrstudios.android.pachi;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import com.gokgs.client.gtp.GtpClient;
import com.gokgs.client.gtp.Options;
import lrstudios.games.ego.lib.ExternalGtpEngine;
import lrstudios.games.ego.lib.Utils;
import lrstudios.util.android.AndroidUtils;
import lrstudios.util.android.ui.BetterFragmentActivity;
import org.apache.http.util.ByteArrayBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


public class KgsGtpActivity extends BetterFragmentActivity
{
    private static final String TAG = "KgsGtpActivity";

    private PachiEngine _engine;
    private EngineStreamHandler _streamHandler;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kgs_gtp_activity);

        Properties props = new Properties();
        props.setProperty("process_args", "-t _900 max_tree_size=512");

        _engine = new PachiEngine(this);
        _engine.init(props);

        Options options;
        try {
            Properties kgsProps = AndroidUtils.loadPropertiesFromAssets(KgsGtpActivity.this, "kgsGtp.properties");
            options = new Options(kgsProps, "KgsGtp");
        }
        catch (IOException e) {
            e.printStackTrace();
            showToast(R.string.err_internal);
            finish();
            return;
        }

        Log.i(TAG, "Connecting to KGS...");
        _streamHandler = new EngineStreamHandler(_engine);
        final GtpClient client = new GtpClient(
                _streamHandler.getInputStream(), _streamHandler.getOutputStream(), options);
        new Thread() {
            @Override
            public void run() {
                client.go();
            }
        }.start();
    }


    // TODO Use these streams directly in ExternalGtpEngine to encapsulate the process streams
    // TODO override all stream methods
    // TODO will probably restart the engine two times in a row if it crashes
    // TODO the sync can cause deadlocks
    private static final class EngineStreamHandler {
        private ExternalGtpEngine _engine;
        private EngineInputStream _in = new EngineInputStream();
        private EngineOutputStream _out = new EngineOutputStream();
        private InputStream _processIn;
        private OutputStream _processOut;
        private final Object _lockObj = new Object();

        public EngineStreamHandler(ExternalGtpEngine engine) {
            _engine = engine;
            updateStreams();
        }

        private void updateStreams() {
            _processIn = _engine.getInputStream();
            _processOut = _engine.getOutputStream();
        }

        public InputStream getInputStream() {
            return _in;
        }

        public OutputStream getOutputStream() {
            return _out;
        }


        private final class EngineInputStream extends InputStream {
            @Override
            public int read() throws IOException {
                int b = -1;
                synchronized (_lockObj) {
                    try {
                        b = _processIn.read();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (b == -1) {
                        _engine.restart();
                        _engine.replayGame();
                        updateStreams();
                        return _processIn.read();
                    }
                }
                return b;
            }

            @Override
            public int available() throws IOException {
                return _processIn.available();
            }

            @Override
            public int read(byte[] buffer, int offset, int length) throws IOException {
                int pos = offset;
                int b = read();
                if (b == -1)
                    return -1;
                buffer[pos++] = (byte) b;
                while (available() > 0 && pos < length) {
                    b = read();
                    buffer[pos++] = (byte) b;
                }
                return pos - offset;
            }
        }

        private final class EngineOutputStream extends OutputStream {
            @Override
            public void write(int i) throws IOException {
                    try {
                        _processOut.write(i);
                    }
                    catch (IOException e) {
                        synchronized (_lockObj) {
                            e.printStackTrace();
                            _engine.restart();
                            _engine.replayGame();
                            updateStreams();
                        }
                        _processOut.write(i);
                    }
            }

            @Override
            public void write(byte[] buffer) throws IOException {
                write(buffer, 0, buffer.length);
            }

            @Override
            public void write(byte[] buffer, int offset, int count) throws IOException {
                int maxOffset = offset + count;
                for (int i = offset; i < maxOffset; i++)
                    write(buffer[i]);
            }
        }
    }
}