package br.com.andersondesouza.filedroid.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import br.com.andersondesouza.filedroid.FiledroidApplication;
import br.com.andersondesouza.filedroid.R;
import br.com.andersondesouza.filedroid.activity.ExceptionActivity;

public class ExceptionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent data) {
        Intent intent = new Intent(context, ExceptionActivity.class);
        intent.putExtra("message", data.getStringExtra("message"));
        intent.putExtra("stackTrace", data.getStringExtra("stackTrace"));

        Notification notification = new NotificationCompat.Builder(context, FiledroidApplication.NOTIFICATION_CHANNEL_ID_EXCEPTIONS)
            .setContentTitle("Exception Caught")
            .setContentText(data.getStringExtra("message"))
            .setSmallIcon(R.drawable.ic_announcement)
            .setContentIntent(PendingIntent.getActivity(context, 1010, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE))
            .build();

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(9090, notification);
    }

}
