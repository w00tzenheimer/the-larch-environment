//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Browser.TestPages;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Fraction;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.MathRoot;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.StyleSheet.StyleSheet;

public class MathRootTestPage extends TestPage
{
	protected MathRootTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Math-root test";
	}
	
	protected String getDescription()
	{
		return "The math-root element places its child within a mathematical square-root symbol."; 
	}


	private static StyleSheet styleSheet = StyleSheet.style( Primitive.editable.as( false ) );
	private static StyleSheet rootStyleSheet = styleSheet.withValues( Primitive.foreground.as( Color.black ), Primitive.hoverForeground.as( new Color( 0.0f, 0.5f, 0.5f ) ) );
	private static StyleSheet textStyleSheet = styleSheet.withValues( Primitive.foreground.as( new Color( 0.0f, 0.5f, 0.0f ) ), Primitive.hoverForeground.as( null ) );

	
	private Pres makeFraction(String numeratorText, String denominatorText)
	{
		return new Fraction( textStyleSheet.applyTo( new Label( numeratorText ) ), textStyleSheet.applyTo( new Label( denominatorText ) ), "/" );
	}

	
	protected Pres createContents()
	{
		ArrayList<Object> children = new ArrayList<Object>( );
		
		children.add( rootStyleSheet.applyTo( new MathRoot( textStyleSheet.applyTo( new Label( "a" ) ) ) ) );
		children.add( rootStyleSheet.applyTo( new MathRoot( textStyleSheet.applyTo( new Label( "a+p" ) ) ) ) );
		children.add( rootStyleSheet.applyTo( new MathRoot( makeFraction( "a", "p+q" ) ) ) );
		
		return new Body( children );
	}
}
