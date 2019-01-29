import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.InputStream;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
class XMLReader {
	private static String OSName = null;
	public static void main(String[] args)
	{
		OSName = System.getProperty("os.name").toLowerCase();
		// TODO Auto-generated method stub
		//parse();
		startPollingTimer();
		
		//System.out.println(SimpleOutPut());
		System.out.println(executePost("https://192.168.1.77:5443/rest/events/openalarms",""));

	}
	
	public static Timer t;

	public static synchronized void startPollingTimer() {
	        if (t == null) {
	            TimerTask task = new TimerTask() {
	                @Override
	                public void run() {
	                	ParseXML();
	                   //Do your work
	                }
	            };

	            t = new Timer();
	            t.scheduleAtFixedRate(task, 0, 3000);
	        }
	    }
	 public static void appendToFile(Exception e) {
	      try {
	    	  File file;
	    	  if (OSName.indexOf("win") >= 0) {
	    		  file= new File("E:\\XMLData\\exception.txt");
	    		} else {
	    			 file = new File("/XMLData/exception.txt");
	    		}
	         //File file = new File("E:\\XMLData\\exception.txt");
	            // If file doesn't exists, then create it
	            if (!file.exists()) {
	                file.createNewFile();
	            }
	         FileWriter fstream = new FileWriter(file.getPath(), true);
	         BufferedWriter out = new BufferedWriter(fstream);
	         PrintWriter pWriter = new PrintWriter(out, true);
	         pWriter.print("*****************--------New exception----------********************");
	         pWriter.println();
	         e.printStackTrace(pWriter);
	      }
	      catch (Exception ie) {
	         throw new RuntimeException("Could not write Exception to file", ie);
	      }
	   }
	
	public static void ParseXML()
	{
		try {
			 File file;
	    	  if (OSName.indexOf("win") >= 0) {
	    		  file= new File("E:\\XMLData\\Sample.xml");
	    		} else {
	    			 file = new File("/XMLData/Sample.xml");
	    		}
			//File file = new File("E:\\XMLData\\Sample.xml");

			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
		                             .newDocumentBuilder();

			Document doc = dBuilder.parse(file);

			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

			if (doc.hasChildNodes()) {

				printNote(doc.getChildNodes());

			}

		    } catch (Exception e) 
			{
		    	appendToFile(e);
			//System.out.println(e.getMessage());
		    }

		  }

		  private static void printNote(NodeList nodeList) {

		    for (int count = 0; count < nodeList.getLength(); count++) {

			Node tempNode = nodeList.item(count);

			// make sure it's element node.
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) {

				// get node name and value
				System.out.println("\nNode Name =" + tempNode.getNodeName() + " [OPEN]");
				System.out.println("Node Value =" + tempNode.getTextContent());

				if (tempNode.hasAttributes()) {

					// get attributes names and values
					NamedNodeMap nodeMap = tempNode.getAttributes();

					for (int i = 0; i < nodeMap.getLength(); i++) {

						Node node = nodeMap.item(i);
						System.out.println("attr name : " + node.getNodeName());
						System.out.println("attr value : " + node.getNodeValue());

					}

				}

				if (tempNode.hasChildNodes()) {

					// loop again if has child nodes
					printNote(tempNode.getChildNodes());

				}

				System.out.println("Node Name =" + tempNode.getNodeName() + " [CLOSE]");

			}

		    }
	}
	
	public static String executePost(String targetURL, String urlParameters) {
		  HttpURLConnection connection = null;
		  String path="E:\\XMLData\\Sample.xml";
		  if (OSName.indexOf("win") >= 0) {
			  path="E:\\XMLData\\Sample.xml";
    		} else {
    			path="/XMLData/Sample.xml";
    		}
		  final Path dst = Paths.get(path);
		  final BufferedWriter writer;
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
		    File file = new File(path);
            // If file doesn't exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            writer = Files.newBufferedWriter(dst, StandardCharsets.UTF_8);
		    StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
		    String line;
		    while ((line = rd.readLine()) != null) {
		      response.append(line);
		      byte[] contentInBytes = line.getBytes();
		      FileOutputStream fop = new FileOutputStream(file);
		      fop.write(contentInBytes);
              fop.flush();
              fop.close();
		      //writer.write(contentInBytes);
		        // must do this: .readLine() will have stripped line endings
		        writer.newLine();
		      response.append('\r');
		    }
		    rd.close();
		    return response.toString();
		  } 
		  catch (Exception e)
		  {
			  appendToFile(e);
		    //e.printStackTrace();
		    return null;
		  } finally {
		    if (connection != null) {
		      connection.disconnect();
		    }
		  }
		}

}
