##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.AttributeTable import AttributeTable

from BritefuryJ.DocPresent.Browser import Location

from BritefuryJ.GSym import GSymBrowserContext, GSymLocationResolver, GSymSubject
from BritefuryJ.GSym.View import GSymViewContext

from Britefury.MainApp.AppWindow import AppWindow

		

class _AppLocationResolver (GSymLocationResolver):
	def __init__(self, app):
		self._app = app
		
	
	def resolveLocationAsPage(self, location, persistentState):
		subject = self.resolveLocationAsSubject( location )
		if subject is not None:
			commandHistory = None
			try:
				doc = subject.getSubjectContext()['document']
			except KeyError:
				doc = None
				
			commandHistory = doc.getCommandHistory()   if doc is not None   else None

			viewContext = GSymViewContext( subject, self._app._browserContext, commandHistory, persistentState )
			return viewContext.getPage()
		
		return None
				
	def resolveLocationAsSubject(self, location):
		appState = self._app._appState
		if appState is not None:
			world = self._app._world
			iterator = location.iterator()
			iterAfterModel = iterator.consumeLiteral( 'model:' )
			perspective = world.getAppStatePerspective()
			if iterAfterModel is not None:
				enclosingSubject = GSymSubject( appState, perspective, '[model]', AttributeTable.instance.withAttrs( world=world, document=None, location=Location( 'model:' ) ) )
				iterator = iterAfterModel
			else:
				enclosingSubject = GSymSubject( appState, perspective, '', AttributeTable.instance.withAttrs( world=world, document=None, location=Location( '' ) ) )
			subject = perspective.resolveRelativeLocation( enclosingSubject, iterator )
			if subject is None:
				return None
			if iterAfterModel:
				subject = subject.withPerspective( self._browserContext.getDefaultPerspective() )
			return subject
		else:
			return None
		

		



class MainApp (object):
	def __init__(self, world, location=Location( '' )):
		self._world = world

		self._resolver = _AppLocationResolver( self )
		self._browserContext = GSymBrowserContext( True, [ self._resolver ] )
		
		self._appState = world.getAppState()
		
		self._rootWindow = AppWindow( self, location )
		

		
	def getWorld(self):
		return self._world
	
	
	def show(self):
		self._rootWindow.show()

		
	def getBrowserContext(self):
		return self._browserContext
	
	
	def _createNewWindow(self, location):
		newWindow = AppWindow( self, location )
		newWindow.show()
	
	
	


	


