//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocModel.Resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.IdentityHashMap;

import org.python.core.PyObject;

import BritefuryJ.DocModel.DMNode;
import BritefuryJ.DocModel.DMNodeClass;
import BritefuryJ.DocModel.DMPickleHelper;

public class DMJavaResource extends DMResource
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	protected static DMNodeClass javaResourceNodeClass = new DMNodeClass( "DMJavaResource" );

	
	private Object value[] = null;
	
	
	public DMJavaResource()
	{
		super();
	}
	
	public DMJavaResource(Object value)
	{
		try
		{
			this.serialised = serialise( value );
		}
		catch (IOException e)
		{
			throw new RuntimeException( "IOException while creating serialised form: " + e.getMessage() );
		}
	}
	
	private DMJavaResource(String serialised)
	{
		super( serialised );
	}
	
	
	
	public PyObject getPyFactory()
	{
		return DMPickleHelper.getDMJavaResourceFactory();
	}
	
	
	public void become(DMNode node)
	{
		if ( node instanceof DMJavaResource )
		{
			DMJavaResource rsc = (DMJavaResource)node;
			serialised = rsc.getSerialisedForm();
			value = null;
		}
		else
		{
			throw new CannotChangeNodeClassException( node.getClass(), getClass() );
		}
	}


	
	public static DMJavaResource serialisedResource(String serialised)
	{
		return new DMJavaResource( serialised );
	}
	
	
	public Object getValue()
	{
		if ( value == null )
		{
			try
			{
				byte bytes[] = serialised.getBytes( "ISO-8859-1" );
				ByteArrayInputStream inStream = new ByteArrayInputStream( bytes );
				ObjectInputStream objIn = new ObjectInputStream( inStream );
				Object v = objIn.readObject();
				this.value = new Object[] { v };
				this.serialised = null;
			}
			catch (UnsupportedEncodingException e)
			{
				throw new RuntimeException( "Cannot get UTF-8 encoding: " + e.getMessage() );
			}
			catch (IOException e)
			{
				throw new RuntimeException( "IOException while reading from serialised form: " + e.getMessage() );
			}
			catch (ClassNotFoundException e)
			{
				throw new RuntimeException( "Cannot read object; class not found: " + e.getMessage() );
			}
		}
		
		return value[0];
	}
	
	
	
	public String getSerialisedForm()
	{
		try
		{
			return serialise( getValue() );
		}
		catch (IOException e)
		{
			throw new RuntimeException( "IOException while creating serialised form: " + e.getMessage() );
		}
	}
	
	
	public static String serialise(Object x) throws IOException
	{
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ObjectOutputStream objOut = new ObjectOutputStream( outStream );
		objOut.writeObject( x );
		return new String( outStream.toByteArray(), "ISO-8859-1" );
	}
	
	
	public boolean equals(Object x)
	{
		if ( x == this )
		{
			return true;
		}
		else if ( x instanceof DMJavaResource )
		{
			DMJavaResource r = (DMJavaResource)x;
			return getSerialisedForm().equals( r.getSerialisedForm() );
		}
		else
		{
			return false;
		}
	}



	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		String s = serialise( getValue() );
		out.writeUTF( s );
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		serialised = in.readUTF();
		value = null;
	}
	
	
	@Override
	protected Object createDeepCopy(IdentityHashMap<Object, Object> memo)
	{
		if ( serialised != null )
		{
			return new DMJavaResource( serialised );
		}
		else
		{
			return new DMJavaResource( getValue() );
		}
	}


	@Override
	public DMNodeClass getDMNodeClass()
	{
		return javaResourceNodeClass;
	}


	@Override
	public Iterable<Object> getChildren()
	{
		return childrenIterable;
	}
}