import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;
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
		//
		//System.out.println(SimpleOutPut());
		//System.out.println(executePost("https://192.168.1.77:5443/rest/events/openalarms",""));

	}
	
	public static Timer t;

	public static synchronized void startPollingTimer() {
	        if (t == null) {
	            TimerTask task = new TimerTask() {
	                @Override
	                public void run() {
	                	executePost("https://192.168.1.77:5443/rest/events/openalarms","");
	                	ParseXML();
	                	//String APIResults= UploadFileAPI();
	                   //Do your work
	                }
	            };

	            t = new Timer();
	            t.scheduleAtFixedRate(task, 0, 30000);
	        }
	    }
	 public static void appendToFile(Exception e) {
	      try {
	    	  File file;
	    	  if (OSName.indexOf("win") >= 0) {
	    		  file= new File("E:\\XMLData\\exception.txt");
	    		} else {
	    			 file = new File("/home/munir/Documents/XMLData/exception.txt");
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
	         DateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss");
	         Date date = new Date();
	         pWriter.print("As on "+dateFormat.format(date));
	         pWriter.println();
	         e.printStackTrace(pWriter);
	      }
	      catch (Exception ie) {
	         throw new RuntimeException("Could not write Exception to file", ie);
	      }
	   }
	
	public static void ParseXML()
	{
		final Object lock = new Object();
		try {
			 File file=null;
			 synchronized(lock)
			 {

		 try {
	    	  if (OSName.indexOf("win") >= 0) {
	    		  file= new File("E:\\XMLData\\Sample.xml");
	    		} else {
	    			 file = new File("/home/munir/Documents/XMLData/Sample.xml");
	    		}
			    }
	    	  catch (Exception e) 
				{
			    	appendToFile(e);
				//System.out.println(e.getMessage());
			    }
			 }
			//File file = new File("E:\\XMLData\\Sample.xml");
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
		                             .newDocumentBuilder();

			Document doc = dBuilder.parse(file);
			 StringBuilder sb = new StringBuilder();
			 sb.append("Root element :" + doc.getDocumentElement().getNodeName());
			//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

			if (doc.hasChildNodes()) {

			String res=	printNote(doc.getChildNodes());
			sb.append(res);
			}
			File fileParsed;
			 
	    	  if (OSName.indexOf("win") >= 0) {
	    		  fileParsed= new File("E:\\XMLData\\ParsedXML.txt");
	    		} else {
	    			fileParsed = new File("/home/munir/Documents/XMLData/ParsedXML.txt");
	    		}
	    	  if (!fileParsed.exists())
	    	   {
	    		  fileParsed.createNewFile();
	            }
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileParsed))) {
			    writer.write(sb.toString());
			}
		    } catch (Exception e) 
			{
		    	appendToFile(e);
			//System.out.println(e.getMessage());
		    }

		  }

		  private static String printNote(NodeList nodeList) throws IOException {
			  
	    	  StringBuilder sb = new StringBuilder();
		    for (int count = 0; count < nodeList.getLength(); count++) {

			Node tempNode = nodeList.item(count);

			// make sure it's element node.
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) {

				// get node name and value
				sb.append("\nNode Name =" + tempNode.getNodeName() + " [OPEN]");
				//System.out.println("\nNode Name =" + tempNode.getNodeName() + " [OPEN]");
				//System.out.println("Node Value =" + tempNode.getTextContent());
				sb.append("Node Value =" + tempNode.getTextContent());

				if (tempNode.hasAttributes()) {

					// get attributes names and values
					NamedNodeMap nodeMap = tempNode.getAttributes();

					for (int i = 0; i < nodeMap.getLength(); i++) {

						Node node = nodeMap.item(i);
						sb.append("attr name : " + node.getNodeName());
						//System.out.println("attr name : " + node.getNodeName());
						//System.out.println("attr value : " + node.getNodeValue());
						sb.append("attr value : " + node.getNodeValue());

					}

				}

				if (tempNode.hasChildNodes()) {

					// loop again if has child nodes
					 String result= printNote(tempNode.getChildNodes());
					sb.append(result);
				}
				sb.append("Node Name =" + tempNode.getNodeName() + " [CLOSE]");
				//FileWriter fstream = new FileWriter(fileParsed.getPath(), true);
				//System.out.println("Node Name =" + tempNode.getNodeName() + " [CLOSE]");

			}

		    }
		    return sb.toString();
	}
	
	public static String executePost(String targetURL, String urlParameters) {
		final Object lock = new Object();  
		HttpURLConnection connection = null;
		  String path=null;
		  if (OSName.indexOf("win") >= 0) {
			  path="E:\\XMLData\\Sample.xml";
    		} else {
    			path="/home/munir/Documents/XMLData/Sample.xml";
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
            writer = Files.newBufferedWriter(dst, StandardCharsets.UTF_8);
		    StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
		    String line;
		    while ((line = rd.readLine()) != null) {
		      response.append(line);
		      byte[] contentInBytes = line.getBytes();
		      File file = new File(path);
		      FileOutputStream fop =null;
	            // If file doesn't exists, then create it
		      synchronized(lock)
				{
	            if (!file.exists()) 
	            {
	                file.createNewFile();
	            }
                fop = new FileOutputStream(file);
		        fop.write(contentInBytes);
				}
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
	public static String UploadFileAPI()
	{
		String result=null;
		File inFile = new File("E:\\XMLData\\Sample.xml");
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(inFile);
			DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
			
			// server back-end URL
			HttpPost httppost = new HttpPost("http://localhost:5797/api/FileUpload/Post");
			MultipartEntity entity = new MultipartEntity();
			// set the file input stream and file name as arguments
			entity.addPart("file", new InputStreamBody(fis, inFile.getName()));
			httppost.setEntity(entity);
			// execute the request
			HttpResponse response = httpclient.execute(httppost);
			
			int statusCode = response.getStatusLine().getStatusCode();
			HttpEntity responseEntity = response.getEntity();
			String responseString = EntityUtils.toString(responseEntity, "UTF-8");
			result= statusCode + "\n " + responseString;
			
		} catch (ClientProtocolException e) {
			//System.err.println("Unable to make connection");
			//e.printStackTrace();
			appendToFile(e);
		} catch (IOException e) {
			//System.err.println("Unable to read file");
			//e.printStackTrace();
			appendToFile(e);
		} finally {
			try {
				if (fis != null) fis.close();
			} 
			catch (IOException e) 
			{
				appendToFile(e);
			}
		}
		return result;
	}
	
	
	
//    public static String ConsumeAPI()
//    {
//    	String result=null;
//    	 URL url;
//		try {
//			url = new URL("http://api.timezonedb.com/v2/list-time-zone?key=LQY1SS2O2Z4L&amp;format=json&amp;country=NG");
//			//your url i.e fetch data from .
//           
//           try {
//        	   HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//   			conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("GET");
//            conn.setRequestProperty("Accept", "application/json");
//            if (conn.getResponseCode() != 200) {
//            	result="Failure";
//                throw new RuntimeException("Failed : HTTP Error code : "
//                        + conn.getResponseCode());
//            }
//            InputStreamReader in = new InputStreamReader(conn.getInputStream());
//            BufferedReader br = new BufferedReader(in);
//            String output;
//            result="Success";
//            while ((output = br.readLine()) != null) {
//                System.out.println(output);
//            }
//   		} 
//           catch (IOException e)
//           {
//   			// TODO Auto-generated catch block
//   			e.printStackTrace();
//   		   }
//		} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//		e.printStackTrace();
//		}//your url i.e fetch data from .
//         
//    	return result;
//    }
}
