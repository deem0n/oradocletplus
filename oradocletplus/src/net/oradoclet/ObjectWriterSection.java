/*
 * ObjectWriterSection.java
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
 * Describes formatting options for each section
 * of the database object description page.
 */
public class ObjectWriterSection {
    protected String  name;
    protected String  attributeName;
    protected boolean tabular;        
    protected boolean transposed;

    /**
     * Standard constructor
     */
    public ObjectWriterSection(String newName, String newAttributeName, boolean newTabular, boolean newTransposed) {
        super();
        name           = newName;
        attributeName  = newAttributeName; 
        tabular        = newTabular;        
        transposed     = newTransposed; 
    }
}
