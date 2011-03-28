##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from Britefury.Util.TrackedList import TrackedListProperty

from BritefuryJ.CommandHistory import Trackable
from BritefuryJ.Cell import Cell

from GSymCore.Project.ProjectNode import ProjectNode



class ProjectContainer (ProjectNode):
	def __init__(self):
		super( ProjectContainer, self ).__init__()
		self._contents_ = []
		self._prevContents = []
		self._contentsMapCell = Cell( self._computeContentsMap )
	
	
	def __getstate__(self):
		state = super( ProjectContainer, self ).__getstate__()
		state['contents'] = self._contents_
		return state
	
	def __setstate__(self, state):
		super( ProjectContainer, self ).__setstate__( state )
		self._contents_ = state['contents']
		self._contentsMapCell = Cell( self._computeContentsMap )
	
	
	def _computeContentsMap(self):
		m = {}
		self._incr.onAccess()
		for x in self._contents:
			m[x.name] = x
		return m
		
		
	def __len__(self):
		self._incr.onAccess()
		return len( self._contents )
	
	def __getitem__(self, index):
		self._incr.onAccess()
		return self._contents[index]
	
	def __iter__(self):
		for x in self._contents:
			self._incr.onAccess()
			yield x
	
	def __contains__(self, x):
		self._incr.onAccess()
		return x in self._contents
	
	def indexOfById(self, x):
		for i, y in enumerate( self._contents ):
			if x is y:
				return i
		return -1
	
	
	
	def __setitem__(self, index, x):
		self._contents[index] = x
	
	def __delitem__(self, index):
		del self._contents[index]
	
	def append(self, x):
		self._contents.append( x )
	
	def insert(self, i, x):
		self._contents.insert( i, x )
	
	def remove(self, x):
		self._contents.remove( x )
		
		
	def _onContentsChange(self):
		for x in self._prevContents:
			x._parent = None
		for x in self._contents_:
			x._parent = self
		self._prevContents = self._contents_[:]
		self._incr.onChanged()
		
		
	def getContentsMap(self):
		return self._contentsMapCell.getValue()
	
	
	def trackContents(self, history):
		self._contents.trackContents( history )
	
	def stopTrackingContents(self, history):
		self._contents.stopTrackingContents( history )
	
		
	contentsMap = property( getContentsMap )

	
	_contents = TrackedListProperty( '_contents_', '_commandHistory', _onContentsChange )
	
	
	