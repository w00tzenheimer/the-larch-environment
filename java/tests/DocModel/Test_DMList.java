//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package tests.DocModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.python.core.Py;
import org.python.core.PyInteger;
import org.python.core.PySlice;

import BritefuryJ.ChangeHistory.ChangeHistory;
import BritefuryJ.DocModel.DMIOReader;
import BritefuryJ.DocModel.DMIOReader.ParseErrorException;
import BritefuryJ.DocModel.DMList;
import BritefuryJ.DocModel.DMNode;
import BritefuryJ.Live.LiveFunction;

public class Test_DMList extends TestCase
{
	ChangeHistory history;
	
	public void setUp()
	{
		history = new ChangeHistory();
	}
	
	public void tearDown()
	{
		history = null;
	}
	
	
	
	public void cmpListSX(List<Object> xs, String expectedSX)
	{
		try
		{
			Object expected = DMIOReader.readFromString( expectedSX );
			assertEquals( expected, xs );
		}
		catch (ParseErrorException e)
		{
			System.out.println( "Could not parse expected SX" );
			fail();
		}
	}
	
	@SuppressWarnings("unchecked")
	public DMList readDMListSX(String sx)
	{
		try
		{
			List<Object> xs = (List<Object>)DMIOReader.readFromString( sx );
			return new DMList( xs );
		}
		catch (ParseErrorException e)
		{
			System.out.println( "Could not parse" );
			fail();
			return null;
		}
	}
	
	
	List<Object> iterToList(Iterator<Object> iter)
	{
		ArrayList<Object> l = new ArrayList<Object>();
		
		while ( iter.hasNext() )
		{
			l.add( iter.next() );
		}
		return l;
	}
	
	
	public DMList readTrackedDMListSX(String sx)
	{
		DMList xs = readDMListSX( sx );
		history.track( xs );
		return xs;
	}
	
	
	public void assertTracked(DMList xs)
	{
		assertSame( xs.getChangeHistory(), history );
	}
	
	public void assertNotTracked(DMList xs)
	{
		assertNull( xs.getChangeHistory() );
	}
	
	
	
	public void testConstructor()
	{
		DMList xs = readDMListSX( "[a b c `null`]" );
		cmpListSX( xs, "[a b c `null`]" );
		
		assertEquals( xs.get( 0 ), "a" );
		assertEquals( xs.get( 1 ), "b" );
		assertEquals( xs.get( 2 ), "c" );


		DMList ys = readDMListSX( "[a b c [x y z]]" );
		cmpListSX( ys, "[a b c [x y z]]" );
	}
	
	public void testCell()
	{
		final DMList xs = readDMListSX( "[a b c]" );
		
		LiveFunction.Function eval = new LiveFunction.Function()
		{
			public Object evaluate()
			{
				return xs.size();
			}
		};
		LiveFunction yCell = new LiveFunction();
		yCell.setFunction( eval );
		
		assertEquals( yCell.getValue(), 3 );
		
		xs.add( "xyz" );

		assertEquals( yCell.getValue(), 4 );
	}
	
	
	
	public void testAdd()
	{
		DMList xs = readTrackedDMListSX( "[a b c]" );
		DMList ys = readDMListSX( "[x y z]" );
		assertTracked( xs );
		assertNotTracked( ys );
		
		xs.realiseAsRoot();
		
		xs.add( "xyz" );
		cmpListSX( xs, "[a b c xyz]" );
		xs.add( null );
		cmpListSX( xs, "[a b c xyz `null`]" );
		assertSame( ys.getParent(), null );
		xs.add( ys );
		cmpListSX( xs, "[a b c xyz `null` [x y z]]" );
		assertSame( ys.getParent(), xs );
		assertTracked( xs );
		assertTracked( ys );
		
		history.undo();
		cmpListSX( xs, "[a b c xyz `null`]" );
		assertSame( ys.getParent(), null );
		assertTracked( xs );
		assertNotTracked( ys );
		history.undo();
		cmpListSX( xs, "[a b c xyz]" );
		history.undo();
		cmpListSX( xs, "[a b c]" );
		history.redo();
		cmpListSX( xs, "[a b c xyz]" );
		history.redo();
		cmpListSX( xs, "[a b c xyz `null`]" );
		history.redo();
		cmpListSX( xs, "[a b c xyz `null` [x y z]]" );
		assertSame( ys.getParent(), xs );
		assertTracked( xs );
		assertTracked( ys );
	}



	public void testAdd_insert()
	{
		DMList xs = readTrackedDMListSX( "[a b c]" );
		DMList ys = readDMListSX( "[x y z]" );
		assertTracked( xs );
		assertNotTracked( ys );
		
		xs.realiseAsRoot();
		
		xs.add( 2, "xyz" );
		cmpListSX( xs, "[a b xyz c]" );
		xs.add( 2, null );
		cmpListSX( xs, "[a b `null` xyz c]" );
		assertSame( ys.getParent(), null );
		xs.add( 2, ys );
		cmpListSX( xs, "[a b [x y z] `null` xyz c]" );
		assertSame( ys.getParent(), xs );
		assertTracked( xs );
		assertTracked( ys );
		
		history.undo();
		cmpListSX( xs, "[a b `null` xyz c]" );
		assertSame( ys.getParent(), null );
		assertTracked( xs );
		assertNotTracked( ys );
		history.undo();
		cmpListSX( xs, "[a b xyz c]" );
		history.undo();
		cmpListSX( xs, "[a b c]" );
		history.redo();
		cmpListSX( xs, "[a b xyz c]" );
		history.redo();
		cmpListSX( xs, "[a b `null` xyz c]" );
		history.redo();
		cmpListSX( xs, "[a b [x y z] `null` xyz c]" );
		assertSame( ys.getParent(), xs );
		assertTracked( xs );
		assertTracked( ys );
	}



	public void testAddAll()
	{
		DMList xs = readTrackedDMListSX( "[a b c]" );
		DMList ys = readDMListSX( "[x y z `null`]" );
		DMList zs = readDMListSX( "[i j [k l] m n]" );
		assertTracked( xs );
		assertNotTracked( ys );
		assertNotTracked( zs );

		xs.realiseAsRoot();
		
		xs.addAll( ys );
		cmpListSX( xs, "[a b c x y z `null`]" );
		assertSame( ((DMNode)zs.get( 2 )).getParent(), null );
		assertTracked( xs );
		assertNotTracked( ys );
		assertNotTracked( zs );
		xs.addAll( zs );
		cmpListSX( xs, "[a b c x y z `null` i j [k l] m n]" );
		assertSame( ((DMNode)zs.get( 2 )).getParent(), xs );
		assertTracked( xs );
		assertNotTracked( ys );
		assertNotTracked( zs );
		
		history.undo();
		cmpListSX( xs, "[a b c x y z `null`]" );
		assertSame( ((DMNode)zs.get( 2 )).getParent(), null );
		assertTracked( xs );
		assertNotTracked( ys );
		assertNotTracked( zs );
		history.undo();
		cmpListSX( xs, "[a b c]" );
		assertTracked( xs );
		assertNotTracked( ys );
		assertNotTracked( zs );
		history.redo();
		cmpListSX( xs, "[a b c x y z `null`]" );
		assertTracked( xs );
		assertNotTracked( ys );
		assertNotTracked( zs );
		history.redo();
		cmpListSX( xs, "[a b c x y z `null` i j [k l] m n]" );
		assertSame( ((DMNode)zs.get( 2 )).getParent(), xs );
		assertTracked( xs );
		assertNotTracked( ys );
		assertNotTracked( zs );
	}


	public void testAddAll_insert()
	{
		DMList xs = readTrackedDMListSX( "[a b c]" );
		DMList ys = readDMListSX( "[x y z `null`]" );
		DMList zs = readDMListSX( "[i j [k l] m n]" );
		assertTracked( xs );
		assertNotTracked( ys );
		assertNotTracked( zs );
		
		xs.realiseAsRoot();
		
		xs.addAll( 2, ys );
		cmpListSX( xs, "[a b x y z `null` c]" );
		assertSame( ((DMNode)zs.get( 2 )).getParent(), null );
		assertTracked( xs );
		assertNotTracked( ys );
		assertNotTracked( zs );
		xs.addAll( 2, zs );
		cmpListSX( xs, "[a b i j [k l] m n x y z `null` c]" );
		assertSame( ((DMNode)zs.get( 2 )).getParent(), xs );
		assertTracked( xs );
		assertNotTracked( ys );
		assertNotTracked( zs );
		
		history.undo();
		cmpListSX( xs, "[a b x y z `null` c]" );
		assertSame( ((DMNode)zs.get( 2 )).getParent(), null );
		assertTracked( xs );
		assertNotTracked( ys );
		assertNotTracked( zs );
		history.undo();
		cmpListSX( xs, "[a b c]" );
		assertTracked( xs );
		assertNotTracked( ys );
		assertNotTracked( zs );
		history.redo();
		cmpListSX( xs, "[a b x y z `null` c]" );
		assertTracked( xs );
		assertNotTracked( ys );
		assertNotTracked( zs );
		history.redo();
		cmpListSX( xs, "[a b i j [k l] m n x y z `null` c]" );
		assertSame( ((DMNode)zs.get( 2 )).getParent(), xs );
		assertTracked( xs );
		assertNotTracked( ys );
		assertNotTracked( zs );
	}


	public void testClear()
	{
		DMList xs = readTrackedDMListSX( "[a b c [x y]]" );
		DMNode zs = (DMNode)xs.get( 3 );
		xs.realiseAsRoot();
		
		assertSame( zs.getParent(), xs );
		assertTracked( xs );
		assertTracked( (DMList)zs );
		
		xs.clear();
		cmpListSX( xs, "[]" );
		assertSame( zs.getParent(), null );
		assertTracked( xs );
		assertNotTracked( (DMList)zs );
		
		history.undo();
		cmpListSX( xs, "[a b c [x y]]" );
		assertSame( zs.getParent(), xs );
		assertTracked( xs );
		assertTracked( (DMList)zs );
		history.redo();
		cmpListSX( xs, "[]" );
		assertSame( zs.getParent(), null );
		assertTracked( xs );
		assertNotTracked( (DMList)zs );
	}



	public void testContains()
	{
		DMList xs = readDMListSX( "[a b c `null`]" );
		assertTrue( xs.contains( "a" ) );
		assertTrue( xs.contains( "b" ) );
		assertTrue( xs.contains( "c" ) );
		assertTrue( xs.contains( null ) );
		assertFalse( xs.contains( "d" ) );
	}


	public void testContainsAll()
	{
		DMList xs = readDMListSX( "[a b c `null`]" );
		DMList ys = readDMListSX( "[c a]" );
		DMList zs = readDMListSX( "[c d]" );
		DMList zzs = readDMListSX( "[c `null`]" );
		assertTrue( xs.containsAll( ys ) );
		assertTrue( xs.containsAll( zzs ) );
		assertFalse( xs.containsAll( zs ) );
	}



	public void testEquals()
	{
		DMList xs = readDMListSX( "[a b c `null`]" );
		DMList ys = readDMListSX( "[a b c `null`]" );
		assertTrue( xs.equals( ys ) );
	}



	public void testGet()
	{
		DMList xs = readDMListSX( "[a b c `null`]" );
		assertEquals( xs.get( 0 ), "a" );
		assertEquals( xs.get( 1 ), "b" );
		assertEquals( xs.get( 2 ), "c" );
		assertEquals( xs.get( 3 ), null );
	}



	public void testIndexOf()
	{
		DMList xs = readDMListSX( "[a b c `null`]" );
		assertEquals( xs.indexOf( "a" ), 0 );
		assertEquals( xs.indexOf( "b" ), 1 );
		assertEquals( xs.indexOf( "c" ), 2 );
		assertEquals( xs.indexOf( null ), 3 );
	}


	public void testIsEmpty()
	{
		assertFalse( readDMListSX( "[a b c]" ).isEmpty() );
		assertTrue( readDMListSX( "[]" ).isEmpty() );
	}
	
	
	public void testIterator()
	{
		DMList xs = readDMListSX( "[a b c `null`]" );
		assertEquals( xs, iterToList( xs.iterator() ) );
	}


	public void testLastIndexOf()
	{
		DMList xs = readDMListSX( "[a b c `null` a b c `null`]" );
		assertEquals( xs.lastIndexOf( "a" ), 4 );
		assertEquals( xs.lastIndexOf( "b" ), 5 );
		assertEquals( xs.lastIndexOf( "c" ), 6 );
		assertEquals( xs.lastIndexOf( null ), 7 );
	}


	public void testListIterator()
	{
		DMList xs = readDMListSX( "[a b c `null`]" );
		assertEquals( xs, iterToList( xs.listIterator() ) );
	}


	public void testListIteratorFrom()
	{
		DMList xs = readDMListSX( "[a b c `null`]" );
		cmpListSX( iterToList( xs.listIterator( 0 ) ), "[a b c `null`]" );
		cmpListSX( iterToList( xs.listIterator( 1 ) ), "[b c `null`]" );
		cmpListSX( iterToList( xs.listIterator( 2 ) ), "[c `null`]" );
		cmpListSX( iterToList( xs.listIterator( 3 ) ), "[`null`]" );
		cmpListSX( iterToList( xs.listIterator( 4 ) ), "[]" );
	}


	public void testRemove()
	{
		DMList xs = readTrackedDMListSX( "[a b c `null` [x y]]" );
		DMNode zs = (DMNode)xs.get( 4 );
		assertTracked( xs );
		assertTracked( (DMList)zs );
		
		xs.realiseAsRoot();
		
		assertSame( zs.getParent(), xs );
		xs.remove( 1 );
		cmpListSX( xs, "[a c `null` [x y]]" );
		assertSame( zs.getParent(), xs );
		xs.remove( 3 );
		cmpListSX( xs, "[a c `null`]" );
		assertSame( zs.getParent(), null );
		assertTracked( xs );
		assertNotTracked( (DMList)zs );
		
		history.undo();
		cmpListSX( xs, "[a c `null` [x y]]" );
		assertSame( zs.getParent(), xs );
		assertTracked( xs );
		assertTracked( (DMList)zs );
		history.undo();
		cmpListSX( xs, "[a b c `null` [x y]]" );
		assertSame( zs.getParent(), xs );
		history.redo();
		cmpListSX( xs, "[a c `null` [x y]]" );
		assertSame( zs.getParent(), xs );
		history.redo();
		cmpListSX( xs, "[a c `null`]" );
		assertSame( zs.getParent(), null );
		assertTracked( xs );
		assertNotTracked( (DMList)zs );
	}

	public void testRemoveObject()
	{
		DMList xs = readTrackedDMListSX( "[a b c `null` [x y]]" );
		DMNode zs = (DMNode)xs.get( 4 );
		assertTracked( xs );
		assertTracked( (DMList)zs );
		
		xs.realiseAsRoot();
		
		assertSame( zs.getParent(), xs );
		xs.remove( "b" );
		cmpListSX( xs, "[a c `null` [x y]]" );
		assertSame( zs.getParent(), xs );
		xs.remove( null );
		cmpListSX( xs, "[a c [x y]]" );
		assertSame( zs.getParent(), xs );
		xs.remove( zs );
		cmpListSX( xs, "[a c]" );
		assertSame( zs.getParent(), null );
		assertTracked( xs );
		assertNotTracked( (DMList)zs );
		
		history.undo();
		cmpListSX( xs, "[a c [x y]]" );
		assertSame( zs.getParent(), xs );
		assertTracked( xs );
		assertTracked( (DMList)zs );
		history.undo();
		cmpListSX( xs, "[a c `null` [x y]]" );
		assertSame( zs.getParent(), xs );
		history.undo();
		cmpListSX( xs, "[a b c `null` [x y]]" );
		assertSame( zs.getParent(), xs );
		history.redo();
		cmpListSX( xs, "[a c `null` [x y]]" );
		assertSame( zs.getParent(), xs );
		history.redo();
		cmpListSX( xs, "[a c [x y]]" );
		assertSame( zs.getParent(), xs );
		history.redo();
		cmpListSX( xs, "[a c]" );
		assertSame( zs.getParent(), null );
		assertTracked( xs );
		assertNotTracked( (DMList)zs );
	}


	public void testSet()
	{
		DMList xs = readTrackedDMListSX( "[a [i j] c]" );
		DMNode ij = (DMNode)xs.get( 1 );
		DMList pq = readDMListSX( "[p q]" );
		
		xs.realiseAsRoot();

		cmpListSX( xs, "[a [i j] c]" );
		assertSame( ij.getParent(), xs );
		assertSame( pq.getParent(), null );
		assertTracked( xs );
		assertTracked( (DMList)ij );
		assertNotTracked( pq );

		xs.set( 1, null );
		cmpListSX( xs, "[a `null` c]" );
		assertSame( ij.getParent(), null );
		assertSame( pq.getParent(), null );
		assertTracked( xs );
		assertNotTracked( (DMList)ij );
		assertNotTracked( pq );
		
		xs.set( 1, pq );
		cmpListSX( xs, "[a [p q] c]" );
		assertSame( ij.getParent(), null );
		assertSame( pq.getParent(), xs );
		assertTracked( xs );
		assertNotTracked( (DMList)ij );
		assertTracked( pq );
		
		
		history.undo();
		cmpListSX( xs, "[a `null` c]" );
		assertSame( ij.getParent(), null );
		assertSame( pq.getParent(), null );
		assertTracked( xs );
		assertNotTracked( (DMList)ij );
		assertNotTracked( pq );
		
		history.undo();
		cmpListSX( xs, "[a [i j] c]" );
		assertSame( ij.getParent(), xs );
		assertSame( pq.getParent(), null );
		assertTracked( xs );
		assertTracked( (DMList)ij );
		assertNotTracked( pq );
		
		history.redo();
		cmpListSX( xs, "[a `null` c]" );
		assertSame( ij.getParent(), null );
		assertSame( pq.getParent(), null );
		assertTracked( xs );
		assertNotTracked( (DMList)ij );
		assertNotTracked( pq );
		
		history.redo();
		cmpListSX( xs, "[a [p q] c]" );
		assertSame( ij.getParent(), null );
		assertSame( pq.getParent(), xs );
		assertTracked( xs );
		assertNotTracked( (DMList)ij );
		assertTracked( pq );
	}


	public void test__setitem__slice()
	{
		DMList xs = readTrackedDMListSX( "[a b [i j] c]" );
		DMNode ij = (DMNode)xs.get( 2 );
		DMList pq = readDMListSX( "[[p] q]" );
		DMList p = (DMList)pq.get( 0 );
		
		xs.realiseAsRoot();
		
		cmpListSX( xs, "[a b [i j] c]" );
		assertSame( ij.getParent(), xs );
		assertSame( pq.getParent(), null );
		assertTracked( xs );
		assertTracked( (DMList)ij );
		assertNotTracked( pq );
		assertNotTracked( p );

		xs.__setitem__( new PySlice( new PyInteger( 1 ), new PyInteger( -1 ), Py.None ), Arrays.asList( new Object[] { null } ) );
		cmpListSX( xs, "[a `null` c]" );
		assertSame( ij.getParent(), null );
		assertSame( pq.getParent(), null );
		assertTracked( xs );
		assertNotTracked( (DMList)ij );
		assertNotTracked( pq );
		assertNotTracked( p );
		
		xs.__setitem__( new PySlice( new PyInteger( 1 ), new PyInteger( -1 ), Py.None ), Arrays.asList( new Object[] { pq } ) );
		cmpListSX( xs, "[a [[p] q] c]" );
		assertSame( ij.getParent(), null );
		assertSame( pq.getParent(), xs );
		assertTracked( xs );
		assertNotTracked( (DMList)ij );
		assertTracked( pq );
		assertTracked( p );

		
		history.undo();
		cmpListSX( xs, "[a `null` c]" );
		assertSame( ij.getParent(), null );
		assertSame( pq.getParent(), null );
		assertTracked( xs );
		assertNotTracked( (DMList)ij );
		assertNotTracked( pq );
		assertNotTracked( p );
		
		history.undo();
		cmpListSX( xs, "[a b [i j] c]" );
		assertSame( ij.getParent(), xs );
		assertSame( pq.getParent(), null );
		assertTracked( xs );
		assertTracked( (DMList)ij );
		assertNotTracked( pq );
		assertNotTracked( p );
		
		history.redo();
		cmpListSX( xs, "[a `null` c]" );
		assertSame( ij.getParent(), null );
		assertSame( pq.getParent(), null );
		assertTracked( xs );
		assertNotTracked( (DMList)ij );
		assertNotTracked( pq );
		assertNotTracked( p );
		
		history.redo();
		cmpListSX( xs, "[a [[p] q] c]" );
		assertSame( ij.getParent(), null );
		assertSame( pq.getParent(), xs );
		assertTracked( xs );
		assertNotTracked( (DMList)ij );
		assertTracked( pq );
		assertTracked( p );
	}


	public void testSize()
	{
		assertEquals( readDMListSX( "[a b c]" ).size(), 3 );
	}
	
	
	public void testSublist()
	{
		DMList xs = readDMListSX( "[a b c d e f]" );
		cmpListSX( xs.subList( 1, 5 ), "[b c d e]" );
	}
	
	
	public void testGetChildren()
	{
		DMList xs = readDMListSX( "[a b c d e f]" );
		List<Object> ys = new ArrayList<Object>();
		for (Object x: xs.getChildren())
		{
			ys.add( x );
		}
		cmpListSX( ys, "[a b c d e f]" );
	}
	
	
	public void testToArray()
	{
		String[] strs = { "a", "b", "c" };
		assertEquals( Arrays.asList( readDMListSX( "[a b c]" ).toArray() ), Arrays.asList( strs ) );

		String[] a = { "a", "b", "c" };
		assertEquals( Arrays.asList( readDMListSX( "[a b c]" ).toArray( a ) ), Arrays.asList( strs ) );
	}
	
	
	
	public void test_trackTree()
	{
		DMList xs = readTrackedDMListSX( "[a b [c d [e f]]]" );
		cmpListSX( xs, "[a b [c d [e f]]]" );

		((DMList)((DMList)xs.get( 2 )).get( 2 )).set( 0, "x" );
		cmpListSX( xs, "[a b [c d [x f]]]" );
		
		history.undo();
		cmpListSX( xs, "[a b [c d [e f]]]" );

		history.redo();
		cmpListSX( xs, "[a b [c d [x f]]]" );
		
		
		DMList ys = readDMListSX( "[p q [s t]]" );

		((DMList)((DMList)xs.get( 2 )).get( 2 )).set( 0, ys );
		cmpListSX( xs, "[a b [c d [[p q [s t]] f]]]" );

		((DMList)((DMList)((DMList)((DMList)xs.get( 2 )).get( 2 )).get( 0 )).get( 2 )).set( 1, "h" );
		cmpListSX( xs, "[a b [c d [[p q [s h]] f]]]" );

		history.undo();
		cmpListSX( xs, "[a b [c d [[p q [s t]] f]]]" );

		history.undo();
		cmpListSX( xs, "[a b [c d [x f]]]" );

		history.redo();
		cmpListSX( xs, "[a b [c d [[p q [s t]] f]]]" );

		history.redo();
		cmpListSX( xs, "[a b [c d [[p q [s h]] f]]]" );
	}
	
}
