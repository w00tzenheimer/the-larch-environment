##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from weakref import WeakValueDictionary

import imp

from BritefuryJ.Incremental import IncrementalOwner, IncrementalValueMonitor
from BritefuryJ.IncrementalUnit import Unit

from BritefuryJ.Pres import InnerFragment


from Britefury import LoadBuiltins

from Britefury.Dispatch.DMObjectNodeMethodDispatch import DMObjectNodeDispatchMethod, dmObjectNodeMethodDispatch

from LarchCore.Languages.Python25 import Python25
from LarchCore.Languages.Python25.Execution import Execution

from LarchCore.Worksheet import Schema





class _Projection (object):
	__dispatch_num_args__ = 1
	

	def __call__(self, node, worksheet):
		return dmObjectNodeMethodDispatch( self, node, worksheet )

	@DMObjectNodeDispatchMethod( Schema.Worksheet )
	def worksheet(self, worksheet, node):
		return WorksheetView( worksheet, node )

	@DMObjectNodeDispatchMethod( Schema.Body )
	def body(self, worksheet, node):
		return BodyView( worksheet, node )

	@DMObjectNodeDispatchMethod( Schema.Paragraph )
	def paragraph(self, worksheet, node):
		return ParagraphView( worksheet, node )
	
	@DMObjectNodeDispatchMethod( Schema.TextSpan )
	def textSpan(self, worksheet, node):
		return TextSpanView( worksheet, node )
	
	@DMObjectNodeDispatchMethod( Schema.PythonCode )
	def pythonCode(self, worksheet, node):
		return PythonCodeView( worksheet, node )
	
	@DMObjectNodeDispatchMethod( Schema.QuoteLocation )
	def quoteLocation(self, worksheet, node):
		return QuoteLocationView( worksheet, node )

_projection = _Projection()



class NodeView (object):
	def __init__(self, worksheet, model):
		self._worksheet = worksheet
		self._model = model
		
	def getModel(self):
		return self._model
	
	def isVisible(self):
		return True
	
	def __present__(self, fragment, inheritedState):
		return InnerFragment( self._model )
	
	def _viewOf(self, model):
		return self._worksheet._viewOf( model )

	


class WorksheetView (NodeView):
	def __init__(self, worksheet, model):
		super( WorksheetView, self ).__init__( worksheet, model )
		self._modelToView = WeakValueDictionary()
		self.refreshResults()
		
		
	def _initModule(self):
		self._module = imp.new_module( 'worksheet' )
		LoadBuiltins.loadBuiltins( self._module )
		
		
	def refreshResults(self):
		self._initModule()
		body = self.getBody()
		body.refreshResults( self._module )
	
		
	def getModule(self):
		return self._module
		
		
	def getBody(self):
		return self._viewOf( self._model['body'] )
	
	
	def _viewOf(self, model):
		key = id( model )
		try:
			return self._modelToView[key]
		except KeyError:
			p = _projection( model, self )
			self._modelToView[key] = p
			return p
		
		


class BodyView (NodeView):
	def __init__(self, worksheet, model):
		super( BodyView, self ).__init__( worksheet, model )
		self._contentsUnit = Unit( self._computeContents )
		
		
	def refreshResults(self, module):
		for v in self.getContents():
			v._refreshResults( module )
		
		
	def getContents(self):
		return self._contentsUnit.getValue()
	
	
	def appendModel(self, node):
		self._model['contents'].append( node )
	
	def insertModelAfterNode(self, node, model):
		try:
			index = self.getContents().index( node )
		except ValueError:
			return False
		self._model['contents'].insert( index + 1, model )
		return True

	def deleteNode(self, node):
		try:
			index = self.getContents().index( node )
		except ValueError:
			return False
		del self._model['contents'][index]
		return True
		
		
		
	def joinConsecutiveTextNodes(self, firstNode):
		assert isinstance( firstNode, ParagraphView )
		contents = self.getContents()
		
		try:
			index = contents.index( firstNode )
		except ValueError:
			return False
		
		if ( index + 1)  <  len( contents ):
			next = contents[index+1]
			if isinstance( next, ParagraphView ):
				firstNode.setText( firstNode.getText() + next.getText() )
				del self._model['contents'][index+1]
				return True
		return False
	
	def splitTextNodes(self, textNode, textLines):
		style = textNode.getStyle()
		textModels = [ Schema.Paragraph( text=t, style=style )   for t in textLines ]
		try:
			index = self.getContents().index( textNode )
		except ValueError:
			return False
		self._model['contents'][index:index+1] = textModels
		return True
		
		
	def _computeContents(self):
		return [ self._viewOf( x )   for x in self._model['contents'] ]
	
		


class ParagraphView (NodeView):
	def __init__(self, worksheet, model):
		super( ParagraphView, self ).__init__( worksheet, model )
	
		
	def getText(self):
		return self._model['text']
	
	def setText(self, text):
		self._model['text'] = text
		
	
	def getStyle(self):
		return self._model['style']
	
	def setStyle(self, style):
		self._model['style'] = style
		
		
	def partialModel(self):
		return Schema.PartialParagraph( style=self._model['style'] )
		
		
	def _refreshResults(self, module):
		pass
	
	
	@staticmethod
	def newParagraphModel(text, style):
		return Schema.Paragraph( text=text, style=style )
		
		
		
class TextSpanView (NodeView):
	def __init__(self, worksheet, model):
		super( TextSpanView, self ).__init__( worksheet, model )
	
		
	def getText(self):
		return self._model['text']
	
	def setText(self, text):
		self._model['text'] = text
		
	
	def getStyleAttrs(self):
		return self._model['styleAttrs']
	
	def setStyleAttrs(self, styleAttrs):
		self._model['styleAttrs'] = styleAttrs
		
		
	def _refreshResults(self, module):
		pass
	
	
	@staticmethod
	def newTextSpanModel(text, styleAttrs):
		return Schema.TextSpan( text=text, styleAttrs=styleAttrs )
		
		
		
class PythonCodeView (IncrementalOwner, NodeView):
	STYLE_MINIMAL_RESULT = 0
	STYLE_RESULT = 1
	STYLE_CODE_AND_RESULT = 2
	STYLE_CODE = 3
	STYLE_EDITABLE_CODE_AND_RESULT = 4
	STYLE_EDITABLE_CODE = 5
	STYLE_HIDDEN = 6
	
	_styleToName  = { STYLE_MINIMAL_RESULT : 'minimal_result',
	                    STYLE_RESULT : 'result',
	                    STYLE_CODE_AND_RESULT : 'code_result',
	                    STYLE_CODE : 'code',
	                    STYLE_EDITABLE_CODE_AND_RESULT : 'editable_code_result',
	                    STYLE_EDITABLE_CODE : 'editable_code',
	                    STYLE_HIDDEN : 'hidden' }
	
	_nameToStyle  = { 'minimal_result' : STYLE_MINIMAL_RESULT,
	                  'result' : STYLE_RESULT,
	                  'code_result' : STYLE_CODE_AND_RESULT,
	                  'code' : STYLE_CODE,
	                  'editable_code_result' : STYLE_EDITABLE_CODE_AND_RESULT,
	                  'editable_code' : STYLE_EDITABLE_CODE,
	                  'hidden' : STYLE_HIDDEN }
	
	
	def __init__(self, worksheet, model):
		NodeView.__init__( self, worksheet, model )
		self._incr = IncrementalValueMonitor( self )
		self._result = None
		
		
	def getCode(self):
		return self._model['code']
	
	def setCode(self, code):
		self._model['code'] = code
		
		
		
	def getStyle(self):
		name = self._model['style']
		try:
			return self._nameToStyle[name]
		except KeyError:
			return self.STYLE_CODE_AND_RESULT
	
	def setStyle(self, style):
		try:
			name = self._styleToName[style]
		except KeyError:
			raise ValueError, 'invalid style'
		self._model['style'] = name
		
		
	def isCodeVisible(self):
		style = self.getStyle()
		return style == self.STYLE_CODE  or  style == self.STYLE_CODE_AND_RESULT  or  style == self.STYLE_EDITABLE_CODE  or  style == self.STYLE_EDITABLE_CODE_AND_RESULT
		
	def isCodeEditable(self):
		style = self.getStyle()
		return style == self.STYLE_EDITABLE_CODE  or  style == self.STYLE_EDITABLE_CODE_AND_RESULT
	
	def isResultVisible(self):
		style = self.getStyle()
		return style == self.STYLE_MINIMAL_RESULT  or  style == self.STYLE_RESULT  or  style == self.STYLE_CODE_AND_RESULT  or  style == self.STYLE_EDITABLE_CODE_AND_RESULT
		
	def isResultMinimal(self):
		style = self.getStyle()
		return style == self.STYLE_MINIMAL_RESULT
	
	def isVisible(self):
		style = self.getStyle()
		return style != self.STYLE_HIDDEN
		
		
		
	def getResult(self):
		self._incr.onAccess()
		return self._result
		
		
		
	def _refreshResults(self, module):
		self._result = Execution.executePythonModule( self.getCode(), module, self.isResultVisible() )
		self._incr.onChanged()
		
		
		
	@staticmethod
	def newPythonCodeModel():
		return Schema.PythonCode( style='code_result', code=Python25.py25NewModule() )

	
	
class QuoteLocationView (IncrementalOwner, NodeView):
	STYLE_MINIMAL = 0
	STYLE_NORMAL = 1
	
	_styleToName  = { STYLE_MINIMAL : 'minimal',
	                    STYLE_NORMAL : 'normal' }
	
	_nameToStyle  = { 'minimal' : STYLE_MINIMAL,
	                  'normal' : STYLE_NORMAL }
	
	
	def __init__(self, worksheet, model):
		NodeView.__init__( self, worksheet, model )
		self._incr = IncrementalValueMonitor( self )
		self._result = None
		
		
	def getLocation(self):
		return self._model['location']
	
	def setLocation(self, location):
		self._model['location'] = location
		
		
		
	def getStyle(self):
		name = self._model['style']
		try:
			return self._nameToStyle[name]
		except KeyError:
			return self.STYLE_NORMAL
	
	def setStyle(self, style):
		try:
			name = self._styleToName[style]
		except KeyError:
			raise ValueError, 'invalid style'
		self._model['style'] = name
		
		
	def isMinimal(self):
		style = self.getStyle()
		return style == self.STYLE_MINIMAL
		
		
	def _refreshResults(self, module):
		pass
		
	@staticmethod
	def newQuoteLocationModel():
		return Schema.QuoteLocation( location='', style='normal' )