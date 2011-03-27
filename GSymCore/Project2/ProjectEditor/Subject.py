##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from BritefuryJ.DocPresent.Browser import Location

from BritefuryJ.Projection import Subject

from GSymCore.Project2.ProjectPage import ProjectPage
from GSymCore.Project2.ProjectPackage import ProjectPackage
from GSymCore.Project2.ProjectEditor.View import perspective
from GSymCore.Project2.ProjectEditor.ModuleFinder import RootFinder, ModuleFinder



def _packageSubject(projectSubject, model, location):
	"""
	Create a package subject
	This is done using a function, that declares a class within its body, as the subject class must used __getattribute__; __getattr__ will prevent
	certain package/page names from being usable (e.g. __init__).
	Unfortunately, __getattribute__ also makes it hard to access instance attributes, so we have it access them from the surrounding scope
	(this function)
	"""
	class _PackageSubject (object):
		# We use __getattribute__ rather than __getattr__, otherwise the __init__ method can prevent some item names from being accessible
		def __getattribute__(self, name):
			item = model.contentsMap.get( name )
			if item is not None:
				itemLocation = location + '.' + name
				if isinstance( item, ProjectPackage ):
					return _packageSubject( projectSubject, item, itemLocation )
				elif isinstance( item, ProjectPage ):
					return projectSubject._document.newModelSubject( item.data, projectSubject, itemLocation, name )
			raise AttributeError, "Did not find item for '%s'"  %  ( name, )
	return _PackageSubject()



class ProjectSubject (Subject):
	def __init__(self, document, model, enclosingSubject, location, title):
		self._document = document
		self._model = model
		self._enclosingSubject = enclosingSubject
		self._location = location
		self._moduleFinder = ModuleFinder( self )
		self._rootFinder = RootFinder( self )
		self._title = title


	def getFocus(self):
		return self._model

	def getPerspective(self):
		return perspective

	def getTitle(self):
		return self._title + ' [Prj]'

	def getSubjectContext(self):
		return self._enclosingSubject.getSubjectContext().withAttrs( document=self._document, docLocation=Location( self._location ), location=Location( self._location ) )

	def getCommandHistory(self):
		return self._document.getCommandHistory()



	def __getattr__(self, name):
		item = self._model.contentsMap.get( name )
		if item is not None:
			itemLocation = self._location + '.' + name
			if isinstance( item, ProjectPackage ):
				return _packageSubject( self, item, itemLocation )
			elif isinstance( item, ProjectPage ):
				return self._document.newModelSubject( item.data, self, itemLocation, name )
		raise AttributeError, "Did not find item for '%s'"  %  ( name, )



	def find_module(self, fullname, path, world):
		return self._rootFinder.find_module( fullname, path, world )
