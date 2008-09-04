package tests.DocPresent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Vector;
import java.util.Arrays;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.DPLineBreak;
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class DPParagraphTest_simple
{
	protected static Vector<DPWidget> makeTexts(String header)
	{
		TextStyleSheet t12 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		TextStyleSheet t18 = new TextStyleSheet( new Font( "Sans serif", Font.BOLD, 18 ), Color.BLACK );
		DPText h = new DPText( t18, header );
		DPText t0 = new DPText( t12, "Hello" );
		DPText t1 = new DPText( t12, "World" );
		DPText t2 = new DPText( t12, "Foo" );
		DPText t3 = new DPText( t12, "j" );
		DPText t4 = new DPText( t12, "q" );
		DPText t5 = new DPText( t12, "'" );
		DPText t6 = new DPText( t12, "." );
		DPText t7 = new DPText( t12, "Bar" );
		
		DPText[] texts = { h, t0, t1, t2, t3, t4, t5, t6, t7 };
		return new Vector<DPWidget>( Arrays.asList( texts ) );
	}
	
	protected static Vector<DPWidget> addLineBreaks(Vector<DPWidget> nodesIn, int step)
	{
		Vector<DPWidget> nodesOut = new Vector<DPWidget>();
		for (int i = 0; i < nodesIn.size(); i++)
		{
			nodesOut.add( nodesIn.get( i ) );
			if ( step <= 1  ||  i % step == (step-1) )
			{
				nodesOut.add( new DPLineBreak() );
			}
		}
		return nodesOut;
	}
	
	public static void main(final String[] args) {
		JFrame frame = new JFrame( "Flow; simple test" );

		//This stops the app on window close.
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		DPPresentationArea area = new DPPresentationArea();
	     
	     
		Vector<DPWidget> c0 = makeTexts( "UNINDENTED" );
		c0 = addLineBreaks( c0, 1 );
		Vector<DPWidget> c1 = makeTexts( "INDENTED" );
		c1 = addLineBreaks( c1, 1 );
		
		ParagraphStyleSheet b0s = new ParagraphStyleSheet( DPParagraph.Alignment.BASELINES, 10.0, 0.0, 0.0 );
		DPParagraph b0 = new DPParagraph( b0s );
		b0.extend( c0 );
		
		ParagraphStyleSheet b1s = new ParagraphStyleSheet( DPParagraph.Alignment.BASELINES, 10.0, 0.0, 30.0 );
		DPParagraph b1 = new DPParagraph( b1s );
		b1.extend( c1 );
		
		VBoxStyleSheet boxS = new VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.EXPAND, 20.0, false, 0.0 );
		DPVBox box = new DPVBox( boxS );
		box.append( b0 );
		box.append( b1 );
	     
	     
		area.setChild( box );
	     
	     
	     
		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
}