//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

import java.util.Map;

import org.python.core.Py;
import org.python.core.PyInteger;
import org.python.core.PyObject;

public class Condition extends UnaryBranchExpression
{
	private static class PyCondition implements MatchCondition
	{
		private PyObject callable;
		
		
		public PyCondition(PyObject callable)
		{
			this.callable = callable;
		}


		public boolean test(Object input, int begin, Object x, Map<String, Object> bindings)
		{
			return Py.py2boolean( callable.__call__( Py.java2py( input ), new PyInteger( begin ), Py.java2py( x ) ) );
		}
	}

	
	
	protected MatchCondition cond;
	
	
	public Condition(String subexp, MatchCondition cond)
	{
		super( subexp );
		this.cond = cond;
	}
	
	public Condition(MatchExpression subexp, MatchCondition cond)
	{
		super( subexp );
		this.cond = cond;
	}
	

	public Condition(String subexp, PyObject cond)
	{
		this( subexp, new PyCondition( cond ) );
	}
	
	public Condition(MatchExpression subexp, PyObject cond)
	{
		this( subexp, new PyCondition( cond ) );
	}
	

	
	public MatchCondition getCondition()
	{
		return cond;
	}
	

	protected MatchResult parseNode(MatcherState state, Object input, int start, int stop)
	{
		MatchResult res = subexp.evaluateNode( state, input, start, stop );
		
		if ( res.isValid() )
		{
			if ( cond.test( input, res.begin, res.value, res.bindings ) )
			{
				return res;
			}
			else
			{
				return MatchResult.failure( res.end );
			}
		}
		else
		{
			return res;
		}
	}



	public boolean compareTo(MatchExpression x)
	{
		if ( x instanceof Condition )
		{
			Condition xc = (Condition)x;
			return super.compareTo( x )  &&  cond == xc.cond;
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "Condition( " + subexp.toString() + " when " + cond.toString() + " )";
	}
}
