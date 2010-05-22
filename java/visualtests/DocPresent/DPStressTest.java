//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package visualtests.DocPresent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPViewport;
import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.PersistentState.PersistentState;
import BritefuryJ.DocPresent.StyleParams.ParagraphStyleParams;
import BritefuryJ.DocPresent.StyleParams.TextStyleParams;

public class DPStressTest
{
	// Memory usage history (10240 lines): (32-bit JVM, measurement is process size)
	// ~480MB: initial
	// 357MB: packed DPContainer.ChildEntry
	// 353MB: packed TextVisual
	// 164MB: shared TextLayout objects
	// 135MB: emptied out DPContainer.ChildEntry
	// 126MB: removed DPContainer.ChildEntry
	// 120MB: shared TextVisual objects
	// 117MB: created pointer->child table in DPContainer on demand
	// 117MB: replaced paragraph ArrayLists with arrays
	// 111MB: replaced Vector2 and Point2 objects in DPElement with doubles
	// 83MB: cached metrics in TextVisual objects, and modified Metrics.scaled() (and subclass) methods to only create a new metrics object in cases scale != 1.0
	
	
	// Version							Platform		# lines	Process size			Measured mem usage		Mem usage at start		Element tree creation time		typeset time
	//
	// [1] Same as with last measurements	A			10240	80.7MB				56.6MB				--					0.839s					0.410s
	// [2] New layout system				A			10240	71.4MB				46.4MB				0.6MB				0.705s					0.294s
	// [3] Split layout into req/alloc			A			10240	57.1MB				32.8MB				0.6MB				0.569s					0.307s
	// [4] With collation					A			10240	58.4MB				36.2MB				0.6MB				0.566s					0.336s
	// [5] With collation finished			A			10240	58.4MB				35.6MB				0.6MB				0.572s					0.335s
	// [6] Element tree functionality			A			10240	65.0MB				41.1MB				0.7MB				0.584s					0.496s
	// [7] Minor optimisations				A			10240	64.4MB				38.7MB				0.9MB				0.600s					0.523s
	// [8] Pointer refactor				A			10240	63.4MB				39.4MB				1.0MB				0.602s					0.321s
	// [9] As of 2010-01-30				B			10240	86.6MB				42.5MB				1.9MB				0.505s					0.332s
	// [10] Layout tree refactor			B			10240	87.5MB				52.4MB				1.9MB				0.530s					0.378s
	//
	//
	// States:
	// [1] No changes from last iteration, just more detailed measurements
	// [2] Replaced hmetrics + vmetrics with a single structure, that also contains fields for position and allocated space
	// [3] Split layout box into requisition and allocation; requisition structures shared among text elements; managed by TextVisual
	// [4] HBoxes, VBoxes and Paragraphs can collate contents from children that are span elements
	// [5] Collation system finished
	// [6] All ElementTree functionality merged into document presentation system
	// [7] Minor optimisations - pointer containment (pointer within bounds of element) moved to root (DPPresentationArea)
	// [8] Pointer refactor - moved all pointer containment state out of the element tree and into the input system
	// [9] As of 2010-01-30; no significant changes, different platform
	// [10] Refactored layout system into separate layout tree
	//
	// Platform A:
	//	CPU: Intel Core Duo 1.86GHz
	//	RAM: 2GB
	//	OS: WinXP 32-bit
	//	JVM: 1.6.0_14 32-bit
	//
	// Platform B:
	//	CPU: Intel Core Duo 1.86GHz
	//	RAM: 2GB
	//	OS: Windows 7 32-bit
	//	JVM: 1.6.0_18 32-bit
	
	private static int NUMLINES = 10240;
	
	
	Font f0 = new Font( "Sans serif", Font.PLAIN, 12 );
	TextStyleParams nameStyle = new TextStyleParams( null, null, null, true, f0, Color.black, null, null, false );
	TextStyleParams puncStyle = new TextStyleParams( null, null, null, true, f0, Color.blue, null, null, false );
	ParagraphStyleParams paraStyle = new ParagraphStyleParams( null, null, null, 0.0, 0.0, 0.0 );

	public DPElement name(String n)
	{
		return new DPText( nameStyle, n );
	}
	
	public DPElement[] attr(DPElement x, String a)
	{
		DPText dot = new DPText( puncStyle, "." );
		DPText attrName = new DPText( nameStyle, a );
		return new DPElement[] { x, dot, attrName };
	}
	
	public DPElement call(DPElement[] x, DPElement... args)
	{
		DPText openParen = new DPText( puncStyle, "(" );
		DPText closeParen = new DPText( puncStyle, ")" );
		ArrayList<DPElement> elems = new ArrayList<DPElement>();
		elems.addAll( Arrays.asList( x ) );
		elems.add( openParen );
		for (int i = 0; i < args.length; i++)
		{
			if ( i > 0 )
			{
				elems.add( new DPText( puncStyle, "," ) );
				elems.add( new DPText( puncStyle, " " ) );
			}
			elems.add( args[i] );
		}
		elems.add( closeParen );
		DPParagraph call = new DPParagraph( paraStyle );
		call.extend( elems );
		return call;
	}
	
	
	
	
	
	
	
	
	protected DPElement createContentNode()
	{
		DPVBox box = new DPVBox( );
		ArrayList<DPElement> children = new ArrayList<DPElement>();
		
		for (int i = 0; i < NUMLINES; i++)
		{
			children.add( call( attr( name( "obj" ), "method" ), name( "a" ), name( "b" ), name( "c" ), name( "d" ), name( "e" ), name( "f" ) ) );
		}
		
		box.setChildren( children );
		
		return box;
	}



	public DPStressTest()
	{
		JFrame frame = new JFrame( "Document presentation system stress test - " + NUMLINES + " lines" );

		//This stops the app on window close.
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		PresentationComponent presentation = new PresentationComponent();
	     
		System.out.println( "Start memory usage = "  + ( Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ) );
	     
	     
		long t1 = System.nanoTime();
		DPElement w = createContentNode();
		long t2 = System.nanoTime();
		System.out.println( "Element tree creation time: " + (double)( t2 - t1 ) / 1000000000.0 );
		DPViewport viewport = new DPViewport( new PersistentState() );
		viewport.setChild( w );
		presentation.setChild( viewport.alignHExpand().alignVExpand() );
	     
	     
	     
		presentation.setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( presentation );
		frame.pack();
		frame.setVisible(true);
	}
	
	
	public static void main(String[] args)
	{
		new DPStressTest();
	}

}
