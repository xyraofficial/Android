package com.xyra.ai;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;
import java.util.Random;

public class NotificationService extends BroadcastReceiver {
    
    private static final String TAG = "NotificationService";
    private static final String CHANNEL_ID = "xyra_notifications";
    private static final String CHANNEL_NAME = "XyraAI Notifications";
    private static final int NOTIFICATION_ID = 1001;
    private static final String PREFS_NAME = "xyra_notifications";
    private static final String KEY_ENABLED = "notifications_enabled";
    private static final String KEY_HOUR = "notification_hour";
    private static final String KEY_MINUTE = "notification_minute";
    
    private static final String[] DAILY_TIPS = {
        "Tip: Kamu bisa mengirim gambar ke saya untuk dianalisis!",
        "Tahukah kamu? Saya bisa membantu menulis dan mengedit kode.",
        "Coba tanya tentang topik yang sedang kamu pelajari!",
        "Tip: Gunakan markdown untuk format pesan yang lebih baik.",
        "Butuh bantuan brainstorming? Aku siap membantu!",
        "Tip: Bookmark pesan penting agar mudah ditemukan nanti.",
        "Aku bisa membantu menerjemahkan berbagai bahasa.",
        "Tip: Coba gunakan voice input untuk bertanya lebih cepat!",
        "Ada pertanyaan coding? Aku bisa jelaskan step by step.",
        "Tip: Share percakapan menarik ke temanmu!",
        "Butuh ide kreatif? Aku bisa bantu brainstorming.",
        "Tip: Pilih persona yang sesuai dengan kebutuhanmu.",
        "Aku bisa membantu meringkas artikel panjang.",
        "Tip: Tanya apapun, aku akan berusaha menjawab!",
        "Butuh motivasi? Tanyakan quote inspiratif padaku!"
    };
    
    private static final String[] GREETINGS = {
        "Hai! Ada yang bisa saya bantu hari ini?",
        "Selamat pagi! Yuk mulai hari dengan produktif!",
        "Halo! Saya siap membantu kapan saja.",
        "Hi! Ada pertanyaan atau ide yang ingin didiskusikan?",
        "Selamat siang! Butuh bantuan dengan sesuatu?"
    };
    
    @Override
    public void onReceive(Context context, Intent intent) {
        showDailyNotification(context);
    }
    
    public static void showDailyNotification(Context context) {
        createNotificationChannel(context);
        
        Random random = new Random();
        String message;
        
        if (random.nextBoolean()) {
            message = DAILY_TIPS[random.nextInt(DAILY_TIPS.length)];
        } else {
            message = GREETINGS[random.nextInt(GREETINGS.length)];
        }
        
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT);
        }
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_xyra_avatar)
            .setContentTitle("XyraAI")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);
        
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }
    
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Tips harian dan reminder dari XyraAI");
            
            NotificationManager notificationManager = 
                context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    public static void scheduleDailyNotification(Context context, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        Intent intent = new Intent(context, NotificationService.class);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT);
        }
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        if (alarmManager != null) {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            );
        }
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .putBoolean(KEY_ENABLED, true)
            .putInt(KEY_HOUR, hour)
            .putInt(KEY_MINUTE, minute)
            .apply();
    }
    
    public static void cancelDailyNotification(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        Intent intent = new Intent(context, NotificationService.class);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT);
        }
        
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ENABLED, false).apply();
    }
    
    public static boolean isNotificationEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ENABLED, false);
    }
    
    public static int getNotificationHour(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_HOUR, 9);
    }
    
    public static int getNotificationMinute(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_MINUTE, 0);
    }
    
    public static void showInstantNotification(Context context, String title, String message) {
        createNotificationChannel(context);
        
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT);
        }
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_xyra_avatar)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);
        
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            int uniqueId = (int) System.currentTimeMillis();
            notificationManager.notify(uniqueId, builder.build());
        }
    }
}
