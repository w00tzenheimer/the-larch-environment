##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.extlibs.json import json
from Britefury.DocPresent.Web.JSScript import jsScriptClasses
from Britefury.DocPresent.Web.SharedObject import SharedObject, sharedObjectClasses, generateJSImplementation
from Britefury.DocPresent.Web.EventFromClient import EventFromClient



class InvalidSharedObjectList (Exception):
	pass



_pageTemplate = """
<html>
<head>
<style type="text/css">
%s
</style>
<script type="text/javascript" src="resources/json2.js"></script>
<script type="text/javascript" src="resources/jquery_1.2.6.js"></script>
<script type="text/javascript" src="resources/highlight.js"></script>
<script type="text/javascript" src="resources/editastext.js"></script>
%s
<script type="text/javascript">
function log(message) {
    if (!log.window_ || log.window_.closed) {
        var win = window.open("", null, "width=400,height=200," +
                              "scrollbars=yes,resizable=yes,status=no," +
                              "location=no,menubar=no,toolbar=no");
        if (!win) return;
        var doc = win.document;
        doc.write("<html><head><title>Debug Log</title></head>" +
                  "<body></body></html>");
        doc.close();
        log.window_ = win;
    }
    var logLine = log.window_.document.createElement("div");
    logLine.appendChild(log.window_.document.createTextNode(message));
    log.window_.document.body.appendChild(logLine);
}


%s
$(document).ready( function()
	{
%s
	}
)
</script>
<title>%s</title>
</head>

<body>
%s
</body>

</html>

"""



_js_pageScripts = \
"""
_outgoingObjectQueue = [];



function _receiveObjectsFromServer(jsonText)
{
	var objsJSon = JSON.parse( jsonText );
	for (i in objsJSon)
	{
		var json = objsJSon[i];
		var obj = _json_to_shared_object( json );
		obj.handle();
	}
};

function doObjectExchange()
{
	var objectJSon = [];
	for (i in _outgoingObjectQueue)
	{
		var obj = _outgoingObjectQueue[i];
		objectJSon.push( _shared_object_to_json( obj ) );
	}
	_outgoingObjectQueue = [];
	
	var objsToServer = JSON.stringify( objectJSon );
	var x = jQuery.post( "objectexchange", { objs : objsToServer }, _receiveObjectsFromServer, "JSON" );
};

function queueObject(obj)
{
	_outgoingObjectQueue.push( obj );
};

function sendObject(obj)
{
	queueObject( obj );
	doObjectExchange();
};

"""




class Page (object):
	def __init__(self, title):
		self._objectQueue = []
		self._pageTitle = title
		self._cssStyles = ''
		self._jsIncludes = []
		self._jsOnLoad = ''
		self._jsOnReady  = ''
		self._eventHandlers = {}
		
		
	def _sendObjectsAsJSon(self):
		objectsJSON = [ o.json()   for o in self._objectQueue ]
		self._objectQueue = []
		return objectsJSON
	
	
	def _receiveObjectsAsJSon(self, objectsJSON):
		if isinstance( objectsJSON, list ):
			objs = [ SharedObject.fromJSon( j )   for j in objectsJSON ]
			for obj in objs:
				self.handleIncomingObject( obj )
			self.objectsHandled()
		else:
			raise InvalidSharedObjectList
		
	
	
	def _class_js(self, c):
		return generateJSImplementation( c )  +  '_classNameToClass.%s = %s\n'  %  ( c.__name__, c.__name__ )  +  '%s.className = "%s"\n'  %  ( c.__name__, c.__name__ )  +  '\n\n'
		
	
	
	def _scriptIncludes(self):
		return ''.join( [ '<script type="text/javascript" src="%s"></script>\n'  %  include   for include in self._jsIncludes ] )

	
	def _scripts(self):
		pageHeaderScript = _js_pageScripts
		
		jsScripts = '\n\n'.join( [ c.__class_js__()   for c in jsScriptClasses ] )
		
		return pageHeaderScript  +  '\n\n\n'  +  jsScripts  +  '\n\n\n'  +  self._initScripts()   +  '\n\n\n'  +  self._jsOnLoad
	
	
	def _initScripts(self):
		return ''
	
	
	def _readyScripts(self):
		return '\n\n'.join( [ c.__class_onReady_js__()   for c in jsScriptClasses ] )   +   '\n\n\n'  +  self._jsOnReady
	
	
	def _styles(self):
		return self._cssStyles
	
	def _title(self):
		return self._pageTitle
	
	
	def _htmlBody(self):
		return ''
	
	
	
	def html(self):
		return _pageTemplate  %  ( self._styles(),   self._scriptIncludes(),   self._scripts(),   self._readyScripts(),   self._title(),   self._htmlBody() )
	
	def doServerObjectExchange(self, objs):
		# Read and process incoming objects that came with the request
		incomingObjs = json.read( objs )
		self._receiveObjectsAsJSon( incomingObjs )
		
		# This will have resulted in some objects being added to the outgoing queue; send them back to the client
		outgoingObjs = self._sendObjectsAsJSon()
		return json.write( outgoingObjs )
	
	
	
	def queueObject(self, obj):
		self._objectQueue.append( obj )
		
	

	def handleIncomingObject(self, obj):
		if isinstance( obj, EventFromClient ):
			try:
				handler = self._eventHandlers[obj.sourceID]
			except KeyError:
				pass
			else:
				handler( obj )
				
				
	def objectsHandled(self):
		pass
		
	
	
	
	def addCSSStyles(self, cssStyles):
		self._cssStyles += cssStyles
	
	def extendJSIncludes(self, jsIncludes):
		self._jsIncludes.extend( jsIncludes )
		
	def addJSOnLoad(self, js):
		self._jsOnLoad += js
		
	def addJSOnReady(self, js):
		self._jsOnReady += js
		
		
		
	def registerEventHandler(self, sourceID, handler):
		self._eventHandlers[sourceID] = handler
		
	def unregisterEventHandler(self, sourceID):
		del self._eventHandlers[sourceID]
		
		