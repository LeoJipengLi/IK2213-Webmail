import java.net.*;
import java.io.*;
import java.util.*;
public class smtptemp {
  public int SMTPCLIENT (String SERVERIP,String INPUTSENDERADDR,String INPUTRECEIVERADDR,String Content,String Subject) {
    String host=SERVERIP;
    int port = 25;
    try {
      Socket socket = new Socket (host,port);
      PrintWriter wr = new PrintWriter(socket.getOutputStream(),true);
      BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      String str;      
      //System.out.println(INPUTSENDERADDR+" "+INPUTRECEIVERADDR);
      boolean SMTPServer_Connect = true;
      boolean Sender_Validation = true;
      boolean Receiver_Validation = true;
      boolean Data_ConnectandSent = true;
      while ((str = rd.readLine()) != null)
        {//System.out.println(str);
      StringTokenizer st1 = new StringTokenizer(str);
      String fd1 = st1.nextToken();
      if (Integer.parseInt(fd1)!=220) {SMTPServer_Connect = false;wr.println("quit");System.out.println("Failed: Mail From "+INPUTSENDERADDR+" to "+INPUTRECEIVERADDR+" is not sent.");return 1;}
      wr.println("helo mail.ik2213.lab");
      break;
      }
     
      while ((str = rd.readLine()) != null&&(SMTPServer_Connect == true)&&(Sender_Validation == true)&&(Receiver_Validation == true)&&(Data_ConnectandSent == true))
        {//System.out.println(str);
      StringTokenizer st2 = new StringTokenizer(str);      
      String fd2 = st2.nextToken();
      
      if (Integer.parseInt(fd2)!=250) {SMTPServer_Connect = false;wr.println("quit");System.out.println("Failed: Mail From "+INPUTSENDERADDR+" to "+INPUTRECEIVERADDR+" is not sent, because of server connection failer.");return 1;}
      wr.println("mail from:<"+INPUTSENDERADDR+">");
       break;
      } 

      while ((str = rd.readLine()) != null&&(SMTPServer_Connect == true)&&(Sender_Validation == true)&&(Receiver_Validation == true)&&(Data_ConnectandSent == true))
      {//System.out.println(str);
      StringTokenizer st3 = new StringTokenizer(str);      
      String fd3 = st3.nextToken();
      if (Integer.parseInt(fd3)!=250) {Sender_Validation= false;wr.println("quit");System.out.println("Failed: Mail From "+INPUTSENDERADDR+" to "+INPUTRECEIVERADDR+" is not sent, because of bad email address.");return 2;}
      wr.println("rcpt to:<"+INPUTRECEIVERADDR+">");
      break;
      }

      while ((str = rd.readLine()) != null&&(SMTPServer_Connect == true)&&(Sender_Validation == true)&&(Receiver_Validation == true)&&(Data_ConnectandSent == true))
      {//System.out.println(str);      
       StringTokenizer st4 = new StringTokenizer(str);      
       String fd4 = st4.nextToken();
      if (Integer.parseInt(fd4)!=250) {Receiver_Validation= false;wr.println("quit");System.out.println("Failed: Mail From "+INPUTSENDERADDR+" to "+INPUTRECEIVERADDR+" is not sent, because of bad email address");return 3;}
       wr.println("data");
       break;
      }

      while ((str = rd.readLine()) != null&&(SMTPServer_Connect == true)&&(Sender_Validation == true)&&(Receiver_Validation == true)&&(Data_ConnectandSent == true))
        {//System.out.println(str);
         StringTokenizer st5 = new StringTokenizer(str);      
         String fd5 = st5.nextToken();
         if (Integer.parseInt(fd5)!=354) {Data_ConnectandSent= false;wr.println("quit");System.out.println("Failed: Mail From "+INPUTSENDERADDR+" to "+INPUTRECEIVERADDR+" is not sent, because of data rejection from server");return 4;}
         wr.println("MIME-Version:1.1"+"\r"+"\n"+"Content-Type:text/plain"+"\r"+"\n"+"Content-Transfer-Encoding:Quoted-printable"+"\r"+"\n"+"FROM:"+INPUTSENDERADDR+"\r"+"\n"+"TO:"+INPUTRECEIVERADDR+"\r"+"\n"+"SUBJECT:"+Subject);
         wr.println(Content+"\r"+"\n"+".");
         break;
      }
      while ((str = rd.readLine()) != null&&(SMTPServer_Connect == true)&&(Sender_Validation == true)&&(Receiver_Validation == true)&&(Data_ConnectandSent == true))
        {//System.out.println(str);         
         StringTokenizer st6 = new StringTokenizer(str);      
         String fd6 = st6.nextToken();
         if (Integer.parseInt(fd6)!=250) {Data_ConnectandSent= false;wr.println("quit");System.out.println("Failed: Mail From "+INPUTSENDERADDR+" to "+INPUTRECEIVERADDR+" is not sent, because of data sending to server.");return 4;}
         break;
         
      }
       wr.println("quit");
       System.out.println("Success: Mail From "+INPUTSENDERADDR+" to "+INPUTRECEIVERADDR+" is sent.");  
     /* while ((str = rd.readLine()) != null)
        {System.out.println(str);
      } */
      socket.close();

     

    

    }catch(IOException e){
      System.err.println(e);
    }return 0;

  }

 public int SMTPCLIENT_Test (String SERVERIP,String INPUTSENDERADDR,String INPUTRECEIVERADDR) {
    String host=SERVERIP;
    int port = 25;
    try {
      Socket socket = new Socket (host,port);
      PrintWriter wr = new PrintWriter(socket.getOutputStream(),true);
      BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      String str;      
      
      boolean SMTPServer_Connect = true;
      boolean Sender_Validation = true;
      boolean Receiver_Validation = true;
      boolean Data_ConnectandSent = true;
      while ((str = rd.readLine()) != null)
        {//System.out.println(str);
      StringTokenizer st1 = new StringTokenizer(str);
      String fd1 = st1.nextToken();
      if (Integer.parseInt(fd1)!=220) {SMTPServer_Connect = false;wr.println("quit");return 1;}
      wr.println("helo mail.ik2213.lab");
      break;
      }
     
      while ((str = rd.readLine()) != null&&(SMTPServer_Connect == true)&&(Sender_Validation == true)&&(Receiver_Validation == true)&&(Data_ConnectandSent == true))
        {//System.out.println(str);
      StringTokenizer st2 = new StringTokenizer(str);      
      String fd2 = st2.nextToken();
      
      if (Integer.parseInt(fd2)!=250) {SMTPServer_Connect = false;wr.println("quit");return 1;}
      wr.println("mail from:<"+INPUTSENDERADDR+">");
       break;
      } 

      while ((str = rd.readLine()) != null&&(SMTPServer_Connect == true)&&(Sender_Validation == true)&&(Receiver_Validation == true)&&(Data_ConnectandSent == true))
      {//System.out.println(str);
      StringTokenizer st3 = new StringTokenizer(str);      
      String fd3 = st3.nextToken();
      if (Integer.parseInt(fd3)!=250) {Sender_Validation= false;wr.println("quit");return 2;}
      wr.println("rcpt to:<"+INPUTRECEIVERADDR+">");
      break;
      }

      while ((str = rd.readLine()) != null&&(SMTPServer_Connect == true)&&(Sender_Validation == true)&&(Receiver_Validation == true)&&(Data_ConnectandSent == true))
      {//System.out.println(str);      
       StringTokenizer st4 = new StringTokenizer(str);      
       String fd4 = st4.nextToken();
      if (Integer.parseInt(fd4)!=250) {Receiver_Validation= false;wr.println("quit");return 3;}
       wr.println("rcpt to:<"+INPUTSENDERADDR+">");
       break;
      }
      while ((str = rd.readLine()) != null&&(SMTPServer_Connect == true)&&(Sender_Validation == true)&&(Receiver_Validation == true)&&(Data_ConnectandSent == true))
      {//System.out.println(str);      
       StringTokenizer st5 = new StringTokenizer(str);      
       String fd5 = st5.nextToken();
      if (Integer.parseInt(fd5)!=250) {Receiver_Validation= false;wr.println("quit");return 2;}
       break;
      }
      wr.println("quit");
      /*while ((str = rd.readLine()) != null)
        {System.out.println(str);
      } */
      socket.close();

     

    

    }catch(IOException e){
      System.err.println(e);
    }return 0;

  }


}


