# JavaUtil

Instructions to run on Tomcat server:

1. Install Apache Tomcat from http://tomcat.apache.org/
2. Once configured, go to "C:\Program Files\Apache Software Foundation\Tomcat 9.0\webapps" directory.
3. Create a new folder for your application i.e. "JavaUtil".
4. From repository directory "JavaUtil/target/JavaUtil-1.0-SNAPSHOT" copy folders META-INF and WEB-INF.
5. Run servlet on browser "http://localhost:8080/JavaUtil/<servlet-name>".
    a. For generating PDF files use URL "http://localhost:8080/JavaUtil/PDFUtil"
    b.For generating Excel files use URL "http://localhost:8080/JavaUtil/ExcelUtil"
6. Only supports HTTP GET requests!

