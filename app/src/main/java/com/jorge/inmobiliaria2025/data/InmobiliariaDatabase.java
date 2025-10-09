package com.jorge.inmobiliaria2025.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.jorge.inmobiliaria2025.model.Inmueble;

@Database(entities = {Inmueble.class}, version = 1, exportSchema = false)
public abstract class InmobiliariaDatabase extends RoomDatabase {

    private static volatile InmobiliariaDatabase INSTANCE;

    public abstract InmuebleDao inmuebleDao();

    public static InmobiliariaDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (InmobiliariaDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    InmobiliariaDatabase.class, "inmobiliaria_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
