package com.university.app;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import java.io.*;

public final class SafeFilesProvider extends ContentProvider {
  public static final String AUTHORITY = "com.university.app.safefiles";

  @Override
  public boolean onCreate() {
    File dir = new File(getContext().getFilesDir(), "public_files");
    dir.mkdirs();
    File demo = new File(dir, "allowed.txt");
    if (!demo.exists())
      try (FileOutputStream out = new FileOutputStream(demo)) {
        out.write("safe".getBytes("UTF-8"));
      } catch (IOException ignored) {
      }
    return true;
  }

  @Override
  public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
    if (!"r".equals(mode)) throw new FileNotFoundException("read-only");
    try {
      String path = Uri.decode(uri.getEncodedPath());
      if (path.contains("../") || path.contains("..\\"))
        throw new SecurityException(".. path rejected");
      if (uri.getPathSegments().size() != 2 || !"file".equals(uri.getPathSegments().get(0)))
        throw new FileNotFoundException("unknown URI");
      File root = new File(getContext().getFilesDir(), "public_files").getCanonicalFile();
      File requested = new File(root, uri.getLastPathSegment()).getCanonicalFile();
      if (!requested.getPath().startsWith(root.getPath() + File.separator))
        throw new SecurityException("Outside directory");
      Log.i("A80_42", "safe path=" + requested);
      return ParcelFileDescriptor.open(requested, ParcelFileDescriptor.MODE_READ_ONLY);
    } catch (IOException e) {
      throw new FileNotFoundException(e.toString());
    }
  }

  @Override
  public String getType(Uri uri) {
    return "text/plain";
  }

  @Override
  public Cursor query(Uri u, String[] projection, String s, String[] a, String o) {
    return null;
  }

  @Override
  public Uri insert(Uri u, ContentValues v) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int update(Uri u, ContentValues v, String s, String[] a) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int delete(Uri u, String s, String[] a) {
    throw new UnsupportedOperationException();
  }
}
