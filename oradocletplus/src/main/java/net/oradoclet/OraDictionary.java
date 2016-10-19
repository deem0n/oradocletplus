/*
 * OraDictionary.java
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

/**
 * The <b>OraDictionary</b> class reads the database schema object attributes
 * from Oracle dictionary views and stores this information in a tree-like 
 * data structure. 
 */
public class OraDictionary {
    /** Database object types supported by this doclet, including the plural form */
    private static final String[][] supportedObjectTypes = 
        {{"TABLE", "TABLES"}, 
         {"VIEW","VIEWS"},
         {"CONSTRAINT","CONSTRAINTS"}, //Attention! The CONSTRAINT is not an Oracle object type. It is added here for uniformity      
         {"INDEX","INDEXES"},
         {"TRIGGER","TRIGGERS"},
         {"PROCEDURE","PROCEDURES"},
         {"FUNCTION","FUNCTIONS"},
         {"PACKAGE","PACKAGES"},
         {"SEQUENCE","SEQUENCES"},
         {"COLUMN","COLUMNS"},         //Attention! The COLUMN is not an Oracle object type. It is added here for uniformity         
         };

    /** Database connection used by the methods accessing the database */
    private Connection connection = null;

    /** Database object types supported by this doclet, presented as a List */
    protected static List supportedObjectTypesList = null;    

    /** Database object tree represents the object attributes hierarchically */
    protected static TreeMap objectTree = null;
    
    /** Name for the column in the result set, that contains a parent object name */ 
    public static final String COL_PARENT_NAME = "parent_name";

    /** Name for the column in the result set, that contains some code. The formatting is kept while output. */ 
    public static final String COL_CODE = "code";

    /** Dictionary type for the stored function */ 
    public static final String DBOBJ_TYPE_FUNCTION = "FUNCTION";

    /**
     * The standard constructor recieves an active connection as argument.
     * 
     */
    public OraDictionary(Connection conn) {
        this.connection = conn;
    }

    /** 
     * Returns the database object types supported by this doclet
     *  
     * @return supportedObjectTypes the database object types supported by this doclet  
     */
    public static String[][] getSupportedObjectTypes(){
        return OraDictionary.supportedObjectTypes;
    }
    
    /** 
     * Returns the database object types supported by this doclet as a List
     *  
     * @return supportedObjectTypes the database object types supported by this doclet  
     */
    public static List getSupportedObjectTypesList(){
        if(null == supportedObjectTypesList) {
            supportedObjectTypesList = new Vector();
            for(int i=0; i<supportedObjectTypes.length; i++) {
                supportedObjectTypesList.add(OraDictionary.supportedObjectTypes[i][0]);
            }
        }
        return supportedObjectTypesList;
    }

    
    /**
     * Returns the object hierarchy, which is used while generating the doc files.
     */
    public TreeMap getObjectTree() throws Exception {
        if(null == objectTree) {
            objectTree = buildObjectTree();
            if(null == objectTree) {
                throw(new Exception("Couldn't build an object tree."));
            }
        }
        return objectTree;
    }    

    
    /**
     * This method builds the object hierarchy, 
     * used later on for file generation. 
     * @return object tree
     */
    protected TreeMap buildObjectTree() {
        final String   delimiter  = ",";
        final String   quotation  = "'";        
                
        Statement      stmt       = null;
        ResultSet      rset       = null;
        TreeMap        objectTree = null;
        DatabaseObject dbobject   = null;
        DatabaseObject parent     = null;
        String         key        = null;        
        String         query      = null;
        String         supportedTypes = null;        

        if(null != OraDictionary.supportedObjectTypes && OraDictionary.supportedObjectTypes.length > 0) {
            supportedTypes = quotation + OraDictionary.supportedObjectTypes[0][0] + quotation;
            for(int i=1; i<OraDictionary.supportedObjectTypes.length; i++) {
                supportedTypes += delimiter + quotation + OraDictionary.supportedObjectTypes[i][0] + quotation;
            }
        }
        objectTree = new TreeMap();

        if(null != connection)
        try {
            // Retrieve the schema objects, pack them into tree hierarchy
            // Attention! The constraint is not an Oracle object type.  
            // It is added here for uniformity just if it were an object type
            stmt = connection.createStatement();
            String q = "SELECT object_type, object_name "
                    + "FROM user_objects "
                    + "WHERE object_type IN("
                    + supportedTypes
                    + ") "
                    + " UNION "  // The USER_OBJECTS view does not show LOB-indexes
                    + "SELECT 'INDEX' object_type, index_name object_name "
                    + "FROM user_indexes "
                    + " UNION "
                    + "SELECT 'CONSTRAINT' object_type, constraint_name object_name "
                    + "FROM user_constraints "
                    + "ORDER BY object_type, object_name ";
            System.out.println(q);
            rset = stmt.executeQuery(q);
            while(null!=rset && rset.next()) {
                dbobject = new DatabaseObject(new String(rset.getString(1)), new String(rset.getString(2)), null);
                dbobject.setLink(getObjectLink(dbobject));
                objectTree.put(dbobject.getObjectID(), dbobject);
                dbobject = null;                
            }
            rset.close();
            rset = null; 
            stmt.close();           
            stmt = null;

            // Columns are no Oracle-objects, retrieve their data separately, linking to parent tables
            stmt = connection.createStatement();
            q = "SELECT uc.column_name object_name, "
                    +       " uc.table_name  parent_name, "
                    +       " uo.object_type parent_type  "
                    +  " FROM user_tab_columns uc,"
                    +       " user_objects     uo "
                    + " WHERE uo.object_type IN ('TABLE','VIEW')"
                    + "   AND uo.object_name = uc.table_name"
                    + " ORDER BY parent_type, parent_name, column_id";
            System.out.println(q);
            rset = stmt.executeQuery(q);
            while(null!=rset && rset.next()) {
                key  = DatabaseObject.genKey(rset.getString(3), rset.getString(2));
                parent  = (DatabaseObject) objectTree.get(key);
                dbobject = new DatabaseObject("COLUMN", new String(rset.getString(1)), parent);
                dbobject.setLink(getObjectLink(dbobject));
                objectTree.put(dbobject.getObjectID(), dbobject);
                dbobject = null;                
            }
            rset.close();
            rset = null; 
            stmt.close();           
            stmt = null;

            
            // Retrieving the object attributes, detect the parent object
            for(int i=0; i<OraDictionary.supportedObjectTypes.length; i++) {
                String currentObjType = OraDictionary.supportedObjectTypes[i][0];
                if(currentObjType.equalsIgnoreCase("TABLE")) {
                    readTableAttributes(connection, objectTree);
                }
                if(currentObjType.equalsIgnoreCase("VIEW")) {
                    readViewAttributes(connection, objectTree);
                }
                if(currentObjType.equalsIgnoreCase("INDEX")) {
                    readIndexAttributes(connection, objectTree);
                }
                if(currentObjType.equalsIgnoreCase("COLUMN")) {
                    readColumnAttributes(connection, objectTree);
                }
                if(currentObjType.equalsIgnoreCase("CONSTRAINT")) {
                    readConstraintAttributes(connection, objectTree);
                }
                if(currentObjType.equalsIgnoreCase("TRIGGER")) {
                    readTriggerAttributes(connection, objectTree);
                }
                if(currentObjType.equalsIgnoreCase("FUNCTION")) {
                    readFunctionAttributes(connection, objectTree);
                }
                if(currentObjType.equalsIgnoreCase("PROCEDURE")) {
                    readProcedureAttributes(connection, objectTree);
                }
                if(currentObjType.equalsIgnoreCase("PACKAGE")) {
                    readPackageAttributes(connection, objectTree);
                }
            }
        } catch(SQLException sqlx) {
            OraDoclet.writeLog(sqlx.getMessage(), OraDoclet.ERROR, "buildObjectTree()", sqlx);            
        } catch(Exception ex) {
            OraDoclet.writeLog(ex.getMessage(), OraDoclet.ERROR, "buildObjectTree()", ex);            
        }
        
        return objectTree;
    }   
    
    /**
     * Returns a relative link to the given object. Each object mentioned in the
     * documentation gets a unique link it can be referenced by. The link ist
     * formed according a certain convention, e.g. object_type-OBJECT_NAME.html,
     * spaces are replaced with underscore '_'. 
     * 
     * @param  dbobject a database object the link will be generated for
     * @return objectLink a relative link to the given object or null if the name is not an object of supported type 
     */
    public String getObjectLink(DatabaseObject dbobject) {
        boolean linkable = false;
        String  link = null;
        
        if(null!=dbobject && null!=dbobject.getObjectType() && null!=dbobject.getObjectName()) {
            // Check whether this object type is supported by the doclet
            for(int i=0; i < OraDictionary.getSupportedObjectTypes().length; i++) {
                if((OraDictionary.getSupportedObjectTypes()[i][0]).equalsIgnoreCase(dbobject.getObjectType())) {
                    linkable = true;
                    break;
                }
            }
            // Exceptions of the rule:
            // a) Non-linkable objects not having own page and  presented all together
            if(dbobject.getObjectType().equalsIgnoreCase("SEQUENCE")) {
                linkable = false;
                link = "sequences-list.html#" + dbobject.getObjectName().toUpperCase();
            } 
            
            // b) Child objects being listed on the parent page, e.g. table-TABLENAME.html#col-COLUMNNAME
            if(null!=dbobject.getParent()) {
                link = dbobject.getParent().getObjectType().toLowerCase()
                    + "-"
                    + dbobject.getParent().getObjectName().toLowerCase() 
                    + ".html#" + dbobject.getObjectType().toLowerCase().substring(0,3) 
                    + "-" 
                    + dbobject.getObjectName().toLowerCase().replace('\\', '_').replace('/', '_').replace(':', '_');
            }
            
            // Generate the link according to the file set naming convention                
            if(null==link && linkable) {
                link = dbobject.getObjectType().toLowerCase() + "-" + dbobject.getObjectName().toLowerCase().replace('\\', '_').replace('/', '_').replace(':', '_') + ".html";
            }
        }
        return link.replace(' ','_');    
    }     
    
    /**
     * Retrieves the attributes from the database using the <b>query</b> string,
     * for the objects of the <b>objectType</b>, which parent objects supposed 
     * to have the type of <b>parentType</b>. The first column of the result set
     * is an object name, the further columns are its attributes.
     * 
     * Those names starting with underscore '_' are supposed not to be displayed 
     * in the output (they become invisible).
     * 
     * The elements of the attribute collection keep the order they were retrieved in. 
     * 
     * @param connection active db connection
     * @param objectTree object tree
     * @param objectType type of the objects, the attributes read for
     * @param parentType supposed object type for the parent of these objects
     * @param query SQL-query delivering the object attributes. The 1st column is an object name
     * @param columnObjectTypes a String array containing the DatabaseObject type, one per query column
     * @param concatenate specifies, whether the values of the field having multiple entries will be concatenated (e.g. code lines) 
     */
    protected void readAttributes(Connection connection, TreeMap objectTree, String objectType, String parentType
        , String query, String[] columnObjectTypes, boolean concatenate) {
        System.out.println(query);
        Statement      stmt          = null;
        ResultSet      rset          = null;
        DatabaseObject dbobject      = null;
        DatabaseObject parent        = null;        
        DatabaseAttribute attr       = null;
        Vector         attrMatrix    = null;
        Vector         attrMatrixLine= null;         
        String         name          = null;
        String         key           = null;
        String         keyOld        = null;        
        String         parentName    = null;
        // if the object appears to be a child, it will be added to parent's list under the childAttributeName        
        String         childAttrName = null;
       
        //TODO: Optimize this method and make it readable
        try {
            stmt = connection.createStatement(); 
            rset = stmt.executeQuery(query);
            while(null!=rset && rset.next() && rset.getMetaData().getColumnCount()==columnObjectTypes.length) {
                // Identify the object the attributes will be assiged to:
                childAttrName = rset.getMetaData().getColumnName(1);
                name = rset.getString(1);
                key  = DatabaseObject.genKey(objectType, name);
                // TODO: Do something about this column-"magic"
                if(objectType.equalsIgnoreCase("COLUMN") ) {
                    // Columns require the ParentID in their key:
                    parentName = rset.getString(2);                    
                    key = DatabaseObject.genKey( DatabaseObject.genKey(parentType, parentName) , key);
                }

                // if the key didn't change, the object remains the same
                if(null!=key && !key.equals(keyOld)) {
                    dbobject = (DatabaseObject) objectTree.get(key);
                }
                
                if(null!=dbobject) {
                    attrMatrix = dbobject.getAttributeMatrix();
                    
                    if(concatenate && null!=key && key.equals(keyOld) 
                        && null!=attrMatrixLine && attrMatrixLine.size() > 0) {
                        // Do nothing. The reference to the previous line will be reused
                    } else {
                        attrMatrixLine= new Vector(); // Allocate a new matrix line
                    }
                                                            
                    for(int i=2; i<=rset.getMetaData().getColumnCount(); i++ ) {
                        // Get the attribute name (is a column name in the result set)
                        String attrName = rset.getMetaData().getColumnName(i);
                        // Get the value, the method depends on database datatype
                        String value = null;
                        if(rset.getMetaData().getColumnTypeName(i).equalsIgnoreCase("LONG")) {
                            if(null!=rset.getAsciiStream(i)) {
                                value = getStringFromAsciiStream(rset.getAsciiStream(i));
                            }
                        } else {
                            value = rset.getString(i);    
                        }
                        // Trim(): Some queries return trailing whitespaces 
                        // (e.g. if a result set contains a string constant)
                        value = (null==value ? "" : value.trim());

                        if(attrName.equalsIgnoreCase(COL_PARENT_NAME)) {
                            // Parent name is not stored with other attributes
                            parentName = value;
                        } else { 
                            // Store the attribute into attribute vector
                            // TODO: Shouldn't the visibility property be specified explicitly, like DBObject type, not basing on the column name convention ?
                            // Attributes which names start with underscore are not for presentation (invisible)
                            boolean isVisible = !(attrName.startsWith("_"));
                            boolean isPreformatted = attrName.toLowerCase().endsWith(COL_CODE.toLowerCase());
                            // If the value returned has a DatabaseObject type, store the reference to this object
                            DatabaseObject attrDBObject = null;
                            if(null!=columnObjectTypes[i - 1]) {
                                String refKey = DatabaseObject.genKey(columnObjectTypes[i - 1], value); 
                                attrDBObject = (DatabaseObject) objectTree.get(refKey);
                            }
                            // This code takes care about the exceedingly long lines
                            if(concatenate) {
                                value = getStringWithLineBreaks(value, 80, "\r\n\t");
                            }
                                                        
                            if(concatenate && null!=key && key.equals(keyOld) 
                                && null!=attrMatrixLine && attrMatrixLine.size() > 0) {
                                for(int j=0; j<attrMatrixLine.size();j++) {
                                    attr = (DatabaseAttribute) attrMatrixLine.elementAt(j);
                                    if(attr.getName().equalsIgnoreCase(attrName)) break;        
                                }
                                attr.setValue(attr.getValue() + "\r\n" + value);   
                            } else {
                                attr = new DatabaseAttribute(attrName, value, attrDBObject, isVisible);
                                attr.setPreformatted(isPreformatted);
                                attrMatrixLine.add(attr);
                            }
                        }
                    }

                    if(concatenate && null!=key && key.equals(keyOld) 
                        && null!=attrMatrixLine && attrMatrixLine.size() > 0) {
                        // Do nothing. The previous line has been already added once
                    } else {
                        if(null!=attrMatrixLine && attrMatrixLine.size() > 0) {
                            attrMatrix.add(attrMatrixLine);
                        }
                    }

                    // Find and link the parent of the current object
                    if(null!=parentType && null!=parentName && !key.equals(keyOld) && !dbobject.isAttached()) {
                        parent = (DatabaseObject) objectTree.get(DatabaseObject.genKey(parentType, parentName));
                        dbobject.setParent(parent);
                        // Assign a link for the objects, which link depends on parent
                        if(parent!=null) {
                            dbobject.setLink(getObjectLink(dbobject));
                            // Insert child objects (ObjectID) into parent's matrix as attributes in their original order
                            Vector childObjectAttributes = new Vector();
                            childObjectAttributes.add(new DatabaseAttribute(childAttrName, dbobject.getObjectID(), true)); 
                            parent.getAttributeMatrix().add(childObjectAttributes);
                            // This prevents superfluous multiple inserts                            
                            dbobject.setAttached(true);                             
                        }
                    }
                }
                
                keyOld = key;                
            }
            rset.close();
            rset = null; 
            stmt.close();           
            stmt = null;                    
        } catch(SQLException sqlx) {
            OraDoclet.writeLog(sqlx.getMessage(), OraDoclet.ERROR, "readAttributes()", sqlx);            
        } catch(Exception ex) {
            OraDoclet.writeLog(ex.getMessage(), OraDoclet.ERROR, "readAttributes()", ex);            
        }
    }
    
    protected void readTableAttributes(Connection connection, TreeMap objectTree) {
        String query;
        String[] columnObjectTypes;
       
        // Table comments
        query = "SELECT utc.table_name AS \"Table\", " 
              + "       utc.comments   AS \"Description\" " 
              + "  FROM user_tab_comments utc "
              + " WHERE utc.table_type = 'TABLE' "       
              + " ORDER BY utc.table_name";
        columnObjectTypes = (new String[] {"TABLE", null});   
        readAttributes(connection, objectTree, "TABLE", null, query, columnObjectTypes, false);
        
        // Physical options 
        query = "SELECT table_name                       \"Table\", "
             + "        'Setting'                        \"Option\", "         
             + "        NVL(cluster_name, 'N')           \"Clustered\", "
             + "        secondary                        \"Generated by Oracle\", "        
             + "        DECODE(iot_type, NULL, 'N', 'Y') \"Index Organized\", "       
             + "        SUBSTR(logging, 1, 1)            \"Logging\", "
             + "        SUBSTR(partitioned, 1, 1)        \"Partitioned\", " 
             + "        temporary                        \"Temporary\", " 
             + "        SUBSTR(nested, 1, 1)             \"Nested\" "
             + "   FROM user_tables "
             + "  ORDER BY table_name";
        columnObjectTypes = (new String[] {"TABLE", null, null, null, null, null, null, null, null});
        readAttributes(connection, objectTree, "TABLE", null, query, columnObjectTypes, false);
        
        // Referenced by
        query = "SELECT b.table_name     \"Table\", " 
             + "        a.table_name     \"Referenced by\", "
             + "        a.constraint_name \"Constraint\" "
             + "   FROM user_constraints a, " 
             + "        user_constraints b  " 
             + " WHERE b.constraint_name = a.r_constraint_name "
             + " ORDER BY b.table_name, a.table_name ";                        
        columnObjectTypes = (new String[] {"TABLE", "TABLE", "CONSTRAINT"});
        readAttributes(connection, objectTree, "TABLE", null, query, columnObjectTypes, false);
    }
    
    protected void readViewAttributes(Connection connection, TreeMap objectTree) {
        String query;
        String[] columnObjectTypes;
                
        query = "SELECT utc.table_name AS \"View\", " 
              + "       utc.comments   AS \"Description\" " 
              + "  FROM user_tab_comments utc "
              + " WHERE utc.table_type = 'VIEW' "       
              + " ORDER BY utc.table_name";
        columnObjectTypes = (new String[] {"VIEW", null});              
        readAttributes(connection, objectTree, "VIEW", null, query, columnObjectTypes, false);
        
        query = "SELECT view_name AS \"View\", " 
              + "       text      AS \"Code\" " 
              + "  FROM user_views "
              + " ORDER BY view_name";
        columnObjectTypes = (new String[] {"VIEW", null});              
        readAttributes(connection, objectTree, "VIEW", null, query, columnObjectTypes, false);
        
        //TODO Implement the reading of  Constraints, Referenced by, Triggers  for views                
    }    
    
    protected void readIndexAttributes(Connection connection, TreeMap objectTree) {
        String query;
        String[] columnObjectTypes;
                
        query = "SELECT index_name \"Index\", " 
              + "       table_name \"parent_name\", " 
              + "       index_type \"Type\", " 
              + "       uniqueness \"Uniqueness\" " 
              + " FROM user_indexes" 
              + " ORDER BY index_name ";
        columnObjectTypes = (new String[] {"INDEX", "TABLE", null, null});              
        readAttributes(connection, objectTree, "INDEX", "TABLE", query, columnObjectTypes, false);              
    }    
    
    protected void readColumnAttributes(Connection connection, TreeMap objectTree) {
        String query;
        String[] columnObjectTypes;
                
        query = "SELECT ucl.column_name \"Column\", "
                    + " ucl.table_name \"parent_name\", " 
                    + " ucl.data_type||"                    
                    + "REPLACE(DECODE(ucl.data_type, 'NUMBER', '('||ucl.data_precision||','||ucl.data_scale||')', "
                        + "'CHAR','(' || DECODE(ucl.char_used, 'C', ucl.char_length ,'B', ucl.char_col_decl_length) || ')', "
                        + "'VARCHAR','(' || DECODE(ucl.char_used, 'C', ucl.char_length ,'B', ucl.char_col_decl_length) || ')', "
                        + "'VARCHAR2','(' || DECODE(ucl.char_used, 'C', ucl.char_length ,'B', ucl.char_col_decl_length) || ')', "
                        + "'DATE', '', "
                        + "'('||ucl.data_length||')'"
                    + "), '(,)', '') \"Datatype\"," 
                    + "ucl.nullable \"Nullable\"," 
                    + "ucl.data_default \"Default value\","
                    + "ucc.comments \"Comment\""
              + " FROM  user_tab_columns  ucl"       
              +       ",user_col_comments ucc"
              + " WHERE ucl.table_name  = ucc.table_name"
              + "   AND ucl.table_name  NOT IN (SELECT view_name FROM user_views)"              
              + "   AND ucl.column_name = ucc.column_name"           
              + " ORDER BY ucl.table_name, ucl.column_id ";
        columnObjectTypes = (new String[] {"COLUMN", "TABLE", null, null, null, null});              
        readAttributes(connection, objectTree, "COLUMN", "TABLE", query, columnObjectTypes, false);

        query = "SELECT ucl.column_name  \"Column\", " 
              + "ucl.table_name   \"parent_name\", "
              + "ucl.data_type||DECODE(ucl.data_type, 'NUMBER', '('||ucl.data_precision||','||ucl.data_scale||')', 'DATE', '',  '('||ucl.data_length||')') \"Datatype\", " 
              + "ucl.nullable     \"Nullable\", "
              + "uuc.insertable   \"Insertable\", "
              + "uuc.updatable    \"Updateable\", " 
              + "uuc.deletable    \"Deletable\", "
              + "ucc.comments     \"Comment\" "
              + "FROM  user_tab_columns       ucl "   
              +      ",user_col_comments      ucc "
              +      ",user_updatable_columns uuc "
              + "WHERE ucl.table_name  = ucc.table_name "
              +   "AND ucl.table_name  = uuc.table_name "
              +   "AND ucl.table_name IN (SELECT view_name FROM user_views) "              
        + "AND ucl.column_name = ucc.column_name "          
        + "AND ucl.column_name = uuc.column_name "             
        + "ORDER BY ucl.table_name, ucl.column_id ";
        columnObjectTypes = (new String[] {"COLUMN", "VIEW", null, null, null, null, null, null});              
        readAttributes(connection, objectTree, "COLUMN", "VIEW", query, columnObjectTypes, false);
        
        query = "SELECT ucl.column_name       \"Column\","
              + "       ucl.table_name        \"parent_name\","
              + "       'INDEX'               \"_owner_type\","
              + "       uic.index_name        \"_owner_name\","
              + "       uic.column_position   \"_position\" "        
              + "  FROM user_tab_columns  ucl"   
              + "      ,user_ind_columns  uic"
              + " WHERE ucl.table_name  = uic.table_name"
              + "   AND ucl.column_name = uic.column_name"           
              + "    UNION "
              + "SELECT ucl.column_name     \"Column\","
              + "       ucl.table_name        \"parent_name\","
              + "       'CONSTRAINT'          \"_owner_type\","
              + "       ucc.constraint_name   \"_owner_name\","
              + "       ucc.position          \"_position\"  "              
              + "  FROM user_tab_columns  ucl "
              + "      ,user_cons_columns ucc "       
              + "WHERE ucl.table_name  = ucc.table_name"
              + "  AND ucl.column_name = ucc.column_name"           
              + "  AND ucc.position IS NOT NULL "
              + "ORDER BY \"parent_name\", \"_owner_type\", \"_owner_name\", \"_position\"  ";
        columnObjectTypes = (new String[] {"COLUMN", "TABLE", null, "INDEX", null});              
        readAttributes(connection, objectTree, "COLUMN", "TABLE", query, columnObjectTypes, false);  
    }   
    
    protected void readConstraintAttributes(Connection connection, TreeMap objectTree) {
        String query;
        String[] columnObjectTypes;
                
        query = "SELECT constraint_name AS \"Primary key\", "
            + "         table_name      AS \"parent_name\" "        
            + "    FROM user_constraints" 
            + "   WHERE constraint_type = 'P' "
            + "ORDER BY constraint_name";
        columnObjectTypes = (new String[] {"CONSTRAINT", "TABLE"});            
        readAttributes(connection, objectTree, "CONSTRAINT", "TABLE", query, columnObjectTypes, false);

        query = "SELECT constraint_name  AS \"Check constraint\", "
            + "         table_name       AS \"parent_name\", "
            + "         search_condition AS \"Check condition\" "
            + "    FROM user_constraints" 
            + "   WHERE constraint_type = 'C' "
            + "ORDER BY constraint_name";  
        columnObjectTypes = (new String[] {"CONSTRAINT", "TABLE", null});                      
        readAttributes(connection, objectTree, "CONSTRAINT", "TABLE", query, columnObjectTypes, false);
            
        query = " SELECT uc1.constraint_name   AS \"Foreign key\", " 
            + "          uc1.table_name        AS \"parent_name\", "        
            + "          uc2.table_name        AS \"Referenced table\", " 
            + "          uc1.r_constraint_name AS \"Referenced constraint\", " 
            + "          uc1.delete_rule       AS \"Delete rule\" "
            + "     FROM user_constraints uc1, "
            + "          user_constraints uc2  "
            + "    WHERE uc1.constraint_type = 'R' "
            + "      AND uc1.r_constraint_name = uc2.constraint_name "
            + " ORDER BY uc1.constraint_name";            
        columnObjectTypes = (new String[] {"CONSTRAINT", "TABLE", "TABLE", "CONSTRAINT", null});            
        readAttributes(connection, objectTree, "CONSTRAINT", "TABLE", query, columnObjectTypes, false);
        
        query = "SELECT constraint_name AS \"Unique key\", "
            + "         table_name      AS \"parent_name\" "        
            + "    FROM user_constraints" 
            + "   WHERE constraint_type = 'U' "
            + "ORDER BY constraint_name";
        columnObjectTypes = (new String[] {"CONSTRAINT", "TABLE"});                        
        readAttributes(connection, objectTree, "CONSTRAINT", "TABLE", query, columnObjectTypes, false);
    }    

    protected void readTriggerAttributes(Connection connection, TreeMap objectTree) {
        String query;
        String[] columnObjectTypes;
                
        query = "SELECT us.name                                           \"Trigger\", "
              + "       ut.table_name                                     \"parent_name\", "  
              + "       us.line||LPAD(':',5 - LENGTH(us.line))||us.text   \"Code\" " 
              + "  FROM user_source   us, "
              + "       user_triggers ut "
              + " WHERE us.name = ut.trigger_name "
              + "   AND us.type = 'TRIGGER' "              
              + " ORDER BY ut.table_name, us.name, line ";
        columnObjectTypes = (new String[] {"TRIGGER", "TABLE", null});              
        readAttributes(connection, objectTree, "TRIGGER", "TABLE", query, columnObjectTypes, true);              
    }
    
    protected void readFunctionAttributes(Connection connection, TreeMap objectTree) {
        String query;
        String[] columnObjectTypes;
        
        query = "SELECT ua.object_name                                    \"Function\", "
              + "       ua.argument_name                                  \"Argument name\", " 
              + "       ua.data_type                                      \"Datatype\", "
              + "       ua.default_value                                  \"Default value\", " 
              + "       ua.in_out                                         \"In/Out\" "
              + "  FROM user_arguments  ua,"
              + "       user_objects    uo "
              + " WHERE ua.object_name = uo.object_name " 
              + "   AND uo.object_type = 'FUNCTION' "
              + "   AND ua.position > 0 "
              + "   AND ua.package_name IS NULL "
              + "   ORDER BY ua.object_name, ua.position ";
        columnObjectTypes = (new String[] {"FUNCTION", null, null, null, null});              
        readAttributes(connection, objectTree, "FUNCTION", null, query, columnObjectTypes, false);

        query = "SELECT ua.object_name                                    \"Function\", "
              + "       ua.data_type                                      \"Returns\" "
              + "  FROM user_arguments  ua,"
              + "       user_objects    uo "
              + " WHERE ua.object_name = uo.object_name " 
              + "   AND uo.object_type = 'FUNCTION' "
              + "   AND ua.position = 0 "
              + "   AND ua.package_name IS NULL "
              + "   ORDER BY ua.object_name ";
        columnObjectTypes = (new String[] {"FUNCTION", null});              
        readAttributes(connection, objectTree, "FUNCTION", null, query, columnObjectTypes, false);
              
        query = "SELECT uo.object_name                                    \"Function\", "
              + "       us.line||LPAD(':',5 - LENGTH(us.line))||us.text   \"Code\" " 
              + "  FROM user_source     us, " 
              + "       user_objects    uo  "  
              + " WHERE us.name = uo.object_name "
              + "   AND uo.object_type = 'FUNCTION' "
              + "   AND uo.object_type = us.type "              
              + " ORDER BY uo.object_name, line ";              
        columnObjectTypes = (new String[] {"FUNCTION", null});              
        readAttributes(connection, objectTree, "FUNCTION", null, query, columnObjectTypes, true);
    }    

    protected void readProcedureAttributes(Connection connection, TreeMap objectTree) {
        String query;
        String[] columnObjectTypes;

        query = "SELECT ua.object_name                                    \"Procedure\", "
              + "       ua.argument_name                                  \"Argument name\", " 
              + "       ua.data_type                                      \"Datatype\", "
              + "       ua.default_value                                  \"Default value\", " 
              + "       ua.in_out                                         \"In/Out\" "
              + "  FROM user_arguments  ua,"
              + "       user_objects    uo "
              + " WHERE ua.object_name = uo.object_name " 
              + "   AND uo.object_type = 'PROCEDURE' "
              + "   AND ua.position > 0 "
              + "   AND ua.package_name IS NULL "
              + "   ORDER BY ua.object_name, ua.position ";
        columnObjectTypes = (new String[] {"PROCEDURE", null, null, null, null});              
        readAttributes(connection, objectTree, "PROCEDURE", null, query, columnObjectTypes, false);
                
        query = "SELECT uo.object_name                                    \"Procedure\", "
              + "       us.line||LPAD(':',5 - LENGTH(us.line))||us.text   \"Code\" " 
              + "  FROM user_source     us, " 
              + "       user_objects    uo  "  
              + " WHERE us.name = uo.object_name "
              + "   AND uo.object_type = 'PROCEDURE' "
              + "   AND uo.object_type = us.type "              
              + " ORDER BY uo.object_name, line ";              
        columnObjectTypes = (new String[] {"PROCEDURE", null});              
        readAttributes(connection, objectTree, "PROCEDURE", null, query, columnObjectTypes, true);              
    }    

    protected void readPackageAttributes(Connection connection, TreeMap objectTree) {
        String query;
        String[] columnObjectTypes;

        query = "SELECT uo.object_name                                    \"Package\", "
              + "       us.line||LPAD(':',5 - LENGTH(us.line))||us.text   \"Package Code\" " 
              + "  FROM user_source     us, " 
              + "       user_objects    uo  "  
              + " WHERE us.name = uo.object_name "
              + "   AND uo.object_type = 'PACKAGE' "
              + "   AND uo.object_type = us.type "              
              + " ORDER BY uo.object_name, line ";              
        columnObjectTypes = (new String[] {"PACKAGE", null});              
        readAttributes(connection, objectTree, "PACKAGE", null, query, columnObjectTypes, true);
        
        query = "SELECT uo.object_name                                    \"Package\", "
              + "       us.line||LPAD(':',5 - LENGTH(us.line))||us.text   \"Package Body Code\" " 
              + "  FROM user_source     us, " 
              + "       user_objects    uo  "  
              + " WHERE us.name = uo.object_name "
              + "   AND uo.object_type = 'PACKAGE BODY' "
              + "   AND uo.object_type = us.type "              
              + " ORDER BY uo.object_name, line ";              
        columnObjectTypes = (new String[] {"PACKAGE", null});              
        readAttributes(connection, objectTree, "PACKAGE", null, query, columnObjectTypes, true);              
    }    
    
    /**
     * Returns the contents of the input stream in the form of String.
     * The stream is closed upon completion of the reading operation.
     * 
     * @param istream InputStream to be read from 
     * @return
     */
    private String getStringFromAsciiStream(InputStream istream) {
        String       value = null;
        StringBuffer sbuf  = new StringBuffer();

        if(null!=istream) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(istream));
                do {
                    value = in.readLine();
                    if(null!=value) {
                        sbuf.append(value + "\r\n"); // The line break is required to keep the code formatting
                    }
                } while(null!=value);
                in.close();
                in = null;
                value = sbuf.toString();
                sbuf = null;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }            
        }
        return value;  
    }
    
    /**
     * Returns the string broken into several lines of the given length
     * by inserting of the lineBreak character combination (e.g. \r\n). 
     * When possible, a line break does not occur in the middle of a word, 
     * but before it. 
     * 
     * @param str The original string
     * @param length The maximal length of the line1
     * @return StringWithLineBreaks having lineBreak inserted after each substring of the given length
     */
    private String getStringWithLineBreaks(String str, int length, String lineBreak) {
        StringBuffer result = new StringBuffer();
        int startIndex     = 0;
        int stopIndex      = 0;        
        int lastSpaceIndex = 0;
        
        if(length <=0 || null==str || str.length() < length) {
             return str; 
        } 

        while(startIndex < str.length()- 1 ) {
            if(startIndex + length < str.length()) {
                stopIndex = startIndex + length;
                lastSpaceIndex = str.substring(startIndex, stopIndex).lastIndexOf(' ');
                if(lastSpaceIndex>=0) {
                    stopIndex = startIndex + lastSpaceIndex + 1;                
                }
            } else {
                stopIndex = str.length();
            }
            
            result.append(str.substring(startIndex, stopIndex) + lineBreak);
            startIndex = stopIndex;
        }
        
        return result.toString(); 
    }
}
