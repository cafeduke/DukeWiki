package com.github.cafeduke.dukewiki.util;

import java.io.*;
import java.nio.charset.Charset;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;

public class Styler
{   
    public static final String LINESEP = System.getProperty("line.separator");
    
    public static final String COMMENT = "#";
    
    public static void main (String arg[]) throws IOException
    {       
        String text = FileUtils.readFileToString(new File("/home/raghu/file.txt"), Charset.defaultCharset());
        String content = Styler.textToHtml(text);
        content = "<html><body>" + LINESEP + content + LINESEP + "</body></html>";
        FileUtils.writeStringToFile(new File("/home/raghu/file.html"), content, Charset.defaultCharset());
    }

    public static String textToHtml (String text)
    {
        StringBuilder builder = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(text, "#\n", true);
        
        final String PRE_STYLE_BEGIN = "<pre class=\"comment\">";
        final String PRE_BEGIN       = "<pre>";
        final String PRE_END         = "</pre>";

        /**
         * State transition inputs
         * ---------------------------------------
         * #  : Comment
         * \n : Newline
         * t  : Text
         * 
         * 
         * Transitions         Actions
         * ---------------------------------------
         * f(q1, #)  --> q2    PRE_STYLE_BEGIN + #
         * f(q1, \n) --> q4    PRE_BIGIN + \n
         * f(q1, t)  --> q4    PRE_BEGIN + t 
         * 
         * f(q2, #)  --> q2    #
         * f(q2, \n) --> q3    \n
         * f(q2, t)  --> q2    t 
         * 
         * f(q3, #)  --> q2    #
         * f(q3, \n) --> q4    PRE_END + PRE_BEGIN + \n 
         * f(q3, t)  --> q4    PRE_END + PRE_BEGIN + t
         * 
         * f(q4, #)  --> q2    PRE_END + PRE_STYLE_BEGIN + #
         * f(q4, \n) --> q4    \n 
         * f(q4, t)  --> q4    t
         */
        String prefix;
        int state = 1;
        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();
            switch (state)
            {
                case 1:
                    prefix = token.equals(COMMENT) ? PRE_STYLE_BEGIN : PRE_BEGIN;
                    builder.append(prefix).append(token);
                    state = token.equals(COMMENT) ? 2 : 4;
                break;
                
                case 2:
                    builder.append(token);
                    state = (token.equals(LINESEP))? 3 : 2;
                break;
                
                case 3:
                    prefix = token.equals(COMMENT) ? "" : PRE_END + LINESEP + PRE_BEGIN;
                    builder.append(prefix).append(token);
                    state = token.equals(COMMENT) ? 2 : 4;
                break;
                
                case 4:
                    prefix = token.equals(COMMENT) ? PRE_END + LINESEP + PRE_STYLE_BEGIN : "";
                    builder.append(prefix).append(token);
                    state = token.equals(COMMENT) ? 2 : 4;
                break;
            }
        }
        return builder.toString();
    }  
}
