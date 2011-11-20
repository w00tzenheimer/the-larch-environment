##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from BritefuryJ.Command import Command, CommandSet

from LarchCore.Languages.Python25.PythonCommands import pythonCommands, makeInsertEmbeddedExpressionAtCaretAction, makeWrapSelectionInEmbeddedExpressionAction,	\
	makeWrapSelectedStatementRangeInEmbeddedObjectAction, makeInsertEmbeddedStatementAtCaretAction, chainActions

from LarchTools.EmbeddedData.IntEditorSpinEntry import IntEditorSpinEntry
from LarchTools.EmbeddedData.StringEditorTextArea import StringEditorTextArea



def _newIntSpinEntryAtCaret(caret):
	return IntEditorSpinEntry()

_intSpinAtCaret = makeInsertEmbeddedExpressionAtCaretAction( _newIntSpinEntryAtCaret )
_intSpinCommand = Command( '&Int &Spin &Entry', _intSpinAtCaret )


def _newTextAreaAtCaret(caret):
	return StringEditorTextArea()

_textAreaAtCaret = makeInsertEmbeddedExpressionAtCaretAction( _newTextAreaAtCaret )
_textAreaCommand = Command( '&Text &Area', _textAreaAtCaret )


_edCommands = CommandSet( 'LarchTools.EmbeddedData', [ _intSpinCommand, _textAreaCommand ] )

pythonCommands.registerCommandSet( _edCommands )