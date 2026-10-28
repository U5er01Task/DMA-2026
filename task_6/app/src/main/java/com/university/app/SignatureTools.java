package com.university.app;

import android.security.keystore.*;
import java.security.*;

public final class SignatureTools {
  private static final String ALIAS = "a80-signing-ecdsa";

  private static KeyStore store() throws Exception {
    KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
    ks.load(null);
    return ks;
  }

  private static void ensureKey() throws Exception {
    if (store().containsAlias(ALIAS)) return;
    KeyPairGenerator gen =
        KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
    gen.initialize(
        new KeyGenParameterSpec.Builder(
                ALIAS, KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
            .setDigests(KeyProperties.DIGEST_SHA256)
            .build());
    gen.generateKeyPair();
  }

  public static byte[] sign(byte[] bytes) throws Exception {
    ensureKey();
    Signature s = Signature.getInstance("SHA256withECDSA");
    s.initSign(((KeyStore.PrivateKeyEntry) store().getEntry(ALIAS, null)).getPrivateKey());
    s.update(bytes);
    return s.sign();
  }

  public static boolean verify(byte[] bytes, byte[] signed) throws Exception {
    if (signed == null) return false;
    ensureKey();
    Signature sig = Signature.getInstance("SHA256withECDSA");
    sig.initVerify(store().getCertificate(ALIAS).getPublicKey());
    sig.update(bytes);
    return sig.verify(signed);
  }
}
