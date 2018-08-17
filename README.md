# JavaUtil by Stefano Alvares

Instructions to run on Tomcat server:

1. Install Apache Tomcat from http://tomcat.apache.org/
2. Setup a Virtual Host to run your app from a custom location:
    2.1. Go to "C:\Windows\System32\drivers\etc".
    2.2. Open "hosts" file and add your computer IP Address with a custom domain. Save and close file.
    2.3. Go to "C:\Program Files\Apache Software Foundation\Tomcat 9.0\conf".
    2.4. In "server.xml" file search for the default host and add the following code snippet after that

            <Host name="<your-custom-domain>"  appBase="<custom-location>"
                unpackWARs="true" autoDeploy="true">

                <!-- SingleSignOn valve, share authentication between web applications
                    Documentation at: /docs/config/valve.html -->
                <!--
                <Valve className="org.apache.catalina.authenticator.SingleSignOn" />
                -->

                <!-- Access log processes all example.
                    Documentation at: /docs/config/valve.html
                    Note: The pattern used is equivalent to using pattern="common" -->
                <Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
                    prefix="localhost_access_log" suffix=".txt"
                    pattern="%h %l %u %t &quot;%r&quot; %s %b" />

            </Host>
    2.5. Restart Tomcat and your network adapter
    2.6. In browser type "<your-custom-domain>:8080".
    2.7 Go to step 5.
If you don't want to set up a virtual host then go to "C:\Program Files\Apache Software Foundation\Tomcat 9.0\webapps" directory and follow step 3.
3. Create a new folder for your application i.e. "JavaUtil".
4. From repository directory "JavaUtil/target/JavaUtil-1.0-SNAPSHOT" copy folders META-INF and WEB-INF.
5. Run servlet on browser "http://localhost:8080/JavaUtil/<servlet-name>".
    a. For generating PDF files use URL "http://localhost:8080/JavaUtil/PDFUtil"
    b.For generating Excel files use URL "http://localhost:8080/JavaUtil/ExcelUtil"
6. Only supports HTTP GET requests!
7. Java files can be found in "JavaUtil\src\main\java\" directory.
8. Class files can be found in "JavaUtil\target\JavaUtil-1.0-SNAPSHOT\WEB-INF\classes\" directory.

Libraries used:
1. iText 7
    a. iText Core
    b. layout
    c. pdftest
    d. kernel
2. Javax
3. mysql-connector

All libraries can be viewed in the "JavaUtil\pom.xml" file.
Jar files for dependencies are saved in the "JavaUtil-1.0-SNAPSHOT\WEB-INF\lib" directory.

Note: All files required to import project in Netbeans are provided, however only the ones mentioned in Point 4 are required to run on Tomcat.