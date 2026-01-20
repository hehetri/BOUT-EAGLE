package LoginServer;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * 
 * @author Marius
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
		Main.debug("[" + owner + "]", msg);
	}

	/**
	 * Loads the configs out of "configs/mysql.conf"
	 */
	private void loadconfigs() {
		try {
			FileInputStream fin = new FileInputStream("configs/mysql.conf");
			sqldata.load(fin);
			fin.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		ip = sqldata.getProperty("MySQL_ip");
		port = sqldata.getProperty("MySQL_port");
		user = sqldata.getProperty("MySQL_id");
		pass = sqldata.getProperty("MySQL_pw");
		database = sqldata.getProperty("MySQL_db");
	}

	public void start() {
		loadconfigs();

		/**
		 * setup the basic connection
		 */
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port
					+ "/" + database + "?autoReconnect=true", user, pass);
			st = con.createStatement();
			if (!con.equals(null))
				debug("SQL Connection started successful");
			// ResultSet rs =
			// st.executeQuery("SELECT * FROM bout_users WHERE username='"+ user
			// +"' LIMIT 1");
		} catch (Exception ex) {
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
	
	public ResultSet doquery(String query) {
		ResultSet rs = null;
		try {
			/**
			 * execute the query and return it
			 */
			rs = st.executeQuery(query);
		}catch (SQLException e){
        	if(e.getSQLState()==null || e.getSQLState().startsWith("08"))
        	{
        		debug("Sql timeout arose" + e);
        		recon();
        		try{
            		//rs.close();
            		}catch(Exception ec){}
            		rs = doquery(query);
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

	public void doupdate(String query) {
		try {
			/**
			 * execute the updatequery
			 */
			st.executeUpdate(query);
		}catch (SQLException e){
        	if(e.getSQLState()==null || e.getSQLState().startsWith("08"))
        	{
        		debug("Sql timeout arose" + e);
        		recon();
        		doupdate(query);
        	}
        	else 
        		debug("Error (query): " + e.getMessage());
        }
        catch (Exception ex)
        {
            debug("Error (query): " + ex.getMessage());
        }
	}

	public void psupdate(String query, String[] value) {
		int a = 0;
		try {
			PreparedStatement stmt = null;
			// conn =
			// setupTheDatabaseConnectionSomehow();"INSERT INTO person (name, email) values (?, ?)"
			stmt = con.prepareStatement(query);
			while (a < value.length) {
				stmt.setString(a + 1, value[a]);
				a++;
			}
			stmt.executeUpdate();
			stmt.close();
		}catch (SQLException e){
        	if(e.getSQLState()==null || e.getSQLState().startsWith("08"))
        	{
        		debug("Sql timeout arose" + e);
        		recon();
        		psupdate(query, value);
        	}
        	else 
        		debug("Error (query): " + e.getMessage());
        }
        catch (Exception ex)
        {
            debug("Error (query): " + ex.getMessage());
        }
	}

	public ResultSet psquery(String query, String[] value) {
		ResultSet rs = null;
		int a = 0;
		try {
			PreparedStatement stmt = null;
			// conn =
			// setupTheDatabaseConnectionSomehow();"INSERT INTO person (name, email) values (?, ?)"
			stmt = con.prepareStatement(query);
			while (a < value.length) {
				stmt.setString(a + 1, value[a]);
				a++;
			}
			rs = stmt.executeQuery();
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
