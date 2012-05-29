//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package Main;

import java.net.MalformedURLException;
import java.net.URL;

import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.__builtin__;
import org.python.core.imp;
import org.python.util.PythonInterpreter;

public class Main
{
	public static void main(String[] args) throws PyException
	{
		PythonInterpreter interp = new PythonInterpreter();
		URL startURL = Main.class.getResource( "Main.class" );
		String larchClassPath = null;
		
		PyObject app_startup_mod = imp.importName( "Britefury.app_startup", false );
		
		if ( startURL.getProtocol().equals( "file" ) )
		{
			String path = startURL.getPath();
			int lastSep = path.lastIndexOf( '/');
			path = path.substring( 0, lastSep );
			lastSep = path.lastIndexOf( '/' );
			larchClassPath = path.substring( 0, lastSep );
			
			PyObject appStartupFromFileSystemFn = __builtin__.getattr( app_startup_mod, Py.newString( "appStartupFromFileSystem" ) );
			appStartupFromFileSystemFn.__call__( Py.newString( larchClassPath ) );
		}
		else if ( startURL.getProtocol().equals( "jar" ) )
		{
			String fullPath = startURL.getPath();
			int exclamation = fullPath.indexOf( '!' );
			fullPath = fullPath.substring( 0, exclamation );
			URL jarURL = null;
			try
			{
				jarURL = new URL( fullPath );
			}
			catch (MalformedURLException e)
			{
				System.err.println( "Bad JAR URL" );
				System.exit( -1 );
			}

			PyObject appStartupFromJarFn = __builtin__.getattr( app_startup_mod, Py.newString( "appStartupFromJar" ) );
			appStartupFromJarFn.__call__( Py.java2py( jarURL ) );
		}
		
		
		// Start Larch
		interp.exec( "from Britefury.app_larch import start_larch" );
		interp.exec( "start_larch()" );
	}
}
