//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocModel;

import BritefuryJ.DocModel.DMObjectField;
import junit.framework.TestCase;

public class Test_DMObjectField extends TestCase
{
	public void test_getName()
	{
		DMObjectField f = new DMObjectField( "hi" );
		assertEquals( f.getName(), "hi" );
	}
}