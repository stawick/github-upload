package com.ibm.sport.rtc.common;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class CategoryNamePath
{
   public static final String DELIMITERS = "/";

   /**
    * Parse a <code>String</code> that contains category names delimited by
    * {@link DELIMITERS}.
    * 
    * @param categoryNamePath a <code>String</code> that contains category
    *        names delimited by {@link DELIMITERS}.
    * 
    * @return a list of category names parsed from
    *         <code>categoryNamePath</code> listed in the order in which they
    *         appear.
    */
   public static final List<String> parseIntoList( String categoryNamePath )
   {
      StringTokenizer namePathTokenizer = new StringTokenizer(
            categoryNamePath, DELIMITERS );
      ArrayList<String> namePathElements = new ArrayList<String>(
            namePathTokenizer.countTokens() );

      while (namePathTokenizer.hasMoreTokens())
      {
         namePathElements.add( namePathTokenizer.nextToken() );
      }

      return namePathElements;
   }
}
