##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from copy import deepcopy

from BritefuryJ.ChangeHistory import Trackable
from BritefuryJ.Incremental import IncrementalValueMonitor

from LarchCore.Project.ProjectContainer import ProjectContainer




class ProjectPackage (ProjectContainer):
	def __init__(self, name='', contents=None):
		super( ProjectPackage, self ).__init__( contents )
		self._name = name
		

	def __getstate__(self):
		state = super( ProjectPackage, self ).__getstate__()
		state['name'] = self._name
		return state
	
	def __setstate__(self, state):
		super( ProjectPackage, self ).__setstate__( state )
		self._name = state['name']
	
	def __copy__(self):
		return ProjectPackage( self._name, self[:] )
	
	def __deepcopy__(self, memo):
		return ProjectPackage( self._name, [ deepcopy( x, memo )   for x in self ] )

	
	def getName(self):
		self._incr.onAccess()
		return self._name
	
	def setName(self, name):
		oldName = self._name
		self._name = name
		self._incr.onChanged()
		if self.__change_history__ is not None:
			self.__change_history__.addChange( lambda: self.setName( name ), lambda: self.setName( oldName ), 'Package set name' )
	
	
	name = property( getName, setName )