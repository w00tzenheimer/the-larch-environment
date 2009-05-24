//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.regex.Pattern;

import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;

public class Word extends ParserExpression
{
	protected String initChars, bodyChars;
	protected Pattern pattern;
	
	
	public Word(String bodyChars)
	{
		initChars = "";
		this.bodyChars = bodyChars;
		pattern = Pattern.compile( "[" + Pattern.quote(  bodyChars ) + "]*" );
	}
	
	public Word(String initChars, String bodyChars)
	{
		this.initChars = initChars;
		this.bodyChars = bodyChars;
		pattern = Pattern.compile( "[" + Pattern.quote(  initChars ) + "][" + Pattern.quote(  bodyChars ) + "]*" );
	}
	
	
	public String getInitChars()
	{
		return initChars;
	}
	
	public String getBodyChars()
	{
		return bodyChars;
	}
	
	
	
	protected ParseResult parseStream(ParserState state, ItemStreamAccessor input, int start)
	{
		start = state.skipJunkChars( input, start );
		
		String match = input.matchRegEx( start, pattern );
		
		if ( match != null )
		{
			return new ParseResult( match, start, start + match.length() );
		}
		else
		{
			return ParseResult.failure( start );
		}
	}


	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof Word )
		{
			Word xw = (Word)x;
			return initChars.equals( xw.initChars )  &&  bodyChars.equals( xw.bodyChars );
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "Word( \"" + initChars + "\", \"" + bodyChars + "\" )";
	}
}
