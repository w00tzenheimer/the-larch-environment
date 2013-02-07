//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Projection;

import org.python.core.*;


public abstract class SubjectPathEntry extends AbstractSubjectPathEntry
{
	public boolean canPersist()
	{
		return true;
	}


	public abstract PyObject __getstate__();

	public abstract void __setstate__(PyObject state);

	public PyObject __reduce__()
	{
		return new PyTuple( Py.java2py( this ).getType(), new PyTuple(), __getstate__() );
	}
}
