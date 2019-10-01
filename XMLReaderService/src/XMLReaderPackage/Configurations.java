package XMLReaderPackage;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Configurations {
		//winndowsExceptionsPath
		@SerializedName("winndowsExceptionsPath")
		@Expose
		private String winndowsExceptionsPath;
		//linuxExceptionPath
		@SerializedName("linuxExceptionPath")
		@Expose
		private String linuxExceptionPath;
		//serverAPIUrl
		@SerializedName("serverAPIUrl")
		@Expose
		private String serverAPIUrl;
		//localXMLPathWindows
		@SerializedName("localXMLPathWindows")
		@Expose
		private String localXMLPathWindows;
		//fileUploadUrl
		@SerializedName("fileUploadUrl")
		@Expose
		private String fileUploadUrl;
		//alarmsActionUrl
		@SerializedName("alarmsActionUrl")
		@Expose
		private String alarmsActionUrl;
		//localXMLPathLinux
		@SerializedName("localXMLPathLinux")
		@Expose
		private String localXMLPathLinux;
		//SNUserName
		@SerializedName("SNUserName")
		@Expose
		private String SNUserName;
		//SNPassword
		@SerializedName("SNPassword")
		@Expose
		private String SNPassword;
		//continueScheduler
		@SerializedName("continueScheduler")
		@Expose
		private boolean continueScheduler;
		//callRepeateTime
		@SerializedName("callRepeateTime")
		@Expose
		private long callRepeateTime;
		//testFilePath
		@SerializedName("testFilePath")
		@Expose
		private String testFilePath;
		//parsedXMLPathWindows
		@SerializedName("parsedXMLPathWindows")
		@Expose
		private String parsedXMLPathWindows;
		//parsedXMLPathLinux
		@SerializedName("parsedXMLPathLinux")
		@Expose
		private String parsedXMLPathLinux;
		//webServiceUserName
		@SerializedName("webServiceUserName")
		@Expose
		private String webServiceUserName;
		//webServicePassword
		@SerializedName("webServicePassword")
		@Expose
		private String webServicePassword;
		//WinndowsExceptionsPath
		public String getWinndowsExceptionsPath() {
		return winndowsExceptionsPath;
		}

		public void setWinndowsExceptionsPath(String winndowsExceptionsPath) {
		this.winndowsExceptionsPath = winndowsExceptionsPath;
		}
		//LinuxExceptionPath
		public String getLinuxExceptionPath() {
		return linuxExceptionPath;
		}

		public void setLinuxExceptionPath(String linuxExceptionPath) {
		this.linuxExceptionPath = linuxExceptionPath;
		}
		//ServerAPIUrl
		public String getServerAPIUrl() {
		return serverAPIUrl;
		}
		public void setServerAPIUrl(String serverAPIUrl) {
		this.serverAPIUrl = serverAPIUrl;
		}
		//AlarmsActionUrl
		public String getAlarmsActionUrl() {
		return alarmsActionUrl;
		}

		public void setAlarmsActionUrl(String alarmsActionUrl) {
		this.alarmsActionUrl = alarmsActionUrl;
		}
		//localXMLPathWindows
		public String getlocalXMLPathWindows() {
		return localXMLPathWindows;
		}
		public void setlocalXMLPathWindows(String localXMLPathWindows) {
		this.localXMLPathWindows = localXMLPathWindows;
		}
		//fileUploadUrl
		public String getfileUploadUrl() {
		return fileUploadUrl;
		}

		public void setfileUploadUrl(String fileUploadUrl) {
		this.fileUploadUrl = fileUploadUrl;
		}
		//localXMLPathLinux
		public String getlocalXMLPathLinux() {
		return localXMLPathLinux;
		}

		public void setlocalXMLPathLinux(String localXMLPathLinux) {
		this.localXMLPathLinux = localXMLPathLinux;
		}
		//SNUserName
		public String getSNUserName() {
		return SNUserName;
		}

		public void setSNUserName(String SNUserName) {
		this.SNUserName = SNUserName;
		}
		//SNPassword
		public String getSNPassword() {
		return SNPassword;
		}

		public void setSNPassword(String SNPassword) {
		this.SNPassword = SNPassword;
		}
		//continueScheduler
		public boolean getcontinueScheduler() {
		return continueScheduler;
		}

		public void setcontinueScheduler(boolean continueScheduler) {
		this.continueScheduler = continueScheduler;
		}
		//callRepeateTime
		public long getCallRepeateTime() {
		return callRepeateTime;
		}

		public void setCallRepeateTime(long callRepeateTime) {
		this.callRepeateTime = callRepeateTime;
		}
		//testFilePath
		public String getTestFilePath() {
		return testFilePath;
		}

		public void setTestFilePath(String testFilePath) {
		this.testFilePath = testFilePath;
		}
		//parsedXMLPathWindows
		public String getParsedXMLPathWindows() {
		return parsedXMLPathWindows;
		}

		public void setParsedXMLPathWindows(String parsedXMLPathWindows) {
		this.parsedXMLPathWindows = parsedXMLPathWindows;
		}
		//parsedXMLPathLinux
		public String getParsedXMLPathLinux() {
		return parsedXMLPathLinux;
		}

		public void setParsedXMLPathLinux(String parsedXMLPathLinux) {
		this.parsedXMLPathLinux = parsedXMLPathLinux;
		}
		//webServiceUserName
		public String getWebServiceUserName() {
		return parsedXMLPathLinux;
		}

		public void setWebServiceUserName(String webServiceUserName) {
		this.webServiceUserName = webServiceUserName;
		}
		//webServicePassword
		public String getWebServicePassword() {
		return parsedXMLPathLinux;
		}

		public void setWebServicePassword(String webServicePassword) {
		this.webServicePassword = webServicePassword;
		}
}
