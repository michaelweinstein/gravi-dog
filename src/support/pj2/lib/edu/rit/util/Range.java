//******************************************************************************
//
// File:    Range.java
// Package: edu.rit.util
// Unit:    Class edu.rit.util.Range
//
// This Java source file is copyright (C) 2013 by Alan Kaminsky. All rights
// reserved. For further information, contact the author, Alan Kaminsky, at
// ark@cs.rit.edu.
//
// This Java source file is part of the Parallel Java 2 Library ("PJ2"). PJ2 is
// free software; you can redistribute it and/or modify it under the terms of
// the GNU General Public License as published by the Free Software Foundation;
// either version 3 of the License, or (at your option) any later version.
//
// PJ2 is distributed in the hope that it will be useful, but WITHOUT ANY
// WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
// A PARTICULAR PURPOSE. See the GNU General Public License for more details.
//
// A copy of the GNU General Public License is provided in the file gpl.txt. You
// may also obtain a copy of the GNU General Public License on the World Wide
// Web at http://www.gnu.org/licenses/gpl.html.
//
//******************************************************************************

package support.pj2.lib.edu.rit.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigInteger;

/**
 * Class Range provides a range of type <TT>int</TT>. A range object has
 * the following attributes: <B>lower bound</B> <I>L</I>, <B>upper bound</B>
 * <I>U</I>, and <B>length</B> <I>N</I>. A range object represents the following
 * set of integers: {<I>L</I>, <I>L</I>+1, <I>L</I>+2, .&nbsp;.&nbsp;.&nbsp;,
 * <I>U</I>}, where <I>N</I> = <I>U</I>&minus;<I>L</I>+1.
 * <P>
 * You construct a range object by specifying the lower bound and upper bound.
 * The length is determined automatically. If the lower bound is greater than
 * the upper bound, the range's length is 0 (an empty range).
 * <P>
 * You can use a range object to control a for loop like this:
 * <PRE>
 *     Range range = new Range (0, N-1);
 *     int lb = range.lb();
 *     int ub = range.ub();
 *     for (int i = lb; i &lt;= ub; ++ i)
 *         . . .</PRE>
 * Note that the range is from <TT>lb()</TT> to <TT>ub()</TT> inclusive, so the
 * appropriate test in the for loop is <TT>i &lt;= ub</TT>. Also note that it
 * usually reduces the running time to call <TT>ub()</TT> once, store the result
 * in a local variable, and use the local variable in the for loop test, than to
 * call <TT>ub()</TT> directly in the for loop test.
 *
 * @author  Alan Kaminsky
 * @version 21-May-2013
 */
public class Range
	implements Externalizable
	{

// Hidden data members.

	private static final long serialVersionUID = 2211598848141217595L;

	private int lb;
	private int ub;

// Exported constructors.

	/**
	 * Construct a new range object representing an empty range.
	 */
	public Range()
		{
		this.lb = 0;
		this.ub = -1;
		}

	/**
	 * Construct a new range object with the given lower bound and upper
	 * bound. The range object represents the following set of integers:
	 * {<I>L</I>, <I>L</I>+1, <I>L</I>+2, .&nbsp;.&nbsp;.&nbsp;, <I>U</I>}. The
	 * range's length <I>N</I> is <I>U</I>&minus;<I>L</I>+1.
	 * <P>
	 * <I>Note:</I> <I>L</I> &gt; <I>U</I> is allowed and stands for an empty
	 * range.
	 *
	 * @param  lb  Lower bound <I>L</I>.
	 * @param  ub  Upper bound <I>U</I>.
	 */
	public Range
		(int lb,
		 int ub)
		{
		this.lb = lb;
		this.ub = ub;
		}

	/**
	 * Construct a new range object that is a copy of the given range object.
	 *
	 * @param  range  Range object to copy.
	 */
	public Range
		(Range range)
		{
		this.lb = range.lb;
		this.ub = range.ub;
		}

// Exported operations.

	/**
	 * Returns this range's lower bound.
	 *
	 * @return  Lower bound.
	 */
	public int lb()
		{
		return lb;
		}

	/**
	 * Returns this range's upper bound.
	 *
	 * @return  Upper bound.
	 */
	public int ub()
		{
		return ub;
		}

	/**
	 * Determine if this range is empty.
	 *
	 * @return  True if this range is empty, false otherwise.
	 */
	public boolean isEmpty()
		{
		return lb > ub;
		}

	/**
	 * Returns this range's length.
	 * <P>
	 * <I>Note:</I> For some ranges, the length is too large to be represented
	 * using type <TT>int</TT>. In such cases, the <TT>length()</TT> method will
	 * throw an IllegalStateException; use the {@link #bigLength() bigLength()}
	 * method to get the exact length.
	 *
	 * @return  Length.
	 *
	 * @exception  IllegalStateException
	 *     (unchecked exception) Thrown if this range's length cannot be
	 *     represented using type <TT>int</TT>.
	 */
	public int length()
		{
		if (lb > ub) return 0;
		int len = ub - lb + 1;
		if (len < 0)
			throw new IllegalStateException
				("Range.length(): Length too large");
		return len;
		}

	/**
	 * Returns this range's length as a <TT>long</TT>.
	 *
	 * @return  Length.
	 */
	public long bigLength()
		{
		return lb > ub ? 0L : (long)ub - (long)lb + 1L;
		}

	/**
	 * Determine if this range contains the given value. This range contains the
	 * given value if <TT>this.lb()</TT> &lt;= <TT>val</TT> &lt;=
	 * <TT>this.ub()</TT>.
	 *
	 * @param  value  Value to test.
	 *
	 * @return  True if this range contains the given <TT>value</TT>, false
	 *          otherwise.
	 */
	public boolean contains
		(int value)
		{
		return this.lb <= value && value <= this.ub;
		}

	/**
	 * Determine if this range contains the given range. This range contains the
	 * given range if <TT>this.lb()</TT> &lt;= <TT>range.lb()</TT> and
	 * <TT>range.ub()</TT> &lt;= <TT>this.ub()</TT>.
	 *
	 * @param  range  Range to test.
	 *
	 * @return  True if this range contains the given <TT>range</TT>, false
	 *          otherwise.
	 */
	public boolean contains
		(Range range)
		{
		return this.lb <= range.lb && range.ub <= this.ub;
		}

	/**
	 * Partition this range and return one subrange. This range is split up into
	 * subranges; the <TT>size</TT> argument specifies the number of subranges.
	 * This range is divided as equally as possible among the subranges; the
	 * lengths of the subranges differ by at most 1. The subranges are numbered
	 * 0, 1, . . . <TT>size-1</TT>. This method returns the subrange whose
	 * number is <TT>rank</TT>.
	 * <P>
	 * Note that if <TT>size</TT> is greater than the length of this range, the
	 * returned subrange may be empty.
	 *
	 * @param  size  Number of subranges, <TT>size</TT> &gt;= 1.
	 * @param  rank  Rank of the desired subrange, 0 &lt;= <TT>rank</TT> &lt;
	 *               <TT>size</TT>.
	 *
	 * @return  Subrange.
	 *
	 * @exception  IllegalArgumentException
	 *     (unchecked exception) Thrown if <TT>size</TT> or <TT>rank</TT> is out
	 *     of bounds.
	 */
	public Range subrange
		(int size,
		 int rank)
		{
		// Verify preconditions.
		if (size < 1)
			throw new IllegalArgumentException (String.format
				("Range.subrange(): size = %d illegal", size));
		if (0 > rank || rank >= size)
			throw new IllegalArgumentException (String.format
				("Range.subrange(): rank = %d illegal", rank));

		// Special case.
		if (size == 1)
			return new Range (this);

		// Split this range.
		long bigLen = bigLength();
		long sublen = bigLen/size;
		long subrem = bigLen%size;
		long resultLb;
		if (rank < subrem)
			{
			++ sublen;
			resultLb = lb + rank*sublen;
			}
		else
			{
			resultLb = lb + subrem + rank*sublen;
			}
		return new Range ((int)resultLb, (int)(resultLb + sublen - 1L));
		}

	/**
	 * Partition this range and return all the subranges. This range is split up
	 * into subranges; the <TT>size</TT> argument specifies the number of
	 * subranges. This range is divided as equally as possible among the
	 * subranges; the lengths of the subranges differ by at most 1. The
	 * subranges are returned in an array with indexes 0, 1, . . .
	 * <TT>size-1</TT>.
	 * <P>
	 * Note that if <TT>size</TT> is greater than the length of this range, some
	 * of the returned subranges may be empty.
	 *
	 * @param  size  Number of subranges, size &gt;= 1.
	 *
	 * @return  Array of subranges.
	 *
	 * @exception  IllegalArgumentException
	 *     (unchecked exception) Thrown if <TT>size</TT> is out of bounds.
	 */
	public Range[] subranges
		(int size)
		{
		// Verify preconditions.
		if (size < 1)
			throw new IllegalArgumentException (String.format
				("Range.subranges(): size = %d illegal", size));

		// Special case.
		if (size == 1)
			return new Range[] { new Range (this) };

		// Compute subranges.
		Range[] result = new Range [size];
		long bigLen = bigLength();
		long sublen = bigLen/size;
		int subrem = (int)(bigLen%size);
		long x = lb;
		++ sublen;
		for (int i = 0; i < subrem; ++ i)
			{
			Range result_i = result[i] = new Range();
			result_i.lb = (int)x;
			result_i.ub = (int)(x + sublen - 1L);
			x += sublen;
			}
		-- sublen;
		for (int i = subrem; i < size; ++ i)
			{
			Range result_i = result[i] = new Range();
			result_i.lb = (int)x;
			result_i.ub = (int)(x + sublen - 1L);
			x += sublen;
			}

		return result;
		}

	/**
	 * Determine if this range is equal to the given object. Two ranges are
	 * equal if they both have the same lower and upper bounds, or if they are
	 * both empty ranges.
	 *
	 * @param  obj  Object to test.
	 *
	 * @return  True if this range is equal to <TT>obj</TT>, false otherwise.
	 */
	public boolean equals
		(Object obj)
		{
		if (obj instanceof Range)
			{
			Range that = (Range) obj;
			return
				(this.isEmpty() && that.isEmpty()) ||
				(this.lb == that.lb && this.ub == that.ub);
			}
		else
			return false;
		}

	/**
	 * Returns a hash code for this range.
	 */
	public int hashCode()
		{
		return (this.lb << 16) + this.ub;
		}

	/**
	 * Returns a string version of this range. The format is
	 * <TT>"<I>L</I>..<I>U</I>"</TT>, where <I>L</I> is the lower bound and
	 * <I>U</I> is the upper bound.
	 */
	public String toString()
		{
		return String.format ("%d..%d", lb, ub);
		}

	/**
	 * Write this range to the given object output stream.
	 *
	 * @param  out  Object output stream.
	 *
	 * @exception  IOException
	 *     Thrown if an I/O error occurred.
	 */
	public void writeExternal
		(ObjectOutput out)
		throws IOException
		{
		out.writeInt (lb);
		out.writeInt (ub);
		}

	/**
	 * Read this range from the given object input stream.
	 *
	 * @param  in  Object input stream.
	 *
	 * @exception  IOException
	 *     Thrown if an I/O error occurred.
	 */
	public void readExternal
		(ObjectInput in)
		throws IOException
		{
		lb = in.readInt();
		ub = in.readInt();
		}

// Unit test main program.

//	/**
//	 * Unit test main program.
//	 */
//	public static void main
//		(String[] args)
//		{
//		if (args.length != 3)
//			{
//			System.err.println ("Usage: edu.rit.util.Range <lb> <ub> <size>");
//			System.exit (1);
//			}
//		int lb = Integer.parseInt (args[0]);
//		int ub = Integer.parseInt (args[1]);
//		int size = Integer.parseInt (args[2]);
//		Range range = new Range (lb, ub);
//		System.out.printf ("Range = %s, length = %s%n",
//			range, range.bigLength());
//		for (int rank = 0; rank < size; ++ rank)
//			{
//			Range subrange = range.subrange (size, rank);
//			System.out.printf ("Subrange rank %d = %s, length = %s%n",
//				rank, subrange, subrange.bigLength());
//			}
//		Range[] subranges = range.subranges (size);
//		for (int rank = 0; rank < size; ++ rank)
//			{
//			System.out.printf ("Subrange rank %d = %s, length = %s%n",
//				rank, subranges[rank], subranges[rank].bigLength());
//			}
//		}

	}
