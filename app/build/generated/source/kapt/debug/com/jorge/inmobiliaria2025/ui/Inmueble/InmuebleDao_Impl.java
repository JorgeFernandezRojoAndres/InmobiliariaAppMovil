package com.jorge.inmobiliaria2025.ui.Inmueble;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.jorge.inmobiliaria2025.model.Inmueble;
import java.lang.Boolean;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class InmuebleDao_Impl implements InmuebleDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Inmueble> __insertionAdapterOfInmueble;

  private final EntityDeletionOrUpdateAdapter<Inmueble> __deletionAdapterOfInmueble;

  private final EntityDeletionOrUpdateAdapter<Inmueble> __updateAdapterOfInmueble;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public InmuebleDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfInmueble = new EntityInsertionAdapter<Inmueble>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `inmueble` (`id`,`direccion`,`precio`,`disponible`,`tipo_id`,`tipo_nombre`,`metros_cuadrados`,`propietario_id`,`activo`,`nombre_propietario`,`imagen_url`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Inmueble entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getDireccion() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getDireccion());
        }
        statement.bindDouble(3, entity.getPrecio());
        final Integer _tmp = entity.isDisponible() == null ? null : (entity.isDisponible() ? 1 : 0);
        if (_tmp == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, _tmp);
        }
        statement.bindLong(5, entity.getTipoId());
        if (entity.getTipoNombre() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getTipoNombre());
        }
        statement.bindLong(7, entity.getMetrosCuadrados());
        statement.bindLong(8, entity.getPropietarioId());
        final Integer _tmp_1 = entity.getActivo() == null ? null : (entity.getActivo() ? 1 : 0);
        if (_tmp_1 == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, _tmp_1);
        }
        if (entity.getNombrePropietario() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getNombrePropietario());
        }
        if (entity.getImagenUrl() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getImagenUrl());
        }
      }
    };
    this.__deletionAdapterOfInmueble = new EntityDeletionOrUpdateAdapter<Inmueble>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `inmueble` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Inmueble entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfInmueble = new EntityDeletionOrUpdateAdapter<Inmueble>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `inmueble` SET `id` = ?,`direccion` = ?,`precio` = ?,`disponible` = ?,`tipo_id` = ?,`tipo_nombre` = ?,`metros_cuadrados` = ?,`propietario_id` = ?,`activo` = ?,`nombre_propietario` = ?,`imagen_url` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Inmueble entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getDireccion() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getDireccion());
        }
        statement.bindDouble(3, entity.getPrecio());
        final Integer _tmp = entity.isDisponible() == null ? null : (entity.isDisponible() ? 1 : 0);
        if (_tmp == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, _tmp);
        }
        statement.bindLong(5, entity.getTipoId());
        if (entity.getTipoNombre() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getTipoNombre());
        }
        statement.bindLong(7, entity.getMetrosCuadrados());
        statement.bindLong(8, entity.getPropietarioId());
        final Integer _tmp_1 = entity.getActivo() == null ? null : (entity.getActivo() ? 1 : 0);
        if (_tmp_1 == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, _tmp_1);
        }
        if (entity.getNombrePropietario() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getNombrePropietario());
        }
        if (entity.getImagenUrl() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getImagenUrl());
        }
        statement.bindLong(12, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM inmueble";
        return _query;
      }
    };
  }

  @Override
  public void insertar(final Inmueble inmueble) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfInmueble.insert(inmueble);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insertAll(final List<Inmueble> inmuebles) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfInmueble.insert(inmuebles);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void eliminar(final Inmueble inmueble) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfInmueble.handle(inmueble);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void actualizar(final Inmueble inmueble) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfInmueble.handle(inmueble);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteAll() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteAll.release(_stmt);
    }
  }

  @Override
  public LiveData<List<Inmueble>> obtenerTodos() {
    final String _sql = "SELECT * FROM inmueble ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"inmueble"}, false, new Callable<List<Inmueble>>() {
      @Override
      @Nullable
      public List<Inmueble> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDireccion = CursorUtil.getColumnIndexOrThrow(_cursor, "direccion");
          final int _cursorIndexOfPrecio = CursorUtil.getColumnIndexOrThrow(_cursor, "precio");
          final int _cursorIndexOfDisponible = CursorUtil.getColumnIndexOrThrow(_cursor, "disponible");
          final int _cursorIndexOfTipoId = CursorUtil.getColumnIndexOrThrow(_cursor, "tipo_id");
          final int _cursorIndexOfTipoNombre = CursorUtil.getColumnIndexOrThrow(_cursor, "tipo_nombre");
          final int _cursorIndexOfMetrosCuadrados = CursorUtil.getColumnIndexOrThrow(_cursor, "metros_cuadrados");
          final int _cursorIndexOfPropietarioId = CursorUtil.getColumnIndexOrThrow(_cursor, "propietario_id");
          final int _cursorIndexOfActivo = CursorUtil.getColumnIndexOrThrow(_cursor, "activo");
          final int _cursorIndexOfNombrePropietario = CursorUtil.getColumnIndexOrThrow(_cursor, "nombre_propietario");
          final int _cursorIndexOfImagenUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imagen_url");
          final List<Inmueble> _result = new ArrayList<Inmueble>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Inmueble _item;
            _item = new Inmueble();
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            _item.setId(_tmpId);
            final String _tmpDireccion;
            if (_cursor.isNull(_cursorIndexOfDireccion)) {
              _tmpDireccion = null;
            } else {
              _tmpDireccion = _cursor.getString(_cursorIndexOfDireccion);
            }
            _item.setDireccion(_tmpDireccion);
            final double _tmpPrecio;
            _tmpPrecio = _cursor.getDouble(_cursorIndexOfPrecio);
            _item.setPrecio(_tmpPrecio);
            final Boolean _tmpDisponible;
            final Integer _tmp;
            if (_cursor.isNull(_cursorIndexOfDisponible)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(_cursorIndexOfDisponible);
            }
            _tmpDisponible = _tmp == null ? null : _tmp != 0;
            _item.setDisponible(_tmpDisponible);
            final int _tmpTipoId;
            _tmpTipoId = _cursor.getInt(_cursorIndexOfTipoId);
            _item.setTipoId(_tmpTipoId);
            final String _tmpTipoNombre;
            if (_cursor.isNull(_cursorIndexOfTipoNombre)) {
              _tmpTipoNombre = null;
            } else {
              _tmpTipoNombre = _cursor.getString(_cursorIndexOfTipoNombre);
            }
            _item.setTipoNombre(_tmpTipoNombre);
            final int _tmpMetrosCuadrados;
            _tmpMetrosCuadrados = _cursor.getInt(_cursorIndexOfMetrosCuadrados);
            _item.setMetrosCuadrados(_tmpMetrosCuadrados);
            final int _tmpPropietarioId;
            _tmpPropietarioId = _cursor.getInt(_cursorIndexOfPropietarioId);
            _item.setPropietarioId(_tmpPropietarioId);
            final Boolean _tmpActivo;
            final Integer _tmp_1;
            if (_cursor.isNull(_cursorIndexOfActivo)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getInt(_cursorIndexOfActivo);
            }
            _tmpActivo = _tmp_1 == null ? null : _tmp_1 != 0;
            _item.setActivo(_tmpActivo);
            final String _tmpNombrePropietario;
            if (_cursor.isNull(_cursorIndexOfNombrePropietario)) {
              _tmpNombrePropietario = null;
            } else {
              _tmpNombrePropietario = _cursor.getString(_cursorIndexOfNombrePropietario);
            }
            _item.setNombrePropietario(_tmpNombrePropietario);
            final String _tmpImagenUrl;
            if (_cursor.isNull(_cursorIndexOfImagenUrl)) {
              _tmpImagenUrl = null;
            } else {
              _tmpImagenUrl = _cursor.getString(_cursorIndexOfImagenUrl);
            }
            _item.setImagenUrl(_tmpImagenUrl);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public List<Inmueble> getAll() {
    final String _sql = "SELECT * FROM inmueble";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfDireccion = CursorUtil.getColumnIndexOrThrow(_cursor, "direccion");
      final int _cursorIndexOfPrecio = CursorUtil.getColumnIndexOrThrow(_cursor, "precio");
      final int _cursorIndexOfDisponible = CursorUtil.getColumnIndexOrThrow(_cursor, "disponible");
      final int _cursorIndexOfTipoId = CursorUtil.getColumnIndexOrThrow(_cursor, "tipo_id");
      final int _cursorIndexOfTipoNombre = CursorUtil.getColumnIndexOrThrow(_cursor, "tipo_nombre");
      final int _cursorIndexOfMetrosCuadrados = CursorUtil.getColumnIndexOrThrow(_cursor, "metros_cuadrados");
      final int _cursorIndexOfPropietarioId = CursorUtil.getColumnIndexOrThrow(_cursor, "propietario_id");
      final int _cursorIndexOfActivo = CursorUtil.getColumnIndexOrThrow(_cursor, "activo");
      final int _cursorIndexOfNombrePropietario = CursorUtil.getColumnIndexOrThrow(_cursor, "nombre_propietario");
      final int _cursorIndexOfImagenUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imagen_url");
      final List<Inmueble> _result = new ArrayList<Inmueble>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Inmueble _item;
        _item = new Inmueble();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final String _tmpDireccion;
        if (_cursor.isNull(_cursorIndexOfDireccion)) {
          _tmpDireccion = null;
        } else {
          _tmpDireccion = _cursor.getString(_cursorIndexOfDireccion);
        }
        _item.setDireccion(_tmpDireccion);
        final double _tmpPrecio;
        _tmpPrecio = _cursor.getDouble(_cursorIndexOfPrecio);
        _item.setPrecio(_tmpPrecio);
        final Boolean _tmpDisponible;
        final Integer _tmp;
        if (_cursor.isNull(_cursorIndexOfDisponible)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getInt(_cursorIndexOfDisponible);
        }
        _tmpDisponible = _tmp == null ? null : _tmp != 0;
        _item.setDisponible(_tmpDisponible);
        final int _tmpTipoId;
        _tmpTipoId = _cursor.getInt(_cursorIndexOfTipoId);
        _item.setTipoId(_tmpTipoId);
        final String _tmpTipoNombre;
        if (_cursor.isNull(_cursorIndexOfTipoNombre)) {
          _tmpTipoNombre = null;
        } else {
          _tmpTipoNombre = _cursor.getString(_cursorIndexOfTipoNombre);
        }
        _item.setTipoNombre(_tmpTipoNombre);
        final int _tmpMetrosCuadrados;
        _tmpMetrosCuadrados = _cursor.getInt(_cursorIndexOfMetrosCuadrados);
        _item.setMetrosCuadrados(_tmpMetrosCuadrados);
        final int _tmpPropietarioId;
        _tmpPropietarioId = _cursor.getInt(_cursorIndexOfPropietarioId);
        _item.setPropietarioId(_tmpPropietarioId);
        final Boolean _tmpActivo;
        final Integer _tmp_1;
        if (_cursor.isNull(_cursorIndexOfActivo)) {
          _tmp_1 = null;
        } else {
          _tmp_1 = _cursor.getInt(_cursorIndexOfActivo);
        }
        _tmpActivo = _tmp_1 == null ? null : _tmp_1 != 0;
        _item.setActivo(_tmpActivo);
        final String _tmpNombrePropietario;
        if (_cursor.isNull(_cursorIndexOfNombrePropietario)) {
          _tmpNombrePropietario = null;
        } else {
          _tmpNombrePropietario = _cursor.getString(_cursorIndexOfNombrePropietario);
        }
        _item.setNombrePropietario(_tmpNombrePropietario);
        final String _tmpImagenUrl;
        if (_cursor.isNull(_cursorIndexOfImagenUrl)) {
          _tmpImagenUrl = null;
        } else {
          _tmpImagenUrl = _cursor.getString(_cursorIndexOfImagenUrl);
        }
        _item.setImagenUrl(_tmpImagenUrl);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
