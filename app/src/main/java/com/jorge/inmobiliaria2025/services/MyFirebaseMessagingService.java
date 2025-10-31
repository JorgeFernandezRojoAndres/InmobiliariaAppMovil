package com.jorge.inmobiliaria2025.services;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.ui.nav.MainActivity;

/**
 * 🔔 MyFirebaseMessagingService
 * Recibe notificaciones push (FCM) incluso con la app cerrada o en segundo plano.
 * Muestra una notificación en la barra del sistema y maneja la creación del canal.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM-Service";
    private static final String CHANNEL_ID = "pagos_channel";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.i(TAG, "🪪 Nuevo token FCM generado: " + token);

        // ⚙️ Aquí podrías enviar el token a tu backend si querés asociarlo a un usuario
        // ApiService api = RetrofitClient.getInstance(getApplicationContext()).create(ApiService.class);
        // api.enviarTokenFCM("Bearer " + tokenJWT, token).enqueue(...);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "📩 Mensaje recibido de FCM: " + remoteMessage.getData());

        // 🚨 Verificar que el mensaje no sea nulo o vacío
        if (remoteMessage.getData() == null || remoteMessage.getData().isEmpty()) {
            Log.w(TAG, "⚠️ Mensaje FCM vacío o sin datos, se ignora para evitar crash.");
            return;
        }

        // ✅ Datos por defecto
        String title = "Nuevo pago registrado";
        String body = "Se ha acreditado un nuevo pago en tu cuenta.";
        String screen = "pagos"; // 🧭 destino por defecto

        // 📦 Si el mensaje contiene notificación, la usamos
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle() != null
                    ? remoteMessage.getNotification().getTitle()
                    : title;
            body = remoteMessage.getNotification().getBody() != null
                    ? remoteMessage.getNotification().getBody()
                    : body;
        }

        // 📦 Si el mensaje vino como data message
        if (remoteMessage.getData().containsKey("title"))
            title = remoteMessage.getData().get("title");
        if (remoteMessage.getData().containsKey("body"))
            body = remoteMessage.getData().get("body");
        if (remoteMessage.getData().containsKey("screen"))
            screen = remoteMessage.getData().get("screen");

        // 🔔 Mostrar notificación
        mostrarNotificacion(title, body, screen);
    }

    private void mostrarNotificacion(String title, String body, String screen) {
        // 🔧 Crear canal si no existe (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notificaciones de pagos",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Avisos de nuevos pagos registrados");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        // 📦 Intent al abrir la notificación (abre MainActivity y redirige según “screen”)
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("screen", screen);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // 🧱 Construcción de la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        // 🚨 Verificar permiso de notificaciones (Android 13+)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "⚠️ Sin permiso POST_NOTIFICATIONS — no se pudo mostrar la notificación");
            return;
        }

        try {
            NotificationManagerCompat.from(this)
                    .notify((int) System.currentTimeMillis(), builder.build());
            Log.i(TAG, "✅ Notificación mostrada correctamente: " + title);
        } catch (Exception e) {
            Log.e(TAG, "💥 Error al mostrar la notificación: " + e.getMessage(), e);
        }
    }
}
