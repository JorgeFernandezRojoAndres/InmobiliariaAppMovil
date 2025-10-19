package com.jorge.inmobiliaria2025.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.jorge.inmobiliaria2025.model.Inmueble;

/**
 * 🧩 InmobiliariaDatabase
 * Base de datos local con Room para almacenar inmuebles offline.
 * Usa patrón Singleton con método getInstance() compatible con el repositorio.
 */
@Database(entities = {Inmueble.class}, version = 2, exportSchema = false)
public abstract class InmobiliariaDatabase extends RoomDatabase {

    // Instancia única de la base de datos
    private static volatile InmobiliariaDatabase INSTANCE;

    // DAO principal
    public abstract InmuebleDao inmuebleDao();

    // ✅ Nuevo nombre estándar usado por el repositorio
    public static InmobiliariaDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (InmobiliariaDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    InmobiliariaDatabase.class,
                                    "inmobiliaria_db"
                            )
                            .fallbackToDestructiveMigration() // ⚠️ recrea la BD si cambia versión
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // 🧩 Conservamos compatibilidad con tu código viejo
    public static InmobiliariaDatabase getDatabase(Context context) {
        return getInstance(context);
    }
}
