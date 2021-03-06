//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package tests.Util.Coroutine;

import junit.framework.TestCase;
import BritefuryJ.Util.Coroutine.Coroutine;

public class Test_Coroutine_1 extends TestCase
{
	private StringBuilder b;
	private Coroutine coA, coB, coC;
	
	
	public void setUp()
	{
		Runnable rA = new Runnable()
		{
			@Override
			public void run()
			{
				b.append( "A" );
				
				coB.yieldTo();
				
				b.append( "E" );
				
				coC.yieldTo();
				
				b.append( "G" );
			}
		};

		Runnable rB = new Runnable()
		{
			@Override
			public void run()
			{
				b.append( "B" );
				
				coC.yieldTo();
				
				b.append( "D" );
				
				coA.yieldTo();
			}
		};

		Runnable rC = new Runnable()
		{
			@Override
			public void run()
			{
				b.append( "C" );
				
				coB.yieldTo();
				
				b.append( "F" );
				
				coA.yieldTo();
			}
		};
		
		b = new StringBuilder();
		coA = new Coroutine( rA, "A" );
		coB = new Coroutine( rB, "B" );
		coC = new Coroutine( rC, "C" );
	}
	
	public void tearDown()
	{
		b = null;
		coA = coB = coC = null;
	}
	
	
	
	public void test_nothing()
	{
		assertEquals( "", b.toString() );
		assertFalse( coA.hasStarted() );
		assertFalse( coB.hasStarted() );
		assertFalse( coC.hasStarted() );
	}

	public void test_startAtA()
	{
		coA.yieldTo();
		assertEquals( "ABCDEFG", b.toString() );
		assertTrue( coA.hasStarted() );
		assertTrue( coB.hasStarted() );
		assertTrue( coC.hasStarted() );
		assertTrue( coA.isFinished() );
		assertFalse( coB.isFinished() );
		assertFalse( coC.isFinished() );
		
		coB.yieldTo();

		assertTrue( coA.isFinished() );
		assertTrue( coB.isFinished() );
		assertFalse( coC.isFinished() );
		
		coC.yieldTo();

		assertTrue( coA.isFinished() );
		assertTrue( coB.isFinished() );
		assertTrue( coC.isFinished() );
	}
}
