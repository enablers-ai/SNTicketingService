package XMLReaderPackage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.print.attribute.standard.DateTimeAtCompleted;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gson.Gson;

import java.io.InputStream;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
class XMLReader {

	private static String OSName = null;
	static Configurations configPropeties = new Configurations();
	//Main Method 
	//Calling getconfigPropetiess method
	//Calling startPollingTimer
	public static void main(String[] args) throws IOException {
		//System.setProperty("java.net.useSystemProxies", "true");
		OSName = System.getProperty("os.name").toLowerCase();
		
		configPropeties= getconfigPropetiess();
		String serverCompleteUrl= configPropeties.getServerAPIUrl()+configPropeties.getAlarmsActionUrl();
		ParseXML();
		//executePost(serverCompleteUrl,"");
		//String ApiResult= UploadFileAPI();
		//startPollingTimer(serverCompleteUrl, configPropeties.getCallRepeateTime()); 
	}
	// static timer's variable.
	public static Timer t;
	// Synchronized static timer repeating method.
	// Repeating call of UploadFileAPI method after specified time in configuration property callRepeateTime
	public static synchronized void startPollingTimer(String serverCompleteUrl, long timePeriod) {
		if (t == null && configPropeties.getcontinueScheduler() == true) {
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					try 
					{
					executePost(serverCompleteUrl,"");
					ParseXML();
					//String APIResults= UploadFileAPI();
					//System.out.println(APIResults);
					}
					catch(Exception ex)
					{
						appendToFile(ex);
					}
					//try 
					//{
						//configPropeties= getconfigPropetiess();
					//} catch (IOException ex)
					//{
						//appendToFile(ex);
						//ex.printStackTrace();
					//}
					//if(configPropeties.getcontinueScheduler()==false)
					//{
						//stopPollingTimer();
					//}
				}
			};

			t = new Timer();
			t.scheduleAtFixedRate(task, new Date(), timePeriod);
		}
	}
	// Used to check if time is already running then stop it.
	public static void stopPollingTimer()
	{
		if(t !=null)
		{
			t.cancel();
		}
	}
	// Calling stopPollingTimer on exit.
	protected void finalize() throws Throwable   
	{
		stopPollingTimer();
	}
	public static void appendToFile(Exception e) {
		try {
			File file;
			if (OSName.indexOf("win") >= 0) {
				file= new File(configPropeties.getWinndowsExceptionsPath());
			} else {
				file = new File(configPropeties.getLinuxExceptionPath());
			}
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
	// Reads and returns configurations properties.
	public static Configurations getconfigPropetiess() throws IOException {
		Configurations config =new Configurations();
		InputStream input = null;

		try {
			//Path currentRelativePath = Paths.get("");
			//String absolutePath = currentRelativePath.toAbsolutePath().toString();
			//String UTF_Encoding="UTF-8";
			if (OSName.indexOf("win") >= 0) 
			{
				//UTF_Encoding="UTF-8";
			input = new FileInputStream("C:\\Users\\Enablers\\git\\StableNetTicketingService\\XMLReaderService\\configurations.properties");
			}
			else
			{
				input = new FileInputStream("/root/Documents/configurations.properties");
				//UTF_Encoding="ISO-8859-1";
			}
			StringWriter writer = new StringWriter();
			//ISO-8859-1 
			IOUtils.copy(input, writer, StandardCharsets.UTF_8);
			String inputStr=writer.toString();
			Gson gson = new Gson();
			config = gson.fromJson(inputStr, Configurations.class);
			//prop.load(input);

		} 
		catch (IOException ex) {
			appendToFile(ex);
			ex.printStackTrace();
		} 
		finally {
			if (input != null){
				try {
					input.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return config;
	}

	//Parsing XML File.
	public static void ParseXML()
	{
		final Object lock = new Object();
		try {
			File file=null;
			synchronized(lock)
			{

				try {
					if (OSName.indexOf("win") >= 0) {
						file= new File(configPropeties.getlocalXMLPathWindows());
					} else {
						file = new File(configPropeties.getlocalXMLPathLinux());
					}
				}
				catch (Exception e) 
				{
					appendToFile(e);
				}
			}
			//File file = new File("E:\\XMLData\\Sample.xml");
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();

			Document doc = dBuilder.parse(file);
			StringBuilder sb = new StringBuilder();
			//sb.append("Root element :" + doc.getDocumentElement().getNodeName());
			//sb.append("<?xml version=\"1.0\"?>\r\n");
			//sb.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns=\"http://schemas.hp.com/SM/7\" xmlns:com=\"http://schemas.hp.com/SM/7/Common\" xmlns:xm=\"http://www.w3.org/2005/05/xmlmime\">\r\n" + 
					//"<soapenv:Header/>\r\n" + 
					//"<soapenv:Body>\r\n");
			//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			
			if (doc.hasChildNodes()) 
			{
				String res=	printNote(doc.getChildNodes());
				sb.append(res);
			}
			//sb.append("</soapenv:Body>\r\n" + 
					//"</soapenv:Envelope>");
			File fileParsed;

			if (OSName.indexOf("win") >= 0) {
				fileParsed= new File(configPropeties.getParsedXMLPathWindows());
			} 
			else
			{
				fileParsed = new File(configPropeties.getParsedXMLPathLinux());
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
		}

	}
//Used to print/write xml data.
	private static  String printNote(NodeList nodeList) throws IOException, UnsupportedOperationException, SOAPException {
		String result = "";
		for (int count = 0; count < nodeList.getLength(); count++) {

			Node tempNode = nodeList.item(count);
			// make sure it's element node.
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) 
			{
				String nodeName=tempNode.getNodeName();
				// get node name and value
				//sb.append("\nNode Name =" + tempNode.getNodeName() + " [OPEN]");
				//System.out.println("\nNode Name =" + tempNode.getNodeName() + " [OPEN]");
				//System.out.println("Node Value =" + tempNode.getTextContent());
				//sb.append("Node Value =" + tempNode.getTextContent());
				if(nodeName=="rootcause")
				{
					result=getNodesData(tempNode);
				}
				if (tempNode.hasChildNodes()) 
				{

					// loop again if has child nodes
					result=printNote(tempNode.getChildNodes());
					
//						if(count==3)
//						{
//							break;
//						}
//						if(count != nodeList.getLength())
//						{
//						sb.append("</soapenv:Body>\r\n" + 
//								"</soapenv:Envelope>");
//						}
						//System.out.println(sb.toString());
						System.out.println("\n");
						//ExecutorService pool = Executors.newFixedThreadPool(count);
						//UploadFileAPI(sb.toString());
						//pool.submit(UploadFileAPI(sb.toString())).get();
						//callSoapWebService(sb.toString());
						//sb=null;
//						File fileParsed;
//
//						if (OSName.indexOf("win") >= 0) {
//							fileParsed= new File(configPropeties.getParsedXMLPathWindows());
//						} 
//						else
//						{
//							fileParsed = new File(configPropeties.getParsedXMLPathLinux());
//						}
//						if (!fileParsed.exists())
//						{
//							fileParsed.createNewFile();
//						}
//						try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileParsed))) {
//							DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//							Date date = new Date();
//							writer.write("\n New request on" +(dateFormat.format(date))+ "\n");
//							writer.write(sb.toString());
//						}
//					catch (Exception e) 
//					{
//						appendToFile(e);
//					}
						//System.out.println(sb.toString());
					}
					//System.out.println(result);
					//sb.append(result);
				}
				//sb.append("Node Name =" + tempNode.getNodeName() + " [CLOSE]");
				//FileWriter fstream = new FileWriter(fileParsed.getPath(), true);
				//System.out.println("Node Name =" + tempNode.getNodeName() + " [CLOSE]");

		}
		return result;
	}
	
	public static String getNodesData(Node tempNode)//NamedNodeMap nnm)
	{
		boolean sendRequest=false;
		StringBuilder sb = new StringBuilder();
		sb.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns=\"http://schemas.hp.com/SM/7\" xmlns:com=\"http://schemas.hp.com/SM/7/Common\" xmlns:xm=\"http://www.w3.org/2005/05/xmlmime\">\r\n" + 
				"<soapenv:Header/>\r\n" + 
				"<soapenv:Body>\r\n");
		sb.append("<ns:CreateIncidentRequest>\r\n" + 
				"         <ns:model>\r\n" + 
				"            <ns:keys>\r\n" + 
				"               <ns:IncidentID type=\"String\"></ns:IncidentID>\r\n"+ 
				"            </ns:keys>\r\n" + 
				"            <ns:instance>\r\n"+
				"<ns:Title type=\"String\">test from SOAP UI</ns:Title>\r\n"+
				"<ns:Description type=\"Array\">\r\n");
		int severityInt=0;
		long alarmId=0;
	if (tempNode.hasAttributes())
	{

		// get attributes names and values
		NamedNodeMap nodeMap = tempNode.getAttributes();
		

		for (int i = 0; i < nodeMap.getLength(); i++) {
			
			Node node = nodeMap.item(i);
			if(node.getNodeName()=="severity")
			{
				String severity="";
				severity=node.getNodeValue();
				if(severity.toLowerCase()=="critical")
					severityInt=1;
				else if(severity.toLowerCase()=="major")
					severityInt=2;
				else if(severity.toLowerCase()=="marginal")
					severityInt=3;
				else
					severityInt=4;
			}
			else if(node.getNodeName()=="alarmid")
			{
				alarmId=Long.parseLong(node.getNodeValue());
			}
			else
			{
			sb.append("<ns:Description type=\"String\" >"+node.getNodeValue()+"</ns:Description>\r\n");
			//sb.append("attr value : " + node.getNodeValue());
			}

		}
		sendRequest=true;
	}
	sb.append("</ns:Description>\r\n");
	sb.append(" <ns:Category type=\"String\">Incident</ns:Category>\r\n");
	sb.append("<ns:Area type=\"String\">Stablenet</ns:Area>\r\n");
	sb.append("<ns:Subarea type=\"String\">Alarm</ns:Subarea>\r\n");
	sb.append("<ns:Urgency type=\"String\">"+severityInt+"</ns:Urgency>\r\n ");
	sb.append("<ns:AssignmentGroup type=\"String\" >ROP HELPDESK</ns:AssignmentGroup>\r\n");
	sb.append("<ns:Service type=\"String\">CI1001366</ns:Service>\r\n");
	//sb.append("<ns:Service type=\"String\">CI1001366</ns:Service>\r\n");
	sb.append("<ns:Impact type=\"String\">1</ns:Impact>\r\n");
	sb.append("<ns:ExternalID type=\"String\">"+alarmId+"</ns:ExternalID>\r\n");
	sb.append("</ns:instance>\r\n" +  
			"</ns:model>\r\n");
	sb.append("</ns:CreateIncidentRequest>\r\n");
	//if(result.indexOf("</soapenv:Body>")==-1)
		sb.append("</soapenv:Body>\r\n" + 
				"</soapenv:Envelope>");
		if(sendRequest)
		{
			callSoapWebService(sb.toString());
			//System.out.println(sb.toString());
		}
		//callSoapWebService(sb.toString());
		String result="";
		return result;
	}
	
	// Calling stableNet's server API for provided url and parameters for that URL.
	public static String executePost(String targetURL, String urlParameters) {
		final Object lock = new Object();  
		HttpURLConnection connection = null;
		String path=null;
		if (OSName.indexOf("win") >= 0) 
		{
			path=configPropeties.getlocalXMLPathWindows();
		} else
		{
			path=configPropeties.getlocalXMLPathLinux();
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
			HostnameVerifier allHostsValid = new HostnameVerifier()
			{
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};
			//Create connection

			URL url = new URL(targetURL);
			String userCredentials = configPropeties.getSNUserName()+":"+configPropeties.getSNPassword();
			//String userCredentials = "infosim:stablenet";
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
				if (!file.exists()) {
					file.createNewFile();
				}
				synchronized(lock)
				{
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
	
	//Upload XML to API specified in configurations file.
	public static synchronized String UploadFileAPI(String strXML) throws UnsupportedOperationException, SOAPException
	{
		String result=null;
		//File inFile =null;
		/*
		 * if (OSName.indexOf("win") >= 0) { inFile= new
		 * File(configPropeties.getParsedXMLPathWindows()); } else { inFile=new
		 * File(configPropeties.getParsedXMLPathLinux()); } FileInputStream fis = null;
		 */
		try {
			
		        //return parseKlanten(response.getSOAPBody());
			//fis = new FileInputStream(inFile);
			//DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
			//httpclient.
			//HttpGet httpGet = new HttpGet(configPropeties.getWebServiceInitialLink());
			// server back-end URL
			String UNPass=configPropeties.getWebServiceUserName()+":"+configPropeties.getWebServicePassword();
			//System.out.println(UNPass);
			String encoding = DatatypeConverter.printBase64Binary(UNPass.getBytes("UTF-8"));
			//httpGet.setHeader("Authorization", "Basic " + encoding);
			//HttpResponse responseInit = httpclient.execute(httpGet);
			//int statusCode =responseInit.getStatusLine().getStatusCode();
			//HttpEntity responseEntity =responseInit.getEntity(); 
			//String responseString =EntityUtils.toString(responseEntity, "UTF-8");
			//System.out.println(statusCode +" \n"+responseEntity+" \n"+ responseString);
			//int statusCodeInit = responseInit.getStatusLine().getStatusCode();
			//HttpEntity responseEntityInit = responseInit.getEntity();
			//String responseStringInit = EntityUtils.toString(responseEntityInit, "UTF-8");
			//result= statusCodeInit + "\n " + responseStringInit;
			//
			//To DO From Tomorrow. TOday 02-oct-2019.
			
			  SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance(); 
			  SOAPConnection soapConnection = soapConnectionFactory.createConnection();
			  String url = configPropeties.getfileUploadUrl(); //SOAPMessage message = ; SOAPMessage
			  MessageFactory mf = MessageFactory.newInstance();
			  MimeHeaders hd = new MimeHeaders();//.getMimeHeaders();
			  hd.addHeader("Authorization", "Basic " + encoding);
			  SOAPMessage msg = mf.createMessage(hd, new ByteArrayInputStream(strXML.getBytes()));
			  SOAPMessage responses = soapConnection.call(msg, url);
			  ByteArrayOutputStream out = new ByteArrayOutputStream();
			  msg.writeTo(out);
			  String strMsg = new String(out.toByteArray());
			  System.out.println(strMsg);
			  File fileParsed;
				if (OSName.indexOf("win") >= 0) {
					fileParsed= new File(configPropeties.getParsedXMLPathWindows());
				} 
				else
				{
					fileParsed = new File(configPropeties.getParsedXMLPathLinux());
				}
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileParsed))) {
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date date = new Date();
					writer.write("\n New request on" +(dateFormat.format(date))+ "\n");
					writer.write(strMsg);
				}
			catch (Exception e) 
			{
				appendToFile(e);
			}
			 
			///
//			SOAPConnectionFactory factory = SOAPConnectionFactory.newInstance();
//
//			SOAPConnection soapConnection =factory.createConnection();
//
//			SOAPMessage message = MessageFactory.newInstance().createMessage(null,yourInputStream);
//
//			message.getMimeHeaders().addHeader("header name", "header value");
//
//			soapConnection.call(message, "server url");
			//
			/*
			 * DefaultHttpClient httpclientPost = new DefaultHttpClient(new
			 * BasicHttpParams()); HttpPost httppostTicket = new
			 * HttpPost(configPropeties.getfileUploadUrl());
			 * httppostTicket.setHeader("Authorization", "Basic " + encoding);
			 * MultipartEntity entity = new MultipartEntity(); //String encoding =
			 * Base64Encoder.encode("" + ":" + ""); //String encoding =
			 * Base64.getEncoder().encodeToString(("test1:test1").getBytes(‌"UTF‌​-8"​)); //
			 * set the file input stream and file name as arguments //entity.addPart("file",
			 * new InputStreamBody(fis, inFile.getName())); entity.addPart("", new
			 * StringBody(strXML)); httppostTicket.setEntity(entity); // execute the request
			 * HttpResponse response = httpclientPost.execute(httppostTicket);
			 * System.out.println(response); int statusCode =
			 * response.getStatusLine().getStatusCode(); HttpEntity responseEntity =
			 * response.getEntity(); String responseString =
			 * EntityUtils.toString(responseEntity, "UTF-8"); result= statusCode + "\n " +
			 * responseString;
			 */

		} catch (ClientProtocolException e) {
			//System.err.println("Unable to make connection");
			//e.printStackTrace();
			appendToFile(e);
		} catch (IOException e)
		{
			//System.err.println("Unable to read file");
			//e.printStackTrace();
			appendToFile(e);
		} finally {
			/*
			 * try { if (fis != null) fis.close(); } catch (IOException e) {
			 * appendToFile(e); }
			 */
		}
		return result;
	}
	//New
	private static void createSoapEnvelope(SOAPMessage soapMessage) throws SOAPException {
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String myNamespace = "myNamespace";
        String myNamespaceURI = "http://www.webserviceX.NET";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(myNamespace, myNamespaceURI);

            /*
            Constructed SOAP Request Message:
            <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:myNamespace="http://www.webserviceX.NET">
                <SOAP-ENV:Header/>
                <SOAP-ENV:Body>
                    <myNamespace:GetInfoByCity>
                        <myNamespace:USCity>New York</myNamespace:USCity>
                    </myNamespace:GetInfoByCity>
                </SOAP-ENV:Body>
            </SOAP-ENV:Envelope>
            */

        // SOAP Body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("GetInfoByCity", myNamespace);
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("USCity", myNamespace);
        soapBodyElem1.addTextNode("New York");
    }

    private static synchronized void callSoapWebService(String strXML) {
    	//String soapEndpointUrl, String soapAction
        try {
            // Create SOAP Connection
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();
            String soapEndpointUrl = configPropeties.getWebServiceInitialLink();
            String soapAction = configPropeties.getfileUploadUrl();//"http://www.webserviceX.NET/GetInfoByCity";
            // Send SOAP Message to SOAP Server
            SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(soapAction, strXML), soapEndpointUrl);
            TimeUnit.MILLISECONDS.sleep(5000);
            // Print the SOAP Response
            
            System.out.println("Response SOAP Message:");
            soapResponse.writeTo(System.out);
            System.out.println();
            soapConnection.close();
            TimeUnit.MILLISECONDS.sleep(200);
        } catch (Exception e) {
            System.err.println("\nError occurred while sending SOAP Request to Server!\nMake sure you have the correct endpoint URL and SOAPAction!\n");
            e.printStackTrace();
        }
    }

    private static SOAPMessage createSOAPRequest(String soapAction, String strXML) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        String UNPass=configPropeties.getWebServiceUserName()+":"+configPropeties.getWebServicePassword();
        MimeHeaders hd = new MimeHeaders();
		String encoding = DatatypeConverter.printBase64Binary(UNPass.getBytes("UTF-8"));
        hd.addHeader("Authorization", "Basic " + encoding);
        hd.addHeader("SOAPAction", soapAction);
		SOAPMessage msg = messageFactory.createMessage(hd, new ByteArrayInputStream(strXML.getBytes()));
       // SOAPMessage soapMessage = messageFactory.createMessage();

        //createSoapEnvelope(soapMessage);

        //MimeHeaders headers = msg.getMimeHeaders();
		

        msg.saveChanges();

        /* Print the request message, just for debugging purposes */
        System.out.println("Request SOAP Message:");
        msg.writeTo(System.out);
        System.out.println("\n");
        return msg;
    }
public class UploadAPIMethod implements Callable<String>
{

	@Override
	public String call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
}
}

