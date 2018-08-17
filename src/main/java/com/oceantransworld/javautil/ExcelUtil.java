/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oceantransworld.javautil;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.POIXMLProperties;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;


/**
 *
 * @author Stefano
 */

@WebServlet(name = "ExcelUtil", urlPatterns = {"/ExcelUtil"})
public class ExcelUtil extends HttpServlet {

    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, FileNotFoundException {
        ((HttpServletResponse) response).addHeader("Access-Control-Allow-Origin", "*");
        ((HttpServletResponse) response).addHeader("Access-Control-Allow-Methods","GET, OPTIONS, HEAD, PUT, POST");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        try {
            new ExcelUtil().Excel(request, response);
        } catch (SQLException ex) {
            Logger.getLogger(ExcelUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ExcelUtil.class.getName()).log(Level.SEVERE, null, ex);
        }           
    }
    
    protected void Excel(HttpServletRequest request, HttpServletResponse response) throws SQLException, FileNotFoundException, IOException, ClassNotFoundException{
        
        ArrayList<String> headers = new ArrayList<>();
        ArrayList<String> paramNames = new ArrayList<>();
        String proc_name = "";
        ArrayList<String> proc_params = new ArrayList<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        int worksheetNo = 0;
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
        
            
        XSSFWorkbook workbook = new XSSFWorkbook();
        POIXMLProperties poixmlp = workbook.getProperties();
        POIXMLProperties.CoreProperties coreProperties = poixmlp.getCoreProperties();
        coreProperties.setCreator("Sakura");
        coreProperties.setTitle("ERP");
        coreProperties.setSubjectProperty("ERP Reports");
        coreProperties.setDescription("Report");
        coreProperties.setCategory("Ex");
        
        StringBuilder query = new StringBuilder("call "+proc_name+"(");

        for(String str: proc_params){
            query.append(str).append(",");
        }
        query.setLength(query.length()-1);
        query.append(")");
            
            Class.forName("com.mysql.jdbc.Driver"); 
        try (Connection con = DriverManager.getConnection("jdbc:mysql://192.168.5.102:3306/sak_erp_v01","root","qwerty")) {
            CallableStatement cs = con.prepareCall(query.toString());
            boolean results = cs.execute();
            while(results){
                ResultSet rs = cs.getResultSet();
                ResultSetMetaData rsmd = rs.getMetaData();
                XSSFSheet sheet = workbook.createSheet("Worksheet"+(++worksheetNo));
                Row row;
                Cell cell;
                CellStyle headerCell = workbook.createCellStyle();
                headerCell.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.index);
                headerCell.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
                headerCell.setFillPattern(FillPatternType.THICK_BACKWARD_DIAG);
                Font headfont = workbook.createFont();
                headfont.setBold(true);
                headerCell.setFont(headfont);

                int rowNum = 0;
                for(int i=0; i<headers.size(); i++){
                    sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 25));
                    row = sheet.createRow(rowNum);
                    cell = row.createCell(0);
                    rowNum += 2;
                    cell.setCellValue(headers.get(i));
                    cell.setCellStyle(headerCell);
                }

                CellStyle colHeaders = workbook.createCellStyle();
                colHeaders.setFillBackgroundColor(IndexedColors.PALE_BLUE.index);
                colHeaders.setFillForegroundColor(IndexedColors.PALE_BLUE.index);
                colHeaders.setFillPattern(FillPatternType.THICK_BACKWARD_DIAG);
                Font colfont = workbook.createFont();
                colfont.setBold(true);
                colfont.setColor(IndexedColors.RED.index);
                colHeaders.setFont(colfont);
                row = sheet.createRow(rowNum);

                for(int i = 0; i<rsmd.getColumnCount(); i++){
                    cell = row.createCell(i);
                    cell.setCellValue(rsmd.getColumnLabel(i+1));
                    cell.setCellStyle(colHeaders);
                }
                rowNum++;

                while(rs.next()){
                    row = sheet.createRow(rowNum++);
                    for(int i = 0; i<rsmd.getColumnCount();i++){
                        cell = row.createCell(i);
                        CellStyle colData = workbook.createCellStyle();
                        colData.setAlignment(HorizontalAlignment.CENTER);
                        cell.setCellValue(rs.getString(i+1));
                        cell.setCellStyle(colData);
                    }
                }
                for(int i = 0; i<rsmd.getColumnCount(); i++){
                    sheet.autoSizeColumn(i);
                }
                
                rs.close();
                results = cs.getMoreResults();
            }
            cs.close();
            workbook.write(response.getOutputStream());
            workbook.close();
        }
    }
}

//Paste on line 113
//XSSFSheet sheet = workbook.createSheet("Worksheet1");
//            
//Row row;
//Cell cell;
//CellStyle headerCell = workbook.createCellStyle();
//headerCell.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.index);
//headerCell.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
//headerCell.setFillPattern(FillPatternType.THICK_BACKWARD_DIAG);
//Font headfont = workbook.createFont();
//headfont.setBold(true);
//headerCell.setFont(headfont);
//
//int rowNum = 0;
//for(int i=0; i<headers.size(); i++){
//    sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 25));
//    row = sheet.createRow(rowNum);
//    cell = row.createCell(0);
//    rowNum += 2;
//    cell.setCellValue(headers.get(i));
//    cell.setCellStyle(headerCell);
//}
//
//CellStyle colHeaders = workbook.createCellStyle();
//colHeaders.setFillBackgroundColor(IndexedColors.PALE_BLUE.index);
//colHeaders.setFillForegroundColor(IndexedColors.PALE_BLUE.index);
//colHeaders.setFillPattern(FillPatternType.THICK_BACKWARD_DIAG);
//Font colfont = workbook.createFont();
//colfont.setBold(true);
//colfont.setColor(IndexedColors.RED.index);
//colHeaders.setFont(colfont);
//row = sheet.createRow(rowNum);
//
//for(int i = 0; i<rsmd.getColumnCount(); i++){
//    cell = row.createCell(i);
//    cell.setCellValue(rsmd.getColumnLabel(i+1));
//    cell.setCellStyle(colHeaders);
//}
//rowNum++;
//
//while(rs.next()){
//    row = sheet.createRow(rowNum++);
//    for(int i = 0; i<rsmd.getColumnCount();i++){
//        cell = row.createCell(i);
//        CellStyle colData = workbook.createCellStyle();
//        colData.setAlignment(HorizontalAlignment.CENTER);
//        cell.setCellValue(rs.getString(i+1));
//        cell.setCellStyle(colData);
//    }
//}
//for(int i = 0; i<rsmd.getColumnCount(); i++){
//    sheet.autoSizeColumn(i);
//
//}