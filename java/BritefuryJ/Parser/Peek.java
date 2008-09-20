//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.List;

public class Peek extends UnaryBranchExpression
{
	public Peek(String subexp)
	{
		super( subexp );
	}
	
	public Peek(List<Object> subexp) throws ParserCoerceException
	{
		super( subexp );
	}
		
	public Peek(ParserExpression subexp)
	{
		super( subexp );
	}
	

	protected ParseResult evaluate(ParserState state, String input, int start, int stop)
	{
		ParseResult res = subexp.evaluate( state, input, start, stop );
		
		if ( res.isValid() )
		{
			return new ParseResult( null, start, start, true );
		}
		else
		{
			return new ParseResult( start );
		}
	}


	public String toString()
	{
		return "Peek( " + subexp.toString() + " )";
	}
}