//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.List;

import org.python.core.Py;
import org.python.core.PyJavaType;
import org.python.core.PyObject;
import org.python.core.PyObjectDerived;
import org.python.core.PyString;
import org.python.core.PyUnicode;

public abstract class DMNode
{
	public static Object coerce(String x)
	{
		// Create a clone of the string to ensure that all String objects in the document are
		// distinct, even if their contents are the same
		return new String( x );
	}
	
	public static Object coerce(PyString x)
	{
		return coerce( x.toString() );
	}
	
	public static Object coerce(PyUnicode x)
	{
		return coerce( x.toString() );
	}
	
	public static Object coerce(List<Object> x)
	{
		return new DMList( x );
	}
	
	public static Object coerce(DMObjectInterface x)
	{
		return new DMObject( x );
	}
	
	@SuppressWarnings("unchecked")
	public static Object coerce(Object x)
	{
		if ( x == null )
		{
			return x;
		}
		else if ( x instanceof DMNode )
		{
			return x;
		}
		else if ( x instanceof PyString )
		{
			return coerce( (PyString)x );
		}
		else if ( x instanceof PyUnicode )
		{
			return coerce( (PyUnicode)x );
		}
		else if ( x instanceof String )
		{
			return coerce( (String)x );
		}
		else if ( x instanceof List )
		{
			return coerce( (List<Object>)x );
		}
		else if ( x instanceof DMObjectInterface )
		{
			return coerce( (DMObjectInterface)x );
		}
		else if ( x instanceof PyJavaType )
		{
			return coerce( Py.tojava( (PyObject)x, Object.class ) );
		}
		else if ( x instanceof PyObjectDerived )
		{
			return coerce( Py.tojava( (PyObject)x, Object.class ) );
		}
		else
		{
			System.out.println( "DMNode.coerce(): attempted to coerce " + x.getClass().getName() + " (" + x.toString() + ")" );
			//throw new RuntimeException();
			return x;
		}
	}
	

	
	
	public static boolean isNull(Object x)
	{
		return "<nil>".equals( x );
	}
	
	public static String newNull()
	{
		return new String( "<nil>" );
	}
}
