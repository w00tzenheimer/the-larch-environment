//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.Parser.TracedParseResult;
import BritefuryJ.Parser.Literal;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Production;
import BritefuryJ.Parser.Utils.Tokens;

public class OperatorTable
{
	private ParserExpression rootParser;
	private ArrayList<OperatorLevel> levels;
	
	
	//
	// Constructor
	//
	
	public OperatorTable(OperatorLevel levels[], ParserExpression rootParser)
	{
		this.rootParser = rootParser;
		this.levels = new ArrayList<OperatorLevel>();
		this.levels.addAll( Arrays.asList( levels ) );
	}
	
	
	
	protected ParserExpression getPrefixLevelForReachUp(ArrayList<Production> reachupForwardDeclarations, OperatorLevel aboveLevel)
	{
		int index = levels.indexOf( aboveLevel );
		for (int i = levels.size() - 1; i >= index; i--)
		{
			OperatorLevel lvl = levels.get( i );
			if ( lvl instanceof PrefixLevel )
			{
				PrefixLevel p = (PrefixLevel)lvl;
				if ( p.isReachUpEnabled() )
				{
					return reachupForwardDeclarations.get( i );
				}
			}
		}
		return null;
	}
	
	
	public List<ParserExpression> buildParsers() throws Production.CannotOverwriteProductionExpressionException
	{
		ParserExpression prevLevelParser = rootParser;
		ArrayList<ParserExpression> levelParsers = new ArrayList<ParserExpression>();
		ArrayList<Production> reachupForwardDeclarations = new ArrayList<Production>();

		for (int i = 0; i < levels.size(); i++)
		{
			reachupForwardDeclarations.add( new Production( "oplvl_reachup_" + i ) );
		}

		for (int i = 0; i < levels.size(); i++)
		{
			OperatorLevel lvl = levels.get( i );
			
			ParserExpression levelParser = new Production( "oplvl_" + i, lvl.buildParser( this, prevLevelParser, reachupForwardDeclarations ).__or__( prevLevelParser ) );
			ParserExpression reachupParser = lvl.buildParserForReachUp( this, prevLevelParser );
			levelParsers.add( levelParser );
			reachupForwardDeclarations.get( i ).setExpression( reachupParser );
			
			prevLevelParser = levelParser;
		}
		
		return levelParsers;
	}
	
	
	
	public static TracedParseResult getOperatorTableTestDebugParseResult() throws BritefuryJ.Parser.Production.CannotOverwriteProductionExpressionException
	{
		BinaryOperatorParseAction mulAction = new BinaryOperatorParseAction()
		{
			public Object invoke(Object input, int begin, int end, Object left, Object right)
			{
				return Arrays.asList( '*', left, right );
			}
		};

		UnaryOperatorParseAction notAction = new UnaryOperatorParseAction()
		{
			public Object invoke(Object input, int begin, int end, Object x)
			{
				return Arrays.asList( '!', x );
			}
		};
		
		
		BinaryOperator mul = new BinaryOperator( new Literal( "*" ), mulAction );
		UnaryOperator inv = new UnaryOperator( new Literal( "!" ), notAction );
		
		InfixRightLevel l0 = new InfixRightLevel( new BinaryOperator[] { mul } );
		//PrefixLevel l1 = new PrefixLevel( Arrays.asList( new UnaryOperator[] { inv } ) );
		SuffixLevel l1 = new SuffixLevel( Arrays.asList( inv ) );
		
		OperatorTable t = new OperatorTable( new OperatorLevel[] { l0, l1 }, Tokens.identifier );
		List<ParserExpression> parsers = t.buildParsers();
		ParserExpression e = parsers.get( parsers.size() - 1 );
		
		return e.traceParseStringChars( "a * b * c!" );
//		DebugParseResult r = e.debugParseString( "a!!!" );
	}
}
