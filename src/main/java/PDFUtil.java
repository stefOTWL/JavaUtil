import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Stefano Alvares
 */

@WebServlet(urlPatterns = {"/PDFUtil"})
public class PDFUtil extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getQueryString();
        ((HttpServletResponse) response).addHeader("Access-Control-Allow-Origin", "*");
        ((HttpServletResponse) response).addHeader("Access-Control-Allow-Methods","GET, OPTIONS, HEAD, PUT, POST");
        response.setContentType("application/pdf");
        try {
            new PDFUtil().manipulatePdf(request, response);
        } catch (Exception ex) {
            Logger.getLogger(PDFUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void manipulatePdf(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        ArrayList<String> headers = new ArrayList<>();
        ArrayList<String> paramNames = new ArrayList<>();
        String proc_name = "";
        ArrayList<String> proc_params = new ArrayList<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while(parameterNames.hasMoreElements()){
            paramNames.add(parameterNames.nextElement());
            
        }
        
        for(String str : paramNames){
            if(str.matches("(hd_).*")){
                headers.add(request.getParameter(str));
            }
            if(str.matches("(pr_).*")){
                proc_name = request.getParameter(str);
            }
            if(str.matches("(pa_).*")){
                proc_params.add(request.getParameter(str));
            }
        }
        
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(response.getOutputStream()));
        try (Document doc = new Document(pdfDoc, PageSize.A3.rotate())) {
            doc.setMargins(30, 0, 50, 30);
            PageXofY event = new PageXofY(pdfDoc);
            pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, event);
            PageDate event2 = new PageDate(pdfDoc);
            pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, event2);
            StringBuilder query = new StringBuilder("call "+proc_name+"(");

            for(String str: proc_params){
                query.append(str).append(",");
            }
            query.setLength(query.length()-1);
            query.append(")");
            Class.forName("com.mysql.jdbc.Driver");  
            Table table;
            try (Connection con = DriverManager.getConnection("jdbc:mysql://192.168.5.102:3306/sak_erp_v01","root","qwerty")) {
                Statement stmt=con.createStatement();
                ResultSet rs=stmt.executeQuery(query.toString());
                ResultSetMetaData rsmd = rs.getMetaData();
                
                table = new Table(rsmd.getColumnCount());
                table.setWidth(1120);
                
                //Set Table Headers
                for(int i=0; i<headers.size(); i++){
                    Cell cellH = new Cell(1,rsmd.getColumnCount()).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER);
                    cellH.add(new Paragraph(headers.get(i)));
                    table.addHeaderCell(cellH);
                }
                
                //Set Column Headers
                for(int i = 1; i<=rsmd.getColumnCount(); i++){
                    Cell colhead = new Cell().add(new Paragraph(rsmd.getColumnLabel(i)));
                    table.addHeaderCell(colhead).setTextAlignment(TextAlignment.CENTER);
                }
                
                while(rs.next()){
                    for(int i=1; i<=rsmd.getColumnCount(); i++){
                        table.addCell(String.valueOf(rs.getString(i))).setTextAlignment(TextAlignment.LEFT);
                    }
                }
                
            }
            doc.add(table);
            event.writeTotal(pdfDoc);
            doc.close();
            
        }
    }
    
    protected class PageXofY implements IEventHandler {
 
        protected PdfFormXObject placeholder;
        protected float side = 20;
        protected float x = 65;
        protected float y = 25;
        protected float space = 4.5f;
        protected float descent = 3;
 
        public PageXofY(PdfDocument pdf) {
            placeholder = new PdfFormXObject(new Rectangle(0, 0, side, side));
        }
 
        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdf = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            int pageNumber = pdf.getPageNumber(page);
            Rectangle pageSize = page.getPageSize();
            PdfCanvas pdfCanvas = new PdfCanvas(
                page.newContentStreamBefore(), page.getResources(), pdf);
            Canvas canvas = new Canvas(pdfCanvas, pdf, pageSize);
            Paragraph p = new Paragraph()
                .add("Page ").add(String.valueOf(pageNumber)).add(" of").setFontSize(8);
            canvas.showTextAligned(p, x, y, TextAlignment.RIGHT);
            pdfCanvas.addXObject(placeholder, x + space, y - descent);
            pdfCanvas.release();
        }
 
        public void writeTotal(PdfDocument pdf) {
            Canvas canvas = new Canvas(placeholder, pdf);
            Paragraph p = new Paragraph(String.valueOf(pdf.getNumberOfPages())).setFontSize(8);
            canvas.showTextAligned(p,
                0, descent, TextAlignment.LEFT);
        }
    }
    
    protected class PageDate implements IEventHandler {
 
        protected PdfFormXObject placeholder;
        protected float side = 20;
        protected float x = 1150;
        protected float y = 25;
        protected float space = 4.5f;
        protected float descent = 3;
 
        public PageDate(PdfDocument pdf) {
            placeholder = new PdfFormXObject(new Rectangle(0, 0, side, side));
        }
 
        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdf = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            int pageNumber = pdf.getPageNumber(page);
            Rectangle pageSize = page.getPageSize();
            PdfCanvas pdfCanvas = new PdfCanvas(
                page.newContentStreamBefore(), page.getResources(), pdf);
            Canvas canvas = new Canvas(pdfCanvas, pdf, pageSize);
            Date date = new Date();
            Paragraph p = new Paragraph()
                .add("Run Date "+date).setFontSize(8);
            canvas.showTextAligned(p, x, y, TextAlignment.RIGHT);
            pdfCanvas.addXObject(placeholder, x + space, y - descent);
            pdfCanvas.release();
        }
    }
}
