package com.ibm.sport.rtc.common;

/**
 * The <code>HashCode</code> class creates hash codes for use by the
 * <code>hashCode()</code> method of other classes.
 */
public class HashCode
{
   private static int X = Integer.MIN_VALUE;
   private static int Y = Integer.MAX_VALUE;
   private static int P = 33;
   private int hashCode;

   /**
    * Return a <code>HashCode</code> object.
    * 
    * @return a <code>HashCode</code> object.
    */
   public static final synchronized HashCode create()
   {
      X = ((X+1 < Integer.MIN_VALUE)) ? ++X : Integer.MIN_VALUE;
      Y = ((Y+1 < Integer.MIN_VALUE)) ? ++Y : Integer.MIN_VALUE;
      
      return new HashCode((X * P) ^ Y);
   }
   
   private HashCode(int hashCode)
   {
      this.hashCode = hashCode;
   }
   
   /**
    * Create a hash code.
    * 
    * @return the hash code.
    */
   public final synchronized int getValue()
   {
      return this.hashCode;
   }
}
