package botsserver;

import java.io.*;
import java.util.*;
import java.sql.*;

/**
 *
 * @author Bouteagle
 */
public class SQLDatabase {

    protected String owner;
    protected Properties sqldata = new Properties();
    protected Connection con;
    protected Statement st;
    protected String ip, port, user, pass, database;


    public SQLDatabase(String createdby) {
        this.owner = createdby;
    }


    protected void debug(String msg) {
        Main.debug("SQL: "+ msg);
    }

     /**
      * Loads the configs out of "configs/mysql.conf"
      */
    private void loadconfigs()
    {
        try
        {
        FileInputStream fin = new FileInputStream("configs/mysql.conf");
        sqldata.load(fin);
        fin.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        ip = sqldata.getProperty("MySQL_ip");
        port = sqldata.getProperty("MySQL_port");
        user = sqldata.getProperty("MySQL_id");
        pass = sqldata.getProperty("MySQL_pw");
        database = sqldata.getProperty("MySQL_db");
        try{
            Main.ExpRate = Double.parseDouble(sqldata.getProperty("exp_rate", "1.0"));
        }catch (Exception ex){
            Main.ExpRate = 1.0;
            debug("Invalid exp_rate in configs/mysql.conf, defaulting to 1.0");
        }
    }

    public void start()
    {
        loadconfigs();

         /**
          * setup the basic connection
          */
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://"+ip+":"+port+"/"+database+"?autoReconnect=true", user,pass);
            st = con.createStatement();
            if (!con.equals(null))
                debug("Connection started successful");
            //ResultSet rs = st.executeQuery("SELECT * FROM bout_users WHERE username='"+ user +"' LIMIT 1");
        }
        catch (Exception ex)
        {
            debug("Error : " + ex.getMessage());
        }
    }
    
    public void recon()
    {
    	try{
    	con.close();
    	start();
    	}catch (Exception ex){}
    }

    public int psupdate(String query, String[] value)
    {
		int a=0;
    	try {
    		PreparedStatement stmt = null;
    		stmt = con.prepareStatement(query);
    		while(a<value.length){
    			stmt.setString(a+1, value[a]);
    			a++;
    		}
    		stmt.executeUpdate();
    		stmt.close();
    		return 2;
    	}catch (SQLException e){
        	if(e.getSQLState()==null || e.getSQLState().startsWith("08"))
        	{
        		debug("Sql timeout arose" + e);
        		recon();
        		return psupdate(query, value);
        	}
        	else 
        		debug("Error (query): " + e.getMessage());
        }
        catch (Exception ex)
        {
            debug("Error (query): " + ex.getMessage());
        }
    	return 1;
    }
    
    public ResultSet psquery(String query, String[] value)
    {
		ResultSet rs = null;
		int a=0;
    	try {
    		PreparedStatement stmt = null;
    		stmt = con.prepareStatement(query);
    		while(a<value.length){
    			stmt.setString(a+1, value[a]);
    			a++;
    		}
    		rs=stmt.executeQuery();
    		stmt.closeOnCompletion();
    	}catch (SQLException e){
        	if(e.getSQLState()==null || e.getSQLState().startsWith("08"))
        	{
        		debug("Sql timeout arose" + e);
        		recon();
        		try{
            		//rs.close();
            		}catch(Exception ec){}
            		rs = psquery(query, value);
            		return rs;
        	}
        	else 
        		debug("Error (query): " + e.getMessage());
        }
        catch (Exception ex)
        {
            debug("Error (query): " + ex.getMessage());
        }
    	return rs;
    }
}
