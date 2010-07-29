//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Paragraph;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Segment;
import BritefuryJ.DocPresent.Combinators.Primitive.Span;
import BritefuryJ.DocPresent.Combinators.Primitive.Text;
import BritefuryJ.DocPresent.Combinators.RichText.Body;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;

public class SegmentTestPage extends SystemPage
{
	protected SegmentTestPage()
	{
		register( "tests.segment" );
	}
	
	
	public String getTitle()
	{
		return "Segment test";
	}

	protected String getDescription()
	{
		return "The segment element is used to control caret movement; this is typically used in situations such as fraction elements; in the case of a nested fraction, " +
		"the caret must stop after exiting the child fraction, before leaving the parent fraction; see the fraction test for an example.";
	}


	private static StyleSheet2 styleSheet = StyleSheet2.instance;
	
	
	
	protected Pres text(String t, Color colour)
	{
		return styleSheet.withAttr( Primitive.foreground, colour ).applyTo( new Text( t ) );
	}
	
	protected Pres text(String t)
	{
		return text( t, Color.black );
	}
	
	
	protected Pres segment(Pres x, boolean bGuardBegin, boolean bGuardEnd)
	{
		return new Segment( bGuardBegin, bGuardEnd, x );
	}
	
	
	protected Pres span(Pres... x)
	{
		return new Span( x );
	}
	
	
	protected Pres line(Pres... x)
	{
		return new Paragraph( x );
	}
	
	
	protected Pres createContents()
	{
		ArrayList<Object> children = new ArrayList<Object>();
		
		children.add( line( text( "Bars (|) indicate segment boundaries" ) ) );
		children.add( line( text( "One segment in middle of text |" ), segment( text( "no guards", Color.red ), false, false ), text( "| finish." ) ) );
		children.add( line( text( "One segment in middle of text |" ), segment( text( "begin guard", Color.red ), true, false ), text( "| finish." ) ) );
		children.add( line( text( "One segment in middle of text |" ), segment( text( "end guard", Color.red ), false, true ), text( "| finish." ) ) );
		children.add( line( text( "One segment in middle of text |" ), segment( text( "begin & end guard", Color.red ), true, true ), text( "| finish." ) ) );
		children.add( line( text( "Nested segment in middle outer seg |" ), segment( span( text( "....", Color.red ), segment( text( "both guards", Color.blue ), true, true ), text( "....", Color.red ) ), true, true ), text( "| finish." ) ) );
		children.add( line( text( "Nested segment at beginning outer seg |" ), segment( span( segment( text( "both guards", Color.blue ), true, true ), text( "....", Color.red ) ), true, true ), text( "| finish." ) ) );
		children.add( line( text( "Nested segment at end outer seg |" ), segment( span( text( "....", Color.red ), segment( text( "both guards", Color.blue ), true, true ) ), true, true ), text( "| finish." ) ) );
		
		return new Body( children );
	}
}
