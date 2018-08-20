package com.oceantransworld.javautil;

import com.google.common.base.Splitter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Stefano Alvares
 * 
 */
@WebServlet(name = "JSONUtil", urlPatterns = {"/JSONUtil"})
public class JSONUtil extends HttpServlet {

@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        StringBuilder buffer = new StringBuilder();
        ArrayList<String> params = new ArrayList<>();
        StringBuilder query;
        JSONArray main_array = new JSONArray();
        ((HttpServletResponse) response).addHeader("Access-Control-Allow-Origin", "*");
        ((HttpServletResponse) response).addHeader("Access-Control-Allow-Methods","POST");
        response.setContentType("application/json");
        response.addHeader("Access-Control-Allow-Origin", "*");
        
        
        
        
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        }
        Map<String, String> map = Splitter.on( "&" ).withKeyValueSeparator( '=' ).split( buffer );
        for(String values : map.values()){
            params.add(values);
        }
        
        query = new StringBuilder("call "+params.get(0)+"(");
        params.remove(0);
        for(String str: params){
            query.append(str).append(",");
        }
        query.setLength(query.length()-1);
        query.append(")");
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://192.168.5.202:3306/sak_erp_v01","root","qwerty");
            CallableStatement cs = con.prepareCall(query.toString());
            boolean results = cs.execute();
            while(results){
                ResultSet rs = cs.getResultSet();
                ResultSetMetaData rsmd = rs.getMetaData();
                JSONArray objArray = new JSONArray();
                
                ArrayList<String> column_names = new ArrayList<>();
                for(int i = 1; i<=rsmd.getColumnCount(); i++){
                    column_names.add(rsmd.getColumnLabel(i));
                }
                
                while (rs.next()){
                    JSONObject object = new JSONObject();
                    for(int i = 1; i<=rsmd.getColumnCount(); i++){
                        String key = column_names.get(i-1);
                        String value = rs.getString(i);
                        object.put(key, value);
                    }
                    objArray.put(object);
                }
                
                main_array.put(objArray);
                rs.close();
                results = cs.getMoreResults();
            }
            cs.close();
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(JSONUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
//        ((HttpServletResponse) response).setContentType("application/json");
        
//        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            out.println(request.getParameterNames());
            out.print(main_array);
        }
    }
}
