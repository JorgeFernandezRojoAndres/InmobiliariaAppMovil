package com.jorge.inmobiliaria2025.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.jorge.inmobiliaria2025.model.Inmueble;

import java.util.List;

@Dao
public interface InmuebleDao {

    // 🔹 Inserta un nuevo inmueble (reemplaza si ya existe el mismo id)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertar(Inmueble inmueble);

    // 🔹 Actualiza un inmueble existente
    @Update
    void actualizar(Inmueble inmueble);

    // 🔹 Elimina un inmueble (nuevo método para usar en ViewModel)
    @Delete
    void eliminar(Inmueble inmueble);

    // 🔹 Obtiene todos los inmuebles, ordenados por id descendente
    // ⚠️ Tabla corregida: "inmueble" (en singular)
    @Query("SELECT * FROM inmueble ORDER BY id DESC")
    LiveData<List<Inmueble>> obtenerTodos();
}
