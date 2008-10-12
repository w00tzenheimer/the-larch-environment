//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocTree;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocModel.DMIORead;
import BritefuryJ.DocModel.DMList;
import BritefuryJ.DocModel.DMIORead.ParseSXErrorException;
import BritefuryJ.DocTree.DocTree;
import BritefuryJ.DocTree.DocTreeList;
import BritefuryJ.DocTree.DocTreeString;
import junit.framework.TestCase;

public class Test_DocTree extends TestCase
{
	@SuppressWarnings("unchecked")
	private static Object listRGet(List<Object> xs, int[] is)
	{
		Object x = xs;
		for (int i: is)
		{
			List<Object> xx = (List<Object>)x;
			x = xx.get( i );
		}
		return x;
	}
	
	private static DMList[] buildDiamondDoc()
	{
		DMList dd = new DMList( Arrays.asList( new Object[] { "d" } ) );
		DMList dc = new DMList( Arrays.asList( new Object[] { dd } ) );
		DMList db = new DMList( Arrays.asList( new Object[] { dd } ) );
		DMList da = new DMList( Arrays.asList( new Object[] { db, dc } ) );
		return new DMList[]{ da, db, dc, dd };
	}
	
	

	public void testWrapString()
	{
		DocTree tree = new DocTree();
		Object a = new DMList( Arrays.asList( new Object[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", } ) );
		DocTreeList w_a = (DocTreeList)tree.treeNode( a );
		DocTreeString w_5 = (DocTreeString)w_a.get( 5 );
		assertEquals( w_5.getString(), "5" );
		assertSame( w_5.getParentTreeNode(), w_a );
		assertEquals( w_5.getIndexInParent(), 5 );
	}
	

	
	
	
	public void testDiamond()
	{
		DMList[] d = buildDiamondDoc();
		DMList da = d[0];
		
		DMList db = (DMList)da.get( 0 );
		DMList dd = (DMList)db.get( 0 );
		
		assertEquals( dd.get( 0 ), "d" );
		
		DMList dc = (DMList)da.get( 1 );
		
		assertSame( dd, dc.get( 0 ) );
	}
	
	
	public void testDiamondTree()
	{
		DMList[] d = buildDiamondDoc();
		DMList da = d[0];
		DMList db = d[1];
		DMList dc = d[2];
		DMList dd = d[3];
		
		DocTree tree = new DocTree();
		DocTreeList ta = (DocTreeList)tree.treeNode( da );
		DocTreeList tb = (DocTreeList)ta.get( 0 );
		DocTreeList tc = (DocTreeList)ta.get( 1 );
		DocTreeList tbd = (DocTreeList)tb.get( 0 );
		DocTreeList tcd = (DocTreeList)tc.get( 0 );
		
		assertNotSame( tbd, tcd );
		assertSame( tbd.getNode(), tcd.getNode() );
		
		// Modify
		DMList de = new DMList( Arrays.asList( new Object[] { dd } ) );
		dc.set( 0, de );
		
		assertSame( ((DMList)dc.get( 0 )).get( 0 ), dd );
		assertSame( ((DMList)dc.get( 0 )).get( 0 ), db.get( 0 ) );
		
		
		DocTreeList tc2 = (DocTreeList)ta.get( 1 );
		DocTreeList te = (DocTreeList)tc2.get( 0 );
		DocTreeList ted = (DocTreeList)te.get( 0 );
		
		assertNotSame( tbd, ted );
		assertSame( tc2, tc );
		assertSame( ted.getNode(), tbd.getNode() );
	}
	
	
	@SuppressWarnings("unchecked")
	public void testModifyTree() throws ParseSXErrorException
	{
		
		DMList module = new DMList( (List<Object>)DMIORead.readSX( "(module (expr (add (ref a) (ref b))))" ) );
		
		DocTree tree = new DocTree();
		
		DocTreeList tModule = (DocTreeList)tree.treeNode( module );
		
		DocTreeList tExpr = (DocTreeList)tModule.get( 1 );
		DocTreeList tAdd = (DocTreeList)tExpr.get( 1 );
		DocTreeList tRefA = (DocTreeList)tAdd.get( 1 );
		DocTreeList tParent = (DocTreeList)tRefA.getParentTreeNode();
		tParent.set( 1, Arrays.asList( new Object[] { "if", Arrays.asList( new Object[] {} ) } ) );
		DocTreeList tIf = (DocTreeList)tParent.get( 1 );
		((DocTreeList)tIf.get( 1 )).add( 0, Arrays.asList( new Object[] { "unbound" } ) );
		DocTreeList tUnbound = (DocTreeList)((DocTreeList)tIf.get( 1 )).get( 0 );
		
		assertEquals( listRGet( module, new int[] { 1, 1, 1, 1, 0 } ), Arrays.asList( new Object[] { "unbound" } ) );
		assertEquals( module, DMIORead.readSX( "(module (expr (add (if ((unbound))) (ref b))))" ) );
		assertSame( tUnbound.getNode(),  listRGet( module, new int[] { 1, 1, 1, 1, 0 } ) );
		assertSame( listRGet( tModule, new int[] { 1, 1, 1, 1, 0 } ), tUnbound );
		assertSame( tUnbound.getParentTreeNode().getParentTreeNode().getParentTreeNode().getParentTreeNode().getParentTreeNode(), tModule );
		assertEquals( tUnbound.getIndexInParent(), 0 );
	}
}