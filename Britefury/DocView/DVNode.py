##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import difflib

from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.ElementTree import *
from BritefuryJ.DocPresent.StyleSheets import *
from BritefuryJ.DocPresent.Marker import *

from BritefuryJ.Cell import Cell

from Britefury.Util.NodeUtil import isListNode



_defaultProxyStyleSheet = ContainerStyleSheet()




class DVNode (object):
	def __init__(self, view, treeNode):
		super( DVNode, self ).__init__()
		self.treeNode = treeNode
		self.docNode = treeNode.node
		self._view = view
		self._parent = None
		self._refreshCell = Cell()
		self._refreshCell.setFunction( self._o_refreshNode )
		
		self._element = ProxyElement( _defaultProxyStyleSheet )
		self._elementContent = None
		self._contentsFactory = None
		self._contentsCell = Cell()
		self._contentsCell.setFunction( self._p_computeContents )
		
		self._children = set()
			
		

	def _changeTreeNode(self, treeNode):
		assert treeNode.node is self.docNode, 'DVNode._changeTreeNode(): doc-node must remain the same'
		self.treeNode = treeNode
		
		

	#
	# DOCUMENT VIEW METHODS
	#
	def getDocView(self):
		return self._view


	
	
	
	#
	# REFRESH METHODS
	#
	
	def _o_refreshNode(self):
		if self._elementContent is not None:
			startContent = self._elementContent.getContent()
		else:
			startContent = ''
		position, bias, contentString = self._getCursorPositionBiasAndContentString( self._elementContent )
		#print 'Node: ', self.docNode[0], position, self._elementContent

		# Set the caret node to self
		if position is not None  and  bias is not None  and  self._elementContent is not None:
			if isListNode( self.docNode ):
				#print 'Node: %s, position=%d'  %  ( self.docNode[0], position )
				pass
			self._view._caretNode = self

		contents = self._contentsCell.getValue()
		for child in self._children:
			child.refresh()

		self._updateElement( contents )
		
		if self._view._caretNode is self:
			# Invoking child.refresh() above can cause this method to be invoked on another node; recursively;
			# Ensure that only the inner-most recursion level handles the caret
			if position is not None  and  bias is not None  and  self._elementContent is not None:
				newContentString = self._elementContent.getContent()
				
				newPosition = position
				newBias = bias
	
				oldIndex = position  +  ( 1  if bias == Marker.Bias.END  else  0 )

				# Compute the difference
				matcher = difflib.SequenceMatcher( lambda x: x in ' \t', contentString, newContentString )
				for tag, i1, i2, j1, j2 in matcher.get_opcodes():
					if ( position > i1  or  ( position == i1  and  bias == Marker.Bias.END ) )   and   position < i2:
						# Caret is in the range of this opcode
						if tag == 'delete':
							# Range deleted; move to position in destination; bias:START
							newPosition = j1
							newBias = Marker.Bias.START
						elif tag == 'replace'  or  tag == 'equal'  or  tag == 'insert':
							# Range replaced or equal; move position by delta
							newPosition += j1 - i1
						else:
							raise ValueError, 'unreckognised tag'
				elementTree = self._elementContent.getElementTree()
				caret = elementTree.getCaret()
				
				
				newIndex = newPosition  +  ( 1  if newBias == Marker.Bias.END  else  0 )
				
				print 'CURSOR POSITION CHANGE'
				if bias == Marker.Bias.START:
					print contentString[:oldIndex].replace( '\n', '\\n' ) + '>|.' + contentString[oldIndex:].replace( '\n', '\\n' )
				else:
					print contentString[:oldIndex].replace( '\n', '\\n' ) + '.|<' + contentString[oldIndex:].replace( '\n', '\\n' )

				if bias == Marker.Bias.START:
					print newContentString[:newIndex].replace( '\n', '\\n' ) + '>|.' + newContentString[newIndex:].replace( '\n', '\\n' )
				else:
					print newContentString[:newIndex].replace( '\n', '\\n' ) + '.|<' + newContentString[newIndex:].replace( '\n', '\\n' )
				
				newPosition = max( 0, newPosition )
				if newPosition >= self._elementContent.getContentLength():
					newPosition = self._elementContent.getContentLength() - 1
					newBias = Marker.Bias.END
				
				leaf = self._elementContent.getLeafAtContentPosition( newPosition )
				if leaf is not None:
					print leaf, "'" + leaf.getContent().replace( '\n', '\\n' ) + "'"
					leafOffset = leaf.getContentOffsetInSubtree( self._elementContent )
					leafPosition = newPosition - leafOffset
					
					if leaf.isEditableEntry():
						#print 'Node "%s"; content was "%s" now "%s"'  %  ( self.docNode[0], startContent, self._elementContent.getContent() )
						#print 'Position was %d, now is %d; leaf (%s) offset is %d, moving to %d in leaf'  %  ( position, newPosition, leaf.getContent(), leafOffset, leafPosition )
						leaf.moveMarker( caret.getMarker(), leafPosition, newBias )
					else:
						segFilter = SegmentElement.SegmentFilter( leaf.getSegment() )
						elemFilter = LeafElement.LeafFilterEditable()
						
						if leafPosition < leaf.getContentLength()/2:
							left = leaf.getPreviousLeaf( segFilter, None, elemFilter )
							if left is not None:
								print left, "'" + left.getContent().replace( '\n', '\\n' ) + "'", left.getSegment() is leaf.getSegment()
								left.moveMarkerToEnd( caret.getMarker() )
							else:
								right = leaf.getNextLeaf( segFilter, None, elemFilter )
								if right is not None:
									print right, "'" + right.getContent().replace( '\n', '\\n' ) + "'"
									right.moveMarkerToStart( caret.getMarker() )
								else:
									leaf.moveMarker( caret.getMarker(), leafPosition, newBias )
						else:
							right = leaf.getNextLeaf( segFilter, None, elemFilter )
							if right is not None:
								print right, "'" + right.getContent().replace( '\n', '\\n' ) + "'"
								right.moveMarkerToStart( caret.getMarker() )
							else:
								left = leaf.getPreviousLeaf( segFilter, None, elemFilter )
								if left is not None:
									print left, "'" + left.getContent().replace( '\n', '\\n' ) + "'"
									left.moveMarkerToEnd( caret.getMarker() )
								else:
									leaf.moveMarker( caret.getMarker(), leafPosition, newBias )

				
				
		
	def _o_resetRefreshCell(self):
		self._refreshCell.setFunction( self._o_refreshNode )


	def refresh(self):
		self._refreshCell.getValue()


	def _getCursorPositionBiasAndContentString(self, element):
		if element is not None:
			contentString = element.getContent()
			elementTree = element.getElementTree()
			caret = elementTree.getCaret()
			try:
				position = caret.getMarker().getPositionInSubtree( self._elementContent )
			except DPWidget.IsNotInSubtreeException:
				return None, None, contentString
			return position, caret.getMarker().getBias(), contentString
		else:
			return None, None, ''
		
	
		
					

	def _p_computeContents(self):
		# Unregister existing child relationships
		for child in self._children:
			self._view._nodeTable.unrefViewNode( child )
		self._children = set()
		
		if self._contentsFactory is not None:
			contents = self._contentsFactory( self, self.treeNode )
			
			# Register new child relationships
			for child in self._children:
				self._view._nodeTable.refViewNode( child )
			
			return contents
		else:
			return None
		
		
	def _updateElement(self, element):
		if element is not None:
			if isinstance( element, Element ):
				pass
			else:
				raise TypeError, 'contents should be an Element, not a \'%s\''  %  type( element ) 
			
			self._elementContent = element
			self._element.setChild( element )
		else:
			self._elementContent = None
			self._element.setChild( None )
	

	def _registerChild(self, child):
		self._children.add( child )
		
		
	def _f_setContentsFactory(self, contentsFactory):
		if contentsFactory is not self._contentsFactory:
			self._contentsFactory = contentsFactory
			self._contentsCell.setFunction( self._p_computeContents )
			
			
			
	#
	# CONTENT ACQUISITION METHODS
	#
	
	def getElementNoRefresh(self):
		return self._element
	
	def getElement(self):
		self.refresh()
		return self._element
	
			
			
			
	#
	# NODE TREE METHODS
	#
	
	def getParentNodeView(self):
		return self._parent

	def isDescendantOf(self, node):
		n = self
		while n is not None:
			if n is node:
				return True
			n = n._parent
		return False




	def getChildViewNodeForChildDocNode(self, childDocNode):
		if childDocNode is not None:
			raise KeyError
		else:
			return None


	def _f_commandHistoryFreeze(self):
		self._view._f_commandHistoryFreeze()


	def _f_commandHistoryThaw(self):
		self._view._f_commandHistoryThaw()





