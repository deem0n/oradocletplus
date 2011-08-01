/*
 * DocFilesetWriter.java
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

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.*;

/**
 * Generates the documentation index file and other related files.
 */
public class DocFilesetWriter {
    private Connection     dbconnection = null;
    private TreeMap        objectTree   = null;
    private String         title        = null;
    private String         destdir      = null;
    private String         copyright    = null;
    private String         encoding     = null;        

    public DocFilesetWriter(Connection  dbconnection, TreeMap objectTree) throws IOException { 
        this.dbconnection = dbconnection;
        this.objectTree   = objectTree;
        this.title        = OraDoclet.getConfiguration().applicationTitle;
        this.destdir      = OraDoclet.getConfiguration().destdirname;
        this.copyright    = OraDoclet.getConfiguration().copyrightLabel;
        this.encoding     = OraDoclet.getConfiguration().encoding;       
    }

    /**
     * Generates a documentation fileset
     */
    public void generate()  {
        String     pageTitle    = null;
        String     objectType   = null;
        String     objectPlural = null;        
        HtmlWriter writer       = null;
        
        String[][] supportedTypes = OraDictionary.getSupportedObjectTypes();        
        
        try {
            writeCSS(destdir);
            // Generate the index.html
            writer = new HtmlWriter(OraDoclet.getConfiguration(), this.destdir, "index.html", this.encoding); 
            generateIndexFile(writer, this.title);
            writer.close();
            writer = null;

            // Generate the overview.html
            writer = new HtmlWriter(OraDoclet.getConfiguration(), this.destdir, "overview.html", this.encoding); 
            generateOverviewFile(writer, this.title, this.copyright);
            writer.close();
            writer = null;

            // Generate the nav.html
            writer = new HtmlWriter(OraDoclet.getConfiguration(), this.destdir, "nav.html", this.encoding); 
            generateNavigationFile(writer, this.title);
            writer.close();
            writer = null;
            
            //Generate type index files, one per object type
            if(null != supportedTypes) {
                for(int i=0; i< supportedTypes.length; i++) {
                    objectType   = supportedTypes[i][0];
                    objectPlural = supportedTypes[i][1];
                    pageTitle = objectPlural.toUpperCase().substring(0,1) + objectPlural.toLowerCase().substring(1);
                    writer = new HtmlWriter(OraDoclet.getConfiguration(), this.destdir, objectPlural.toLowerCase() + "-index.html", this.encoding); 
                    generateObjectIndex(writer, this.objectTree, objectType, pageTitle);
                    writer.close();
                    writer = null;
                }
            }
            
            //Generate type list files, one per object type
            if(null != supportedTypes) {
                for(int i=0; i< supportedTypes.length; i++) {
                    objectType   = supportedTypes[i][0];
                    objectPlural = supportedTypes[i][1];
                    pageTitle = objectPlural.toUpperCase().substring(0,1) + objectPlural.toLowerCase().substring(1);                    
                    writer = new HtmlWriter(OraDoclet.getConfiguration(), this.destdir, objectPlural.toLowerCase() + "-list.html", this.encoding); 
                    generateObjectList(writer, this.objectTree, objectType, pageTitle, this.title, this.dbconnection);
                    writer.close();
                    writer = null;
                }
            }
            
            //Generate an overall name index file
            writer = new HtmlWriter(OraDoclet.getConfiguration(), this.destdir, "name-index.html", this.encoding); 
            generateNameIndexFile(writer, this.objectTree, this.title);
            writer.close();
            writer = null;
        } catch(Exception ex) {
            ex.printStackTrace();    
        }
    }
                               

    /**
     * Generates the documentation index file.
     */
  public void generateIndexFile(HtmlWriter writer, String title) { 
      writer.html();
      writer.head();
      writer.title(title);
      writer.headEnd();

      writer.println("<frameset cols=\"20%,80%\">");      
      writer.println("<frameset rows=\"20%,80%\">");      
      writer.println("<frame src=\"nav.html\" name=\"GlobalNav\">");
      writer.println("<frame src=\"tables-index.html\" name=\"List\">");
      writer.println("</frameset>");
      writer.println("<frame src=\"overview.html\" name=\"Main\">");
      writer.println("</frameset>");
      writer.println("<noframes>");
      writer.println("<h2>Frame Alert</h2>");
      writer.println("<p>");
      writer.println("This document is designed to be viewed using the frames feature.");
      writer.println("If you see this message, you are using a non-frame-capable web client.");
      writer.println("<br>Link to<a HREF=\"overview.html\">Non-frame version.</a></noframes>");
      writer.htmlEnd();
    }
    
    /**
     * Generates the documentation overview file
     */
    public void generateOverviewFile(HtmlWriter writer, String title, String copyright) {
        writer.html();
        writer.head();
        writer.title(title);
        writer.link("rel='stylesheet' type='text/css' href='style.css'");
        writer.headEnd();
        writer.body(true);
        generateTopBar(writer, title);
        writer.println("<h1>" + title + "</h1>");
        writer.br();
        generateBottomBar(writer, copyright);
        writer.bodyEnd();        
        writer.htmlEnd();        
    }
    
    /**
     * Generates the documentation navigation file
     */
    public void generateNavigationFile(HtmlWriter writer, String title) {
        writer.html();
        writer.head();
        writer.title(title);
        writer.link("rel='stylesheet' type='text/css' href='style.css'");
        writer.headEnd();
        writer.body(true);      
        writer.anchorTarget("tables-index.html","List","<b>Tables</b>"); 
        writer.br();
        writer.anchorTarget("views-index.html","List","<b>Views</b>"); 
        writer.br();
        writer.anchorTarget("indexes-index.html","List","<b>Indexes</b>"); 
        writer.br();
        writer.anchorTarget("constraints-index.html","List","<b>Constraints</b>"); 
        writer.br();
        writer.anchorTarget("triggers-index.html","List","<b>Triggers</b>"); 
        writer.br();
        writer.anchorTarget("procedures-index.html","List","<b>Procedures</b>"); 
        writer.br();
        writer.anchorTarget("functions-index.html","List","<b>Functions</b>"); 
        writer.br();
        writer.anchorTarget("packages-index.html","List","<b>Packages</b>"); 
        writer.br();
        writer.anchorTarget("sequences-index.html","List","<b>Sequences</b>"); 
        writer.br();
        writer.bodyEnd();        
        writer.htmlEnd();        
    }

    /**
     * Generates the index files, one per object type.
     * The index file represents an alphabetically sorted list 
     * of all objects of the given type with the links to the
     * detailed description on each of these objects.
     */
    public void generateObjectIndex(HtmlWriter writer, TreeMap objectTree, String objectType, String title) {
        DatabaseObject dbobject = null;
        
        writer.html();
        writer.head();
        writer.title(title);
        writer.link("rel='stylesheet' type='text/css' href='style.css'");
        writer.headEnd();
        writer.body(true);        
        writer.println("<h1>" + title + "</h1>");
        writer.hr();
        
        Iterator it = objectTree.values().iterator();
        while(it.hasNext()) {
            dbobject = (DatabaseObject) it.next(); 
            if(dbobject.getObjectType().equalsIgnoreCase(objectType)) {
                writer.anchorTarget(dbobject.getLink() ,"Main"," " + dbobject.getObjectName().toUpperCase() + " ");
                writer.br();
            }
        }
        it = null;       
        writer.br();
        writer.bodyEnd();        
        writer.htmlEnd();        
    }
    
    /**
     * Generates the standard top bar for a page
     * 
     * @param writer
     * @param title
     */
    public void generateTopBar(HtmlWriter writer, String title) {
        writer.write("<div id='topbar'>");
        writer.anchor("overview.html", "Overview");
        writer.anchor("tables-list.html","Tables"); 
        writer.anchor("views-list.html","Views");
        writer.anchor("indexes-list.html","Indexes");
        writer.anchor("constraints-list.html","Constraints");
        writer.anchor("triggers-list.html","Triggers");
        writer.anchor("procedures-list.html","Procedures");
        writer.anchor("functions-list.html","Functions");
        writer.anchor("packages-list.html","Packages");
        writer.anchor("sequences-list.html","Sequences");
        writer.anchor("name-index.html","Index");
        writer.println("<h3>" + title + "</h3>");
        writer.write("</div>");
        writer.hr();
    }    
    
    /**
     * Generates the standard footprint bar for a page
     * 
     * @param writer
     * @param title
     */
    public void generateBottomBar(HtmlWriter writer, String copyright) {
        writer.hr(1, "noshade");
        writer.println("<small>"+OraDoclet.PROJ_GEN_STR+", "
        + " Copyright &copy; " + copyright + "</small>");
    }    
    
    /**
     * Generates the list files, one per object type.
     * The list file represents an alphabetically sorted list of all objects 
     * of the given type with their essential object-specific attributes and 
     * links to the detailed description.
     */
    public void generateObjectList(HtmlWriter writer, TreeMap objectTree, String objectType, String pageTitle, String appTitle, Connection conn) {
        DatabaseObject dbobject  = null;
        String query = null;        
        
        writer.html();
        writer.head();
        writer.title(title);
        writer.link("rel='stylesheet' type='text/css' href='style.css'");
        writer.headEnd();
        writer.body(true);
        generateTopBar(writer, appTitle);                
        writer.println("<h3>" + pageTitle + "</h3>");

        if(objectType.equalsIgnoreCase("TABLE")) {
            query = "SELECT utc.table_name AS \"Table\", utc.comments AS \"Description\" " 
                  + "  FROM user_tab_comments utc "
                  + " WHERE utc.table_type = 'TABLE' "       
                  + " ORDER BY utc.table_name";
        }
        if(objectType.equalsIgnoreCase("VIEW")) {
            query = "SELECT utc.table_name AS \"View\", utc.comments AS \"Description\" " 
                  + "  FROM user_tab_comments utc "
                  + " WHERE utc.table_type = 'VIEW' "       
                  + " ORDER BY utc.table_name";
        }        
        if(objectType.equalsIgnoreCase("INDEX")) {
            query = "SELECT index_name AS \"Index\", index_type AS \"Type\", table_name AS \"Table\"  " 
                  + "  FROM user_indexes "
                  + " ORDER BY index_name";
        }
        if(objectType.equalsIgnoreCase("CONSTRAINT")) {
            query = "SELECT constraint_name \"Constraint\", " 
                  + "DECODE(constraint_type,'C','Check','R','Referential','P','Primary Key','U','Unique key','Unknown') \"Type\", table_name \"Table\" "
                  //, search_condition, r_owner, r_constraint_name , delete_rule
                  + "  FROM user_constraints "
                  + " WHERE r_owner IS NULL OR r_owner = user "                  
                  + " ORDER BY constraint_name";
        }
        if(objectType.equalsIgnoreCase("TRIGGER")) {
            query = "SELECT trigger_name AS \"Trigger\", trigger_type AS \"Type\", table_name AS \"Table\"  " 
                  + "  FROM user_triggers "
                  + " ORDER BY trigger_name";
        }
        if(objectType.equalsIgnoreCase("PROCEDURE")) {
            query = "SELECT object_name AS \"Procedure\"  " 
                  + "  FROM user_objects "
                  + " WHERE object_type = 'PROCEDURE' "                  
                  + " ORDER BY object_name";
        }
        if(objectType.equalsIgnoreCase("FUNCTION")) {
            query = "SELECT object_name AS \"Function\"  " 
                  + "  FROM user_objects "
                  + " WHERE object_type = 'FUNCTION' "                  
                  + " ORDER BY object_name";
        }
        if(objectType.equalsIgnoreCase("PACKAGE")) {
            query = "SELECT object_name AS \"Package\"  " 
                  + "  FROM user_objects "
                  + " WHERE object_type = 'PACKAGE' "                  
                  + " ORDER BY object_name";
        }
        if(objectType.equalsIgnoreCase("SEQUENCE")) {
            query = "SELECT sequence_name AS \"Sequence\", min_value \"Min Value\" " 
                  + ", max_value \"Max Value\", increment_by \"Increment by\", cycle_flag \"Cycle\"," 
                  + " order_flag \"Ordered\", cache_size  \"Cache Size\" " 
                  + "  FROM user_sequences "
                  + " ORDER BY sequence_name";
        }
        
        if(null!=query && query.length() > 0) {
            generateObjectListFile(writer, objectTree, objectType, conn, query);            
        }
                        
        writer.br();
        generateBottomBar(writer, writer.configuration.copyrightLabel);
        writer.bodyEnd();        
        writer.htmlEnd();        
    }
    

    /**
     * Generates the list file for the object of a given type.
     * 
     * @param writer
     * @param objectTree
     * @param objectType
     * @param connection
     * @param query Specifies the SQL-query that delivers detailed information 
     * on this type of objects. Attention: Be sure the first column in the query 
     * is always an object name and the column name corresponds to the object type 
     */
    public void generateObjectListFile(HtmlWriter writer, TreeMap objectTree, String objectType, Connection connection, String query) {
        final String   nbsp ="&nbsp;";
        int colCount = 0;
        DatabaseObject dbobject = null;
        Statement      stmt = null;
        ResultSet      rset = null;
        String         objectName = null;
        String         objectLink = null;
        String         objectAncor= null;        
        String         key        = null;            
        String         value      = null;

        try {
            stmt = connection.createStatement();
            rset = stmt.executeQuery(query);
            if(null!=rset) {
                writer.table(1, "100%");
                // Table header
                writer.trClass("header");
                //Get the table header from the meta-information
                colCount = rset.getMetaData().getColumnCount();
                for(int i=1; i<=colCount; i++) {
                    writer.println("<th>" + rset.getMetaData().getColumnName(i) + "</th>");
                }
                writer.trEnd();
                // Table body
                while(null!=rset && rset.next()) {
                    // Be sure the first column in the query is always an object name
                    // and the column name corresponds to the object type
                    objectName = rset.getString(1); 
                    writer.tr();
                    for(int i=1; i<=colCount; i++) {
                        writer.td();
                        value = (null==rset.getString(i) ? nbsp : rset.getString(i));
                        // Obtain a link for linkable objects
                        key = DatabaseObject.genKey(rset.getMetaData().getColumnName(i), value);
                        dbobject = (DatabaseObject) objectTree.get(key);
                        if(null!=dbobject) {
                            objectLink = dbobject.getLink();
                            // The link is unnecessary if it points to the page itself, embed an ancor instead:
                            if(objectLink.toLowerCase().startsWith(writer.htmlFilename)) {
                                objectAncor = "<a name=\"" + objectLink.substring(objectLink.indexOf('#') + 1) + "\"></a>"; 
                                objectLink  = null;                                
                            }
                        } else {
                            objectLink = null;
                        }
                        dbobject = null;
                        if(null!=objectLink) {
                            writer.anchorTarget(objectLink, "Main"," " + value.toUpperCase() + " ");
                        } else{
                            writer.println(value);                            
                        }
                        if(null!=objectAncor) {
                            writer.println(objectAncor);                            
                        }
                        writer.tdEnd();                        
                    }
                    writer.trEnd();
                }
                writer.tableEnd();            
            }
            rset.close();
            rset = null; 
            stmt.close();           
            stmt = null;
        } catch(SQLException sqlx) {
            sqlx.printStackTrace();            
        } catch(Exception ex) {
            ex.printStackTrace();            
        }
    }

    /**
     * Generates the name index file for all the object names of the given schema.
     * 
     * @param writer
     * @param objectTree
     */
    public void generateNameIndexFile(HtmlWriter writer, TreeMap objectTree, String appTitle) {
        final String   nbsp ="&nbsp;";
        int colCount = 0;
        String         objectName = null;
        String         objectLink = null;
        String         name       = null;   
        String         letter     = null;        
        
        class alphaComparator implements Comparator {
            public int compare(Object a, Object b) {
                if(a instanceof DatabaseObject && b instanceof DatabaseObject) {
                    return(((DatabaseObject)a).getObjectName().compareToIgnoreCase(((DatabaseObject)b).getObjectName()));             
                } else {
                    throw (new ClassCastException());            
                }   
            }
        };
        
        // Sort the tree alphabetically by the name regardless of the case  
        Vector         objectTreeSortedByName = new Vector(objectTree.values());
        Collections.sort(objectTreeSortedByName, new alphaComparator()); 

        writer.html();
        writer.head();
        writer.title(title);
        writer.link("rel='stylesheet' type='text/css' href='style.css'");
        writer.headEnd();
        writer.body(true);
        generateTopBar(writer, appTitle);                

        // Alphabetical navigation bar
        writer.table();
        writer.tr();
        letter = "";
        writer.tdBgcolorStyle("");
        for(int i=0;i<objectTreeSortedByName.size();i++) {
            name = ((DatabaseObject)objectTreeSortedByName.elementAt(i)).getObjectName();            
            if(null!=name && null!=letter && !name.substring(0,1).equalsIgnoreCase(letter)) {
                letter = name.substring(0,1).toUpperCase();
                writer.anchor("#" + letter , letter);
                writer.space();
            }
        }
        writer.tdEnd();        
        writer.trEnd();
        writer.tableEnd();
        writer.hr();
        
        letter = "";
        for(int i=0;i<objectTreeSortedByName.size();i++) {
            DatabaseObject dbobject = (DatabaseObject)objectTreeSortedByName.elementAt(i); 
            name = dbobject.getObjectName(); 
            if(null!=name && null!=letter && !name.substring(0,1).equalsIgnoreCase(letter)) {
                letter = name.substring(0,1).toUpperCase();
                writer.aName(letter);                
                writer.println("<h3>" + letter + "</h3>");
            }
            // Prepare the description
            String description = nbsp + dbobject.getObjectType().toLowerCase();
            if(null!=dbobject.getParent()) {
                description += " of " + dbobject.getParent().getObjectType().toLowerCase() 
                    + " " + dbobject.getParent().getObjectName().toUpperCase();   
            }
           
            // Obtain a link for linkable objects
            objectLink = dbobject.getLink();
            if(null!=objectLink) {
                writer.anchorTarget(objectLink, "Main"," " + name.toUpperCase());
                writer.println(description);                            
            } else{
                writer.println(name.toUpperCase() + description);                            
            }
            writer.br();            
        }
        
        writer.br();
        generateBottomBar(writer, writer.configuration.copyrightLabel);
        writer.bodyEnd();        
        writer.htmlEnd();        
    }

    private void writeCSS(String destdir) throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(destdir+"/style.css"));
        InputStream is = getClass().getResourceAsStream("/net/oradoclet/style.css");
        int r;
        while ((r = is.read()) != -1) {
            fos.write(r);
        }
        is.close();
        fos.close();
    }
}


