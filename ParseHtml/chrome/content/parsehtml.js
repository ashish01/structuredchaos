//this is the list of all urls, loaded from the file
var urls = null;

//window handle for newly opened window
var wid = null;

//next url to load
var count = 0;

//variable to prevent multiple invocation
var processed = true;

//logging function
function LOG(msg) {
  var consoleService = Components.classes["@mozilla.org/consoleservice;1"]
	.getService(Components.interfaces.nsIConsoleService);
  consoleService.logStringMessage(msg);
}

function ptext(text) {
    text = text.replace(/\(/g, " RIGHTROUND ");
    text = text.replace(/\)/g, " LEFTROUND ");
    text = text.replace(/\s+/g, " ");
    var tokens = text.split(/[\s+]/);
    var count=0;
    var ret = "";
    
    if (tokens.length > 10) {
		for (count=0;count<5;count++) {
			if (tokens[count].length == 0)
				ret += "(WSEQ EMPTY)";
			else
				ret += "(WSEQ " + tokens[count] + ")";
		}
		
		var intermediate = "";
		for (;count<tokens.length-5;count++) {
			if (tokens[count].length == 0)
				intermediate += "EMPTY";
			else
				intermediate += tokens[count];
			intermediate += "_";
		}
		
		ret += "(WSEQINTER " + intermediate + ")";
		
		for (;count<tokens.length;count++) {
			if (tokens[count].length == 0)
				ret += "(WSEQ EMPTY)";
			else
				ret += "(WSEQ " + tokens[count] + ")";
		}
	} else {
		for (count=0;count<tokens.length;count++) {
			if (tokens[count].length == 0)
				ret += "(WSEQ EMPTY)";
			else
				ret += "(WSEQ " + tokens[count] + ")";
		}
	}
    return ret;
}

function walk(root) {
    if (root.childNodes.length == 0) {
        if (root.textContent.trim().length > 0) {
            return ptext(root.textContent);
        } else {
            return "(WSEQ EMPTY)";
        }
    }
    else {
        var i = 0;
        var output = "(" + root.tagName;
        var hasChild = false;
        for (i=0; i<root.childNodes.length; i++) {
            if (root.childNodes[i].nodeName != "SCRIPT"
                && root.childNodes[i].nodeName != "#comment"
                && root.childNodes[i].nodeName != "META"
                && root.childNodes[i].nodeName != "STYLE"
                && root.childNodes[i].nodeName != "NOSCRIPT") {
                output += walk(root.childNodes[i]);
                hasChild = true;
            }
        }
        
        if (!hasChild) {
            output += "(WSEQ NOC)";
        }
        output += ")";
        
        return output;
    }
}

//loads the urls from the file (file must be in utf-8 file format
function loadurls() {
	var dirService = Components.classes["@mozilla.org/file/directory_service;1"].
		getService(Components.interfaces.nsIProperties); 
	var homeDirFile = dirService.get("Home", Components.interfaces.nsIFile); // returns an nsIFile object
	
	//get the urls file home folder of the user
	homeDirFile.append("urls")
	var homeDir = homeDirFile.path;
	LOG(homeDir);
	
	if (homeDirFile.exists()) {
		//file is present, so lets load it up
		var data = "";
		var fstream = Components.classes["@mozilla.org/network/file-input-stream;1"].
			createInstance(Components.interfaces.nsIFileInputStream);
		var cstream = Components.classes["@mozilla.org/intl/converter-input-stream;1"].
			createInstance(Components.interfaces.nsIConverterInputStream);
		fstream.init(homeDirFile, -1, 0, 0);
		cstream.init(fstream, "UTF-8", 0, 0); // you can use another encoding here if you wish

		var str = {};
		while (cstream.readString(4096, str) != 0) {
			data += str.value;
		}

		LOG(data.length);
		
		cstream.close(); // this closes fstream
		data = data.trim();
		urls = data.split("\n");
	} else {
		//just add a dummy url
		urls = "http://www.google.com".split("\n");
	}
	
	//LOG(urls);
	LOG(urls.length);
}

//given the window id, parse the html inside this window
function parsehtml(e) {
	if (processed) return;
	processed = true;
	
	try {
		//call run for next processing to occur
		var s = walk(wid.document.childNodes[1]);
		var dirService = Components.classes["@mozilla.org/file/directory_service;1"].
			getService(Components.interfaces.nsIProperties); 
		var homeDirFile = dirService.get("Home", Components.interfaces.nsIFile); // returns an nsIFile object
	
		homeDirFile.append("file-" + count)
		var foStream = Components.classes["@mozilla.org/network/file-output-stream;1"].
			createInstance(Components.interfaces.nsIFileOutputStream);

		// use 0x02 | 0x10 to open file for appending.
		foStream.init(homeDirFile, 0x02 | 0x08 | 0x20, 0666, 0); 
		// write, create, truncate
		// In a c file operation, we have no need to set file mode with or operation,
		// directly using "r" or "w" usually.

		// if you are sure there will never ever be any non-ascii text in data you can 
		// also call foStream.writeData directly
		var converter = Components.classes["@mozilla.org/intl/converter-output-stream;1"].
			createInstance(Components.interfaces.nsIConverterOutputStream);
    
		converter.init(foStream, "UTF-8", 0, 0);
		converter.writeString(s);
		converter.writeString("\n");
		converter.close(); // this closes foStream
	} catch (e) {
		LOG(e);
	} finally {
		try {
			wid.close();
		} catch (e) {
			LOG(e);
		} finally {
			run();
		}
	}
}

//main run loop
function run() {
	if (count < urls.length) {
		//get the next url
		var currUrl = urls[count];
		count += 1;
		
		//open the window
		var strWindowFeatures = "menubar=no,location=no,resizable=no,scrollbars=no,status=no,width=480,height=600";
		
		LOG(currUrl);
		
		wid = window.open(currUrl,currUrl,strWindowFeatures);
		var wm = Components.classes["@mozilla.org/appshell/window-mediator;1"]  
			.getService(Components.interfaces.nsIWindowMediator);  
		var newWindow = wm.getMostRecentWindow("navigator:browser");  
		var b = newWindow.gBrowser;
		
		processed = false;
		//call html parse
		b.addEventListener("DOMContentLoaded", parsehtml, true);
	}
}

function start() {
	count = 0;
	loadurls();
	run();
}
