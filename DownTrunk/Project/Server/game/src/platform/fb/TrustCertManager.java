package platform.fb;

import util.ErrorPrint;

public class TrustCertManager {
	 static void trustAllHttpsCertificates() {
		 try {
			 javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
			 javax.net.ssl.TrustManager tm = new miTM();
			 trustAllCerts[0] = tm;
			 javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext
			 .getInstance("SSL");
			 sc.init(null, trustAllCerts, null);
			 javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc
			 .getSocketFactory());
		 }
		 catch(Exception e)
		 {
			 ErrorPrint.print(e);
		 }
		 
	 }
	 
	 static class miTM implements javax.net.ssl.TrustManager,
		 javax.net.ssl.X509TrustManager {
		 public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		 return null;
	 }
	 
	 public boolean isServerTrusted(
		 java.security.cert.X509Certificate[] certs) {
		 return true;
	 }
	 
	 public boolean isClientTrusted(
		 java.security.cert.X509Certificate[] certs) {
		 return true;
	 }
	 
	 public void checkServerTrusted(
		 java.security.cert.X509Certificate[] certs, String authType)
		 throws java.security.cert.CertificateException {
		 return;
	 }
	 
	 public void checkClientTrusted(
		 java.security.cert.X509Certificate[] certs, String authType)
		 throws java.security.cert.CertificateException {
		 return;
	 }
 }

}
