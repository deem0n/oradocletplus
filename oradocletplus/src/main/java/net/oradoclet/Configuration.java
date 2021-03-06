/*  
 * Configuration.java
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

import java.util.*;

public class Configuration {
    public static final String DEFAULT_OUTPUT_DIR_NAME = "doc";
    public static final String DEFAULT_IMAGE_DIR_NAME = "images";    

    public boolean nodeprecated;
    public boolean createoverview;
    public boolean showauthor;
    public boolean showversion;
    public boolean nodate;
    
    public String charset;
    public TreeSet objectTree;
    public String destdirname;
    public String stylesheetfile;
    public String helpfile;
    public String docencoding;
    public String encoding;
    public String sourcepath;
    public String header;    
    public String bottom;    
    public String footer;
    public Set<String> schemas;

    public String applicationTitle;
    public String copyrightLabel;    

    protected Set excludedDocFileDirs;
    protected Set excludedQualifiers;
    
    /**
     * Manages the doclet configuration settings
     */
    public Configuration() {
        charset = "";
        destdirname = "";
        docencoding = null;
        encoding = null;
        showauthor = false;
        showversion = false;
        nodate = false;
        sourcepath = "";
        nodeprecated = false;
        excludedDocFileDirs = new HashSet();
        excludedQualifiers = new HashSet();
        schemas = new LinkedHashSet<>();
    }
}
