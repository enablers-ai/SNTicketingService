import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.InputStream;

class XMLReader {

	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		System.out.println(SimpleOutPut());
		System.out.println(executePost("https://192.168.1.77:5443/rest/events/openalarms",""));

	}
	
	public static String SimpleOutPut()
	{
		return "All is well";
	}
	
	
	
	public static String executePost(String targetURL, String urlParameters) {
		  HttpURLConnection connection = null;

		  try {
			  // Create a trust manager that does not validate certificate chains
		        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
		                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		                    return null;
		                }
		                public void checkClientTrusted(X509Certificate[] certs, String authType) {
		                }
		                public void checkServerTrusted(X509Certificate[] certs, String authType) {
		                }
		            }
		        };
		        
		     // Install the all-trusting trust manager
		        SSLContext sc = SSLContext.getInstance("SSL");
		        sc.init(null, trustAllCerts, new java.security.SecureRandom());
		        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		     // Create all-trusting host name verifier
		        HostnameVerifier allHostsValid = new HostnameVerifier() {
		            public boolean verify(String hostname, SSLSession session) {
		                return true;
		            }
		        };
		    //Create connection
		    
		    URL url = new URL(targetURL);
		    String userCredentials = "muneer:muneer";
		    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
		    HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		    connection = (HttpURLConnection) url.openConnection();
		    
		    connection.setRequestProperty ("Authorization", basicAuth);
		    connection.setRequestMethod("GET");
		    //connection.setRequestProperty("Content-Type", 
		       // "application/xml");

		    //connection.setRequestProperty("Content-Length", 
		     //   Integer.toString(urlParameters.getBytes().length));
		    //connection.setRequestProperty("Content-Language", "en-US");  

		    //connection.setUseCaches(false);
//		    connection.setDoOutput(true);

		    //Send request
//		    DataOutputStream wr = new DataOutputStream (
//		        connection.getOutputStream());
//		    wr.writeBytes(urlParameters);
//		    wr.close();
//		    Reader reader = new InputStreamReader(connection.getInputStream());
//	        while (true) {
//	            int ch = reader.read();
//	            if (ch==-1) {
//	                break;
//	            }
//	            System.out.println((char)ch);
//	        }
		    //Get Response  
		    InputStream is = connection.getInputStream();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		    StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
		    String line;
		    while ((line = rd.readLine()) != null) {
		      response.append(line);
		      response.append('\r');
		    }
		    rd.close();
		    return response.toString();
		  } 
		  catch (Exception e)
		  {
		    e.printStackTrace();
		    return null;
		  } finally {
		    if (connection != null) {
		      connection.disconnect();
		    }
		  }
		}

}
