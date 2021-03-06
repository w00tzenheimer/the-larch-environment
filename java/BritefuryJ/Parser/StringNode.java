//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;

import java.util.List;

import BritefuryJ.Util.RichString.RichStringAccessor;

public class StringNode extends UnaryBranchExpression
{
	public StringNode(ParserExpression subexp)
	{
		super( subexp );
	}
	
	public StringNode(Object subexp) throws ParserCoerceException
	{
		super( subexp );
	}
	
	
	private ParseResult matchNode(ParserState state, Object input, int start)
	{
		if ( input instanceof String )
		{
			String s = (String)input;
			ParseResult res = subexp.handleStringChars( state, s, 0 );
			if ( res.getEnd() == s.length() )
			{
				return res.withRange( start, start + 1 );
			}
		}
		else if ( input instanceof RichStringAccessor )
		{
			RichStringAccessor s = (RichStringAccessor)input;
			ParseResult res = subexp.handleRichStringItems( state, s, 0 );
			if ( res.getEnd() == s.length() )
			{
				return res.withRange( start, start + 1 );
			}
		}

		return ParseResult.failure( start );
	}
	
	
	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		return matchNode( state, input, 0 );
	}
	
	protected ParseResult evaluateStringChars(ParserState state, String input, int start)
	{
		return ParseResult.failure( start );
	}
	
	protected ParseResult evaluateRichStringItems(ParserState state, RichStringAccessor input, int start)
	{
		if ( start < input.length() )
		{
			Object structural[] = input.matchStructuralNode( start );
			
			if ( structural != null )
			{
				return matchNode( state, structural[0], start );
			}
		}
		
		return ParseResult.failure( start );
	}
	
	protected ParseResult evaluateListItems(ParserState state, List<Object> input, int start)
	{
		if ( start < input.size() )
		{
			return matchNode( state, input.get( start ), start );
		}
		

		return ParseResult.failure( start );
	}


	
	
	
	public boolean isEquivalentTo(ParserExpression x)
	{
		if ( x instanceof StringNode )
		{
			return super.isEquivalentTo( x );
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "StringNode( " + subexp.toString() + " )";
	}
}
