package lrstudios.games.ego.lib;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;


/**
 * Provides functions to start a GTP engine in another process and communicate with it.
 */
public abstract class ExternalGtpEngine extends GtpEngine {

    protected static Process _engineProcess;

    private static final String TAG = "ExternalGtpEngine";

    private Thread _stdErrThread;
    private Thread _exitThread;
    private OutputStreamWriter _writer;
    private BufferedReader _reader;
    private boolean _isRunning;
    private Properties _properties;


    /**
     * Returns the location of the engine executable file.
     */
    protected abstract File getEngineFile();


    public ExternalGtpEngine(Context context) {
        super(context);
    }

    @Override
    public boolean init(Properties properties) {
        _properties = properties;
        try {
            if (!_isRunning) {
                String propArgs = properties.getProperty("process_args");
                String[] processArgs = (propArgs == null) ? getProcessArgs() : propArgs.split(" ");
                int len = processArgs.length;
                String[] args = new String[len + 1];
                args[0] = getEngineFile().getAbsolutePath();
                System.arraycopy(processArgs, 0, args, 1, len);
                _engineProcess = new ProcessBuilder(args).start();
                _isRunning = true;
            }
            else {
                Log.d(TAG, "Called init() again");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        InputStream is = _engineProcess.getInputStream();
        _reader = new BufferedReader(new InputStreamReader(is), 8192);
        _writer = new OutputStreamWriter(_engineProcess.getOutputStream());

        if (_stdErrThread != null && _stdErrThread.isAlive())
            _stdErrThread.interrupt();
        _stdErrThread = new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream is = _engineProcess.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8192);
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Log.e(TAG, "[Err] " + line);
                        if (Thread.currentThread().isInterrupted())
                            return;
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        _stdErrThread.start();

        // Starts a thread to restart the Pachi process if it was killed
        _exitThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process ep = _engineProcess;
                    if (ep != null)
                        ep.waitFor();
                    _isRunning = false;
                    Log.w(TAG, "##### Engine process has exited with code " + (ep != null ? ep.exitValue() : "[null]"));
                }
                catch (InterruptedException ignored) {
                }
            }
        });
        _exitThread.start();
        return true;
    }

    /**
     * Restarts the engine process with the same properties given to the last init() call.
     */
    public boolean restart() {
        return init(_properties);
    }

    /**
     * Clears the board and send commands to the engine to replay the whole game.
     */
    public void replayGame() throws IOException {
        GoGame game = getGame();
        _intSendGtpCommand("clear_board");
        _intSendGtpCommand("boardsize " + game.board.getSize());
        _intSendGtpCommand("komi " + ((int) (game.info.komi * 10.0) / 10.0));
        if (game.info.handicap > 0)
            _intSendGtpCommand("fixed_handicap " + game.info.handicap);

        GameNode node = game.getBaseNode();
        while (node.nextNodes.size() > 0) {
            GameNode nextNode = node.nextNodes.get(0);
            if (nextNode.color != GoBoard.EMPTY) {
                _intSendGtpCommand(String.format("play %s %s",
                        _getColorString(nextNode.color), _point2str(nextNode.x, nextNode.y)));
            }
            node = nextNode;
        }
    }


    @Override
    public String sendGtpCommand(String command) {
        try {
            return _intSendGtpCommand(command);
        }
        catch (IOException e) {
            e.printStackTrace();
            // An IOException means that Android killed the process, so we start it
            // again and replay the whole game
            if (restart()) {
                try {
                    replayGame();
                    return _intSendGtpCommand(command);
                }
                catch (IOException e2) {
                    Log.e(TAG, "[sendGtpCommand] Unable to restart the engine : cannot replay moves");
                    e2.printStackTrace();
                    return null;
                }
            }
            else {
                Log.e(TAG, "[sendGtpCommand] Unable to restart the engine : init() failed");
                return null;
            }
        }
    }

    private String _intSendGtpCommand(String command) throws IOException {
        Log.v(TAG, "Send: " + command);
        _writer.write(command + "\n");
        _writer.flush();
        String res;
        char ch;
        do {
            res = _reader.readLine();
            if (res == null)
                throw new IOException("The process is not running");
/*            res += "\n";
            if (command.contains("list_commands")) { // This is the only multi-line command
                String line;
                while ((line = _reader.readLine()).length() > 2)
                    res += line + "\n";
                res += "\n";
            }*/
            Log.v(TAG, " >> " + res);
            ch = res.length() > 0 ? res.charAt(0) : 0;
        } while (ch != '=' && ch != '?');
        return res;
    }

    public InputStream getInputStream() {
        return _engineProcess.getInputStream();
    }

    public OutputStream getOutputStream() {
        return _engineProcess.getOutputStream();
    }

    /**
     * Override this to return custom command line arguments. This will be called just before
     * starting the engine process.
     */
    protected String[] getProcessArgs() {
        return new String[0];
    }
}
