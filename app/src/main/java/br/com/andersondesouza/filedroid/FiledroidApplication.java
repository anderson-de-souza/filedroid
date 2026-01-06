package br.com.andersondesouza.filedroid;

import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.PrintWriter;
import java.io.StringWriter;

public class FiledroidApplication extends Application {

    public static final String NOTIFICATION_CHANNEL_ID_EXCEPTIONS = "exceptions";

    @Override
    public void onCreate() {
        super.onCreate();

        final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            String stackTrace = stackTraceToString(exception);
            Intent intent = new Intent(getApplicationContext(), ExceptionReceiver.class);
            intent.putExtra("message", exception.getMessage() != null ? exception.getMessage() : "No Message");
            intent.putExtra("stackTrace", stackTrace);
            AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, PendingIntent.getBroadcast(this, 1010, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE));
            defaultUncaughtExceptionHandler.uncaughtException(thread, exception);

        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_EXCEPTIONS, "Exceptions", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

    }

    private String stackTraceToString(Throwable exception) {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        do {
            exception.printStackTrace(printWriter);
        } while ((exception = exception.getCause()) != null);

        String result = stringWriter.toString();

        return result;

    }

}
