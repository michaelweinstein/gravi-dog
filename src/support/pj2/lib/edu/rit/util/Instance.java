//******************************************************************************
//
// File:    Instance.java
// Package: edu.rit.util
// Unit:    Class edu.rit.util.Instance
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;

/**
 * Class Instance provides static methods for creating instances of classes.
 *
 * @author  Alan Kaminsky
 * @version 13-Mar-2013
 */
public class Instance
	{

// Prevent construction.

	private Instance()
		{
		}

// Exported operations.

	/**
	 * Create a new instance of a class as specified by the given string. The
	 * string must consist of a fully-qualified class name, a left parenthesis,
	 * zero or more comma-separated arguments, and a right parenthesis. No
	 * whitespace is allowed. This method attempts to find a constructor for the
	 * given class as follows, where <I>N</I> is the number of arguments:
	 * <UL>
	 * <P><LI>
	 * If <I>N</I> = 0, use a no-argument constructor.
	 * <P><LI>
	 * Else if all arguments are integers, use a constructor with <I>N</I>
	 * arguments of type <TT>int</TT>.
	 * <P><LI>
	 * Else if all arguments are integers and there is no such constructor, use
	 * a constructor with one argument of type <TT>int[]</TT>.
	 * <P><LI>
	 * Else if not all arguments are integers, use a constructor with <I>N</I>
	 * arguments of type <TT>String</TT>.
	 * <P><LI>
	 * Else if not all arguments are integers and there is no such constructor,
	 * use a constructor with one argument of type <TT>String[]</TT>.
	 * <P><LI>
	 * Else throw a NoSuchMethodException.
	 * </UL>
	 * <P>
	 * This method invokes the chosen constructor, passing in the given argument
	 * values, and returns a reference to the newly-created instance.
	 * <P>
	 * <I>Note:</I> To find the given class, the calling thread's context class
	 * loader is used.
	 *
	 * @param  s  Constructor expression string.
	 *
	 * @return  New instance.
	 *
	 * @exception  IllegalArgumentException
	 *     (unchecked exception) Thrown if <TT>s</TT> does not obey the required
	 *     syntax.
	 * @exception  ClassNotFoundException
	 *     Thrown if the given class cannot be found.
	 * @exception  NoSuchMethodException
	 *     Thrown if a suitable constructor cannot be found in the given class.
	 * @exception  InstantiationException
	 *     Thrown if an instance cannot be created because the given class is an
	 *     interface or an abstract class.
	 * @exception  IllegalAccessException
	 *     Thrown if an instance cannot be created because the calling method
	 *     does not have access to the given constructor.
	 * @exception  InvocationTargetException
	 *     Thrown if the given constructor throws an exception.
	 */
	public static Object newInstance
		(String s)
		throws
			ClassNotFoundException,
			NoSuchMethodException,
			InstantiationException,
			IllegalAccessException,
			InvocationTargetException
		{
		int state = 0;
		int nest = 0;
		int i = 0;
		int len = s.length();
		char c;
		StringBuilder token = new StringBuilder();
		String classname = null;
		ArrayList<String> arglist = new ArrayList<String>();

		while (i < len)
			{
			c = s.charAt (i);
			switch (state)
				{
				case 0: // Class name
					if (c == '(')
						{
						if (token.length() == 0)
							syntaxError (s, i);
						classname = token.toString();
						token = new StringBuilder();
						state = 1;
						}
					else if (c == ')')
						syntaxError (s, i);
					else if (c == ',')
						syntaxError (s, i);
					else
						token.append (c);
					break;
				case 1: // Constructor argument
					if (c == '(')
						{
						token.append (c);
						++ nest;
						}
					else if (c == ')')
						{
						if (nest == 0)
							{
							if (token.length() == 0 && arglist.size() > 0)
								syntaxError (s, i);
							else if (token.length() > 0)
								arglist.add (token.toString());
							state = 2;
							}
						else
							{
							token.append (c);
							-- nest;
							}
						}
					else if (c == ',')
						{
						if (nest == 0)
							{
							if (token.length() == 0)
								syntaxError (s, i);
							arglist.add (token.toString());
							token = new StringBuilder();
							}
						else
							token.append (c);
						}
					else
						token.append (c);
					break;
				case 2: // After closing right parenthesis
					syntaxError (s, i);
					break;
				}
			++ i;
			}
		if (state == 0)
			syntaxError (s, i - 1);

		// Get arguments as strings and integers.
		String[] args = arglist.toArray (new String [arglist.size()]);
		Integer[] intargs = new Integer [args.length];
		boolean allAreInts = true;
		for (i = 0; i < args.length; ++ i)
			{
			try
				{
				intargs[i] = new Integer (args[i]);
				}
			catch (NumberFormatException exc)
				{
				allAreInts = false;
				}
			}

		// Get class.
		Class<?> theClass = Class.forName
			(classname,
			 true,
			 Thread.currentThread().getContextClassLoader());

		// Get constructor and create instance.
		Constructor<?> ctor = null;
		Class<?>[] argtypes = null;

		// No-argument constructor.
		if (args.length == 0)
			{
			try
				{
				ctor = theClass.getConstructor();
				return ctor.newInstance();
				}
			catch (NoSuchMethodException exc)
				{
				}
			}

		// Constructor(int,int,...,int).
		if (allAreInts)
			{
			try
				{
				argtypes = new Class<?> [args.length];
				for (i = 0; i < args.length; ++ i)
					{
					argtypes[i] = Integer.TYPE;
					}
				ctor = theClass.getConstructor (argtypes);
				return ctor.newInstance ((Object[]) intargs);
				}
			catch (NoSuchMethodException exc)
				{
				}
			}

		// Constructor(int[]).
		if (allAreInts)
			{
			try
				{
				ctor = theClass.getConstructor (int[].class);
				return ctor.newInstance ((Object) intargs);
				}
			catch (NoSuchMethodException exc)
				{
				}
			}

		// Constructor(String,String,...,String).
		try
			{
			argtypes = new Class<?> [args.length];
			for (i = 0; i < args.length; ++ i)
				{
				argtypes[i] = String.class;
				}
			ctor = theClass.getConstructor (argtypes);
			return ctor.newInstance ((Object[]) args);
			}
		catch (NoSuchMethodException exc)
			{
			}

		// Constructor(String[]).
		try
			{
			ctor = theClass.getConstructor (String[].class);
			return ctor.newInstance ((Object) args);
			}
		catch (NoSuchMethodException exc)
			{
			}

		// Could not find suitable constructor.
		throw new NoSuchMethodException (String.format
			("Instance.newInstance(\"%s\"): Cannot find suitable constructor",
			 s));
		}

// Hidden operations.

	/**
	 * Throw an exception indicating a syntax error.
	 */
	private static void syntaxError
		(String s,
		 int i)
		{
		throw new IllegalArgumentException (String.format
			("Instance.newInstance(): Syntax error in \"%s<<<%s\" at <<<",
			 s.substring (0, i + 1), s.substring (i + 1)));
		}

// Unit test main program.

//	/**
//	 * Unit test main program.
//	 */
//	public static void main
//		(String[] args)
//		throws Exception
//		{
//		System.out.println (Instance.newInstance (args[0]));
//		}

	}
