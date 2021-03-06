//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;

import java.util.List;

import BritefuryJ.Util.RichString.RichStringAccessor;

/*
 * ClearBindings
 * 
 * ClearBindings:node( input )			->  clearBindings( Bind.subexp:node( input ) )
 * ClearBindings:string( input, start )		->  clearBindings( Bind.subexp:string( input, start ) )
 * ClearBindings:richStr( input, start )		->  clearBindings( Bind.subexp:richStr( input, start ) )
 * ClearBindings:list( input, start )		->  clearBindings( Bind.subexp:list( input, start ) )
 */
public class ClearBindings extends UnaryBranchExpression
{
	public ClearBindings(ParserExpression subexp)
	{
		super( subexp );
	}
	
	public ClearBindings(Object subexp) throws ParserCoerceException
	{
		super( subexp );
	}
	
	
	
	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		ParseResult res = subexp.handleNode( state, input );
		
		if ( res.isValid() )
		{
			return res.clearBindings();
		}
		else
		{
			return res;
		}
	}

	protected ParseResult evaluateStringChars(ParserState state, String input, int start)
	{
		ParseResult res = subexp.handleStringChars( state, input, start );
		
		if ( res.isValid() )
		{
			return res.clearBindings();
		}
		else
		{
			return res;
		}
	}

	protected ParseResult evaluateRichStringItems(ParserState state, RichStringAccessor input, int start)
	{
		ParseResult res = subexp.handleRichStringItems( state, input, start );
		
		if ( res.isValid() )
		{
			return res.clearBindings();
		}
		else
		{
			return res;
		}
	}

	protected ParseResult evaluateListItems(ParserState state, List<Object> input, int start)
	{
		ParseResult res = subexp.handleListItems( state, input, start );
		
		if ( res.isValid() )
		{
			return res.clearBindings();
		}
		else
		{
			return res;
		}
	}
	
	
	public boolean isEquivalentTo(ParserExpression x)
	{
		if ( x instanceof ClearBindings )
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
		return "ClearBindings( " + subexp.toString() + " )";
	}
}
