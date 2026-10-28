package com.university.app;

import android.content.*;
import android.database.*;
import android.net.Uri;
import android.os.*;
import android.security.keystore.*;
import android.util.Log;
import java.io.*;
import java.security.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public final class EncryptedLogsProvider extends ContentProvider {
  public static final String AUTHORITY = "com.university.app.encryptedlogs";
  public static final Uri URI = Uri.parse("content://" + AUTHORITY + "/logs");
  private static final String ALIAS = "logs-aes-key";
  private File dir;
  private final Object lock = new Object();

  @Override
  public boolean onCreate() {
    dir = new File(getContext().getFilesDir(), "securelogs");
    return dir.mkdirs() || dir.isDirectory();
  }

  private static SecretKey key() throws Exception {
    KeyStore store = KeyStore.getInstance("AndroidKeyStore");
    store.load(null);
    if (!store.containsAlias(ALIAS)) {
      KeyGenerator generator = KeyGenerator.getInstance("AES", "AndroidKeyStore");
      generator.init(
          new KeyGenParameterSpec.Builder(
                  ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
              .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
              .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
              .setKeySize(256)
              .build());
      generator.generateKey();
    }
    return ((KeyStore.SecretKeyEntry) store.getEntry(ALIAS, null)).getSecretKey();
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    if (android.os.Binder.getCallingUid() != android.os.Process.myUid())
      throw new SecurityException("Only the owner can write");
    String s = values == null ? null : values.getAsString("message");
    if (s == null || s.isEmpty()) throw new IllegalArgumentException("message required");
    synchronized (lock) {
      try {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key());
        byte[] ciphertext = cipher.doFinal(s.getBytes("UTF-8"));
        long id = System.currentTimeMillis();
        File file = new File(dir, id + ".bin");
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
          out.writeInt(cipher.getIV().length);
          out.write(cipher.getIV());
          out.write(ciphertext);
        }
        getContext().getContentResolver().notifyChange(URI, null);
        Log.i("A80_21", "encrypted log id=" + id);
        return Uri.withAppendedPath(URI, String.valueOf(id));
      } catch (Exception e) {
        throw new IllegalStateException("AES encryption failed", e);
      }
    }
  }

  @Override
  public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
    if (!"r".equals(mode)) throw new FileNotFoundException("read only");
    String id = uri.getLastPathSegment();
    if (id == null || !id.matches("[0-9]+")) throw new FileNotFoundException("bad id");
    return ParcelFileDescriptor.open(
        new File(dir, id + ".bin"), ParcelFileDescriptor.MODE_READ_ONLY);
  }

  @Override
  public Cursor query(Uri u, String[] projection, String selection, String[] args, String sort) {
    MatrixCursor c = new MatrixCursor(new String[] {"_id", "name", "size"});
    File[] files = dir.listFiles((d, n) -> n.matches("[0-9]+\\.bin"));
    if (files != null)
      for (File file : files) {
        String id = file.getName().replace(".bin", "");
        c.addRow(new Object[] {id, file.getName(), file.length()});
      }
    c.setNotificationUri(getContext().getContentResolver(), URI);
    return c;
  }

  @Override
  public String getType(Uri uri) {
    return "application/octet-stream";
  }

  @Override
  public int delete(Uri uri, String sel, String[] args) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int update(Uri uri, ContentValues values, String sel, String[] args) {
    throw new UnsupportedOperationException();
  }
}
