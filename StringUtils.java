package com.ibm.sport.rtc.common;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils
{
   public static final String BOX_DRAWING_HORIZONTAL = "\u2500";
   public static final String BOX_DRAWING_VERTICAL = "\u2502";
   public static final String BOX_DRAWING_VERTICAL_HORIZONTAL = "\u253C";
   // _B68102A Wiki table formatting
   // Unicode equivalents not used here. String methods that require regular
   // expressions will escape the string representation of the special
   // characters.
   public static final String WIKI_TABLE_HEADER_CELL = "|=";
   public static final String WIKI_TABLE_ENTRY_CELL = "|";
   // Some additional formatting needed for improved Web browser display.
   public static final String WIKI_FORMAT_BOLDTAG = "**";
   public static final String WIKI_FORMAT_UNDERLINETAG = "__";

   public static boolean endsWithNewline( String s )
   {
      return s.endsWith( "\r\n" ) || s.endsWith( "\r" ) || s.endsWith( "\n" );
   }

   public static int indexOfNewline( String s, int start )
   {
      Pattern pattern = Pattern.compile( "\r|\n" );
      Matcher matcher = pattern.matcher( s );
      return (matcher.find( start )) ? matcher.start() : -1;
   }

   public static String removeTrailingNewline( String s )
   {
      if (s.endsWith( "\r\n" ))
         return s.substring( 0, s.length() - 2 );
      else if (s.endsWith( "\r" ) || s.endsWith( "\n" ))
         return s.substring( 0, s.length() - 1 );
      else
         return s;
   }

   /*
    * RTC encoding type is UTF-8. A way to safely truncate a string that may
    * contain char with 1-4 bytes preserving its validity [for DB insert]
    * http://www.jroller.com/holy/entry/truncating_utf_string_to_the a single
    */
   // F25601 want to get the last 32768 bytes, not the first, so use length of
   // string - threshold to get the starting point
   // and go up to the length of the string to get the string to be stored in
   // RTC
   // Change made in this file accommodate RTC client creates and updates.
   public static String truncateLargeString( String string )
   {

      int DB_FIELD_LENGTH = 32768;
      String value = "";
      String headerStr = "**DATA TRUNCATED**";

      try
      {
         int lenHeaderStr = headerStr.getBytes( "UTF-8" ).length;
         int len = string.getBytes( "UTF-8" ).length;

         if (len > DB_FIELD_LENGTH)
         {
            value = string;

            Charset utf8Charset = Charset.forName( "UTF-8" );
            CharsetDecoder cd = utf8Charset.newDecoder();
            byte[] sba = value.getBytes( "UTF-8" );
            // Ensure truncating by having byte buffer = DB_FIELD_LENGTH
            ByteBuffer bb = ByteBuffer.wrap( sba, (len - DB_FIELD_LENGTH)
                  + lenHeaderStr, DB_FIELD_LENGTH - lenHeaderStr ); // len in
                                                                    // [B]
            CharBuffer cb = CharBuffer.allocate( DB_FIELD_LENGTH ); // len in
                                                                    // [char]
                                                                    // <= #
                                                                    // [B]
            // Ignore an incomplete character
            cd.onMalformedInput( CodingErrorAction.IGNORE );
            cd.decode( bb, cb, true );
            cd.flush( cb );
            value = new String( cb.array(), 0, cb.position() );
            value = headerStr + value;
         }
         else
            value = new String( string.getBytes( "UTF-8" ), 0, len, "UTF-8" );

      }
      catch (UnsupportedEncodingException e)
      {
         value = "";
      }

      return value;

   }

   /**
    * Repeats a specified {@code String} a specified number of times.
    * 
    * @param s the String to repeat
    * @param n the number times to repeat
    * @return a {@code String} containing {@code s} repeated {@code n} times.
    */
   public static String repeat( String s, int n )
   {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < n; ++i)
         sb.append( s );
      return sb.toString();
   }

   /**
    * Formats the exception to display nicely.
    * 
    * @param origStr The original string
    * @param findStr String to search for
    * 
    * @return Formatted exception
    * 
    */
   public static String getFormattedException( String origStr, String findStr )
   {
      StringBuffer sb = new StringBuffer( "" );
      int index = origStr.indexOf( findStr );
      String sportErrorMsg = "";
      String retainErrorMsg = "";
      String getAssistance = "Please correct the problem. If the problem persists, contact your SPoRT Administrator for assistance.";

      if (index > 0)
      {
         sportErrorMsg = origStr.substring( 0, index );
         retainErrorMsg = origStr.substring( index );
      }
      else
         sportErrorMsg = origStr;

      sb.append( sportErrorMsg + "\n" );
      sb.append( getAssistance + "\n\n" );
      sb.append( retainErrorMsg );

      return sb.toString();
   }

   // _T266500A
   /**
    * Common utility to transform an array of strings to a more user
    * friendly ArrayList of strings (may be empty, but not null).
    * 
    * @param stringArray - input array
    * @return - ArrayList&ltString&gt derived from input argument.
    */
   public static ArrayList<String> arrayListFromArray( String[] stringArray )
   {
      ArrayList<String> resultArrayList = new ArrayList<String>();
      if (stringArray != null && stringArray.length > 0)
      {
         for (String arrayElement : stringArray)
         {
            // Add value as-is
            resultArrayList.add( arrayElement );
         }
      }
      return resultArrayList;
   }
}
