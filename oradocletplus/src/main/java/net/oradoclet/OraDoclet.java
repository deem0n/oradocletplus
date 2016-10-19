/*
 * OraDoclet.java
 *
 * OraDoclet 0.1
 * Oracle Schema Documentation Generator
 * Copyright (C) 2004 Vladimir Katchourovski <oradoclet@narod.ru>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *   
 */
package net.oradoclet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeMap;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.RootDoc;

/**
 * The <b>OraDoclet</b> class generates a JavaDoc-like documentation 
 * for Oracle Database objects, belonging to a particular data schema. 
 *
 * The class may be called from the command line as an application. 
 *
 */
public class OraDoclet {
    /** The actual version of this OraDoclet instance */
    public static final String oraDocletVersion = "2.0";
    public static final String PROJ_GEN_STR = "Generated by <a href=\"http://www.richardnichols.net/open-source/oradocletplus/\">OraDocletPlus "+oraDocletVersion+"</a>";
    
    private static final String signature = "Database Creators";
    
    /** The reference to this instance of OraDoclet */    
    private static OraDoclet thisOraDoclet = null; 

    /** Database connection used by the methods throughout the generation cycle */
    private Connection connection = null;

    /** Database connect string, having one of the following formats:
     * <li> <b>username/password@tnsname</b> - when connecting using SQL*Net </li>
     * <li> <b>username/password@hostname:port:sid</b> - when connecting using a thin driver</li> 
     */    
    private String dbconnect = null; 

    /** Constant for DEBUG logging message type */
    public static final int    DEBUG     = 0;

    /** Constant for INFORMATION logging message type */
    public static final int    INFO      = 1;
    
    /** Constant for WARNING logging message type */    
    public static final int    WARNING   = 2;
    
    /** Constant for ERROR logging message type, is used as default */    
    public static final int    ERROR     = 3;

    /** Name for the column in the result set, that contains a parent object name */ 
    public static final String COL_PARENT_NAME = "parent_name";

    /** Name for the column in the result set, that contains some code. The formatting is kept while output. */ 
    public static final String COL_CODE = "code";

    /** Dictionary type for the stored function */ 
    public static final String DBOBJ_TYPE_FUNCTION = "FUNCTION";

    /** The command line option that specifies database connection parameters */
    private static final String OPTION_DBCONNECT = "-dbconnect";

    /** The command line option that specifies the destination directory for output files */
    private static final String OPTION_DESTDIR = "-d";

    /** The command line option that specifies the copyright notice text
     * appearing on the bottom of each documentation page 
     */
    private static final String OPTION_COPYRIGHT = "-copyright";

    /** Doclet configuration parameters */
    private static Configuration configuration = null;

    /*
     * This code is executed once upon class loading 
     */
    static {
        configuration = new Configuration();
        configuration.copyrightLabel = new String(signature);        
    }

    /**
     * The OraDoclet constructor requires database connection parameters 
     * 
     * @param connectString Connect parameters having one of the following formats:
     * <li> <b>username/password@tnsname</b> - when connecting using SQL*Net </li>
     * <li> <b>username/password@hostname:port:sid</b> - when connecting using a thin driver</li> 
     */
    public OraDoclet(String connectString) {
        this.dbconnect = connectString;
    }

    /**
     * The OraDoclet constructor requires database connection parameters 
     * 
     * @param parameters Command line parameters
     */
    public OraDoclet(String[] parameters) {
        if(null != parameters) {
            // Assign the parameters depending on their presence.
            // The parameter order is predefined.
            switch(parameters.length) {
                // schems:
                case 4: configuration.schemas.addAll(Arrays.asList(parameters[3].split(",")));
                // copyright notice:                        
                case 3: configuration.copyrightLabel = parameters[2];
                        // no break, fall through here
                // destination directory:        
                case 2: configuration.destdirname = parameters[1];
                        // no break, fall through here
                // connect string:
                case 1: this.dbconnect = parameters[0];
                        // The title equals to the schema name
                        // configuration.applicationTitle = dbconnect.substring(0, dbconnect.indexOf('/')).toUpperCase();
                        break;
                default:
                        // Do nothing                                        
            }
        }
    }

    /** 
     * The main entry point when calling the OraDoclet in the command line 
     * The first command line argument is a connect string having one of the following formats:
     * <li> <b>username/password@tnsname</b> - when connecting using SQL*Net </li>
     * <li> <b>username/password@hostname:port:sid</b> - when connecting using a thin driver</li> 
     */
    public static void main(String[] args) {
        try {
            if(args.length > 0 && null != args[0]) {
                thisOraDoclet = new OraDoclet(args);
                thisOraDoclet.run();
                thisOraDoclet = null;             
            } else {
                System.out.println("OraDoclet version " + oraDocletVersion);
                System.out.println("");                                
                System.out.println("Usage:    java -jar OraDocletPlus-"+oraDocletVersion+".jar <dbconnect> [<output_directory> [<copyright_notice>]] ");
                System.out.println("");                
                System.out.println("Where the <dbconnect> has one of the following formats:");
                System.out.println("");                
                System.out.println("    username/password@tnsname              - when connecting using SQL*Net");                
                System.out.println("    username/password@hostname:port:sid    - when connecting using a thin driver");                
            }
        } catch(Exception ex) {
            ex.printStackTrace();                
        }
    }


    /**
     * Returns the doclet configuration
     * @return configuration The doclet configuration parameters, defined by the command line options
     */
    public static Configuration getConfiguration() {
        return configuration;
    }


    String targetdir;

    /**
     * The main routine of the OraDoclet application
     */
    public void run() throws IOException {
        System.out.println("OraDoclet version " + oraDocletVersion);
        targetdir = configuration.destdirname;

        HtmlWriter writer = new HtmlWriter(OraDoclet.getConfiguration(), targetdir, "index.html", "UTF-8");
        writer.html();
        writer.head();
        writer.title("");
        writer.link("rel='stylesheet' type='text/css' href='style.css'");
        writer.headEnd();
        writer.body(true);

        for(String schema : configuration.schemas) {
            writer.println("<h1><a href=\"" + schema + "/index.html\">" + schema + "</a></h1>");
            writer.br();
        }
        writer.hr(1, "noshade");
        writer.println("<small>"+OraDoclet.PROJ_GEN_STR+", "
                + " Copyright &copy; " + configuration.copyrightLabel + "</small>");
        writer.bodyEnd();
        writer.htmlEnd();
        writer.close();
        writer = null;

        FileOutputStream fos = new FileOutputStream(new File(targetdir+"/style.css"));
        InputStream is = getClass().getResourceAsStream("/net/oradoclet/style.css");
        int r;
        while ((r = is.read()) != -1) {
            fos.write(r);
        }
        is.close();
        fos.close();

        connection = getDBConnection();

        for(String schema : configuration.schemas){
            System.out.println("Generate for schema " + schema);
            configuration.destdirname = targetdir + '/' + schema;
            Files.createDirectories(new File(configuration.destdirname).toPath());

            try {
                generate(); // Main routine that generates the documentation files
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        // Free the connection resource
        if(null != connection) {
            try {
                connection.close();
            } catch(SQLException sqlx) {
                // Do nothing
            }
        }
        connection = null;
    }

    
    /**
     * The entry point when calling as a doclet 
     * (like <b>javadoc -doclet net.oradoclet.OraDoclet _sourcepath_\*.java</b> ) 
     * <p>
     * Note: The method is expected by <b>javadoc</b> tool. 
     * 
     * @param root
     * @return
     */
    public static boolean start(RootDoc root) {
        try {
            // Read options and assign them to the configuration
            readConfiguration(root, configuration);
            thisOraDoclet = new OraDoclet(readOptions(root.options(), OPTION_DBCONNECT));
            thisOraDoclet.run();
            thisOraDoclet = null;             
        } catch(Exception ex) {
            ex.printStackTrace();                
        }
        return true;
    }


    /**
     * Returns the number of separate pieces or tokens in the option 
     * for each custom option that this doclet recognizes:
     * <p>  
     * <li><b>-dbconnect</b> database connect string of the following format:</li>
     * <p>
     * <li> <b>username/password@tnsname</b> - when connecting using SQL*Net </li>
     * <li> <b>username/password@hostname:port:sid</b> - when connecting using a thin driver</li> 
     * <p>
     * Note: The method is expected by <b>javadoc</b> tool. 
     * 
     * @param option The option to be checked
     * @return The number of possible tokens, including the option itself. If the option is not supported, 0 is returned.
     */
    public static int optionLength(String option) {
        if (option.equals(OPTION_DBCONNECT)) {
            return 2;
        }
        if (option.equals(OPTION_DESTDIR)) {
            return 2;
        }
        if (option.equals(OPTION_COPYRIGHT)) {
            return 2;
        }
        return 0;
    }


    /**
     * The method is an optional method that can be used to test the validity 
     * of the usage of command-line options. It is automatically invoked by the <b>javadoc</b>; 
     * The method prints appropriate error messages from validOptions when 
     * improper usages of command-line options are found.
     * <p>
     * Note: The method is used by <b>javadoc</b> tool. 
     * 
     * @param options A two-dimensional array of options, received in the command line
     * @param reporter 
     * @return True if the option usage is valid, and false otherwise.
     */
    public static boolean validOptions(String options[][], DocErrorReporter reporter) {
        boolean foundTagOption = false;

        for(int i = 0; i < options.length; i++) {
            String[] opt = options[i];
            if(opt[0].equals(OPTION_DBCONNECT)) {
                // This rule checks if the option was specified repeatedly, or the format was wrong:
                if(foundTagOption  
                   | opt[1].indexOf('@') < 1  
                   | opt[1].indexOf('/') < 1
                ) {
                    reporter.printError("Invalid option (-dbconnect).");
                    reporter.printError(" The <dbconnect> option has one of the following formats:");
                    reporter.printError("        username/password@tnsname              - when connecting using SQL*Net");                
                    reporter.printError("        username/password@hostname:port:sid    - when connecting using a thin driver");                

                    return false;
                } else {
                    foundTagOption = true;
                }
            }
        }
        if(!foundTagOption) {
            reporter.printError("Usage: javadoc -doclet net.oradoclet.OraDoclet -docletpath <docletpath> -dbconnect <dbconnect> oradoclet [options]");
            reporter.printError("");            
            reporter.printError("Where ");
            reporter.printError("    - <dbconnect> has one of the following formats:");
            reporter.printError("        username/password@tnsname              - when connecting using SQL*Net");                
            reporter.printError("        username/password@hostname:port:sid    - when connecting using a thin driver");
            reporter.printError("    - <docletpath> must include the path to the Oracle JDBC driver, such as:");
            reporter.printError("        .;<classpath to oracle.jdbc.driver.OracleDriver>");                            
            reporter.printError("    - the last token oradoclet has no meaning and only fits the expectation of javadoc");            
            reporter.printError("    - [options] are further javadoc options");            
        }
        return foundTagOption;
    }


    /**
     * Extracts the value of the optionName option, passed when calling as a javadoc doclet
     * 
     * @param options A two-dimensional array of options, received in the command line
     * @param optionName The name of the option, which value is requested 
     * @return The value of the parameter, passed with the specified doclet option
     */
    private static String readOptions(String[][] options, String optionName) {
        String tagName = null;
        for (int i = 0; i < options.length; i++) {
            String[] opt = options[i];
            if (opt[0].equals(optionName)) {
                tagName = opt[1];
            }
        }
        return tagName;
    }

    /**
     * 
     * @param root doclet API data structure holding the javadoc options
     * @param config OraDoclet configuration settings structure
     */
    protected static void readConfiguration(RootDoc root, Configuration config) {
        // The title is a schema name
        String dbconnect = readOptions(root.options(), OPTION_DBCONNECT);
        config.applicationTitle = dbconnect.substring(0, dbconnect.indexOf('/')).toUpperCase();
        // The destination directory for output files 
        config.destdirname = readOptions(root.options(), OPTION_DESTDIR);
        // The copyright notice text
        config.copyrightLabel = readOptions(root.options(), OPTION_COPYRIGHT);        
    }

    /**
     * Obtains a database connection or returns an allocated one.
     *
     * @return dbConnection - an active database connection
     */
    public Connection getDBConnection() {
        if(null == this.connection) {
            this.connection = getDBConnection(this.dbconnect);
        }
        return this.connection;
    }

    /**
     * Obtains a database connection
     *
     * @return dbConnection - an active database connection
     */
    public Connection getDBConnection(String dbconnect) {
        Connection dbConnection = null;
        String     dbUser       = null;        
        String     dbPassword   = null;
        String     dbConnectStr = null;        
        String     dbProtocol   = null;

        // Leave if wrong connect string specified
        if(null == dbconnect 
            || dbconnect.indexOf('@') < 1 
            || dbconnect.indexOf('/') < 1) {
            writeLog("Error: Wrong or no connect string is specified.", ERROR, "getDBConnection()", null);
            return null;
        }
        
        // Analyse the connect string, parse the parameters and detect the connecting method
        dbUser       = dbconnect.substring(0, dbconnect.indexOf('/'));
        dbPassword   = dbconnect.substring(dbconnect.indexOf('/') + 1, dbconnect.indexOf('@'));
        dbConnectStr = dbconnect.substring(dbconnect.indexOf('@') + 1);
        if(dbConnectStr.indexOf(':') > 0) {
            dbProtocol = "thin";
        } else {
            dbProtocol = "oci8";            
        }
        
        // Get a connection
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");

            // Connect using either an OCI-driver and a tnsname (SQL*Net must be installed) 
            // or using a thin driver 
            dbConnection = DriverManager.getConnection
                 ("jdbc:oracle:" + dbProtocol + ":@" + dbConnectStr, dbUser, dbPassword);

        } catch(SQLException ex) {
            writeLog("SQL error: " + ex.getMessage(), ERROR, "getDBConnection()", ex);
            return null;
        } catch(ClassNotFoundException ex) {
            writeLog("Class not found: " + ex.getMessage() 
                + "\r\n         System classpath: " + System.getProperty("java.class.path"), ERROR, "getDBConnection()", ex);
            return null;
        }
        return dbConnection;
    }

    //TODO Think over how to make the method non-static
    /**
     * Writes a message or warning into particular log destination (System.out by default)
     *
     * @param message - A message to be written in the log
     */
    protected static void writeLog(String message, int level, String method, Throwable t) {
        String messageType = null;

        switch(level) {
            case 0:
                messageType = "DEBUG  ";
                break;
            case 1:
                messageType = "INFO   ";            
                break;
            case 2:
                messageType = "WARNING";            
                break;
            case 3:
            default:
                messageType = "ERROR  ";            
                break;
        }
        // If no message available, print the exception class name at least
        if(null == message) {
            message = t.getClass().getName();
        }
        System.out.println(messageType + ": " + message);        
    }
    
    /**
     * Starts the generation of files. Calls generate methods of the individual
     * writers, which will in turn generate the documentation files. At first the 
     * object hierarchy is built, which is used while generating the files.
     */
    protected void generate() throws Exception {
        OraDictionary oraDict = new OraDictionary(getDBConnection());
        TreeMap objectTree = oraDict.buildObjectTree();

        // Test output of the object tree
        Iterator it = objectTree.values().iterator();
        while(it.hasNext()) {
            DatabaseObject dbobj = (DatabaseObject) it.next(); 
        }

        // Begin the file generation
        // Create a documentation index file and other related files
        DocFilesetWriter docFilesetWriter = new DocFilesetWriter(getDBConnection(), objectTree);
        docFilesetWriter.generate(); 
        docFilesetWriter = null;    

        // Tables
        DatabaseObject dbobject = null;
        it = objectTree.values().iterator();
        while(it.hasNext()) {
            dbobject = (DatabaseObject)it.next();
            if(dbobject.getObjectType().equalsIgnoreCase("TABLE")) {
                ObjectWriter tableWriter = new ObjectWriter(getDBConnection(), objectTree, dbobject, null);
                tableWriter.generate(); 
                tableWriter.close(); // Important, otherwise the writing efforts get lost               
                tableWriter = null;    
            }
            if(dbobject.getObjectType().equalsIgnoreCase("VIEW")) {
                ObjectWriter viewWriter = new ViewWriter(getDBConnection(), objectTree, dbobject);
                viewWriter.generate(); 
                viewWriter.close(); // Important, otherwise the writing efforts get lost               
                viewWriter = null;    
            }
            if(dbobject.getObjectType().equalsIgnoreCase("PROCEDURE")
                || dbobject.getObjectType().equalsIgnoreCase("FUNCTION")) {
                ObjectWriter procedureWriter = new ProcedureWriter(getDBConnection(), objectTree, dbobject);
                procedureWriter.generate(); 
                procedureWriter.close(); // Important, otherwise the writing efforts get lost               
                procedureWriter = null;    
            }
            if(dbobject.getObjectType().equalsIgnoreCase("PACKAGE")) {
                ObjectWriter packageWriter = new PackageWriter(getDBConnection(), objectTree, dbobject);
                packageWriter.generate(); 
                packageWriter.close(); // Important, otherwise the writing efforts get lost               
                packageWriter = null;    
            }
            
            dbobject = null;
        }
        it = null;
        objectTree = null;
        oraDict = null;
    }
}
