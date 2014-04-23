import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import org.xbill.DNS.*;
public class Httpd {
  static final String USAGE = "java Httpd [base] [port] ";
  static List<String> record = new ArrayList<String>();
  public static void main (String[] args) {
    String base = "/home/charlie/webmail";
    int port = 80;
    System.out.println("Server is running");

    if (args.length > 0) base = args[0];
    if (base.equalsIgnoreCase("-h")
        || base.equalsIgnoreCase("-help")) {
      System.out.println(USAGE);
      System.exit(1);
    }
    if (args.length > 1)
      try { port = Integer.parseInt(args[1]);}
    catch (NumberFormatException e) {
    System.err.println(USAGE); System.exit(0);}
    try {
      ServerSocket ss = new ServerSocket(port);
      while (true) {    // the main server's loop
        Socket s = ss.accept();
        Handler h = new Handler(s, base,record);
        h.setPriority(h.getPriority() + 1);
        h.start();
      }
    } catch (IOException e) {
    System.out.println(e); System.exit(0);}
  }
}

/** The class Handler processes the client's request in a separate thread */

class Handler extends Thread implements Serializable {
  static final String SERVER = "Server: Httpd 1.0";
  static final String OK = "HTTP/1.0 200 OK";
  static final String NOT_FOUND = "HTTP/1.0 404 File Not Found";
  static final String NOT_FOUND_HTML =
      "<HTML><HEAD><TITLE>File Not Found</TITLE></HEAD><BODY><H1>HTTP Error 404: File Not Found</H1></BODY></HTML>";
  static final String NOT_IMPL = "HTTP/1.0 501 Not Implemented";
  static final String NOT_IMPL_HTML =
      "<HTML><HEAD><TITLE>Not Implemented</TITLE></HEAD><BODY><H1>HTTP Error 501: Not Implemented</H1></BODY></HTML>";
  static final String INVALIDEMAILADDRESS_HTML = "<HTML><HEAD><TITLE>InVALID EMAIL ADDRESS</TITLE></HEAD><BODY><H1>NOT EMAIL ADDRESSES. PLEASE CHECK.</H1></BODY></HTML>";
  static final String SMTPSERVERCONNECTIONFAILER_HTML =
      "<HTML><HEAD><TITLE>SERVER FAILED</TITLE></HEAD><BODY><H1>SMTP SERVER FAILED</H1></BODY></HTML>";
  static final String EMAILADDRESSINVALIDATION_S_HTML =
      "<HTML><HEAD><TITLE>INVALID EMAIL ADDRESS</TITLE></HEAD><BODY><H1>INVALID EMAIL ADDRESS, REJECTED BY THE MAIL SERVER.</H1></BODY></HTML>";
  static final String EMAILADDRESSINVALIDATION_R_HTML =
      "<HTML><HEAD><TITLE>INVALID EMAIL ADDRESS</TITLE></HEAD><BODY><H1>INVALID EMAIL ADDRESS, REJECTED BY THE MAIL SERVER</H1></BODY></HTML>";
  static final String DATAFAILED_HTML =
      "<HTML><HEAD><TITLE>FAILED TO SEND EMAIL</TITLE></HEAD><BODY><H1>SORRY FAILED TO SEND EMAIL. PLEASE TRY AGAIN.</H1></BODY></HTML>";
  static final String SEND_HTML =
      "<HTML><HEAD><TITLE>EMAIL SEND</TITLE></HEAD><BODY><H1>SEND SUCCESSFULLY.</H1></BODY></HTML>";
  static final String IVALIDDELAYTIME_HTML =
      "<HTML><HEAD><TITLE>IVALIDDELAYTIME</TITLE></HEAD><BODY><H1>INVALID DELAY TIME.</H1></BODY></HTML>";
  static final String CANNOTFINDSMTPSERVER_HTML = "<HTML><HEAD><TITLE>SMTP SERVER WRONG</TITLE></HEAD><BODY><H1>CAN NOT FIND MAIL SERVER FOR THE INPUT EMAIL ADDRESS.</H1></BODY></HTML>";
  static final String WAITINGLIST_HTML =
      "<HTML><HEAD><TITLE>WAITINGLIST</TITLE></HEAD><BODY><H1>YOUR MAIL IS IN THE WAITING LIST. THANK YOU.<br><form action=\"statuspage.html\" method=\"get\"><input type=\"submit\" value=\"Status Page\"/></form></H1></BODY></HTML>";
  private Socket s;
  private String base;
  private List<String> record;
  Handler (Socket s, String base,List<String> record) {
    this.s = s; this.base = base;this.record=record;
  }
  public void run() {
    try {
      BufferedReader r = new BufferedReader(
          new InputStreamReader(s.getInputStream()));
      String str;
      if ((str = r.readLine()) == null){
        s.close(); return;
      };
      OutputStream os = s.getOutputStream(); // get output stream of the socket
      //System.out.println(str);   // for log
      StringTokenizer st = new StringTokenizer(str);
      String method = st.nextToken();
      String name = st.nextToken();
      String version = st.nextToken();
      /*
      while ((str = r.readLine()) != null && !str.trim().equals("")) {
        System.out.println(str); // for log
      } // away empty lines */
      //if (str != null) System.out.println(str); // for log
     
      // Here is the GET request processed
      if (method.equals("GET")) { //System.out.println("method get");
        if (name.endsWith("/")) {name += "index.html";}
        if (name.startsWith("/index")){
        try {
          File file = new File(base, name.substring(1,name.length()));
          //System.out.println("new file success");
          FileInputStream fis = new FileInputStream(file);
	  //System.out.println("new fileinputstream success");
          byte buf[] = new byte[(int)file.length()];
	  //System.out.println("new buff success");
          fis.read(buf);
          //System.out.println("read buff success");
          fis.close();
          if (version.startsWith("HTTP/")) {//System.out.println("stars with HTTP/!");
            PrintWriter pw = new PrintWriter(os);
            // Printing a head for response
            pw.println(OK);
            pw.println("Date:" + (new Date()));
            pw.println(SERVER);
            pw.println("Content-length: " + buf.length);
            pw.println("Content-type: " + ContentTypeFrom(name));
            pw.println(); pw.flush();
          }
          os.write(buf); os.flush();os.close();
        } catch (IOException e) {
        sendHTMLMessage(os, NOT_FOUND, NOT_FOUND_HTML, version);}
      }}
      if (name.startsWith("/status")){String recordhtmlnow=recordhtml();sendHTMLMessage(os,OK,recordhtmlnow, version);
      }
      // Here is the POST request processed
      else if (method.equals("POST")) {             //Http Post Request
	String query;
        while ((query = r.readLine()) != null)
	{
   	  if (query.startsWith("Content-Length: "))
   	  {
      	     break;
   	  }
	}
	int contentLength = Integer.parseInt(query.substring("Content-Length: ".length()));
	StringBuilder requestContent = new StringBuilder();
	int ch=0;
	for (int i = 0; i < contentLength+2; i++)
	{ch =r.read();
	if (i<2) continue;
   	requestContent.append((char) ch);
	}
	String content=requestContent.toString();
	content=content.replaceAll("&"," ");
	//Getting Email Addresses and other information
	StringTokenizer stcontent = new StringTokenizer(content);
        String SenderAddress = stcontent.nextToken();
	String ReceiverAddress = stcontent.nextToken();
	String Subject = stcontent.nextToken();
	String SMTPServer = stcontent.nextToken();
	String MailContent = stcontent.nextToken();
        String DelayTime = stcontent.nextToken();
	//Parsing Post Content	
	SenderAddress = SenderAddress.substring(14,SenderAddress.length());
	ReceiverAddress = ReceiverAddress.substring(16,ReceiverAddress.length());
	Subject = Subject.substring(8,Subject.length());
	SMTPServer = SMTPServer.substring(11,SMTPServer.length());
        MailContent = MailContent.substring(12,MailContent.length());
	DelayTime = DelayTime.substring(10,DelayTime.length());
        //System.out.println("Mailcontent before Q encode: "+MailContent);
        //URLDecoding Strings
        SenderAddress=URLDecoder.decode(SenderAddress,"UTF-8");
	ReceiverAddress = URLDecoder.decode(ReceiverAddress,"UTF-8");
	SMTPServer=URLDecoder.decode(SMTPServer,"UTF-8");
        DelayTime=URLDecoder.decode(DelayTime,"UTF-8");

        /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

        String subjectonstatuspage=StatusPage_Subject(Subject);

        subjectonstatuspage=URLDecoder.decode(subjectonstatuspage,"UTF-8");
        //String Subject_SP=URLDecoder.decode(Subject,"iso-8859-15");
        

        /*~~~~~~~~~~~~~~~~~~~~~~~```*/
	Subject = MIMEforSwedish_Subject(Subject);				//Quoted-Printable encoding for Subject
	MailContent = MIMEforSwedish(MailContent);			//Quoted-Printable encoding for MailContent
	//System.out.println("Mailcontent after MIME before urldecoded: "+MailContent);
        Subject = URLDecoder.decode(Subject,"iso-8859-15");
        


	MailContent = URLDecoder.decode(MailContent,"iso-8859-15");
	//System.out.println("Mailcontent after urldecoded: "+MailContent);
        //Check out the Delay Time Validation
        boolean vdelaytime=true;
        long DelayTimeINT= 0;
        if (DelayTime.length()==0){DelayTime="0";}
        try{
        DelayTimeINT= (long)Integer.parseInt(DelayTime)*1000;
        }catch(NumberFormatException ee){sendHTMLMessage(os,OK,IVALIDDELAYTIME_HTML, version);vdelaytime=false;}
        if (vdelaytime==true){
	//Check Email Address Validation
        boolean vsender = checkValidEmail(SenderAddress);
	boolean vreceiver = checkValidEmail(ReceiverAddress);
	if (!(vsender&&vreceiver)) {sendHTMLMessage(os, OK, INVALIDEMAILADDRESS_HTML, version);}
	else{	
	if (SMTPServer.startsWith("smtp")||SMTPServer.startsWith("mail")) {SMTPServer=SMTPDNSClient(SMTPServer);}
	else {System.out.println("Input smtp server is empty");SMTPServer=ReceiverAddress.split("@")[1];SMTPServer=SMTPDNSClient(SMTPServer);}
        
        if (SMTPServer==null&&DelayTimeINT==0){sendHTMLMessage(os,OK,CANNOTFINDSMTPSERVER_HTML, version);}
        else{
        /*smtptemp SMTPC = new smtptemp();
        int srcheck=SMTPC.SMTPCLIENT_Test(SMTPServer,SenderAddress,ReceiverAddress);
        if (srcheck==1){sendHTMLMessage(os,OK,SMTPSERVERCONNECTIONFAILER_HTML, version);}
        if (srcheck==2){sendHTMLMessage(os,OK,EMAILADDRESSINVALIDATION_S_HTML, version);}
        else if (srcheck==3){sendHTMLMessage(os,OK,EMAILADDRESSINVALIDATION_R_HTML, version);}
        else{*/
        /*Saving Records*/
        if (DelayTimeINT!=0){
        Date date = new Date();
        String SubmitTime=date.toString();
        date.setTime(date.getTime()+DelayTimeINT);
        String ReceiveTime=date.toString();
        
        
        String recordstring="Sender: "+SenderAddress+"<br>Receiver: "+ReceiverAddress+"<br>Subject: "+subjectonstatuspage+"<br>Submitted Time: "+SubmitTime+"<br>Receiving Time: "+ReceiveTime;

        boolean b1 = record.add(recordstring);
        }
        /*Set up Timer*/
        int feedback=0;
        if (DelayTimeINT==0){smtptemp SMTPC = new smtptemp();
        feedback = SMTPC.SMTPCLIENT(SMTPServer,SenderAddress,ReceiverAddress,MailContent,Subject);
        
        if (SMTPServer==null){feedback=1;}
        //System.out.println(feedback);
        if (feedback==0){sendHTMLMessage(os,OK,SEND_HTML, version);}
        if (feedback==1){sendHTMLMessage(os,OK,SMTPSERVERCONNECTIONFAILER_HTML, version);}
        if (feedback==2){sendHTMLMessage(os,OK,EMAILADDRESSINVALIDATION_S_HTML, version);}
        if (feedback==3){sendHTMLMessage(os,OK,EMAILADDRESSINVALIDATION_R_HTML, version);}
        if (feedback==4){sendHTMLMessage(os,OK,DATAFAILED_HTML, version);}
        }
        else{
        Timer timer= new Timer();
        MyTask t =new MyTask(SMTPServer,SenderAddress,ReceiverAddress,MailContent,Subject,DelayTimeINT,feedback);
        /*Sending Email*/
        timer.schedule(t,DelayTimeINT);
        feedback=t.getfeedback();
        

        String recordhtmlnow=recordhtml();
        
        sendHTMLMessage(os,OK,WAITINGLIST_HTML, version);
        //if (SMTPServer==null){feedback=1;}
        //System.out.println(feedback);
        //if (feedback==0){sendHTMLMessage(os,OK,SEND_HTML, version);}
        //if (feedback==1){sendHTMLMessage(os,OK,SMTPSERVERCONNECTIONFAILER_HTML, version);}
        /*if (feedback==2){sendHTMLMessage(os,OK,EMAILADDRESSINVALIDATION_S_HTML, version);}
        if (feedback==3){sendHTMLMessage(os,OK,EMAILADDRESSINVALIDATION_R_HTML, version);}
        if (feedback==4){sendHTMLMessage(os,OK,DATAFAILED_HTML, version);}*/
      }}}}}
      else sendHTMLMessage(os, NOT_IMPL, NOT_IMPL_HTML, version);
      s.close();
    } catch (IOException e) {
      System.err.println("OBS, "+e.toString());
    }
  }

  String recordhtml(){
  String html="<HTML><HEAD><TITLE>Status Page</TITLE></HEAD><BODY><H3>";
  for (int i=0;i<record.size();i++){
  html=html.concat(record.get(i));
  html=html.concat("<br><br>");
  }
  html=html.concat("</H3></BODY></HTML>");
  return html;
  }

  /* Gets the requested documents type from its extension*/
  String ContentTypeFrom(String name) {
    if (name.endsWith(".html") || name.endsWith(".htm")) return "text/html";
    else if (name.endsWith(".txt") || name.endsWith(".java")) return "text/plain";
    else if (name.endsWith(".gif") ) return "image/gif";
    else if (name.endsWith(".class") ) return "application/octet-stream";
    else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
    else return "text/plain";
  }

  /* Prints an error code into a given output stream */
  void sendHTMLMessage(OutputStream os, String code, String html, String version) {
    PrintWriter pw = new PrintWriter(os);
    if (version.startsWith("HTTP/")) {
      pw.println(code);
      pw.println("Date:" + (new Date()));
      pw.println(SERVER);
      pw.println("Content-type: text/html");
    pw.println();}
    pw.println(html); pw.flush();pw.close();
  }

  /* Check if it is a valid email address */
  boolean checkValidEmail (String emailaddress)
  {
     String regEx= "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
		+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
     Pattern p = Pattern.compile(regEx);
     Matcher m = p.matcher(emailaddress);
     if (m.find())
     {return true;}
     else
     {return false;}
  }
  
  /* Quoted-Printable encoding Method*/
  String MIMEforSwedish (String str)
  {  if (str.startsWith(".")){str=str.replaceFirst(".","..");}
     //Six Swedish Characters
     str=str.replaceAll("%C4","=C4");
     str=str.replaceAll("%C5","=C5");
     str=str.replaceAll("%D6","=D6");
     str=str.replaceAll("%E4","=E4");
     str=str.replaceAll("%E5","=E5");
     str=str.replaceAll("%F6","=F6");
     //"=" character
     str=str.replaceAll("%3D","=3D");
     //"." character
     //str=str.replaceAll("%0D%0A.%0D%0A","%0D%0A..%0D%0A");
     str=str.replaceAll("%0D%0A%0D%0A","%0D%0A %0D%0A");
     str=str.replaceAll("%0D%0A","%0D%0A.");
     //str=str.replaceAll(".","=2E");
     return str;
  }
  String MIMEforSwedish_Subject (String str)
  {
     //Six Swedish Characters
     
     str=str.replaceAll("%C4","=?ISO-8859-1?Q?=C4?=");
     str=str.replaceAll("%C5","=?ISO-8859-1?Q?=C5?=");
     str=str.replaceAll("%D6","=?ISO-8859-1?Q?=D6?=");
     str=str.replaceAll("%E4","=?ISO-8859-1?Q?=E4?=");
     str=str.replaceAll("%E5","=?ISO-8859-1?Q?=E5?=");
     str=str.replaceAll("%F6","=?ISO-8859-1?Q?=F6?=");
     //"=" character
     str=str.replaceAll("%3D","=?ISO-8859-1?Q?=3D?=");
     //"." character
     str=str.replaceAll("%0D%0A.%0D%0A","%0D%0A..%0D%0A");
     return str;
  }
  String StatusPage_Subject(String str)
  {
   str=str.replaceAll("%C4","%26%23196");
   str=str.replaceAll("%C5","%26%23197");
   str=str.replaceAll("%D6","%26%23214");
   str=str.replaceAll("%E4","%26%23228");
   str=str.replaceAll("%E5","%26%23229");
   str=str.replaceAll("%F6","%26%23246");
   //str=str.replaceAll("=3D","=");
   //str=str.replaceAll("%0D%0A..%0D%0A",".");
   return str;
  }


  String SMTPDNSClient(String SMTPServer)
  {
    String SMTP_IP=null;
    if (SMTPServer.startsWith("smtp")||SMTPServer.startsWith("mail")){SMTPServer=SMTPServer.substring(5,SMTPServer.length());}
	try {
            //String hostName = mailTo.split("@")[1];
            String hostName=SMTPServer;
	    String host = null;

            Lookup lookup = new Lookup(hostName, Type.MX); 
            lookup.run();
            if (lookup.getResult() != Lookup.SUCCESSFUL) {
                System.out.println("Cannot find SMTP Server"); return null;
            }
            else {
                Record[] result = lookup.getAnswers();
                host = result[0].getAdditionalName().toString(); 
                InetAddress addr = Address.getByName(host);
		SMTP_IP=addr.getHostAddress();
                
	    }
         }catch (Exception e){System.out.println("totally wrong");return null;}
    return SMTP_IP;
   }
}
class MyTask extends TimerTask {
     String SERVERIP;
     String INPUTSENDERADDR;
     String INPUTRECEIVERADDR;
     String Content;
     String Subject;
     long delay;
     int feedback;
     static int temp;
     public MyTask(String SERVERIP,String INPUTSENDERADDR,String INPUTRECEIVERADDR,String Content,String Subject,long delay,int feedback)
     {
      this.SERVERIP=SERVERIP;
      this.INPUTSENDERADDR=INPUTSENDERADDR;
      this.INPUTRECEIVERADDR=INPUTRECEIVERADDR;
      this.Content=Content;
      this.Subject=Subject;
      this.delay=delay;
      this.feedback=feedback;
     }
     public void run() {
	smtptemp SMTPC = new smtptemp();
        if (SERVERIP==null){feedback=1;SERVERIP=INPUTSENDERADDR.split("@")[1];SERVERIP=SMTPDNSClient(SERVERIP);if(SERVERIP==null){return;}}
        
        else{
        feedback = SMTPC.SMTPCLIENT(SERVERIP,INPUTSENDERADDR,INPUTRECEIVERADDR,Content,Subject);}
        temp=feedback;
        //System.out.println("inside"+feedback);
        if (delay!=0){
          String ServerAddress="smtpserver@ik2213.lab";
          String Sent_Successfully_Content="Your mail to <"+INPUTRECEIVERADDR+"> is sent successfully.";
          String Sent_Failed_Content="Your mail to <"+INPUTRECEIVERADDR+"> is not sent. Please check and try again.";
          if (feedback==0){ int fdbk1=SMTPC.SMTPCLIENT(SERVERIP,ServerAddress,INPUTSENDERADDR,Sent_Successfully_Content,"Mail Sent");}
          else {int fdbk2=SMTPC.SMTPCLIENT(SERVERIP,ServerAddress,INPUTSENDERADDR,Sent_Failed_Content,"Mail Failed");
          }




    }
    }
    public int getfeedback(){return temp;}
    String SMTPDNSClient(String SMTPServer)
  {
    String SMTP_IP=null;
    if (SMTPServer.startsWith("smtp")||SMTPServer.startsWith("mail")){SMTPServer=SMTPServer.substring(5,SMTPServer.length());}
	try {
            //String hostName = mailTo.split("@")[1];
            String hostName=SMTPServer;
	    String host = null;

            Lookup lookup = new Lookup(hostName, Type.MX); 
            lookup.run();
            if (lookup.getResult() != Lookup.SUCCESSFUL) {
                System.out.println("Cannot find SMTP Server"); return null;
            }
            else {
                Record[] result = lookup.getAnswers();
                host = result[0].getAdditionalName().toString(); 
                InetAddress addr = Address.getByName(host);
		SMTP_IP=addr.getHostAddress();
                
	    }
         }catch (Exception e){System.out.println("totally wrong");return null;}
    return SMTP_IP;
   }
}



