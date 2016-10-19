/*
 * DatabaseAttribute.java
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

/**
 * Contains arbitrary attributes of a database object.
 */

public class DatabaseAttribute {
    /**
     * Attribute name or type
     */
    private String name;
    
    /**
     * Attribute value, whatever datatype will be converted to String
     */
    private String value;    

    /**
     * Determines whether the attribute is to be displayed in the output
     */
    private boolean visible;
    
    /**
     * Determines whether the attribute value is pre-formatted and therefore should be displayed in the output as is
     */
    private boolean preformatted;    
    
    /**
     * Direkt reference to the DatabaseObject(in case that the attribute is a DatabaseObject) 
     */
    private DatabaseObject dbobject;    
    
    /**
     * Default constructor, does nothing 
     */
    public DatabaseAttribute() {
        super();
    }

    /**
     * Constructor that initializes the attribute name, value and visibility
     * 
     * @param newName attribute name
     */
    public DatabaseAttribute(String newName, String newValue, boolean isVisible) {
        super();
        this.name  = newName;
        this.value = newValue;
        this.visible = isVisible;    
    }

    /**
     * Constructor that initializes all the fields
     * 
     * @param newName attribute name
     */
    public DatabaseAttribute(String newName, String newValue, DatabaseObject newDBObject, boolean isVisible) {
        super();
        this.name  = newName;
        this.value = newValue;
        this.visible = isVisible;
        this.dbobject = newDBObject;    
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @param string
     */
    public void setName(String newName) {
        name = newName;
    }
    /**
     * @return
     */
    public String getValue() {
        if(value == null) return value;
        return value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }

    /**
     * @param string
     */
    public void setValue(String newValue) {
        value = newValue;
    }
    
    public String toString() {
        String result = "DatabaseAttribute = { name=" + this.getName() 
            + ", value=" + this.getValue() 
            + ", visible=" + this.visible
            + ", preformatted=" + this.preformatted            
            + ", dbobject=" + (null!=this.dbobject ? this.dbobject.getObjectID() : "null")            
            + "}";
        return result;
    }
    /**
     * @return
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @param b
     */
    public void setVisible(boolean b) {
        visible = b;
    }

    /**
     * @return
     */
    public DatabaseObject getDbobject() {
        return dbobject;
    }

    /**
     * @param object
     */
    public void setDbobject(DatabaseObject object) {
        dbobject = object;
    }

    /**
     * @return
     */
    public boolean isPreformatted() {
        return preformatted;
    }

    /**
     * @param b
     */
    public void setPreformatted(boolean b) {
        preformatted = b;
    }

}
