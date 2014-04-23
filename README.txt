The uploaded file including these files:
A folder named "org", including the libraries that is needed for DNS. Note this file is downloaded from http://www.dnsjava.org/
a HTML file named index
a java source code file named Httpd.java
a java source code file named smtptemp.java
a report pdf file
a README.txt

Note the VPN configuration is not part of these Webmail Project source code.
The user client should configure the openvpn file of ik2213.lab first and should be able to ping 192.168.3.12 and should be able to set up the DNS host file, save the 192.168.3.12 address as "nameserver".

Compile and run the server:
(1) javac Httpd.java smtptemp.java
(2) sudo java Httpd "directory"
directory argument is the directory that saves these project file.
Example:
(1) javac Httpd.java smtptemp.java
(2) sudo java Httpd /home/charlie/webmail

If you find any problem, please contact:
Yuxin Cheng yuxinc@kth.se
Daniel Abad dabad@kth.se
