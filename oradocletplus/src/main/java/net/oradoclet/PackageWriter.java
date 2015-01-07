/*
 * PackageWriter.java
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

import java.io.IOException;
import java.sql.Connection;
import java.util.TreeMap;

/**
 * The class is intended for generating the documentation page 
 * for a PL/SQL package.
 * 
 */
public class PackageWriter extends ObjectWriter {

    /**
     * @param dbconnection
     * @param objectTree
     * @param dbobject
     * @param newSections
     * @throws IOException
     */
    public PackageWriter(
        Connection dbconnection,
        TreeMap objectTree,
        DatabaseObject dbobject)
        throws IOException {
        super(dbconnection, objectTree, dbobject, getSections());
    }
    
    /**
     * The method returns the array of sections.
     * It overrides the default behaviour of the superclass method
     * due to fit the layout of a page describing the attributes 
     * of a PL/SQL package.
     */
    protected static ObjectWriterSection[] getSections() {
        ObjectWriterSection[] s = 
            {new ObjectWriterSection("Package Source",      "Package Code", true, false),
             new ObjectWriterSection("Package Body Source", "Package Body Code", true, false)
            };

        return s;
    }    
}
