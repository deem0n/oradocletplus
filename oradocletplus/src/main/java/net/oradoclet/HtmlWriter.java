/*
 * HtmlWriter.java
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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * Provides the base methods for generation of a HTML-document.
 * The target document is written into a file using the encoding
 * specified.
 */
public class HtmlWriter extends PrintWriter {
    public static final String fileseparator = File.pathSeparator;
    
    protected final String htmlFilename;
    
    static protected final String NBSP           = "&nbsp;";    

    protected final String TAG_ANCHOR     = "a";
    protected final String TAG_ADDRESS    = "address";    
    protected final String TAG_BOLD       = "b";
    protected final String TAG_BODY       = "body";
    protected final String TAG_BR         = "br";
    protected final String TAG_BLOCKQUOTE = "blockquote";
    protected final String TAG_CENTER     = "center";    
    protected final String TAG_CODE       = "code";
    protected final String TAG_DL         = "dl";    
    protected final String TAG_DD         = "dd";    
    protected final String TAG_DT         = "dt";    
    protected final String TAG_EM         = "em";    
    protected final String TAG_FONT       = "font";    
    protected final String TAG_H1         = "h1";    
    protected final String TAG_H2         = "h2";    
    protected final String TAG_H3         = "h3";    
    protected final String TAG_H4         = "h4";    
    protected final String TAG_H5         = "h5";    
    protected final String TAG_HEAD       = "head";
    protected final String TAG_HR         = "hr";
    protected final String TAG_HTML       = "html";
    protected final String TAG_ITALIC     = "i";            
    protected final String TAG_LI         = "li";
    protected final String TAG_MENU       = "menu";
    protected final String TAG_NO_FRAMES  = "noframes";    
    protected final String TAG_PARAGRAPH  = "p";
    protected final String TAG_PRE        = "pre";
    protected final String TAG_SCRIPT     = "script";                
    protected final String TAG_SUP        = "sup";                
    protected final String TAG_TABLE      = "table";
    protected final String TAG_TABLEDIV   = "td";    
    protected final String TAG_TABLEROW   = "tr";    
    protected final String TAG_TITLE      = "title";
    protected final String TAG_UL         = "ul";    
    
    protected String winTitle;

    protected Configuration configuration;

    /**
     * The class implements methods required for writing to an HTML-file
     * 
     * @param config Generation configuration
     * @param dirname Destination directory name (will be created if not exists)
     * @param filename The name of the file generated
     * @param encoding Encoding used when generating the file
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public HtmlWriter(Configuration config, String dirname, String filename, String encoding)
        throws IOException, UnsupportedEncodingException {
        super(getWriter(config, dirname, filename, encoding), true);
        configuration = config;
        htmlFilename = filename;
    }

    /**
     * creates a directory with the specified name, if it does not exist
     * @param dirname the name of the directory created
     * @return created true when the directory exists or has been created, false otherwise 
     */
    static protected boolean createDirectory(String dirname) {
        boolean result = false;
        File    newdir = null;
        if(null != dirname) {
            try {
                newdir = new File(dirname);
                if(!newdir.exists()) {
                    result = newdir.mkdir();                    
                } else {
                    result = true; 
                }
            } catch (NullPointerException npe) {
                // Do nothing here
            }
        }
        return result;
    }

    /**
     * Returns an instance of the writer for the destination specified
     * 
     * @param config Generation configuration
     * @param dirname Destination directory name (will be created if not exists)
     * @param filename The name of the file generated
     * @param encoding Encoding used when generating the file
     * @return writer An instance of the writer
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public static Writer getWriter(Object config, String dirname, String filename, String encoding)
        throws IOException, UnsupportedEncodingException {
        FileOutputStream fileoutputstream;
         
        //if the directory does not exist, it will be created
        if(null!=dirname && createDirectory(dirname)) {
            fileoutputstream = new FileOutputStream(
                (dirname.length() <= 0 ? "" : dirname + File.separator) + filename);
        } else {
            // write to the current directory
            fileoutputstream = new FileOutputStream(filename);
        }
        if(null==encoding) {
            OutputStreamWriter outputstreamwriter = 
                new OutputStreamWriter(fileoutputstream);
            encoding = outputstreamwriter.getEncoding();
            return outputstreamwriter;
        } else {
            return new OutputStreamWriter(fileoutputstream, encoding);
        }
    }

    protected String getTag(String tag) {
        return "<" + tag + ">"; 
    }

    protected String getClosingTag(String tag) {
        return "</" + tag + ">"; 
    }
    
    public void aEnd() {
        print(getClosingTag(TAG_ANCHOR));
    }

    public void address() {
        println(getTag(TAG_ADDRESS));
    }

    public void addressEnd() {
        println(getClosingTag(TAG_ADDRESS));
    }

    public void blockquote() {
        println(getTag(TAG_BLOCKQUOTE));
    }

    public void blockquoteEnd() {
        println(getClosingTag(TAG_BLOCKQUOTE));
    }

    public void bodyEnd() {
        println(getClosingTag(TAG_BODY));
    }

    public void bold() {
        print(getTag(TAG_BOLD));
    }

    public void boldEnd() {
        print(getClosingTag(TAG_BOLD));
    }

    public void br() {
        println();
        println(getTag(TAG_BR));
    }

    public void center() {
        println(getTag(TAG_CENTER));
    }

    public void centerEnd() {
        println(getClosingTag(TAG_CENTER));
    }

    public void code() {
        print(getTag(TAG_CODE));
    }

    public void codeEnd() {
        print(getClosingTag(TAG_CODE));
    }

    public void commentEnd() {
        println("-->");
    }

    public void commentBegin() {
        print("<!-- ");
    }

    public void dd() {
        print(getTag(TAG_DD));
    }

    public void ddEnd() {
        println(getClosingTag(TAG_DD));
    }

    public void dl() {
        println(getTag(TAG_DL));
    }

    public void dlEnd() {
        println(getClosingTag(TAG_DL));
    }

    public void dt() {
        print(getTag(TAG_DT));
    }

    public void em() {
        println(getTag(TAG_EM));
    }

    public void emEnd() {
        println(getClosingTag(TAG_EM));
    }

    public void fontEnd() {
        print(getClosingTag(TAG_FONT));
    }

    public void h1() {
        println(getTag(TAG_H1));
    }

    public void h1End() {
        println(getClosingTag(TAG_H1));
    }

    public void h2() {
        println(getTag(TAG_H2));
    }

    public void h2End() {
        println(getClosingTag(TAG_H2));
    }

    public void h3() {
        println(getTag(TAG_H3));
    }

    public void h3End() {
        println(getClosingTag(TAG_H3));
    }

    public void h4() {
        println(getTag(TAG_H4));
    }

    public void h4End() {
        println(getClosingTag(TAG_H4));
    }

    public void h5() {
        println(getTag(TAG_H5));
    }

    public void h5End() {
        println(getClosingTag(TAG_H5));
    }

    public void head() {
        println(getTag(TAG_HEAD));
        println("<meta charset=\"UTF-8\">");
    }

    public void headEnd() {
        println(getClosingTag(TAG_HEAD));
    }

    public void hr() {
        println(getTag(TAG_HR));
    }

    public void html() {
        println(getTag(TAG_HTML));
    }

    public void htmlEnd() {
        println(getClosingTag(TAG_HTML));
    }

    public void italic() {
        print(getTag(TAG_ITALIC));
    }

    public void italicEnd() {
        print(getClosingTag(TAG_ITALIC));
    }

    public void li() {
        print(getTag(TAG_LI));
    }

    public void menu() {
        println(getTag(TAG_MENU));
    }

    public void menuEnd() {
        println(getClosingTag(TAG_MENU));
    }

    public void noFrames() {
        println(getTag(TAG_NO_FRAMES));
    }

    public void noFramesEnd() {
        println(getClosingTag(TAG_NO_FRAMES));
    }

    public void p() {
        println();
        println(getTag(TAG_PARAGRAPH));
    }

    public void pEnd() {
        println();
        println(getClosingTag(TAG_PARAGRAPH));
    }

    public void pre() {
        println(getTag(TAG_PRE));
    }

    public void preEnd() {
        println(getClosingTag(TAG_PRE));
    }

    public void javaScript() {
        println(getTag(TAG_SCRIPT + " type=\"text/javascript\""));
    }

    public void javaScriptEnd() {
        println(getClosingTag(TAG_SCRIPT));
    }

    public void space() {
        print(NBSP);
    }

    public void sup() {
        println(getTag(TAG_SUP));
    }

    public void supEnd() {
        println(getClosingTag(TAG_SUP));
    }

    public void table() {
        table(0, "100%");
    }

    public void tableEnd() {
        println(getClosingTag(TAG_TABLE));
    }

    public void td() {
        print(getTag(TAG_TABLEDIV));
    }

    public void tdEnd() {
        println(getClosingTag(TAG_TABLEDIV));
    }

    public void tdNowrap() {
        print(getTag(TAG_TABLEDIV + " nowrap"));
    }

    public void title() {
        println(getTag(TAG_TITLE));
    }

    public void titleEnd() {
        println(getClosingTag(TAG_TITLE));
    }

    private boolean trOdd = false;

    public void tr() {
        println(getTag(TAG_TABLEROW+" class='"+(trOdd ? "odd" : "even")+"'"));
        trOdd = !trOdd;
    }

    public void trEnd() {
        println(getClosingTag(TAG_TABLEROW));
    }

    public void ul() {
        println(getTag(TAG_UL));
    }

    public void ulEnd() {
        println(getClosingTag(TAG_UL));
    }

    public void tdColspan(int i) {
        print(getTag(TAG_TABLEDIV + " colspan=" + i));
    }

    public void hr(int i, int j) {
        println(getTag(TAG_HR + " size=\"" + i + "\" width=\"" + j + "%\""));
    }

    public void table(int i, int j, int k) {
        println("\n");
        println(getTag(TAG_TABLE 
            + "table border=\"" + i 
            + "\" cellpadding=\"" + j
            + "\" cellspacing=\"" + k
            + "\" summary=\"\""));
    }

    protected String getWindowTitleOnload() {
        if(winTitle != null && winTitle.length() > 0) {
            return " onload=\"windowTitle();\"";            
        } else {
            return "";            
        }
    }

    public void hr(int i, String s) {
        println(getTag(TAG_HR + " size=\"" + i + "\" noshade"));
    }

    /**
     * Opens the table tag having the specified border and width.
     * 
     * @param border border width
     * @param width  table width (if set to null, the tag option will not be specified)
     */
    public void table(int border, String width) {
        println("\n");
        println(getTag(TAG_TABLE 
         //   + " border=\"" + border
//            + (null == width ? "" : "\" width=\"" + width)
            + " summary=\"\""));//+ "\" summary=\"\""));
    }

    public void table(int i, String s, int j, int k) {
        println("\n");
        println(getTag(TAG_TABLE 
//            + " border=\"" + i
            + " width=\"" + s
//            + "\" cellpadding=\"" + j
//            + "\" cellspacing=\"" + k
            + "\" summary=\"\""));
    }

    /*
        public void anchor(String s) {
            aName(s);
            print("<!-- -->");
            aEnd();
        }
    */
    public void aName(String s) {
        print(getTag(TAG_ANCHOR + " name=\"" + s + "\"")); 
    }

    public void anchor(String href) {
        print(getTag(TAG_ANCHOR + " href=\"" + href + "\""));
    }    

    public void anchor(String href, String text) {
        anchor(href);
        print(text);
        aEnd();
    }

    public void anchorTarget(String href, String target, String text) {
        print(getTag(TAG_ANCHOR + " href=\"" + href + "\" target=\"" + target + "\""));
        print(text);        
        aEnd();
    }

    public void bold(String s) {
        bold();
        print(s);
        boldEnd();
    }

    public void font(String s) {
        println("<font size=\"" + s + "\">");
    }

    public void fontStyle(String s) {
        print("<font class=\"" + s + "\">");
    }

    public void h1(String s) {
        h1();
        println(s);
        h1End();
    }

    public void h2(String s) {
        h2();
        println(s);
        h2End();
    }

    public void h3(String s) {
        h3();
        println(s);
        h3End();
    }

    public void h4(String s) {
        h4();
        println(s);
        h4End();
    }

    public void italics(String s) {
        italic();
        print(s);
        italicEnd();
    }

    public void li(String s) {
        print("<li type=\"" + s + "\">");
    }

    public void link(String s) {
        println("<link " + s + ">");
    }

    protected void printWinTitleScript(String s) {
        if (s != null && s.length() > 0) {
            javaScript();
            println("function windowTitle()");
            println("{");
            println("    parent.document.title=\"" + s + "\";");
            println("}");
            javaScriptEnd();
        }
    }

    public void tdAlign(String s) {
        print("<td>");
    }

    public void tdVAlign(String s) {
        print("<td valign=\"" + s + "\">");
    }

    public void tdWidth(String s) {
        print("<td width=\"" + s + "\">");
    }

    public void title(String titleString) {
        title();
        println(titleString);
        titleEnd();
    }

    public void trBgcolor(String s) {
        println("<tr bgcolor=\"" + s + "\">");
    }
    public void trClass(String s) {
        println("<tr class=\"" + s + "\">");
    }

    public void tdAlignRowspan(String s, int i) {
        print("<td align=\"" + s + "\" rowspan=" + i + ">");
    }

    public void body(boolean getTitleOnload) {
        print("<body ");
        if (getTitleOnload)
            print(getWindowTitleOnload());
        println(">");
    }

    public void tdColspanBgcolorStyle(int i, String s, String s1) {
        print(
            "<td colspan="
                + i
                + " bgcolor=\""
                + s
                + "\" class=\""
                + s1
                + "\">");
    }

    public String getCodeText(String s) {
        return "<code>" + s.replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "</code>";
    }

    public String getFontColor(String s) {
        return "<font color=\"" + s + "\">";
    }

    public String getItalicsText(String s) {
        return "<i>" + s + "</i>";
    }

    public void fontSize(String size) {
        println("<font size=\"" + size + "\">");
    }

    public void fontSizeStyle(String s, String s1) {
        println("<font size=\"" + s + "\" class=\"" + s1 + "\">");
    }

    public void tdAlignVAlign(String s, String s1) {
        print("<td align=\"" + s + "\" valign=\"" + s1 + "\">");
    }

    public void tdBgcolorStyle(String s1) {
        print("<td class=\"" + s1 + "\">");
    }

    public void tdVAlignClass(String s, String s1) {
        print("<td valign=\"" + s + "\" class=\"" + s1 + "\">");
    }

    public void trAlignVAlign(String s, String s1) {
        println("<tr align=\"" + s + "\" valign=\"" + s1 + "\">");
    }

    public void trBgcolorStyle(String s, String s1) {
        println("<tr bgcolor=\"" + s + "\" class=\"" + s1 + "\">");
    }

    public void tdAlignVAlignRowspan(String s, String s1, int i) {
        print(
            "<td align=\"" + s + "\" valign=\"" + s1 + "\" rowspan=" + i + ">");
    }

    public void img(String s, String s1, int i, int j) {
        println(
            "<img src=\"images/"
                + s
                + ".gif\""
                + " width=\""
                + i
                + "\" height=\""
                + j
                + "\" alt=\""
                + s1
                + "\">");
    }

    /**
     * Prints the user specified bottom.
     */ 
    public void printBottom() {
        hr();
        print(configuration.bottom);
    }
}
