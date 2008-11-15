//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

import java.util.List;

public class Choice extends BranchExpression
{
	public Choice(MatchExpression[] subexps)
	{
		super( subexps );
	}
	
	public Choice(Object[] subexps)
	{
		super( subexps );
	}
	
	public Choice(List<Object> subexps)
	{
		super( subexps );
	}
	
	
	protected MatchResult parseNode(MatchState state, Object input, int start, int stop)
	{
		int maxErrorPos = start;
		
		for (MatchExpression subexp: subexps)
		{
			MatchResult result = subexp.evaluateNode(  state, input, start, stop );
			if ( result.isValid() )
			{
				return result;
			}
			else
			{
				maxErrorPos = Math.max( maxErrorPos, result.end );
			}
		}
		
		return MatchResult.failure( maxErrorPos );
	}
	
	

	public MatchExpression __or__(MatchExpression x)
	{
		return new Choice( appendToSubexps( x ) );
	}

	public MatchExpression __or__(Object x)
	{
		return new Choice( appendToSubexps( toMatchExpression( x ) ) );
	}


	public String toString()
	{
		return "Choice( " + subexpsToString() + " )";
	}
}
