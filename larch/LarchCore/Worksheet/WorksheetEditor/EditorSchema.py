##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from BritefuryJ.DocModel import DMNode

from BritefuryJ.Incremental import IncrementalValueMonitor

from BritefuryJ.Projection import SubjectPath

from BritefuryJ.Editor.RichText.Attrs import RichTextAttributes


from Britefury.Dispatch.MethodDispatch import DMObjectNodeDispatchMethod, methodDispatch

from LarchCore.Languages.Python2.Embedded import _py25NewModule, _py25NewExpr

from LarchCore.Worksheet import Schema
from LarchCore.Worksheet import AbstractViewSchema
from LarchCore.Worksheet import WorksheetEditor as WSEditor





class _Projection (object):
	__dispatch_num_args__ = 1
	

	def __call__(self, node, worksheet):
		return methodDispatch( self, node, worksheet )

	@DMObjectNodeDispatchMethod( Schema.Body )
	def body(self, worksheet, node):
		return BodyEditor( worksheet, node )

	@DMObjectNodeDispatchMethod( Schema.Paragraph )
	def paragraph(self, worksheet, node):
		return ParagraphEditor( worksheet, node )
	
	@DMObjectNodeDispatchMethod( Schema.TextSpan )
	def textSpan(self, worksheet, node):
		return TextSpanEditor( worksheet, node )

	@DMObjectNodeDispatchMethod( Schema.Link )
	def link(self, worksheet, node):
		return LinkEditor( worksheet, node )

	@DMObjectNodeDispatchMethod( Schema.PythonCode )
	def pythonCode(self, worksheet, node):
		return PythonCodeEditor( worksheet, node )

	@DMObjectNodeDispatchMethod( Schema.InlinePythonCode )
	def inlinePythonCode(self, worksheet, node):
		return InlinePythonCodeEditor( worksheet, node )

	@DMObjectNodeDispatchMethod( Schema.InlineEmbeddedObject )
	def inlineEmbeddedObject(self, worksheet, node):
		return InlineEmbeddedObjectEditor( worksheet, node )

	@DMObjectNodeDispatchMethod( Schema.ParagraphEmbeddedObject )
	def paragraphEmbeddedObject(self, worksheet, node):
		return ParagraphEmbeddedObjectEditor( worksheet, node )





class WorksheetEditor (AbstractViewSchema.WorksheetAbstractView):
	_projection = _Projection()

		


class BodyEditor (AbstractViewSchema.BodyAbstractView):
	def __init__(self, worksheet, model):
		super( BodyEditor, self ).__init__( worksheet, model )
		self._editorModel = WSEditor.RichTextController.WorksheetRichTextController.instance.editorModelBlock( [] )



	def appendEditorNode(self, editor):
		self._model['contents'].append( editor.getModel() )

	def insertEditorNodeAfter(self, editor, pos):
		try:
			index = self.getContents().index( pos ) + 1
		except ValueError:
			index = len( self.getContents() )
		self._model['contents'].insert( index, editor.getModel() )
		return True

	def deleteEditorNode(self, editor):
		try:
			index = self.getContents().index( editor )
		except ValueError:
			return False
		del self._model['contents'][index]
		return True



	def setContents(self, contents):
		modelContents = [ x.getModel()   for x in contents   if not isinstance( x, BlankParagraphEditor ) ]
		self._model['contents'] = modelContents


	def _computeContents(self):
		xs = [ self._viewOf( x )   for x in self._model['contents'] ]
		self._editorModel.setModelContents( WSEditor.RichTextController.WorksheetRichTextController.instance, xs )
		return xs



# Must derive from NodeAbstractView so that WorksheetRichTextController.isDataModelObject will recognise it
class BlankParagraphEditor (AbstractViewSchema.NodeAbstractView):
	def __init__(self, worksheet, blockEditor):
		super( BlankParagraphEditor, self ).__init__( worksheet, None )
		self._style = 'normal'
		paraAttrs = RichTextAttributes.fromValues({ 'style' : self._style }, None)
		self._editorModel = WSEditor.RichTextController.WorksheetRichTextController.instance.editorModelParagraph( None, [ '' ], paraAttrs )
		self._incr = IncrementalValueMonitor()
		self._blockEditor = blockEditor


	def getText(self):
		self._incr.onAccess()
		return ''

	def setContents(self, contents):
		self._editorModel.setModelContents( WSEditor.RichTextController.WorksheetRichTextController.instance, contents )
		if len( contents ) == 0:
			return
		elif len( contents ) == 1  and  contents[0] == '':
			return
		p = ParagraphEditor.newParagraph( contents, self._style )
		self._blockEditor.appendEditorNode( p )


	def getStyle(self):
		self._incr.onAccess()
		return self._style

	def setStyle(self, style):
		self._style = style
		self._incr.onChanged()


	def _refreshResults(self, module):
		pass




class ParagraphEditor (AbstractViewSchema.ParagraphAbstractView):
	def __init__(self, worksheet, model, projectedContents=None):
		super( ParagraphEditor, self ).__init__( worksheet, model )
		if projectedContents is None:
			projectedContents = self._computeText()
		paraAttrs = RichTextAttributes.fromValues({'style': model['style']}, None)
		self._editorModel = WSEditor.RichTextController.WorksheetRichTextController.instance.editorModelParagraph( self, projectedContents, paraAttrs )
	

	def setContents(self, contents):
		modelContents = self._textToModel( contents )
		self._model['text'] = modelContents
		self._editorModel.setModelContents( WSEditor.RichTextController.WorksheetRichTextController.instance, contents )


	def setStyle(self, style):
		self._model['style'] = style
		paraAttrs = RichTextAttributes.fromValues({'style': style}, None)
		self._editorModel.setParaAttrs(paraAttrs)


	def __clipboard_copy__(self, memo):
		return ParagraphEditor( None, memo.copy( self._model ) )
		
		
	@staticmethod
	def newParagraph(contents, style):
		m = ParagraphEditor.newParagraphModel( ParagraphEditor._textToModel( contents ), style )
		return ParagraphEditor( None, m, contents )

	@staticmethod
	def newParagraphModel(text, style):
		return Schema.Paragraph( text=text, style=style )
		
		
		
class TextSpanEditor (AbstractViewSchema.TextSpanAbstractView):
	def __init__(self, worksheet, model, projectedContents=None):
		super( TextSpanEditor, self ).__init__( worksheet, model )
		spanAttrs = RichTextAttributes()
		for a in model['styleAttrs']:
			spanAttrs.putOverride(a['name'], a['value'])
		if projectedContents is None:
			projectedContents = self._computeText()
		self._editorModel = WSEditor.RichTextController.WorksheetRichTextController.instance.editorModelSpan( projectedContents, spanAttrs )

		
	def setContents(self, contents):
		modelContents = self._textToModel( contents )
		self._model['text'] = modelContents
		self._editorModel.setModelContents( WSEditor.RichTextController.WorksheetRichTextController.instance, contents )

	
	def setStyleAttrs(self, styleMap):
		spanAttrs = RichTextAttributes.fromValues({n: v   for n, v in styleMap.items()}, None)
		modelAttrs = [ Schema.StyleAttr( name=n, value=v )   for n, v in styleMap.items() ]

		self._model['styleAttrs'] = modelAttrs
		self._editorModel.setSpanAttrs( spanAttrs )


	def __clipboard_copy__(self, memo):
		return TextSpanEditor( None, memo.copy( self._model ) )

		
	@staticmethod
	def newTextSpan(contents, styleMap):
		styleAttrs = [ Schema.StyleAttr( name=n, value=v )   for n, v in styleMap.items()   if v is not None ]
		m = TextSpanEditor.newTextSpanModel( ParagraphEditor._textToModel( contents ), styleAttrs )
		return TextSpanEditor( None, m, contents )

	@staticmethod
	def newTextSpanModel(text, styleAttrs):
		return Schema.TextSpan( text=text, styleAttrs=styleAttrs )



class LinkEditor (AbstractViewSchema.LinkAbstractView):
	def __init__(self, worksheet, model):
		super( LinkEditor, self ).__init__( worksheet, model )
		self._editorModel = WSEditor.RichTextController.WorksheetRichTextController.instance.editorModelInlineEmbed( self )


	@property
	def text(self):
		return self._model['text']

	@text.setter
	def text(self, value):
		self._model['text'] = value


	def setSubject(self, docSubject, subject):
		rel = self.__relativePath( docSubject, subject )
		self._model['path'] = rel


	def copy(self, worksheet=None):
		model = Schema.Link( text=self._model['text'], path=self._model['path'] )
		return LinkEditor( worksheet, model )


	def __clipboard_copy__(self, memo):
		return LinkEditor( None, memo.copy( self._model ) )


	@staticmethod
	def newLinkForModel(model):
		return LinkEditor( None, model )

	@staticmethod
	def newDocumentLink(text):
		return LinkEditor( None, LinkEditor.newDocumentLinkModel( text ) )

	@staticmethod
	def newDocumentLinkModel(text):
		return LinkEditor.newLinkModel( text, SubjectPath() )

	@staticmethod
	def newLink(docSubject, text, subject):
		rel = LinkEditor.__relativePath( docSubject, subject )
		m = LinkEditor.newLinkModel( text, rel )
		return LinkEditor( None, m )

	@staticmethod
	def newLinkModel(text, relativePath):
		return Schema.Link( text=text, path=relativePath )


	@staticmethod
	def __relativePath(docSubject, subject):
		docPath = docSubject.path()
		path = subject.path()
		if not path.isWithin( docPath ):
			raise ValueError, 'the subject must be contained within the document'
		return path.relativeTo( docPath )
		
		
		
class PythonCodeEditor (AbstractViewSchema.PythonCodeAbstractView):
	def __init__(self, worksheet, model):
		super( PythonCodeEditor, self ).__init__( worksheet, model )
		self._editorModel = WSEditor.RichTextController.WorksheetRichTextController.instance.editorModelParagraphEmbed( self, self )

		
	def setCode(self, code):
		self._model['code'] = code
		
		
		
	def setStyle(self, style):
		try:
			name = self._styleToName[style]
		except KeyError:
			raise ValueError, 'invalid style'
		self._model['style'] = name


	def copy(self, worksheet=None):
		return PythonCodeEditor( worksheet, self._model )


	def __clipboard_copy__(self, memo):
		return PythonCodeEditor( None, memo.copy( self._model ) )


	@staticmethod
	def newPythonCode():
		m = PythonCodeEditor.newPythonCodeModel()
		return PythonCodeEditor( None, m )

	@staticmethod
	def newPythonCodeModel():
		return Schema.PythonCode( style='code_result', code=_py25NewModule() )



class InlinePythonCodeEditor (AbstractViewSchema.InlinePythonCodeAbstractView):
	def __init__(self, worksheet, model):
		super( InlinePythonCodeEditor, self ).__init__( worksheet, model )
		self._editorModel = WSEditor.RichTextController.WorksheetRichTextController.instance.editorModelInlineEmbed( self )


	def setExpr(self, expr):
		self._model['expr'] = expr



	def setStyle(self, style):
		try:
			name = self._styleToName[style]
		except KeyError:
			raise ValueError, 'invalid style'
		self._model['style'] = name


	def copy(self, worksheet=None):
		return InlinePythonCodeEditor( worksheet, self._model )


	def __clipboard_copy__(self, memo):
		return InlinePythonCodeEditor( None, memo.copy( self._model ) )


	@staticmethod
	def newInlinePythonCode():
		m = InlinePythonCodeEditor.newInlinePythonCodeModel()
		return InlinePythonCodeEditor( None, m )

	@staticmethod
	def newInlinePythonCodeModel():
		return Schema.InlinePythonCode( style='minimal_result', expr=_py25NewExpr() )



class InlineEmbeddedObjectEditor (AbstractViewSchema.InlineEmbeddedObjectAbstractView):
	def __init__(self, worksheet, model):
		super( InlineEmbeddedObjectEditor, self ).__init__( worksheet, model )
		self._editorModel = WSEditor.RichTextController.WorksheetRichTextController.instance.editorModelInlineEmbed( self )


	def copy(self, worksheet=None):
		return self.newInlineEmbeddedObject( self.value )


	def __clipboard_copy__(self, memo):
		return InlineEmbeddedObjectEditor( None, memo.copy( self._model ) )


	@staticmethod
	def newInlineEmbeddedObject(value):
		m = InlineEmbeddedObjectEditor.newInlineEmbeddedObjectModel( value )
		return InlineEmbeddedObjectEditor( None, m )

	@staticmethod
	def newInlineEmbeddedObjectModel(value):
		embeddedValue = DMNode.embedIsolated( value )
		return Schema.InlineEmbeddedObject( embeddedValue=embeddedValue )




class ParagraphEmbeddedObjectEditor (AbstractViewSchema.ParagraphEmbeddedObjectAbstractView):
	def __init__(self, worksheet, model):
		super( ParagraphEmbeddedObjectEditor, self ).__init__( worksheet, model )
		self._editorModel = WSEditor.RichTextController.WorksheetRichTextController.instance.editorModelParagraphEmbed( self, self )


	def copy(self, worksheet=None):
		return self.newParagraphEmbeddedObject( self.value )


	def __clipboard_copy__(self, memo):
		return ParagraphEmbeddedObjectEditor( None, memo.copy( self._model ) )


	@staticmethod
	def newParagraphEmbeddedObject(value):
		m = ParagraphEmbeddedObjectEditor.newParagraphEmbeddedObjectModel( value )
		return ParagraphEmbeddedObjectEditor( None, m )

	@staticmethod
	def newParagraphEmbeddedObjectModel(value):
		embeddedValue = DMNode.embedIsolated( value )
		return Schema.ParagraphEmbeddedObject( embeddedValue=embeddedValue )

