package com.gov.ducadegliabruzzitreviso.ducaapp.classes;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.gov.ducadegliabruzzitreviso.ducaapp.R;
import com.gov.ducadegliabruzzitreviso.ducaapp.activities.CircolariActivity;
import com.gov.ducadegliabruzzitreviso.ducaapp.activities.FeedActivity;
import com.gov.ducadegliabruzzitreviso.ducaapp.activities.InfoActivity;
import com.gov.ducadegliabruzzitreviso.ducaapp.interfaces.Filterable;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class NotifWorker extends Worker {
    private static String data_path;
    public NotifWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        data_path = getInputData().getString("data_path");
        return checkFeed() && checkCircolari() ? Result.success() : Result.failure();
    }

    private boolean checkFeed(){
        File file;
        boolean file_exists;
        URLConnection con;
        InputStream remote_is;
        InputStream local_is;
        try{
            con = (new URL(getInputData().getString("url_feed"))).openConnection();
            remote_is = con.getInputStream();
            file = new File(data_path+"/feed_file.xml");
            file_exists = file.exists();
            if(file_exists) local_is = new FileInputStream(file);
            else local_is = null;
        }catch(MalformedURLException e){
            return false;
        }catch(IOException e){
            return false;
        }
        XMLFeedParser parser = new XMLFeedParser();
        List<FeedItem> remote_items, local_items;
        try{
            remote_items = parser.parse(remote_is);
            local_items = parser.parse(local_is);
        }
        catch(XmlPullParserException e){ return false; }
        catch (IOException e){ return false; }
        List<FeedItem> news = new ArrayList<>();
        for(FeedItem f : remote_items) if(!local_items.contains(f)) news.add(f);
        if(news.size() == 1) sendNotificationFeed("Duca Degli Abruzzi | Nuova notizia", news.get(0));
        if(news.size() > 1) sendBigNotificationFeed(news);
        return true;
    }

    private boolean checkCircolari(){
        File file;
        boolean file_exists;
        URLConnection con;
        InputStream remote_is;
        InputStream local_is;
        try{
            con = (new URL(getInputData().getString("url_circolari"))).openConnection();
            remote_is = con.getInputStream();
            file = new File(data_path+"/circolari.htm");
            file_exists = file.exists();
            if(file_exists) local_is = new FileInputStream(file);
            else local_is = null;
        }catch(MalformedURLException e){
            return false;
        }catch(IOException e){
            return false;
        }
        CircolariParser parser = new CircolariParser();
        List<Filterable> remote_items, local_items;
        try{
            remote_items = parser.parse(remote_is);
            local_items = parser.parse(local_is);
        }
        catch(XmlPullParserException e){ return false; }
        catch (IOException e){ return false; }
        List<Filterable> news = new ArrayList<>();
        for(Filterable f : remote_items){
            for(Filterable l : local_items) if(l.equals(f)) news.add(f);
        }
        if(news.size() == 1) sendNotificationCircolari("Duca degli Abruzzi | Nuova Circolare", ((Circolare)news.get(0)).titolo);
        if(news.size() > 1) sendBigNotificationCircolari(news);
        return true;
    }

    public void sendNotificationFeed(String title, FeedItem item){
        Intent resultIntent = new Intent(getApplicationContext(), InfoActivity.class);
        resultIntent.putExtra("title", item.title);
        resultIntent.putExtra("description", item.description);
        resultIntent.putExtra("date", item.date);
        resultIntent.putExtra("link", item.link);
        resultIntent.putExtra("notif", true);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "DucaApp7455")
                .setContentTitle(title)
                .setContentText(item.title)
                .setSmallIcon(R.drawable.duca_app_notif)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);
        notificationManager.notify(7455, notification.build());
    }

    public void sendNotificationCircolari(String title, String description){
        Intent resultIntent = new Intent(getApplicationContext(), CircolariActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "DucaApp2690")
                .setContentTitle(title)
                .setContentText(description)
                .setSmallIcon(R.drawable.duca_app_notif)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);
        notificationManager.notify(2690, notification.build());
    }

    public void sendBigNotificationCircolari(List news){
        Intent resultIntent = new Intent(getApplicationContext(), CircolariActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "DucaApp2690")
                .setContentTitle("Duca degli Abruzzi | Nuove Circolari")
                .setSmallIcon(R.drawable.duca_app_notif)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);
        if(news.size() <= 6) notification.setContentText("Ci sono " + news.size() + " nuove circolari");
        else notification.setContentText("Ci sono almeno 6 nuove circolari");
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        for(int i = 0; i < news.size(); i++){
            style.addLine(((Circolare)news.get(i)).titolo);
        }
        notification.setStyle(style);
        notificationManager.notify(2690, notification.build());
    }

    public void sendBigNotificationFeed(List news){
        Intent resultIntent = new Intent(getApplicationContext(), FeedActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "DucaApp7455")
                .setContentTitle("Duca degli Abruzzi | Nuove notizie")
                .setSmallIcon(R.drawable.duca_app_notif)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);
        if(news.size() <= 6) notification.setContentText("Ci sono " + news.size() + " nuove notizie");
        else notification.setContentText("Ci sono almeno 6 nuove notizie");
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        for(int i = 0; i < news.size(); i++){
            style.addLine(((FeedItem)news.get(i)).title);
        }
        notification.setStyle(style);
        notificationManager.notify(7455, notification.build());
    }
}