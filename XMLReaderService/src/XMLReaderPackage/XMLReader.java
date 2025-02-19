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
import java.io.StringReader;
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.jnlp.UnavailableServiceException;
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
import org.apache.commons.io.output.StringBuilderWriter;
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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.sun.xml.internal.ws.wsdl.parser.InaccessibleWSDLException;

import java.io.InputStream;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
class XMLReader 
{

	private static String OSName = null;
	static Configurations configPropeties = new Configurations();
	static ConnectionPoolManager cpm=null;//new ConnectionPoolManager();
	static StringBuilder allAlarmIds = null;
	//Main Method 
	//Calling getconfigPropetiess method
	//Calling startPollingTimer
	public static void main(String[] args) throws IOException 
	{
		System.setProperty("java.net.useSystemProxies", "true");
		OSName = System.getProperty("os.name").toLowerCase();
		
		configPropeties= getconfigPropetiess();
		cpm=new ConnectionPoolManager("jdbc:mysql://"+configPropeties.getDataBaseURL(),
				configPropeties.getDataBaseUserName(), configPropeties.getDataBasePassword());
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
					allAlarmIds = new StringBuilder();
					String restResult= executePost(serverCompleteUrl,"");
					ParseXML(restResult);
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
			//fw = new FileWriter ("exception.txt", true);
			FileWriter fstream = new FileWriter(file.getPath(), true);
			BufferedWriter out = new BufferedWriter(fstream);
			PrintWriter pWriter = new PrintWriter(out, true);
			try
			{
			
			pWriter.print("*****************--------New exception----------********************");
			pWriter.println();
			DateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss");
			Date date = new Date();
			pWriter.print("As on "+dateFormat.format(date));
			pWriter.println();
			e.printStackTrace(pWriter);
			}
			catch(Exception ex)
			{
				throw new RuntimeException("Exception occured while trying to write Exception to file", ex);
			}
			finally 
			{
			    try 
			    {
			    	fstream.close();
					pWriter.close();
					out.close();
			    }
			    catch (IOException ex) 
			    {
			    	throw new RuntimeException("unable to close exceptions file, stream or writer.", ex);
			    }
			}
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
			input = new FileInputStream("D:\\SNTicket\\SNTicketingService\\XMLReaderService\\configurations.properties");
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
	public static void ParseXML(String restResult)
	{
		//final Object lock = new Object();
		try 
		{
//			File file=null;
//			synchronized(lock)
//			{
//
//				try 
//				{
//					if (OSName.indexOf("win") >= 0)
//					{
//						file= new File(configPropeties.getlocalXMLPathWindows());
//					} else 
//					{
//						file = new File(configPropeties.getlocalXMLPathLinux());
//					}
//				}
//				catch (Exception e) 
//				{
//					appendToFile(e);
//				}
//			}
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			InputSource iss = new InputSource();
			iss.setCharacterStream(new StringReader(restResult));
			
			//Document doc = dBuilder.parse(file);
			Document doc = dBuilder.parse(iss);;
			if (doc.hasChildNodes()) 
			{
				Node tempNode=doc.getChildNodes().item(0);
				String res=	printNote(tempNode.getChildNodes());
				//CLoseALarmsTicketsMethod
				closeAlarmsTickets(allAlarmIds.toString());
			}
			//File fileParsed;

//			if (OSName.indexOf("win") >= 0)
//			{
//				fileParsed= new File(configPropeties.getParsedXMLPathWindows());
//			} 
//			else
//			{
//				fileParsed = new File(configPropeties.getParsedXMLPathLinux());
//			}
//			if (!fileParsed.exists())
//			{
//				fileParsed.createNewFile();
//			}
//			try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileParsed))) 
//			{
//				writer.write(sb.toString());
//			}
		} catch (Exception e) 
		{
			appendToFile(e);
		}

	}
	//Comment Test
//Used to print/write xml data.
	private static  String printNote(NodeList nodeList) throws IOException, UnsupportedOperationException, SOAPException {
		String result = "";
		
	    //int nodeLength=nodeList.getLength();
		for (int count = 0; count < nodeList.getLength(); count++)
		{

			Node tempNode = nodeList.item(count);
			// make sure it's element node.
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) 
			{
				String nodeName=tempNode.getNodeName().trim();
				if(nodeName.equals("openalarm"))
				{
					Node tempRootCauseNode = tempNode.getFirstChild();
					String nodeNameChild=tempRootCauseNode.getNodeName();
					if(nodeNameChild.equals("rootcause"))
					{
						long resultAlarmId=getNodesData(tempRootCauseNode);
						if(resultAlarmId !=0)
						{
							if(allAlarmIds == null || allAlarmIds.toString().equals(""))
							allAlarmIds.append(resultAlarmId);
							else
								allAlarmIds.append(", " + resultAlarmId);
						}
					 }
				}
//				else if (nodeName == "openalarm" && tempNode.hasChildNodes()) 
//				{
//					// loop again if has child nodes
//					//result=printNote(tempNode.getChildNodes());
//				}
				else
					continue;
				}

		}
		return result;
	}
	
	private static void closeAlarmsTickets(String alarmsList)
	{
		ResultSet res=null;
		try
		{
		String getQuery="Select alarm_id, incident_id from alarm_ticket where action_executed !='Closed'";
		if(!alarmsList.isEmpty())
			getQuery= getQuery+" and alarm_id not in(" + alarmsList + ")" ; 
		res= getAlarmsStateData(getQuery);
		if(res !=null && res.next())
		{
			String actionName=configPropeties.getSOAPActionNameResolve();
			 do {
				String resultedMessageAndIncidentId[]=new String[2];
				long alarmId= res.getLong("alarm_id");
				String incidentId=res.getString("incident_id");
				String ResolveTicketSoap=getResolveTicketSoap(incidentId, alarmId);
			    resultedMessageAndIncidentId=callSoapWebService(ResolveTicketSoap, actionName);
				String IncidentId=resultedMessageAndIncidentId[1];
				if(resultedMessageAndIncidentId[0].equals("Success"))
				{
				String updateQuery="update alarm_ticket set action_executed='closed', "
				+"action_executed_datetime = '" + getSystemCurentTime() + "' where alarm_id =" + alarmId +"";
				boolean result= SaveAlarmState(updateQuery);
				}
				else
					throw new  MicrofocusServerException("Exception occured while accessing microfocus server. Message is "
				+"" + resultedMessageAndIncidentId[0] +"and Incident Id is "+ IncidentId +" and AlarmId is "+ alarmId);
			 }
			while (res.next());
		}
		}
		catch(Exception ex)
		{
			appendToFile(ex);
		}
	}
	//To format XML Nodes data according to required SOAP Format.
	public static long getNodesData(Node tempNode)//NamedNodeMap nnm)
	{
		boolean sendRequest=false;
		StringBuilder sb=new StringBuilder();
		StringBuilder descriptinSb = new StringBuilder();
		String title="", descriptionString="", infoString="", sourceString="", alarmCountString="";
		String SOAPRes="";
//		NamedNodeMap tempNamedNodeMap=tempNode.getAttributes();
//		try
//		{
//			title=tempNamedNodeMap.getNamedItem("info").toString();
//		if(!title.equals(""))
//		{
//			title = title.replace("info=", "");
//		}
//		}
//		catch(Exception ex)
//		{
//			appendToFile(ex);
//		}
		int severityInt=0;
		long alarmId=0, commitId=0, alarmCount=0;
		String severity="";
	if (tempNode.hasAttributes())
	{
		// get attributes names and values
		NamedNodeMap nodeMap = tempNode.getAttributes();
		for (int i = 0; i < nodeMap.getLength(); i++) 
		{
			
			Node node = nodeMap.item(i);
			String nodeName= node.getNodeName();
			String nodeValue=node.getNodeValue();
			switch(nodeName)
			{
			case "info":
				title=nodeValue;
				break;
			case "severity":
			{
				severity=node.getNodeValue();
				if(severity.trim().toLowerCase().equals("marginal") || severity.trim().toLowerCase().equals("minor"))
				{
					return 0;
				}
				switch (node.getNodeName()) 
				{
				  case "critical":
					  severityInt=1;
				    break;
				  case "major":
				    severityInt=2;
				    break;
				  case "marginal":
					    severityInt=3;
					    break;
				  default:
					  severityInt=4;
				}
				break;
			}
			case "alarmid":
				alarmId=Long.parseLong(nodeValue);
				break;
			case "commitid":
				commitId=Long.parseLong(nodeValue);
				break;
			case "alarmcount":
				alarmCount=Long.parseLong(nodeValue);
				break;
			case "description":
				descriptionString=nodeValue;
				break;
			case "source":
				sourceString=nodeValue;
				break;
			 default:
			//sb.append("<ns:Description type=\"String\" >"+node.getNodeValue()+"</ns:Description>\r\n");
			//sb.append("attr value : " + node.getNodeValue());
				 break;
			}
		}
		descriptinSb.append("Alarm occured on source: " + sourceString +" with Information " + title 
				+" and description is: "+ descriptionString);
		sb.append("<ns:Description type=\"String\" >" + descriptinSb + "</ns:Description>\r\n");
		
		sendRequest=true;
	}
	
		String getQuery="Select * from alarm_ticket where action_executed !='Closed'"
				+" AND alarm_id=" + alarmId + " "; //+ alarmCount + " > alarm_count";
		ResultSet res =null;
		String resultedMessageAndIncidentId[]=new String[2];
		try
		{
			String query="";
			String actionName="";
			int count= 0;
			String severityDB="", actionExecuted="";;
			res =getAlarmsStateData(getQuery);
			if(res !=null && res.next())
			{
				count= res.getInt("alarm_count");
				severityDB=res.getString("alarm_severity");
				actionExecuted=res.getString("action_executed");
				//if(count<alarmCount || severityDB !=severity)
				if(count==alarmCount && severityDB.toLowerCase().trim().equals(severity.toLowerCase().trim())
						&& !actionExecuted.equals("closed"))
				{
					sendRequest=false;
					//do nothing so for.
					return alarmId;
				}
				else
				{
					String incidentId=res.getString("incident_id");
					String updateSOAP= getUpdateTicketSoap(incidentId, severityInt, alarmCount);
					//should be update call in future.
					actionName=configPropeties.getSOAPActionNameUpdate();
					try
					{
					   resultedMessageAndIncidentId=callSoapWebService(updateSOAP, actionName);
					}
					catch(Exception ex)
					{
						appendToFile(ex);
					}
					if(resultedMessageAndIncidentId[0].equals("Success"))
					{
					query="update alarm_ticket set alarm_count=" + alarmCount + ""
					+", alarm_severity='" + severity + "', action_executed='updated'"
					+", alarm_commitId=" + commitId + ", action_executed_datetime="
					+"'" + getSystemCurentTime() + "' where alarm_id =" + alarmId + "";
					try 
					{
						SaveAlarmState(query);
					} 
					catch (ClassNotFoundException e) 
					{
						appendToFile(e);
					} 
					}
				}
			}
			else
			{
				SOAPRes= getCreateTicketSoap(title,sb.toString(),severityInt, alarmId);
				actionName=configPropeties.getSOAPActionNameCreate();
				String IncidentId="";
				if(sendRequest)
				{
					 resultedMessageAndIncidentId=callSoapWebService(SOAPRes, actionName);
					 IncidentId=resultedMessageAndIncidentId[1];
					//System.out.println(sb.toString());
				}
				//System.out.println(formatter.format(date));
				if(resultedMessageAndIncidentId[0].equals("Success"))
				{
				query="INSERT INTO alarm_ticket (alarm_id, alarm_severity, alarm_count,"
				+" action_executed, action_executed_datetime, alarm_commitId, incident_id) VALUES "
				+"(" + alarmId + ", '" + severity + "', " + alarmCount + ", " + "'Created'" + " ,"
				+" '" + getSystemCurentTime() + "', " + commitId + ", '"+IncidentId+"')";
				try 
				{
					SaveAlarmState(query);
				} 
				catch (ClassNotFoundException e) 
				{
					appendToFile(e);
				} 
				catch (SQLException e) 
				{
					appendToFile(e);
				}
				}
				else if(IncidentId == null || IncidentId.isEmpty())
				{
					throw new  MicrofocusServerException("Exception occured while accessing microfocus server."+ IncidentId); 
				}
			}
			
			//callSoapWebService(sb.toString());
		} 
		catch (Exception e)
		{
			appendToFile(e);
		}
		return alarmId;
	}
	//Method to get SOAP response to create new ticket.
	private static String getCreateTicketSoap(String title, String descriptionString, int severityInt, long alarmId)
	{
		StringBuilder sb=new StringBuilder();
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
				"<ns:Title type=\"String\">" + title + "</ns:Title>\r\n"+
				"<ns:Description type=\"Array\">\r\n");
		//
		sb.append(descriptionString);
		sb.append("</ns:Description>\r\n");
		sb.append(" <ns:Category type=\"String\">Incident</ns:Category>\r\n");
		sb.append("<ns:Area type=\"String\">performance</ns:Area>\r\n");
		sb.append("<ns:Subarea type=\"String\">performance degradation</ns:Subarea>\r\n");
		sb.append("<ns:Urgency type=\"String\">"+severityInt+"</ns:Urgency>\r\n ");
		sb.append("<ns:AssignmentGroup type=\"String\" >" + configPropeties.getAssignmentGroupTitle() + "</ns:AssignmentGroup>\r\n");
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
//			sb.append("</soap-body-content>\r\n" + 
//					" </soap-rpc-request>\r\n" + 
//					"</request-data>");
		return sb.toString();
	}
	//Method to get SOAP response to update an existing ticket.
	private static String getUpdateTicketSoap(String incidentId, long severity, long alarmCount)
	{
		StringBuilder sb=new StringBuilder();
		sb.append("<?xml version=\"1.0\" standalone=\"no\"?>\r\n");
		sb.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns=\"http://schemas.hp.com/SM/7\" xmlns:com=\"http://schemas.hp.com/SM/7/Common\" xmlns:xm=\"http://www.w3.org/2005/05/xmlmime\">\r\n" + 
				"<soapenv:Header/>\r\n" + 
				"<soapenv:Body>\r\n");
		sb.append("<ns:UpdateIncidentRequest attachmentInfo=\"?\" attachmentData=\"?\" ignoreEmptyElements=\"true\" updateconstraint=\"-1\">\r\n" + 
				"         <ns:model>\r\n" + 
				"            <ns:keys>\r\n" + 
				"               <!--Optional:-->\r\n" + 
				"               <ns:IncidentID>" + incidentId + "</ns:IncidentID>\r\n" + 
				"            </ns:keys>\r\n" + 
				"            <ns:instance>\r\n" + 
				"                        \r\n" + 
				" \r\n" + 
				"               <JournalUpdates>\r\n" + 
				"                  <JournalUpdates>"+severity+"</JournalUpdates>\r\n" + 
				"		  		   <JournalUpdates>" + alarmCount + "</JournalUpdates>\r\n" + 
				"               </JournalUpdates>\r\n" + 
				"               \r\n" + 
				"            </ns:instance>\r\n" + 
				"            <!--Optional:-->\r\n" + 
				"            \r\n" + 
				"         </ns:model>\r\n" + 
				"      </ns:UpdateIncidentRequest>");
		sb.append("</soapenv:Body>\r\n" + 
				"</soapenv:Envelope>");
		return sb.toString();
	}
	private static String getResolveTicketSoap(String incidentId, long alarmId)
	{
		StringBuilder sb=new StringBuilder();
		sb.append("<?xml version=\"1.0\" standalone=\"no\"?>\r\n");
		sb.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns=\"http://schemas.hp.com/SM/7\" xmlns:com=\"http://schemas.hp.com/SM/7/Common\" xmlns:xm=\"http://www.w3.org/2005/05/xmlmime\">\r\n" + 
				"<soapenv:Header/>\r\n" + 
				"<soapenv:Body>\r\n");
		sb.append("<ns:ResolveIncidentRequest attachmentInfo=\"\" attachmentData=\"\" ignoreEmptyElements=\"true\" updateconstraint=\"-1\">\r\n" + 
				"         <ns:model query=\"\">\r\n" + 
				"            <ns:keys query=\"\" updatecounter=\"\">\r\n" + 
				"               <ns:IncidentID type=\"String\" mandatory=\"?\" readonly=\"?\">" + incidentId + "</ns:IncidentID>\r\n" + 
				"			<!--<ns:ExternalID type=\"String\" mandatory=\"\" readonly=\"\">1568808040464308</ns:ExternalID>-->\r\n" + 
				"            </ns:keys>\r\n" + 
				"            <ns:instance query=\"\" uniquequery=\"\" recordid=\"\" updatecounter=\"\">\r\n" + 
				"               \r\n" + 
				"               <!--Optional:-->\r\n" + 
				"               <ns:Solution type=\"String\" mandatory=\"?\" readonly=\"?\">Alarm resolved</ns:Solution>\r\n" + 
				"               <ns:ClosureCode type=\"String\" mandatory=\"?\" readonly=\"?\">Automatically Closed</ns:ClosureCode>\r\n" + 
				"		     \r\n" + 
				"               \r\n" + 
				"            </ns:instance>\r\n" + 
				"           \r\n" + 
				"         </ns:model>\r\n" + 
				"      </ns:ResolveIncidentRequest>");
		sb.append("</soapenv:Body>\r\n" + 
				"</soapenv:Envelope>");
		return sb.toString();
	}
	 private static String getSystemCurentTime()
	 {
		 	SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date(System.currentTimeMillis());
			String currentTime = formatter.format(date);
			return currentTime;
		 
	 }
	
	// Calling stableNet's server API for provided url and parameters for that URL.
	public static String executePost(String targetURL, String urlParameters) {
		//final Object lock = new Object();  
		HttpURLConnection connection = null;
		//String path=null;
//		if (OSName.indexOf("win") >= 0) 
//		{
//			path=configPropeties.getlocalXMLPathWindows();
//		} 
//		else
//		{
//			path=configPropeties.getlocalXMLPathLinux();
//		}
//		final Path dst = Paths.get(path);
		//final BufferedWriter writer;
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
			String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty ("Authorization", basicAuth);
			connection.setRequestMethod("GET");
			//Get Response  
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			//writer = Files.newBufferedWriter(dst, StandardCharsets.UTF_8);
			StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
			String line;
			
			//TODO: Remove file writing code for production. This is just for test purposes.
			
			while ((line = rd.readLine()) != null) {
				response.append(line);
				//byte[] contentInBytes = line.getBytes();
				//File file = new File(path);
				//FileOutputStream fop =null;
				// If file doesn't exists, then create it
//				if (!file.exists())
//				{
//					file.createNewFile();
//				}
//				synchronized(lock)
//				{
//					fop = new FileOutputStream(file);
//					fop.write(contentInBytes);
//				}
				//fop.flush();
				//fop.close();

				//writer.write(contentInBytes);
				// must do this: .readLine() will have stripped line endings
				//writer.newLine();
				response.append('\r');
			}
			rd.close();
			is.close();
			//return is;
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
    private static synchronized String[] callSoapWebService(String strXML, String actionName) {
    	//String soapEndpointUrl, String soapAction
    	String resultedMessageAndIncidentId[]=new String[2];
        try
        {
            // Create SOAP Connection
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();
            String soapEndpointUrl = configPropeties.getWebServiceInitialLink();
            String soapAction = configPropeties.getfileUploadUrl();//"http://www.webserviceX.NET/GetInfoByCity";
            // Send SOAP Message to SOAP Server
            SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(soapAction, strXML, actionName), soapEndpointUrl);
           // Response for test purposes.
//            String send="<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n" + 
//            		"   <SOAP-ENV:Body>\r\n" + 
//            		"      <CreateIncidentResponse message=\"Success\" returnCode=\"0\" schemaRevisionDate=\"2019-10-01\" schemaRevisionLevel=\"1\" status=\"SUCCESS\" xsi:schemaLocation=\"http://schemas.hp.com/SM/7 http://smsvr1-mct-1a.scnrop.gov.om:13080/SM/7/Incident.xsd\" xmlns=\"http://schemas.hp.com/SM/7\" xmlns:cmn=\"http://schemas.hp.com/SM/7/Common\" xmlns:xmime=\"http://www.w3.org/2005/05/xmlmime\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n" + 
//            		"         <model>\r\n" + 
//            		"            <keys>\r\n" + 
//            		"               <IncidentID type=\"String\">IM575132</IncidentID>\r\n" + 
//            		"            </keys>\r\n" + 
//            		"            <instance recordid=\"IM575132 - Stablenet ALARM : Desription\" uniquequery=\"number=&quot;IM575132&quot;\">\r\n" + 
//            		"               <IncidentID type=\"String\">IM575132</IncidentID>\r\n" + 
//            		"               <Category type=\"String\">Incident</Category>\r\n" + 
//            		"               <OpenTime type=\"DateTime\">2019-12-30T10:52:36+00:00</OpenTime>\r\n" + 
//            		"               <OpenedBy type=\"String\">int-sn</OpenedBy>\r\n" + 
//            		"               <Urgency type=\"String\">4</Urgency>\r\n" + 
//            		"               <UpdatedTime type=\"DateTime\">2019-12-30T10:52:36+00:00</UpdatedTime>\r\n" + 
//            		"               <AssignmentGroup type=\"String\">ROP-ETESALAT-FO</AssignmentGroup>\r\n" + 
//            		"               <Description type=\"Array\">\r\n" + 
//            		"                  <Description type=\"String\">text1</Description>\r\n" + 
//            		"                  <Description type=\"String\">text2</Description>\r\n" + 
//            		"                  <Description type=\"String\">text3</Description>\r\n" + 
//            		"               </Description>\r\n" + 
//            		"               <Title type=\"String\">Stablenet ALARM : Desription</Title>\r\n" + 
//            		"               <UpdatedBy type=\"String\">int-sn</UpdatedBy>\r\n" + 
//            		"               <Status type=\"String\">Categorize</Status>\r\n" + 
//            		"               <Phase type=\"String\">Categorization</Phase>\r\n" + 
//            		"               <Area type=\"String\">performance</Area>\r\n" + 
//            		"               <Subarea type=\"String\">performance degradation</Subarea>\r\n" + 
//            		"               <Impact type=\"String\">1</Impact>\r\n" + 
//            		"               <Service display=\"Default\" type=\"String\">CI1001366</Service>\r\n" + 
//            		"               <ExternalID type=\"String\">IM15</ExternalID>\r\n" + 
//            		"            </instance>\r\n" + 
//            		"         </model>\r\n" + 
//            		"         <messages>\r\n" + 
//            		"            <cmn:message type=\"String\">US/Mountain 12/30/19 03:52:36:  Incident IM575132 has been opened by int-sn</cmn:message>\r\n" + 
//            		"            <cmn:message type=\"String\">Incident \"IM575132\" added.</cmn:message>\r\n" + 
//            		"         </messages>\r\n" + 
//            		"      </CreateIncidentResponse>\r\n" + 
//            		"   </SOAP-ENV:Body>\r\n" + 
//            		"</SOAP-ENV:Envelope>\r\n" + 
//            		"";
//            InputStream is = new ByteArrayInputStream(send.getBytes());
//            SOAPMessage soapResponse = MessageFactory.newInstance().createMessage(null, is);
           Document doc= parseSoapResponse(soapResponse);
           if (doc.hasChildNodes()) 
			{
        	   resultedMessageAndIncidentId = ParseAndUpdateSoapResponse(doc.getChildNodes(), actionName);
				//System.out.println(resultedIncidentId);
				//sb.append(res);
			}
            //String soapString=soapResponse.toString();
            //TimeUnit.MILLISECONDS.sleep(200);
            // Print the SOAP Response
            //System.out.println("Response SOAP Message:");
            //soapResponse.writeTo(System.out);
            //System.out.println();
            soapConnection.close();
            //TimeUnit.MILLISECONDS.sleep(50);
         }
        catch (Exception e)
        {
            System.err.println("\nError occurred while sending SOAP Request to Server!\nMake sure you have the correct endpoint URL and SOAPAction!\n");
            e.printStackTrace();
            appendToFile(e);
        }
       return resultedMessageAndIncidentId;
    }
    //To create proper SOAP Message including Headers ETC.
    private static SOAPMessage createSOAPRequest(String soapAction, String strXML, String actionName) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        String UNPass=configPropeties.getWebServiceUserName()+":"+configPropeties.getWebServicePassword();
        MimeHeaders hd = new MimeHeaders();
		String encoding = DatatypeConverter.printBase64Binary(UNPass.getBytes("UTF-8"));
        hd.addHeader("Authorization", "Basic " + encoding);
        hd.addHeader("SOAPAction", actionName);
        hd.addHeader("endPoint", soapAction);//"http://smsvr1-dkl-2a.scnrop.gov.om:13081/SM/7/CreateIncident"
        hd.setHeader("Content-Type", "text/xml;charset=utf-8");
		SOAPMessage msg = messageFactory.createMessage(hd, new ByteArrayInputStream(strXML.getBytes()));
       // SOAPMessage soapMessage = messageFactory.createMessage();

        //createSoapEnvelope(soapMessage);

        //MimeHeaders headers = msg.getMimeHeaders();
		

        msg.saveChanges();

        /* Print the request message, just for debugging purposes */
       // System.out.println("Request SOAP Message:");
       // msg.writeTo(System.out);
       // System.out.println("\n");
        return msg;
    }
    public static List<String> getFullNameFromXml(String response, String tagName) throws Exception 
    {
    	List<String> ids;
    	try 
    	{
        Document xmlDoc = loadXMLString(response);
        NodeList nodeList = xmlDoc.getElementsByTagName(tagName);
        ids = new ArrayList<String>(nodeList.getLength());
        for(int i=0;i<nodeList.getLength(); i++) {
            Node x = nodeList.item(i);
            ids.add(x.getFirstChild().getNodeValue());             
            //System.out.println(nodeList.item(i).getFirstChild().getNodeValue());
        }

    	}
    	catch(Exception e)
    	{
    		ids=null;
    		appendToFile(e);
    	}
        return ids;
    }
    private static Document parseSoapResponse(SOAPMessage soapMessage)
    {
    	//String []Res;
    	 final ByteArrayOutputStream baos = new ByteArrayOutputStream();
         try 
         {
			soapMessage.writeTo(baos);
			final InputSource inputSource = new InputSource(new StringReader(
		             new String(baos.toByteArray())));
		         final DocumentBuilderFactory dbFactory = DocumentBuilderFactory
		             .newInstance();
		         final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		         final Document doc = dBuilder.parse(inputSource);
		      return doc; //doc.normalize();
			
		} 
         catch (Exception e)
         {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
         
    }
    public static Document loadXMLString(String response) throws Exception
    {
    	try
    	{
    		
        DocumentBuilderFactory dbf =DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(response));

        return db.parse(is);
    	}
    	catch(Exception ex)
    	{
    		appendToFile(ex);
    		return null;
    	}
    }
    private static ResultSet getAlarmsStateData(String query) throws ClassNotFoundException
    {
    	Connection conn= cpm.getConnectionFromPool();
        ResultSet rs;
		try 
		{
			Statement s = conn.createStatement();
	    	rs = s.executeQuery(query);
	    	//md = rs.getMetaData();
	    	
		}
		catch (SQLException e)
		{
			rs=null;
			e.printStackTrace();
			appendToFile(e);
		}
		finally
		{
			cpm.returnConnectionToPool(conn);
		}
		return rs;
    	
    }
    private static boolean SaveAlarmState(String query) throws SQLException, ClassNotFoundException
    {
    	Connection con= cpm.getConnectionFromPool();
    	boolean result=false;
    	try 
    	{
    	//Connection con=GetConnection();
        Statement st = con.createStatement();
        result= st.execute(query);
    	//Comment
    	}
    	catch(Exception e)
    	{
    		appendToFile(e);
    	}
    	finally
    	{
    		cpm.returnConnectionToPool(con);
    	}
    	return result;
    }
    
    private static String[] ParseAndUpdateSoapResponse(NodeList nodeList, String actionName) throws IOException, UnsupportedOperationException, SOAPException {
		String resultantArr[]= new String[2];
    	String result = "";
		String message="";
		for (int count = 0; count < nodeList.getLength(); count++)
		{
			if(result !="")
				break;
			Node tempNode = nodeList.item(count);
			// make sure it's element node.
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) 
			{
				String nodeName=tempNode.getNodeName();
				switch (nodeName)
				{
				case "CreateIncidentResponse":
					Node messageNodeCreate = tempNode.getAttributes().getNamedItem("message");//.getNodeValue();//("message");//.getTextContent().trim();
					message=messageNodeCreate.getNodeValue();
					//if(actionName.equals(configPropeties.getSOAPActionNameCreate()))
					result=getResponseNodesData(tempNode);
					resultantArr[0]=message;
					resultantArr[1]=result;
					break;
				case "UpdateIncidentResponse":
					Node messageNodeUpdate = tempNode.getAttributes().getNamedItem("message");//.getNodeValue();//("message");//.getTextContent().trim();
					message=messageNodeUpdate.getNodeValue();
					//if(actionName.equals(configPropeties.getSOAPActionNameCreate()))
					result=getResponseNodesData(tempNode);
					resultantArr[0]=message;
					resultantArr[1]=result;
					break;
				case "ResolveIncidentResponse":
					Node messageNodeResolve = tempNode.getAttributes().getNamedItem("message");//.getNodeValue();//("message");//.getTextContent().trim();
					message=messageNodeResolve.getNodeValue();
					//if(actionName.equals(configPropeties.getSOAPActionNameCreate()))
					result=getResponseNodesData(tempNode);
					resultantArr[0]=message;
					resultantArr[1]=result;
					break;
				default:
					break;
				}
				if (tempNode.hasChildNodes() && result=="") 
				{
					// loop again if has child nodes
					resultantArr=ParseAndUpdateSoapResponse(tempNode.getChildNodes(), actionName);
				}
				}
		}
		return resultantArr;
	}
    public static String getResponseNodesData(Node tempNode)//NamedNodeMap nnm)
	{
		String IncidentID="";
		NodeList list= tempNode.getChildNodes();
		boolean breakOuterloop=false;
		//NamedNodeMap tempNamedNodeMap=tempNode.getAttributes();
		for (int count = 0; count < list.getLength(); count++)
		{
			if(breakOuterloop)
				break;
			try
			{
			//NamedNodeMap tempNamedNodeMap=tempNode.getAttributes();//getChildNodes();
			String nodeName=list.item(count).getNodeName();
			//NodeList tempNamedNodeMap;
			if(nodeName=="model")
			{
				NodeList tempNamedNodeMap=list.item(count).getChildNodes();
				for (int subCount = 0; subCount < tempNamedNodeMap.getLength(); subCount++)
				{
				 String subNodeName=tempNamedNodeMap.item(count).getTextContent().trim();
				 if(subNodeName !="" && subNodeName !=null)
				 {
				 IncidentID=subNodeName;
				 breakOuterloop=true;
				// System.out.println(subNodeName);
				 break;
				 }
				
				}
			}
			}
			catch(Exception ex)
			{
				appendToFile(ex);
			}
		}
	return IncidentID;
	}
}
