package com.university.app;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.net.Uri;
import java.io.File;

public final class NotesProvider extends ContentProvider {
  public static final String AUTHORITY = "com.university.app.notes";
  public static final Uri URI = Uri.parse("content://" + AUTHORITY + "/notes");
  private static final UriMatcher MATCH = new UriMatcher(-1);

  static {
    MATCH.addURI(AUTHORITY, "notes", 1);
    MATCH.addURI(AUTHORITY, "notes/#", 2);
    MATCH.addURI(AUTHORITY, "notes/category/*", 3);
    MATCH.addURI(AUTHORITY, "notes/search", 4);
  }

  private final Object writeLock = new Object();
  private SQLiteOpenHelper helper;

  private SQLiteDatabase database(boolean writable) {
    try {
      return writable ? helper.getWritableDatabase() : helper.getReadableDatabase();
    } catch (SQLiteDatabaseCorruptException error) {
      recover(error);
      return writable ? helper.getWritableDatabase() : helper.getReadableDatabase();
    }
  }

  private void recover(SQLiteDatabaseCorruptException cause) {
    synchronized (writeLock) {
      android.util.Log.e("A80_47", "Database corruption detected; preserving a copy", cause);
      try {
        helper.close();
      } catch (Exception ignored) {
      }
      File db = getContext().getDatabasePath("notes.db");
      File backup =
          new File(
              getContext().getFilesDir(), "notes_corrupt_" + System.currentTimeMillis() + ".db");
      try (java.io.InputStream in = new java.io.FileInputStream(db);
          java.io.OutputStream out = new java.io.FileOutputStream(backup)) {
        byte[] buffer = new byte[8192];
        int n;
        while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
        android.util.Log.w("A80_47", "backed up corrupt file: " + backup);
      } catch (Exception e) {
        android.util.Log.e("A80_47", "cannot copy corrupted file", e);
      }
      if (!getContext().deleteDatabase("notes.db"))
        android.util.Log.w("A80_47", "deleteDatabase returned false");
    }
  }

  private static void setupFts(SQLiteDatabase db) {
    db.execSQL("CREATE VIRTUAL TABLE notes_fts USING fts4(title,note_id,notindexed=note_id)");
    db.execSQL(
        "CREATE TRIGGER notes_ai AFTER INSERT ON notes BEGIN INSERT INTO"
            + " notes_fts(rowid,title,note_id) VALUES(new._id,new.title,new._id); END");
    db.execSQL(
        "CREATE TRIGGER notes_ad AFTER DELETE ON notes BEGIN DELETE FROM notes_fts WHERE"
            + " rowid=old._id; END");
    db.execSQL(
        "CREATE TRIGGER notes_au AFTER UPDATE ON notes BEGIN DELETE FROM notes_fts WHERE"
            + " rowid=old._id; INSERT INTO notes_fts(rowid,title,note_id)"
            + " VALUES(new._id,new.title,new._id); END");
  }

  @Override
  public boolean onCreate() {
    android.util.Log.i("A80_6_1", "ContentProvider.onCreate time=" + System.nanoTime());
    helper =
        new SQLiteOpenHelper(getContext(), "notes.db", null, 2) {
          @Override
          public void onCreate(SQLiteDatabase db) {
            db.execSQL(
                "CREATE TABLE notes (_id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT NOT"
                    + " NULL,category TEXT NOT NULL DEFAULT '')");
            setupFts(db);
          }

          @Override
          public void onUpgrade(SQLiteDatabase db, int o, int n) {
            if (o < 2) {
              setupFts(db);
              db.execSQL(
                  "INSERT INTO notes_fts(rowid,title,note_id) SELECT _id,title,_id FROM notes");
            }
          }
        };
    return true;
  }

  @Override
  public Cursor query(Uri u, String[] projection, String selection, String[] args, String order) {
    int m = MATCH.match(u);
    if (m < 0) throw new IllegalArgumentException(u.toString());
    String where = selection;
    String[] a = args;
    if (m == 2) {
      where = append(where, "_id=?");
      a = add(args, u.getLastPathSegment());
    }
    if (m == 3) {
      where = append(where, "category=?");
      a = add(args, u.getLastPathSegment());
    }
    if (m == 4) {
      String q = u.getQueryParameter("q");
      if (q == null || q.trim().isEmpty()) q = "__none__";
      where = append(where, "_id IN (SELECT note_id FROM notes_fts WHERE title MATCH ?)");
      a = add(a, q);
    }
    String limit = null;
    String l = u.getQueryParameter("limit"), off = u.getQueryParameter("offset");
    boolean powerSave =
        getContext().getSharedPreferences("status", 0).getBoolean("power_save", false);
    if (powerSave && l == null) l = "10";
    if (l != null || off != null) {
      int count = integer(l, 20, 1, 200);
      if (powerSave) count = Math.min(count, 10);
      int skip = integer(off, 0, 0, 1000000);
      limit = skip + "," + count;
    }
    Cursor c;
    try {
      c = database(false).query("notes", projection, where, a, null, null, order, limit);
    } catch (SQLiteDatabaseCorruptException e) {
      recover(e);
      c = database(false).query("notes", projection, where, a, null, null, order, limit);
    }
    c.setNotificationUri(getContext().getContentResolver(), URI);
    return c;
  }

  private static int integer(String s, int fallback, int min, int max) {
    if (s == null) return fallback;
    try {
      int n = Integer.parseInt(s);
      if (n < min || n > max) throw new IllegalArgumentException("pagination range");
      return n;
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid pagination", e);
    }
  }

  private static String append(String old, String s) {
    return old == null || old.isEmpty() ? s : "(" + old + ") AND " + s;
  }

  private static String[] add(String[] a, String value) {
    int n = a == null ? 0 : a.length;
    String[] out = new String[n + 1];
    if (n > 0) System.arraycopy(a, 0, out, 0, n);
    out[n] = value;
    return out;
  }

  @Override
  public Uri insert(Uri u, ContentValues values) {
    if (MATCH.match(u) != 1) throw new IllegalArgumentException("directory expected");
    if (values == null
        || !values.containsKey("title")
        || values.getAsString("title") == null
        || values.getAsString("title").trim().isEmpty())
      throw new IllegalArgumentException("title required");
    long id;
    synchronized (writeLock) {
      id = database(true).insertOrThrow("notes", null, values);
    }
    getContext().getContentResolver().notifyChange(URI, null);
    return ContentUris.withAppendedId(URI, id);
  }

  @Override
  public int update(Uri u, ContentValues v, String s, String[] a) {
    int m = MATCH.match(u);
    if (m != 1 && m != 2) throw new IllegalArgumentException(u.toString());
    if (m == 2) {
      s = append(s, "_id=?");
      a = add(a, u.getLastPathSegment());
    }
    int n;
    synchronized (writeLock) {
      n = database(true).update("notes", v, s, a);
    }
    if (n > 0) getContext().getContentResolver().notifyChange(URI, null);
    return n;
  }

  @Override
  public int delete(Uri u, String s, String[] a) {
    int m = MATCH.match(u);
    if (m != 1 && m != 2) throw new IllegalArgumentException(u.toString());
    if (m == 2) {
      s = append(s, "_id=?");
      a = add(a, u.getLastPathSegment());
    }
    int n;
    synchronized (writeLock) {
      n = database(true).delete("notes", s, a);
    }
    if (n > 0) getContext().getContentResolver().notifyChange(URI, null);
    return n;
  }

  @Override
  public String getType(Uri u) {
    switch (MATCH.match(u)) {
      case 1:
      case 3:
      case 4:
        return "vnd.android.cursor.dir/vnd.com.university.app.note";
      case 2:
        return "vnd.android.cursor.item/vnd.com.university.app.note";
      default:
        throw new IllegalArgumentException(u.toString());
    }
  }

  @Override
  public ContentProviderResult[] applyBatch(
      java.util.ArrayList<ContentProviderOperation> operations)
      throws android.content.OperationApplicationException {
    synchronized (writeLock) {
      SQLiteDatabase db = database(true);
      db.beginTransaction();
      try {
        ContentProviderResult[] results = super.applyBatch(operations);
        db.setTransactionSuccessful();
        return results;
      } finally {
        db.endTransaction();
      }
    }
  }

  @Override
  public int bulkInsert(Uri u, ContentValues[] v) {
    if (MATCH.match(u) != 1) throw new IllegalArgumentException(u.toString());
    SQLiteDatabase db = database(true);
    int n = 0;
    synchronized (writeLock) {
      db.beginTransaction();
      try {
        for (ContentValues item : v) {
          if (item.getAsString("title") == null)
            throw new IllegalArgumentException("title required");
          db.insertOrThrow("notes", null, item);
          n++;
        }
        db.setTransactionSuccessful();
      } finally {
        db.endTransaction();
      }
    }
    getContext().getContentResolver().notifyChange(URI, null);
    return n;
  }
}
