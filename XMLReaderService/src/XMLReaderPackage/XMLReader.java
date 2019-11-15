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
class XMLReader 
{

	private static String OSName = null;
	static Configurations configPropeties = new Configurations();
	//Main Method 
	//Calling getconfigPropetiess method
	//Calling startPollingTimer
	public static void main(String[] args) throws IOException 
	{
		System.setProperty("java.net.useSystemProxies", "true");
		OSName = System.getProperty("os.name").toLowerCase();
		
		configPropeties= getconfigPropetiess();
		String serverCompleteUrl= configPropeties.getServerAPIUrl()+configPropeties.getAlarmsActionUrl();
		//ParseXML();
		//executePost(serverCompleteUrl,"");
		//String ApiResult= UploadFileAPI();
		startPollingTimer(serverCompleteUrl, configPropeties.getCallRepeateTime()); 
	}
	// static timer's variable.
	public static Timer t;
	// Synchronized static timer repeating method.
	// Repeating call of UploadFileAPI method after specified time in configuration property callRepeateTime
	public static synchronized void startPollingTimer(String serverCompleteUrl, long timePeriod) 
	{
		if (t == null && configPropeties.getcontinueScheduler() == true) 
		{
			TimerTask task = new TimerTask() 
			{
				@Override
				public void run() 
				{
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
	//Exception write to file method.
	public static void appendToFile(Exception e) 
	{
		try {
			File file;
			if (OSName.indexOf("win") >= 0)
			{
				file= new File(configPropeties.getWinndowsExceptionsPath());
			} 
			else 
			{
				file = new File(configPropeties.getLinuxExceptionPath());
			}
			if (!file.exists())
			{
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
		catch (Exception ie) 
		{
			throw new RuntimeException("Could not write Exception to file", ie);
		}
	}
	// Reads and returns configurations properties.
	public static Configurations getconfigPropetiess() throws IOException 
	{
		Configurations config =new Configurations();
		InputStream input = null;

		try {
			if (OSName.indexOf("win") >= 0) 
			{
				//UTF_Encoding="UTF-8";
			input = new FileInputStream("D:\\Projects\\SNTicketingService\\XMLReaderService\\configurations.properties");
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
		catch (IOException ex)
		{
			appendToFile(ex);
			ex.printStackTrace();
		} 
		finally 
		{
			if (input != null)
			{
				try 
				{
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
		try 
		{
			File file=null;
			synchronized(lock)
			{

				try 
				{
					if (OSName.indexOf("win") >= 0)
					{
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
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();

			Document doc = dBuilder.parse(file);
			StringBuilder sb = new StringBuilder();
			
			if (doc.hasChildNodes()) 
			{
				String res=	printNote(doc.getChildNodes());
				sb.append(res);
			}
			File fileParsed;

			if (OSName.indexOf("win") >= 0)
			{
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
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileParsed))) 
			{
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
		for (int count = 0; count < nodeList.getLength(); count++)
		{

			Node tempNode = nodeList.item(count);
			// make sure it's element node.
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) 
			{
				String nodeName=tempNode.getNodeName();
				if(nodeName=="rootcause")
				{
					result=getNodesData(tempNode);
				}
				if (tempNode.hasChildNodes()) 
				{

					// loop again if has child nodes
					result=printNote(tempNode.getChildNodes());
				}
				}

		}
		return result;
	}
	//To format XML Nodes data according to required SOAP Format.
	public static String getNodesData(Node tempNode)//NamedNodeMap nnm)
	{
		boolean sendRequest=false;
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" standalone=\"no\"?>\r\n");
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
		

		for (int i = 0; i < nodeMap.getLength(); i++) 
		{
			
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
//		sb.append("</soap-body-content>\r\n" + 
//				" </soap-rpc-request>\r\n" + 
//				"</request-data>");
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
		} 
		else
		{
			path=configPropeties.getlocalXMLPathLinux();
		}
		final Path dst = Paths.get(path);
		final BufferedWriter writer;
		try {
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager()
			{
				public java.security.cert.X509Certificate[] getAcceptedIssuers() 
				{
					return null;
				}
				public void checkClientTrusted(X509Certificate[] certs, String authType)
				{
				}
				public void checkServerTrusted(X509Certificate[] certs, String authType)
				{
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
				public boolean verify(String hostname, SSLSession session) 
				{
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
				if (!file.exists())
				{
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
		} 
		finally 
		{
			if (connection != null) 
			{
				connection.disconnect();
			}
		}
	}
	
	//Simple
	// TO Call SOAP method and Pass required SOAP Data.
    private static synchronized void callSoapWebService(String strXML) {
    	//String soapEndpointUrl, String soapAction
        try
        {
            // Create SOAP Connection
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();
            String soapEndpointUrl = configPropeties.getWebServiceInitialLink();
            String soapAction = configPropeties.getfileUploadUrl();//"http://www.webserviceX.NET/GetInfoByCity";
            // Send SOAP Message to SOAP Server
            SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(soapAction, strXML), soapEndpointUrl);
            TimeUnit.MILLISECONDS.sleep(200);
            // Print the SOAP Response
            System.out.println("Response SOAP Message:");
            soapResponse.writeTo(System.out);
            System.out.println();
            soapConnection.close();
            TimeUnit.MILLISECONDS.sleep(50);
         }
        catch (Exception e)
        {
            System.err.println("\nError occurred while sending SOAP Request to Server!\nMake sure you have the correct endpoint URL and SOAPAction!\n");
            e.printStackTrace();
        }
    }
    //To create proper SOAP Message including Headers ETC.
    private static SOAPMessage createSOAPRequest(String soapAction, String strXML) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        String UNPass=configPropeties.getWebServiceUserName()+":"+configPropeties.getWebServicePassword();
        MimeHeaders hd = new MimeHeaders();
		String encoding = DatatypeConverter.printBase64Binary(UNPass.getBytes("UTF-8"));
        hd.addHeader("Authorization", "Basic " + encoding);
        hd.addHeader("SOAPAction", configPropeties.getSOAPActionName());
        hd.addHeader("endPoint", soapAction);//"http://smsvr1-dkl-2a.scnrop.gov.om:13081/SM/7/CreateIncident"
        hd.setHeader("Content-Type", "text/xml;charset=utf-8");
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
}

