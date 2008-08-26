##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.GLisp.GLispUtil import isGLispList


from Britefury.gSym.View.gSymView import border, indent, text, hbox, ahbox, vbox, paragraph, script, scriptLSuper, scriptLSub, scriptRSuper, scriptRSub, listView, interact, viewEval, mapViewEval, GSymView
from Britefury.gSym.View.ListView import ParagraphListViewLayout, HorizontalListViewLayout, VerticalInlineListViewLayout, VerticalListViewLayout

from Britefury.gSym.View.Interactor import keyEventMethod, accelEventMethod, textEventMethod, backspaceStartMethod, deleteEndMethod, Interactor

from Britefury.gSym.View.EditOperations import replace, append, prepend, insertBefore, insertAfter

from Britefury.gSym.View.UnparsedText import UnparsedText


from BritefuryJ.DocPresent.StyleSheets import *
from BritefuryJ.DocPresent import *


from GSymCore.Languages.LISP import Parser
from GSymCore.Languages.LISP.Styles import *




def _parseText(text):
	res, pos = Parser.parser.parseString( text )
	if res is not None:
		if pos == len( text ):
			return res.result
		else:
			print '<INCOMPLETE>'
			print 'FULL TEXT:', text
			print 'PARSED:', text[:pos]
			return None
	else:
		print 'FULL TEXT:', text
		print '<FAIL>'
		return None


class ParsedNodeInteractor (Interactor):
	@textEventMethod()
	def tokData(self, bUserEvent, bChanged, value, node):
		if bChanged:
			parsed = _parseText( value )
			if parsed is not None:
				replace( node, parsed )
			else:
				replace( node, value )
	
	eventMethods = [ tokData ]

	
	
	
def nodeEditor(ctx, node, contents, metadata, state):
	#return interact( focus( customEntry( highlight( contents ), text.getText() ) ),  ParsedNodeInteractor( node ) ),   text
	return contents, metadata


def stringNodeEditor(ctx, node, metadata, state):
	#return interact( focus( customEntry( highlight( label( text.getText() ) ), text.getText() ) ),  ParsedNodeInteractor( node ) )
	return text( ctx, string_textStyle, node ), metadata



MODE_HORIZONTAL = 0
MODE_VERTICALINLINE = 1
MODE_VERTICAL = 2


def viewStringNode(ctx, node, state):
	res, pos = Parser.unquotedString.parseString( node )
	node = repr( node )
	# String
	return stringNodeEditor( ctx, node, None, state )


def lispViewEval(ctx, node, state):
	if isGLispList( node ):
		return viewEval( ctx, node )
	else:
		return viewStringNode( ctx, node, state )


def viewLispNode(ctx, node, state):
	if isGLispList( node ):
		# List
		xViews = [ lispViewEval( ctx, x, state )   for x in node ]
		
		# Check the contents:
		mode = MODE_HORIZONTAL
		if len( node ) > 0:
			if isGLispList( node[0] ):
				mode = MODE_VERTICAL
			else:
				for x in node[1:]:
					if isGLispList( x ):
						mode = MODE_VERTICALINLINE
						break
		
			
		if mode == MODE_HORIZONTAL:
			layout = horizontal_listViewLayout
		elif mode == MODE_VERTICALINLINE:
			layout = verticalInline_listViewLayout
		elif mode == MODE_VERTICAL:
			layout = vertical_listViewLayout
		else:
			raise ValueError
		v = listView( ctx, layout, lambda: text( ctx, punctuation_textStyle, '(' ), lambda: text( ctx, punctuation_textStyle, ')' ), None, xViews )
		
		return nodeEditor( ctx, node, v, None, state )
	else:
		raise TypeError
	
	
	
def LISPView():
	return viewLispNode