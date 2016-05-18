//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//

package com.cloud.utils.security;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import com.cloud.utils.Ternary;
import com.cloud.utils.exception.CloudRuntimeException;

public class CertificateHelper {
    public static byte[] buildAndSaveKeystore(String alias, String cert, String privateKey, String storePassword) throws KeyStoreException, CertificateException,
        NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        KeyStore ks = buildKeystore(alias, cert, privateKey, storePassword);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ks.store(os, storePassword != null ? storePassword.toCharArray() : null);
        os.close();
        return os.toByteArray();
    }

    public static byte[] buildAndSaveKeystore(List<Ternary<String, String, String>> certs, String storePassword) throws KeyStoreException, NoSuchAlgorithmException,
        CertificateException, IOException, InvalidKeySpecException {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, storePassword != null ? storePassword.toCharArray() : null);

        //name,cert,key
        for (Ternary<String, String, String> cert : certs) {
            if (cert.third() == null) {
                Certificate c = buildCertificate(cert.second());
                ks.setCertificateEntry(cert.first(), c);
            } else {
                Certificate[] c = new Certificate[certs.size()];
                int i = certs.size();
                for (Ternary<String, String, String> ct : certs) {
                    c[i - 1] = buildCertificate(ct.second());
                    i--;
                }
                ks.setKeyEntry(cert.first(), buildPrivateKey(cert.third()), storePassword != null ? storePassword.toCharArray() : null, c);
            }
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ks.store(os, storePassword != null ? storePassword.toCharArray() : null);
        os.close();
        return os.toByteArray();
    }

    public static KeyStore loadKeystore(byte[] ksData, String storePassword) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        assert (ksData != null);
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new ByteArrayInputStream(ksData), storePassword != null ? storePassword.toCharArray() : null);

        return ks;
    }

    public static KeyStore buildKeystore(String alias, String cert, String privateKey, String storePassword) throws KeyStoreException, CertificateException,
        NoSuchAlgorithmException, InvalidKeySpecException, IOException {

        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, storePassword != null ? storePassword.toCharArray() : null);
        Certificate[] certs = new Certificate[1];
        certs[0] = buildCertificate(cert);
        ks.setKeyEntry(alias, buildPrivateKey(privateKey), storePassword != null ? storePassword.toCharArray() : null, certs);
        return ks;
    }

    public static Certificate buildCertificate(String content) throws CertificateException {
        assert (content != null);

        BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(content.getBytes()));
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return cf.generateCertificate(bis);
    }

    public static Key buildPrivateKey(String base64EncodedKeyContent) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keysp = new PKCS8EncodedKeySpec(Base64.decodeBase64(base64EncodedKeyContent));
        return kf.generatePrivate(keysp);
    }

    public static List<Certificate> parseChain(String chain) throws IOException, CertificateException {

        final List<Certificate> certs = new ArrayList<Certificate>();
        try(final PemReader pemReader = new PemReader(new StringReader(chain));)
        {
            Certificate cert = null;
            final PemObject pemObject = pemReader.readPemObject();
            final ByteArrayInputStream bais = new ByteArrayInputStream(pemObject.getContent());
            final CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");

            Collection<? extends Certificate> c = certificateFactory.generateCertificates(bais);
            Iterator<? extends Certificate> i = c.iterator();
            while (i.hasNext()) {
                cert = i.next();
                if (cert instanceof X509Certificate) {
                    certs.add(cert);
                }
            }
            if (certs.size() == 0) {
                throw new IllegalArgumentException("Unable to decode certificate chain");
            }
        }
        finally {
            // just close the pemReader
        }

        return certs;
    }

    public static String generateFingerPrint(Certificate cert) {

        final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

        StringBuilder buffer = new StringBuilder(60);
        try {

            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] data = md.digest(cert.getEncoded());

            for (final byte element : data) {
                if (buffer.length() > 0) {
                    buffer.append(":");
                }

                buffer.append(HEX[(0xF0 & element) >>> 4]);
                buffer.append(HEX[0x0F & element]);
            }

        } catch (final CertificateEncodingException e) {
            throw new CloudRuntimeException("Bad certificate encoding");
        } catch (final NoSuchAlgorithmException e) {
            throw new CloudRuntimeException("Bad certificate algorithm");
        }

        return buffer.toString();
    }

}
