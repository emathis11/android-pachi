package net.lrstudios.android.pachi;

import android.app.Application;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;


@ReportsCrashes(
        formKey = "",
        formUri = "http://www.lr-studios.net/scripts/reports-crashes/index.php")
public class MyApplication extends Application
{
    @Override
    public void onCreate()
    {
        ACRA.init(this);
        super.onCreate();
    }
}
