##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from copy import copy

from BritefuryJ.Incremental import IncrementalOwner, IncrementalValueMonitor

from GSymCore.PythonConsole import ConsoleSchema


class AppState (IncrementalOwner):
	def __init__(self):
		self._incr = IncrementalValueMonitor( self )
		
		self._openDocuments = []
		self._consoles = []
		self._configuration = AppConfiguration()
		
		
	def getOpenDocuments(self):
		self._incr.onAccess()
		return copy( self._openDocuments )
		
	def addOpenDocument(self, doc):
		self._openDocuments.append( doc )
		self._incr.onChanged()
		
		
	def getConsoles(self):
		self._incr.onAccess()
		return copy( self._consoles )
	
	def addConsole(self, console):
		self._consoles.append( console )
		self._incr.onChanged()

		
	def getConfiguration(self):
		self._incr.onAccess()
		return self._configuration
	
		
	
class AppDocument (IncrementalOwner):
	def __init__(self, name, location):
		self._incr = IncrementalValueMonitor( self )
		
		self._name = name
		self._location = location
		
		
		
	def getName(self):
		self._incr.onAccess()
		return self._name
	
	def getLocation(self):
		self._incr.onAccess()
		return self._location
		
	

class AppConsole (IncrementalOwner):
	def __init__(self, name):
		self._incr = IncrementalValueMonitor( self )
		
		self._name = name
		self._console = ConsoleSchema.Console()
		
		
		
	def getName(self):
		self._incr.onAccess()
		return self._name
	
	def getConsole(self):
		self._incr.onAccess()
		return self._console
		
	

class AppConfiguration (IncrementalOwner):
	def __init__(self):
		self._incr = IncrementalValueMonitor( self )
		

