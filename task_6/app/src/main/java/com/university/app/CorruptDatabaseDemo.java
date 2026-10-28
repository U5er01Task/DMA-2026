package com.university.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.io.*;

public final class CorruptDatabaseDemo {
  public static void run(Context c) {
    File db = new File(c.getCacheDir(), "broken_test.db");
    try (FileOutputStream out = new FileOutputStream(db)) {
      out.write("not a database".getBytes("UTF-8"));
    } catch (IOException e) {
      Log.e("A80_47", "setup", e);
      return;
    }
    try {
      SQLiteDatabase wrong =
          SQLiteDatabase.openDatabase(db.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
      try {
        wrong.rawQuery("PRAGMA integrity_check", null).close();
      } finally {
        wrong.close();
      }
    } catch (Exception broken) {
      Log.w("A80_47", "corrupt detected: " + broken);
      File saved = new File(c.getCacheDir(), "recovery_copy.bak");
      if (!db.renameTo(saved)) Log.e("A80_47", "backup failed");
      try (SQLiteDatabase rebuilt = SQLiteDatabase.openOrCreateDatabase(db, null)) {
        rebuilt.execSQL("CREATE TABLE IF NOT EXISTS recovered(_id INTEGER PRIMARY KEY,title TEXT)");
        Log.i("A80_47", "database recovered; damaged copy=" + saved.getAbsolutePath());
      }
    }
  }
}
