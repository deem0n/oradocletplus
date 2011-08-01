/*
 * DatabaseObject.java
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

import java.util.Vector;

/**
 * Contains meta-data of a particular database object.
 */
public class DatabaseObject implements Comparable {
    /** A reference to the parent object */
    private DatabaseObject parent   = null;
    
    /** Indicates whether the object is attached to the parent's attribute matrix */
    private boolean attached = false;
        
    /** Object identifier */        
    private String  objectID        = null;
    
    /** Type of the object, is one of the types supported by the doclet */        
    private String  objectType      = null;
    
    /** Name of the object */    
    private String  objectName      = null;
    
    /** Direct hyperlink to the object */    
    private String  link            = null;
    
    // The matrix is implemented as a vector of vectors
    private Vector  attributeMatrix = null;    
    
    public DatabaseObject() {
        // Default constructor, nothing is done here 
    }

    public DatabaseObject(String newObjectType,
                          String newObjectName,
                          DatabaseObject newParent
                          ) {
        this.parent         = newParent;
        this.objectType     = newObjectType;        
        this.objectName     = newObjectName;
        this.objectID       = genKey(newObjectType, newObjectName);
        // Actually there can be no object with the same type and name within an Oracle-schema
        // For the COLUMN is an artificial type, the statement above is not true for columns.
        // Therefore their key is combined with the parent object key
        // TODO: eliminate any key (ID) manipulation and dependencies in the code 
        if(null!=this.parent) {
            this.objectID = genKey(parent.getObjectID(), this.objectID);
        }
        this.attributeMatrix = new Vector();
        this.attached = false;
    }
    
    /**
     * Creates a key out of the arguments   
     * 
     * @param type object type 
     * @param name object name
     * @return key the key
     */ 
    public static String genKey(String type, String name) {
        return type.toLowerCase() + "." + name.toLowerCase().replace('\\', '_').replace('/', '_').replace(':', '_');
    }
        
        
    /* Indicates whether some other object is "equal to" this one.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if(obj instanceof DatabaseObject) {
            return (((DatabaseObject)obj).getObjectID().equalsIgnoreCase(this.getObjectID()));             
        } else {
            return false;            
        }
    }
    
    /* Compares this object with the specified object for order. 
     * Returns a negative integer, zero, or a positive integer 
     * as this object is less than, equal to, or greater than 
     * the specified object.
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) {
        if(obj instanceof DatabaseObject) {
            return (this.getObjectID().compareTo(((DatabaseObject)obj).getObjectID()) );             
        } else {
            throw (new ClassCastException());            
        }            
    }
        
    /**
     * @return
     */
    public String getObjectName() {
        return objectName;
    }


    /**
     * @param string
     */
    public void setObjectName(String string) {
        objectName = string;
    }


    /**
     * @return
     */
    public String getObjectType() {
        return objectType;
    }


    /**
     * @param string
     */
    public void setObjectType(String string) {
        objectType = string;
    }


    /**
     * @return
     */
    public String getObjectID() {
        return objectID;
    }


    /**
     * @return
     */
    public DatabaseObject getParent() {
        return parent;
    }


    /**
     * @param key
     */
    public void setParent(DatabaseObject newParent) {
        parent = newParent;
    }
    /**
     * @return
     */
    public String getLink() {
        return link;
    }

    /**
     * @param link
     */
    public void setLink(String newLink) {
        this.link = newLink;
    }

    /**
     * @return
     */
    public Vector getAttributeMatrix() {
        return attributeMatrix;
    }
    
    public String toString() {
        String result = "DatabaseObject = {\r\n";
         
        result += "\tObjectID  =" + this.getObjectID()  + "\r\n";
        result += "\tObjectName=" + this.getObjectName()+ "\r\n";         
        result += "\tObjectType=" + this.getObjectType()+ "\r\n";
        result += "\tLink      =" + this.getLink()      + "\r\n";
                        
        Vector attrMX = this.getAttributeMatrix();
        result += "\tattributeMatrix= {\r\n";          
        for(int i=0; i<attrMX.size(); i++) {
            Vector attrMXline = (Vector) attrMX.elementAt(i);
            result += "\t\t{ ";
            if(null!=attrMXline) {
                for(int j=0; j<attrMXline.size(); j++) {
                    DatabaseAttribute attrTmp = (DatabaseAttribute) attrMXline.elementAt(j);
                    if(j > 0) result += " , "; 
                    result += attrTmp.toString();               
                }
            }
            result += "}\r\n";              
        }
        result += "\t}\r\n";          
        result += "}";
                
        return result;
    }
    
    /**
     * @return
     */
    public boolean isAttached() {
        return attached;
    }

    /**
     * @param isAttached
     */
    public void setAttached(boolean isAttached) {
        attached = isAttached;
    }

}
