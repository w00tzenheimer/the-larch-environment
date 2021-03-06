//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package tests.LSpace;

import junit.framework.TestCase;
import BritefuryJ.LSpace.LSBin;
import BritefuryJ.LSpace.LSBlank;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSHiddenText;
import BritefuryJ.LSpace.LSParagraph;
import BritefuryJ.LSpace.LSRow;
import BritefuryJ.LSpace.LSText;
import BritefuryJ.LSpace.RootPresentationComponent;
import BritefuryJ.LSpace.StyleParams.ContainerStyleParams;

public class ElementContentTest extends TestCase
{
	public void testEmpty()
	{
		LSBlank empty = new LSBlank( );
		
		assertEquals( empty.getTextRepresentation(), "" );
		assertEquals( empty.getTextRepresentationLength(), 0 );
	}
	
	public void testText()
	{
		LSText text = new LSText( "Hello" );
		
		assertEquals( text.getTextRepresentation(), "Hello" );
		assertEquals( text.getTextRepresentationLength(), 5 );
	}
	
	
	public void testBin()
	{
		LSText t0 = new LSText( "abc" );

		LSBin b = new LSBin( ContainerStyleParams.defaultStyleParams, t0 );
		
		RootPresentationComponent component = new RootPresentationComponent();
		component.getRootElement().setChild( b );

		assertEquals( b.getTextRepresentation(), "abc" );
		assertEquals( b.getTextRepresentationLength(), 3 );
	}
	
	
	public void testPara()
	{
		LSText t0 = new LSText( "abc" );
		LSText t1 = new LSText( "ghi" );
		LSText t2 = new LSText( "mno" );
		LSText t3 = new LSText( "stu" );
		
		LSRow p = new LSRow( new LSElement[] { t0, t1, t2, t3 } );
		
		RootPresentationComponent component = new RootPresentationComponent();
		component.getRootElement().setChild( p );
	
		assertEquals( p.getTextRepresentation(), "abcghimnostu" );
		assertEquals( p.getTextRepresentationLength(), 12 );
	}
	
	
	
	public void testStructure()
	{
		LSText ta0 = new LSText( "abc" );
		LSText ta1 = new LSText( "ghi" );
		LSText ta2 = new LSText( "mno" );
		LSText ta3 = new LSText( "stu" );

		LSRow pa = new LSRow( new LSElement[] { ta0, ta1, ta2, ta3 } );
		
		LSHiddenText e = new LSHiddenText( );
		

	
		LSText tb0 = new LSText( "vw" );
		LSText tb1 = new LSText( "xy" );
		LSText tb2 = new LSText( "z" );

		LSRow pb = new LSRow( new LSElement[] { tb0, tb1, tb2 } );
		
		LSBin b = new LSBin( ContainerStyleParams.defaultStyleParams, pb );

		
		
		LSText tx = new LSText( "11" );
		LSText ty = new LSText( "22" );
		
		
		LSRow root = new LSRow( new LSElement[] { pa, e, b, tx, ty } );
		
		
		RootPresentationComponent component = new RootPresentationComponent();
		component.getRootElement().setChild( root );

		
		// Test getContent() and getContentLength() of all elements
		assertEquals( ta0.getTextRepresentation(), "abc" );
		assertEquals( ta0.getTextRepresentationLength(), 3 );
		
		assertEquals( pa.getTextRepresentation(), "abcghimnostu" );
		assertEquals( pa.getTextRepresentationLength(), 12 );

		assertEquals( e.getTextRepresentation(), "" );
		assertEquals( e.getTextRepresentationLength(), 0 );

		assertEquals( pb.getTextRepresentation(), "vwxyz" );
		assertEquals( pb.getTextRepresentationLength(), 5 );

		assertEquals( b.getTextRepresentation(), "vwxyz" );
		assertEquals( b.getTextRepresentationLength(), 5 );

		assertEquals( root.getTextRepresentation(), "abcghimnostuvwxyz1122" );
		assertEquals( root.getTextRepresentationLength(), 21 );
		
		
		// Test getContentOffsetOfChild() for containers
		assertEquals( ta0.getTextRepresentationOffsetInSubtree( pa ), 0 );
		assertEquals( ta1.getTextRepresentationOffsetInSubtree( pa ), 3 );
		assertEquals( ta2.getTextRepresentationOffsetInSubtree( pa ), 6 );
		assertEquals( ta3.getTextRepresentationOffsetInSubtree( pa ), 9 );

		assertEquals( pb.getTextRepresentationOffsetInSubtree( b ), 0 );
		
		assertEquals( pa.getTextRepresentationOffsetInSubtree( root ), 0 );
		assertEquals( e.getTextRepresentationOffsetInSubtree( root ), 12 );
		assertEquals( b.getTextRepresentationOffsetInSubtree( root ), 12 );
		assertEquals( tx.getTextRepresentationOffsetInSubtree( root ), 17 );
		assertEquals( ty.getTextRepresentationOffsetInSubtree( root ), 19 );
		
		// Test getChildAtContentPosition() for containers
		LSElement[] getChildAtContentPositionResultsPA = { ta0, ta0, ta0, ta1, ta1, ta1, ta2, ta2, ta2, ta3, ta3, ta3, null };
		for (int i = 0; i < getChildAtContentPositionResultsPA.length; i++)
		{
			System.out.println( i );
			assertSame( pa.getLeafAtTextRepresentationPosition( i ), getChildAtContentPositionResultsPA[i] );
		}
		
		assertEquals( b.getLeafAtTextRepresentationPosition( 0 ), tb0 );
		assertEquals( b.getLeafAtTextRepresentationPosition( 4 ), tb2 );
		assertEquals( b.getLeafAtTextRepresentationPosition( 5 ), null );

		assertEquals( root.getLeafAtTextRepresentationPosition( 0 ), ta0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 1 ), ta0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 11 ),ta3 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 12 ), tb0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 16 ), tb2 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 17 ), tx );
		assertEquals( root.getLeafAtTextRepresentationPosition( 18 ), tx );
		assertEquals( root.getLeafAtTextRepresentationPosition( 19 ), ty );
		assertEquals( root.getLeafAtTextRepresentationPosition( 20 ), ty );
		assertEquals( root.getLeafAtTextRepresentationPosition( 21 ), null );

		
		// Test getLeafAtContentPosition() for elements
		assertSame( ta0.getLeafAtTextRepresentationPosition( 0 ), ta0 );
		
		assertNull( e.getLeafAtTextRepresentationPosition( 0 ) );
		
		for (int i = 0; i < getChildAtContentPositionResultsPA.length; i++)
		{
			assertSame( pa.getLeafAtTextRepresentationPosition( i ), getChildAtContentPositionResultsPA[i] );
		}

		assertEquals( root.getLeafAtTextRepresentationPosition( 0 ), ta0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 1 ), ta0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 2 ), ta0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 3 ), ta1 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 11 ), ta3 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 12 ), tb0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 13 ), tb0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 14 ), tb1 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 15 ), tb1 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 16 ), tb2 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 17 ), tx );
		assertEquals( root.getLeafAtTextRepresentationPosition( 18 ), tx );
		assertEquals( root.getLeafAtTextRepresentationPosition( 19 ), ty );
		assertEquals( root.getLeafAtTextRepresentationPosition( 20 ), ty );
		assertEquals( root.getLeafAtTextRepresentationPosition( 21 ), null );
		
		assertEquals( b.getLeafAtTextRepresentationPosition( 0 ), tb0 );
		assertEquals( b.getLeafAtTextRepresentationPosition( 1 ), tb0 );
		assertEquals( b.getLeafAtTextRepresentationPosition( 2 ), tb1 );
		assertEquals( b.getLeafAtTextRepresentationPosition( 3 ), tb1 );
		assertEquals( b.getLeafAtTextRepresentationPosition( 4 ), tb2 );
		
		
		// Test getContentOffsetInSubtree() for elements
		assertEquals( ta0.getTextRepresentationOffsetInSubtree( pa ), 0 );
		assertEquals( ta3.getTextRepresentationOffsetInSubtree( pa ), 9 );
		assertEquals( ta0.getTextRepresentationOffsetInSubtree( root ), 0 );
		assertEquals( ta3.getTextRepresentationOffsetInSubtree( root ), 9 );
		assertEquals( tb0.getTextRepresentationOffsetInSubtree( pb ), 0 );
		assertEquals( tb2.getTextRepresentationOffsetInSubtree( pb ), 4 );
		assertEquals( tb0.getTextRepresentationOffsetInSubtree( b ), 0 );
		assertEquals( tb2.getTextRepresentationOffsetInSubtree( b ), 4 );
		assertEquals( tb0.getTextRepresentationOffsetInSubtree( root ), 12 );
		assertEquals( tb2.getTextRepresentationOffsetInSubtree( root ), 16 );
	}



	public void testParagraphStructure()
	{
		LSText ta0 = new LSText( "abc" );
		LSText ta1 = new LSText( "ghi" );
		LSText ta2 = new LSText( "mno" );
		LSText ta3 = new LSText( "stu" );

		LSParagraph pa = new LSParagraph( new LSElement[] { ta0, ta1, ta2, ta3 } );
		
		LSHiddenText e = new LSHiddenText( );
		

	
		LSText tb0 = new LSText( "vw" );
		LSText tb1 = new LSText( "xy" );
		LSText tb2 = new LSText( "z" );

		LSParagraph pb = new LSParagraph( new LSElement[] { tb0, tb1, tb2 } );
		
		
		LSText tx = new LSText( "11" );
		LSText ty = new LSText( "22" );
		
		
		LSParagraph root = new LSParagraph( new LSElement[] { pa, e, pb, tx, ty } );
		
		
		RootPresentationComponent component = new RootPresentationComponent();
		component.getRootElement().setChild( root );

		
		assertEquals( root.getTextRepresentation(), "abcghimnostuvwxyz1122" );
		assertEquals( root.getTextRepresentationLength(), 21 );

		
		// Test getContent() and getContentLength() of all elements
		assertEquals( ta0.getTextRepresentation(), "abc" );
		assertEquals( ta0.getTextRepresentationLength(), 3 );
		
		assertEquals( pa.getTextRepresentation(), "abcghimnostu" );
		assertEquals( pa.getTextRepresentationLength(), 12 );

		assertEquals( e.getTextRepresentation(), "" );
		assertEquals( e.getTextRepresentationLength(), 0 );

		assertEquals( pb.getTextRepresentation(), "vwxyz" );
		assertEquals( pb.getTextRepresentationLength(), 5 );

		assertEquals( root.getTextRepresentation(), "abcghimnostuvwxyz1122" );
		assertEquals( root.getTextRepresentationLength(), 21 );
		
		
		// Test getContentOffsetOfChild() for containers
		assertEquals( ta0.getTextRepresentationOffsetInSubtree( pa ), 0 );
		assertEquals( ta1.getTextRepresentationOffsetInSubtree( pa ), 3 );
		assertEquals( ta2.getTextRepresentationOffsetInSubtree( pa ), 6 );
		assertEquals( ta3.getTextRepresentationOffsetInSubtree( pa ), 9 );

		assertEquals( pa.getTextRepresentationOffsetInSubtree( root ), 0 );
		assertEquals( e.getTextRepresentationOffsetInSubtree( root ), 12 );
		assertEquals( tx.getTextRepresentationOffsetInSubtree( root ), 17 );
		assertEquals( ty.getTextRepresentationOffsetInSubtree( root ), 19 );

		// Test getChildAtContentPosition() for containers
		LSElement[] getChildAtContentPositionResultsPA = { ta0, ta0, ta0, ta1, ta1, ta1, ta2, ta2, ta2, ta3, ta3, ta3, null };
		for (int i = 0; i < getChildAtContentPositionResultsPA.length; i++)
		{
			assertSame( pa.getLeafAtTextRepresentationPosition( i ), getChildAtContentPositionResultsPA[i] );
		}
		
		assertEquals( root.getLeafAtTextRepresentationPosition( 0 ), ta0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 1 ), ta0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 11 ), ta3 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 17 ), tx );
		assertEquals( root.getLeafAtTextRepresentationPosition( 18 ), tx );
		assertEquals( root.getLeafAtTextRepresentationPosition( 19 ), ty );
		assertEquals( root.getLeafAtTextRepresentationPosition( 20 ), ty );
		assertEquals( root.getLeafAtTextRepresentationPosition( 21 ), null );

		
		// Test getLeafAtContentPosition() for elements
		assertSame( ta0.getLeafAtTextRepresentationPosition( 0 ), ta0 );
		
		assertNull( e.getLeafAtTextRepresentationPosition( 0 ) );
		
		for (int i = 0; i < getChildAtContentPositionResultsPA.length; i++)
		{
			assertSame( pa.getLeafAtTextRepresentationPosition( i ), getChildAtContentPositionResultsPA[i] );
		}

		assertEquals( root.getLeafAtTextRepresentationPosition( 0 ), ta0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 1 ), ta0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 2 ), ta0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 3 ), ta1 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 11 ), ta3 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 12 ), tb0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 13 ), tb0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 14 ), tb1 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 15 ), tb1 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 16 ), tb2 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 17 ), tx );
		assertEquals( root.getLeafAtTextRepresentationPosition( 18 ), tx );
		assertEquals( root.getLeafAtTextRepresentationPosition( 19 ), ty );
		assertEquals( root.getLeafAtTextRepresentationPosition( 20 ), ty );
		assertEquals( root.getLeafAtTextRepresentationPosition( 21 ), null );
		
		
		
		// Test getContentOffsetInSubtree() for elements
		assertEquals( ta0.getTextRepresentationOffsetInSubtree( pa ), 0 );
		assertEquals( ta3.getTextRepresentationOffsetInSubtree( pa ), 9 );
		assertEquals( ta0.getTextRepresentationOffsetInSubtree( root ), 0 );
		assertEquals( ta3.getTextRepresentationOffsetInSubtree( root ), 9 );
		assertEquals( tb0.getTextRepresentationOffsetInSubtree( pb ), 0 );
		assertEquals( tb2.getTextRepresentationOffsetInSubtree( pb ), 4 );
		assertEquals( tb0.getTextRepresentationOffsetInSubtree( root ), 12 );
		assertEquals( tb2.getTextRepresentationOffsetInSubtree( root ), 16 );
	}
}
