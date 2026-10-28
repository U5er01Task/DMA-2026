package com.university.app;

import android.content.*;
import android.database.Cursor;
import android.util.Log;
import java.io.*;
import org.json.*;

final class JSONArrayCompat {
  static void save(Context c, Cursor cursor) throws Exception {
    JSONArray a = new JSONArray();
    if (cursor != null)
      while (cursor.moveToNext()) {
        JSONObject o = new JSONObject();
        for (int k = 0; k < cursor.getColumnCount(); k++)
          o.put(cursor.getColumnName(k), cursor.getString(k));
        a.put(o);
      }
    File f = new File(c.getFilesDir(), "backup.json");
    try (FileOutputStream s = new FileOutputStream(f)) {
      s.write(a.toString(2).getBytes("UTF-8"));
    }
    Log.i("A80_36", "Saved " + f.getAbsolutePath());
  }
}
