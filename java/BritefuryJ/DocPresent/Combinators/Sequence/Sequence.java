//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Sequence;

import BritefuryJ.AttributeTable.InheritedAttributeNonNull;

public class Sequence
{
	public static final InheritedAttributeNonNull addLineBreaks = new InheritedAttributeNonNull( "sequence", "addLineBreaks", Boolean.class, true );
	public static final InheritedAttributeNonNull addParagraphIndentMarkers = new InheritedAttributeNonNull( "sequence", "addParagraphIndentMarkers", Boolean.class, true );
	public static final InheritedAttributeNonNull addLineBreakCost = new InheritedAttributeNonNull( "sequence", "addLineBreakCost", Boolean.class, true );
	public static final InheritedAttributeNonNull indentation = new InheritedAttributeNonNull( "sequence", "indentation", Double.class, 30.0 );
}
