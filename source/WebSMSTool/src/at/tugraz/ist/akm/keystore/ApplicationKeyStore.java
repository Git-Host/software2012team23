package at.tugraz.ist.akm.keystore;

import java.io.File;
import java.io.FileInputStream;
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
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import at.tugraz.ist.akm.trace.LogClient;

public class ApplicationKeyStore
{
    private LogClient mLog = new LogClient(this);
    private SecureRandom mRandom = new SecureRandom();

    private Certificate mCertificate = null;
    private KeyPair mKeyPair = null;
    private KeyManagerFactory mKeyFactory = null;
    private KeyStore mInKeyStore = null;
    private String mKeystorePassword = null;


    public ApplicationKeyStore()
    {
        try
        {
            mKeyFactory = KeyManagerFactory.getInstance(KeyManagerFactory
                    .getDefaultAlgorithm());
        } catch (Throwable t)
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


    private boolean writeNewKeystore(String password, String filePath)
    {
        try
        {
            wipeKeystore(filePath);
            createNewCertificate();
            storeNewKeystore(password.toCharArray(), filePath);
            mKeystorePassword = password;
        } catch (Exception e)
        {
            mLog.error("failed to create new keystore with password [*****] and filepath ["
                    + filePath + "]");
            return false;
        }
        return true;
    }


    public boolean loadKeystore(String password, String filePath)
    {
        try
        {
            mInKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            if (!keystoreExists(filePath))
            {
                mLog.debug("request to load missing KeyStore -> create new KeyStore first");
                if (!writeNewKeystore(password, filePath))
                {
                    throw new Exception("failed to build new keystore");
                }
            }
            InputStream is = new FileInputStream(filePath);
            mInKeyStore.load(is, password.toCharArray());
            mKeyFactory.init(mInKeyStore, password.toCharArray());
            mKeystorePassword = password;
        } catch (Throwable t)
        {
            mLog.error("failed to load keystore with password [*****] and filepath ["
                    + filePath + "]");
            return false;
        }
        mLog.debug("keystore loaded with password [*****] and filepath ["+ filePath + "]");
        return true;
    }


    public KeyManager[] getKeystoreManagers()
    {
        try
        {
         //   mKeyFactory.init(mInKeyStore, mKeystorePassword.toCharArray());
            return mKeyFactory.getKeyManagers();
        } catch (Throwable t)
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
        }
        ;
        return false;
    }


    private boolean keystoreExists(String filePath)
    {
        File file = new File(filePath);
        return file.exists();
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


    private boolean storeNewKeystore(char[] keystorePassword, String filePath)
            throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException
    {
        boolean isNewStoreCreated = true;
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        KeyStore.ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(
                keystorePassword);

        keyStore.load(null, null);

        java.security.cert.Certificate[] certificateChain = { mCertificate };
        KeyStore.PrivateKeyEntry privateKeyEntry = new KeyStore.PrivateKeyEntry(
                mKeyPair.getPrivate(), certificateChain);
        keyStore.setEntry(CertificateDefaultAttributes.ALIAS_NAME,
                privateKeyEntry, protectionParameter);

        java.io.FileOutputStream fos = null;
        try
        {
            mLog.debug("writing to store [" + filePath + "] ...");
            fos = new java.io.FileOutputStream(filePath);
            keyStore.store(fos, keystorePassword);
        } catch (Exception e)
        {
            mLog.error("failed to write keystore to file: " + filePath, e);
        } finally
        {
            if (fos != null)
            {
                fos.close();
            }
            isNewStoreCreated = false;
        }
        return isNewStoreCreated;
    }

}
