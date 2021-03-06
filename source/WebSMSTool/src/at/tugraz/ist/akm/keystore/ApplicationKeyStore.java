/*
 * Copyright 2012 software2012team23
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.tugraz.ist.akm.keystore;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import android.annotation.SuppressLint;
import at.tugraz.ist.akm.trace.LogClient;

@SuppressWarnings("deprecation")
public class ApplicationKeyStore implements Closeable
{
    private LogClient mLog = new LogClient(this);

    @SuppressLint("TrulyRandom")
    // secure random is initialized manually when application starts
    private SecureRandom mRandom = new SecureRandom();

    private Certificate mCertificate = null;
    private KeyPair mKeyPair = null;
    private KeyManagerFactory mKeyFactory = null;
    private KeyStore mInKeyStore = null;
    private boolean mIsLoaded = false;

    private InputStream mInKeyStoreStream = null;


    public ApplicationKeyStore()
    {
        try
        {
            mKeyFactory = KeyManagerFactory.getInstance(KeyManagerFactory
                    .getDefaultAlgorithm());
        }
        catch (Throwable t)
        {
            mLog.error(
                    "failed getting KeyManagerFactory expecting NullpointerException soon :/",
                    t);
        }
    }


    public String newRandomPassword()
    {
        return new BigInteger(130, mRandom).toString(32);
    }


    private void writeNewKeystore(String password, String filePath)
            throws Exception
    {
        try
        {
            createNewCertificate();
            storeNewKeystore(password.toCharArray(), filePath, mKeyPair,
                    mCertificate);
        }
        catch (Exception e)
        {
            mLog.debug("failed to store new keystore with password [*****] and filepath ["
                    + filePath + "]");
            throw new Exception(e);
        }

        mKeyPair = null;
        mCertificate = null;
    }


    private void wipeAndLoadNewKeystore(String password, String filePath)
            throws Exception, KeyStoreException, CertificateException,
            IOException
    {
        wipeKeystore(filePath);
        writeNewKeystore(password, filePath);
        tryLoadKeystore(password, filePath);
    }


    public void deleteKeystore(String filePath)
    {
        wipeKeystore(filePath);
    }


    public boolean loadKeystore(String password, String filePath)
    {
        try
        {
            tryLoadKeystore(password, filePath);
            mIsLoaded = true;
        }
        catch (CertificateException e)
        {
            mLog.debug(
                    "failed to load certificate from keystore, create new store",
                    e);
            try
            {
                wipeAndLoadNewKeystore(password, filePath);
                mIsLoaded = true;
            }
            catch (Exception f)
            {
                mLog.error("unrecoverale keystore error", f);
            }
        }
        catch (IOException g)
        {
            mLog.debug("failed to load keystore (file missing or wrong password) create new store");
            try
            {
                wipeAndLoadNewKeystore(password, filePath);
                mIsLoaded = true;
            }
            catch (Exception h)
            {
                mLog.error("unrecoverale keystore error", h);
            }
        }
        catch (Exception i)
        {
            mLog.error("unrecoverale keystore error", i);
        }

        return mIsLoaded;
    }


    public X509Certificate getX509Certficate()
    {
        if (mCertificate != null)
        {
            try
            {
                CertificateFactory certFactory = CertificateFactory
                        .getInstance("X.509");
                InputStream in = new ByteArrayInputStream(
                        mCertificate.getEncoded());
                return (X509Certificate) certFactory.generateCertificate(in);
            }
            catch (Exception e)
            {
                return null;
            }
        }
        return null;
    }


    private void tryLoadKeystore(String password, String filePath)
            throws NoSuchAlgorithmException, IOException,
            UnrecoverableKeyException, KeyStoreException, CertificateException
    {

        mInKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        mInKeyStoreStream = new FileInputStream(filePath);
        mInKeyStore.load(mInKeyStoreStream, password.toCharArray());
        mKeyFactory.init(mInKeyStore, password.toCharArray());
        mLog.debug("keystore loaded with password [*****] and filepath ["
                + filePath + "]");

        mCertificate = mInKeyStore
                .getCertificate(CertificateDefaultAttributes.ALIAS_NAME);
    }


    @Override
    public void close() throws IOException
    {
        if (mInKeyStoreStream != null)
        {
            try
            {
                mInKeyStoreStream.close();
            }
            catch (IOException e)
            {
                mLog.error(
                        "failed closing InputStream while releasing resources",
                        e);
            }
            mInKeyStoreStream = null;
        }

        mRandom = null;
        mCertificate = null;
        mKeyPair = null;
        mKeyFactory = null;
        mInKeyStore = null;
        mLog = null;
    }


    public KeyManager[] getKeystoreManagers()
    {
        try
        {
            return mKeyFactory.getKeyManagers();
        }
        catch (Throwable t)
        {
            mLog.error("failed getting KeystoreManagers", t);
        }
        return null;
    }


    private boolean wipeKeystore(String filePath)
    {
        File file = new File(filePath);

        if (file.exists())
        {
            if (file.delete())
            {
                mLog.debug("wiped keytore: " + filePath);
                return true;
            }
            mLog.warning("failed to wipe keytore: " + filePath);
        } else
        {
            mLog.debug("nothing to delete: " + filePath);
        }
        return false;
    }


    private void createNewCertificate() throws InvalidKeyException,
            SecurityException, SignatureException, NoSuchAlgorithmException,
            CertificateEncodingException, IllegalStateException,
            NoSuchProviderException
    {
        Security.addProvider(new BouncyCastleProvider());
        X509V3CertificateGenerator certGenerator = new X509V3CertificateGenerator();
        certGenerator.setSerialNumber(BigInteger.valueOf(Math.abs(mRandom
                .nextInt() + 1)));
        certGenerator.setIssuerDN(new X500Principal(
                CertificateDefaultAttributes.ISSUER));
        certGenerator.setSubjectDN(new X500Principal(
                CertificateDefaultAttributes.SUBJECT));
        certGenerator
                .setNotBefore(new Date(
                        System.currentTimeMillis()
                                - CertificateDefaultAttributes.VALID_DURATION_BEFORE_NOW_MILLISECONDS));
        certGenerator
                .setNotAfter(new Date(
                        System.currentTimeMillis()
                                + CertificateDefaultAttributes.VALID_DURATION_FROM_NOW_MILLISECONDS));

        KeyPairGenerator keyGenerator = KeyPairGenerator
                .getInstance(CertificateDefaultAttributes.KEYPAIR_GENERATOR);
        keyGenerator
                .initialize(CertificateDefaultAttributes.KEYPAIR_LENGTH_BITS);
        KeyPair newKeyPair = keyGenerator.generateKeyPair();

        certGenerator.setPublicKey(newKeyPair.getPublic());
        certGenerator
                .setSignatureAlgorithm(CertificateDefaultAttributes.ENCRYPTION_ALGORITHM);
        X509Certificate newCertificate = certGenerator.generate(newKeyPair
                .getPrivate());

        mCertificate = newCertificate;
        mKeyPair = newKeyPair;
    }


    private void storeNewKeystore(char[] keystorePassword, String filePath,
            KeyPair keyPair, Certificate certificate) throws KeyStoreException,
            NoSuchAlgorithmException, CertificateException, IOException
    {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        KeyStore.ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(
                keystorePassword);

        keyStore.load(null, null);

        java.security.cert.Certificate[] certificateChain = { certificate };
        KeyStore.PrivateKeyEntry privateKeyEntry = new KeyStore.PrivateKeyEntry(
                keyPair.getPrivate(), certificateChain);
        keyStore.setEntry(CertificateDefaultAttributes.ALIAS_NAME,
                privateKeyEntry, protectionParameter);

        FileOutputStream fos = null;
        mLog.debug("writing to store [" + filePath + "] ...");
        fos = new FileOutputStream(filePath);
        keyStore.store(fos, keystorePassword);
        fos.close();
    }

}
