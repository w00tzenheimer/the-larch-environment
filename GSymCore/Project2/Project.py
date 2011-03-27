##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from BritefuryJ.CommandHistory import Trackable
from BritefuryJ.Incremental import IncrementalValueMonitor

from Britefury.gSym.gSymUnitClass import GSymUnitClass, GSymDocumentFactory
from Britefury.gSym.gSymDocument import gSymUnit, GSymDocument

from GSymCore.Project2.ProjectEditor.View import perspective as projectEditorPerspective
from GSymCore.Project2.ProjectEditor.Subject import ProjectSubject
from GSymCore.Project2.ProjectRoot import ProjectRoot


	


def newProject():
	return ProjectRoot()

def _newProjectDocment(world):
	return GSymDocument( world, newProject() )


newDocumentFactory = GSymDocumentFactory( 'gSym Document [2]', _newProjectDocment )
