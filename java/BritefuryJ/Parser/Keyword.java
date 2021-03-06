//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;

import java.util.regex.Pattern;

import BritefuryJ.Util.RichString.RichStringAccessor;

/*
 * Keyword
 * 
 * Keyword:node( input )			->  input == Keyword.keywordString  ?  input  :  fail
 * Keyword:string( input, start )		->  input[start:start+Keyword.keywordString.length()] == Keyword.keywordString  &&
 * 									input[start+Keyword.keywordString.length()] not in Keyword.disallowedSubsequentChars  ?  input[start:start+Keyword.keywordString.length()] : fail
 * Keyword:richStr( input, start )		->  input[start:start+Keyword.keywordString.length()] == Keyword.keywordString  &&
 * 								input[start+Keyword.keywordString.length()] not in Keyword.disallowedSubsequentChars  ?  input[start:start+Keyword.keywordString.length()] : fail
 * Keyword:list( input, start )		->  input[start] == Keyword.keywordString  ?  input[start]  :  fail
 */
public class Keyword extends TerminalString
{
	protected final String keywordString, disallowedSubsequentChars;
	private final Pattern postPattern;
	
	
	public Keyword(String keywordString)
	{
		this( keywordString, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_" );
	}
	
	public Keyword(String keywordString, String disallowedSubsequentChars)
	{
		this.keywordString = keywordString;
		this.disallowedSubsequentChars = disallowedSubsequentChars;
		postPattern = Pattern.compile( "[" + Pattern.quote( disallowedSubsequentChars ) + "]*" );
	}
	
	
	public String getKeywordString()
	{
		return keywordString;
	}
	
	public String getDisallowedSubsequentChars()
	{
		return disallowedSubsequentChars;
	}
	
	
	
	protected ParseResult consumeString(String input, int start)
	{
		int end = start + keywordString.length();
		
		if ( end <= input.length() )
		{
			if ( input.subSequence( start, end ).equals( keywordString ) )
			{
				if ( end == input.length()  ||  !postPattern.matcher( input.subSequence( end, end + 1 ) ).matches() )
				{
					return new ParseResult( keywordString, start, end );
				}
			}
		}
		
		return ParseResult.failure( start );
	}
	
	protected ParseResult consumeRichString(RichStringAccessor input, int start)
	{
		CharSequence itemText = input.getItemTextFrom( start );
		int end = keywordString.length();
		
		if ( end <= itemText.length() )
		{
			if ( itemText.subSequence( 0, end ).equals( keywordString ) )
			{
				if ( end == itemText.length()  ||  !postPattern.matcher( itemText.subSequence( end, end + 1 ) ).matches() )
				{
					return new ParseResult( keywordString, start, start + end );
				}
			}
		}
		
		return ParseResult.failure( start );
	}
	
	
	public boolean isEquivalentTo(ParserExpression x)
	{
		if ( x instanceof Keyword )
		{
			Keyword xk = (Keyword)x;
			return keywordString.equals( xk.keywordString )  &&  disallowedSubsequentChars.equals(  xk.disallowedSubsequentChars );
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "Keyword( \"" + keywordString + "\", \"" + disallowedSubsequentChars + "\" )";
	}
}
