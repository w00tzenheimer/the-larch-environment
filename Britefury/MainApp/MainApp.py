##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from javax.swing import JOptionPane
from BritefuryJ.AttributeTable import SimpleAttributeTable

from BritefuryJ.Command import CommandConsole

from BritefuryJ.DocPresent.Browser import Location

from BritefuryJ.Projection import ProjectiveBrowserContext, Subject

from Britefury.MainApp.AppWindow import AppWindow

		
class _ModelSubject (Subject):
	def __init__(self, innerSubject):
		super( _ModelSubject, self ).__init__( innerSubject )
		self._innerSubject = innerSubject

		
	def getFocus(self):
		return self._innerSubject.getFocus()
	
	def getPerspective(self):
		return None
	
	def getTitle(self):
		return '[model]'

	
	
class MainApp (object):
	def __init__(self, world, location=Location( '' )):
			
		

		self._world = world
		
		
		self._browserContext = ProjectiveBrowserContext( True )
		self._world.registerBrowserContext( self._browserContext )
		
		def _createCommandConsole(presentationComponent):
			return CommandConsole( self._browserContext, presentationComponent )
		
		self._createCommandConsole = _createCommandConsole

		self._appState = world.getAppStateSubject().getFocus()
		self._browserContext.registerMainSubject( world.getAppStateSubject() )
		self._browserContext.registerNamedSubject( 'model', _ModelSubject )
		
		self._rootWindow = AppWindow( self, self._createCommandConsole, location )
		self._rootWindow.setCloseRequestListener( self._onWindowCloseRequest )
		self._openWindows = set( [ self._rootWindow ] )
		
		self.onAppClose = None
		

		
	def getWorld(self):
		return self._world
	
	
	def show(self):
		self._rootWindow.show()

		
	def getBrowserContext(self):
		return self._browserContext
	
	
	def setCloseListener(self, listener):
		self.onAppClose = listener

		
	def _createNewWindow(self, location):
		newWindow = AppWindow( self, self._createCommandConsole, location )
		newWindow.setCloseRequestListener( self._onWindowCloseRequest )
		newWindow.show()
		self._openWindows.add( newWindow )
		
		
	def _onWindowCloseRequest(self, window):
		if len( self._openWindows ) == 1:
			try:
				hasUnsavedDataFn = self._appState.hasUnsavedData
			except AttributeError:
				pass
			else:
				if hasUnsavedDataFn():
					# Dialog here
					response = JOptionPane.showOptionDialog( window.getFrame(),
						                                 'You have not saved your work. Close anyway?', 'Unsaved data', JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, None, [ 'Close', 'Cancel' ], 'Cancel' )
					if response == JOptionPane.YES_OPTION:
						pass
					else:
						return
				
		window.close()
		self._openWindows.remove( window )
		
		if len( self._openWindows ) == 0:
			if self.onAppClose is not None:
				self.onAppClose( self )
	
	

	

	


