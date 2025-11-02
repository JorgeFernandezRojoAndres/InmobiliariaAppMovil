package com.jorge.inmobiliaria2025.localdata;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.jorge.inmobiliaria2025.ui.Inmueble.InmuebleDao;
import com.jorge.inmobiliaria2025.ui.Inmueble.InmuebleDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class InmobiliariaDatabase_Impl extends InmobiliariaDatabase {
  private volatile InmuebleDao _inmuebleDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `inmueble` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `direccion` TEXT, `precio` REAL NOT NULL, `disponible` INTEGER, `tipo_id` INTEGER NOT NULL, `tipo_nombre` TEXT, `metros_cuadrados` INTEGER NOT NULL, `propietario_id` INTEGER NOT NULL, `activo` INTEGER, `nombre_propietario` TEXT, `imagen_url` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '6481be119486eb32361733a314e37d9a')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `inmueble`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsInmueble = new HashMap<String, TableInfo.Column>(11);
        _columnsInmueble.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInmueble.put("direccion", new TableInfo.Column("direccion", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInmueble.put("precio", new TableInfo.Column("precio", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInmueble.put("disponible", new TableInfo.Column("disponible", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInmueble.put("tipo_id", new TableInfo.Column("tipo_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInmueble.put("tipo_nombre", new TableInfo.Column("tipo_nombre", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInmueble.put("metros_cuadrados", new TableInfo.Column("metros_cuadrados", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInmueble.put("propietario_id", new TableInfo.Column("propietario_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInmueble.put("activo", new TableInfo.Column("activo", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInmueble.put("nombre_propietario", new TableInfo.Column("nombre_propietario", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInmueble.put("imagen_url", new TableInfo.Column("imagen_url", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysInmueble = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesInmueble = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoInmueble = new TableInfo("inmueble", _columnsInmueble, _foreignKeysInmueble, _indicesInmueble);
        final TableInfo _existingInmueble = TableInfo.read(db, "inmueble");
        if (!_infoInmueble.equals(_existingInmueble)) {
          return new RoomOpenHelper.ValidationResult(false, "inmueble(com.jorge.inmobiliaria2025.model.Inmueble).\n"
                  + " Expected:\n" + _infoInmueble + "\n"
                  + " Found:\n" + _existingInmueble);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "6481be119486eb32361733a314e37d9a", "d5df87ee87c4a967108110de37a47e87");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "inmueble");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `inmueble`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(InmuebleDao.class, InmuebleDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public InmuebleDao inmuebleDao() {
    if (_inmuebleDao != null) {
      return _inmuebleDao;
    } else {
      synchronized(this) {
        if(_inmuebleDao == null) {
          _inmuebleDao = new InmuebleDao_Impl(this);
        }
        return _inmuebleDao;
      }
    }
  }
}
