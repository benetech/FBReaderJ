package org.geometerplus.android.fbreader.network;

import android.util.Log;

import org.benetech.android.BuildConfig;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

/**
 * Created by avanticatechnologies on 1/5/17.
 */

public class CertificateBypassHelper {

    static public void applyBypassIfStaging() {

        if(BuildConfig.BOOKSHARE_STAGING_IGNORE_CERT) {
            try {
                HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
                SSLContext context = null;
                context = SSLContext.getInstance("TLS");
                context.init(null, new X509TrustManager[]{new NullX509TrustManager()}, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
            } catch (NoSuchAlgorithmException e) {
                Log.e("login failed", "verifier algorithm error", e);
            } catch (KeyManagementException e) {
                Log.e("login failed", "certificate error", e);
            }
        }

    }

    static class NullHostNameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            Log.i("RestUtilImpl", "Approving certificate for " + hostname);
            return true;
        }

    }

    static class NullX509TrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

}
