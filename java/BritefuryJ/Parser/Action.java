//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyInteger;
import org.python.core.Py;

import java.util.List;
import java.util.Map;

public class Action extends UnaryBranchExpression
{
	private static class PyCallAction implements ParseAction
	{
		private PyObject callable;
		
		
		public PyCallAction(PyObject callable)
		{
			this.callable = callable;
		}


		public Object invoke(String input, int begin, Object x, Map<String, Object> bindings)
		{
			return callable.__call__( new PyString( input ), new PyInteger( begin ), Py.java2py( x ) );
		}
	}
	
	
	protected ParseAction a;
	
	
	public Action(String subexp, ParseAction a)
	{
		super( subexp );
		this.a = a;
	}
	
	public Action(List<Object> subexp, ParseAction a) throws ParserCoerceException
	{
		super( subexp );
		this.a = a;
	}
		
	public Action(ParserExpression subexp, ParseAction a)
	{
		super( subexp );
		this.a = a;
	}
	
	
	public Action(String subexp, PyObject a)
	{
		this( subexp, new PyCallAction( a ) );
	}
	
	public Action(List<Object> subexp, PyObject a) throws ParserCoerceException
	{
		this( subexp, new PyCallAction( a ) );
	}
		
	public Action(ParserExpression subexp, PyObject a)
	{
		this( subexp, new PyCallAction( a ) );
	}
	
	
	public ParseAction getAction()
	{
		return a;
	}
	

	protected ParseResult evaluate(ParserState state, String input, int start, int stop)
	{
		ParseResult res = subexp.evaluate( state, input, start, stop );
		
		if ( res.isValid() )
		{
			return new ParseResult( this.a.invoke( input, res.begin, res.value, res.bindings ), res.begin, res.end );
		}
		else
		{
			return res;
		}
	}



	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof Action )
		{
			Action ax = (Action)x;
			return super.compareTo( x )  &&  a == ax.a;
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "Action( " + subexp.toString() + " -> " + a.toString() + " )";
	}
}
