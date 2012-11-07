import java.io.*

//def shell = application.shell ?: {application.shell = new GroovyShell();application.shell}

def output


if (!session) {
	session = request.getSession(true)
}

def baseDir = System.getProperty("user.home") + "/groovy_script/"

new File(baseDir).mkdirs()

def code = params.code ?: ""

def file = params.file ?: "~"



html.html{
	head{
		title("Groovy Web Console")
		println """

<link rel="stylesheet" href="codemirror/codemirror.css">
<link rel="stylesheet" href="codemirror/theme/blackboard.css">
<link rel="stylesheet" href="codemirror/util/simple-hint.css">
<link rel="stylesheet" href="codemirror/util/dialog.css">

<script src="codemirror/codemirror.js"></script>
<script src="codemirror/groovy.js"></script>
<script src="codemirror/util/dialog.js"></script>
<script src="codemirror/util/search.js"></script>
<script src="codemirror/util/searchcursor.js"></script>
<script src="codemirror/util/simple-hint.js"></script>
<script src="codemirror/util/foldcode.js"></script>
<script src="codemirror/util/match-highlighter.js"></script>
<script src="codemirror/util/closetag.js"></script>

<style>.CodeMirror {border-top: 1px solid #500; border-bottom: 1px solid #500;}</style>
<script>
</script>
"""
	}
	
	def reload = {
		try{
				code = new File(baseDir + file).getText()
						session["file:" + file] = null
						h2 "Loaded from file :" + file
					}
					catch(e){
		h2 "Load Failed: "  + file + "->" + e
	}}
	body{
		
		switch(params.action){
			case "clear":
				session.invalidate()
				session = request.getSession(true)
				break
				
			case "reload":
				reload.call()
				break

			case "changefile":
				//Save prev buffer
				def prevfile = session["prevfile"] ?: "~"
				//Save in buffer if code is diff
				def origtext
				try{
					origtext = new File(baseDir + prevfile).getText()
				}catch(e){}
				if(code != origtext){
					session["file:" + prevfile] = code
				}
									
				//Load from file if not in buffer	
				if(!session["file:" + file]){
					reload.call()			
				}
				else{
					code = session["file:" + file]
					h3 "Loaded from buffer :" + file
				}
				
				session["prevfile"] = file
				break
				
			case "save":
				file = params.newfile.trim()
				try{
					new File(baseDir + file).write(code)
					session["file:" + file] = null
					h3 "Saved " + file
				}
				catch(e){
					h3 "Save Failed:" + file  + e
				}
				session["prevfile"] = file
				break
				
			case "run":
			
				def shell = new GroovyShell()
				def writer = new StringWriter()
				shell.out = writer

				try{
					output ="RET-> " + shell.evaluate(code) + "<BR>"
					output ="OUT->" + writer.buffer
				}
				catch(e){
					e.printStackTrace(new PrintWriter(writer))
					output ="ERR->" + writer.buffer.replaceAll("\n", "<BR>")
				}
				break
		}
		
		h5 "Base dir->" + baseDir
					
		form(method:"post"){
			h2 "GWC Enter code:"
			textarea(id:"code", name:"code", rows:"40", cols:"80", code)
			select(name:"file",onchange:"forms[0].action.value='changefile';forms[0].submit()"){
				option("~")
				new File(baseDir).eachFile { 
					//show * if in buffer
					def mod = session["file:" + it.name] ? "*" : ""
					println "<option ${it.name==file ?'selected':''} value=${it.name} >${it.name}${mod}</option>"
				}
			}			
			input(type:"hidden", name:"newfile")
			input(type:"hidden", name:"action")
			input(type:"button", value:"run", onclick:"forms[0].action.value='run';forms[0].submit()")
			input(type:"button", value:"save", onclick:"forms[0].newfile.value = prompt ('filename ?','${file}');if(forms[0].newfile.value) forms[0].action.value='save';forms[0].submit()")
			input(type:"button", value:"reload", onclick:"forms[0].action.value='reload';forms[0].submit()")
			input(type:"button", value:"clear", onclick:"forms[0].action.value='clear';forms[0].submit()")
		}
		println """
 <script>
      var editor = CodeMirror.fromTextArea(document.getElementById("code"), {
        lineNumbers: true,
        matchBrackets: true,
        mode: "text/x-groovy"
      });
    </script>
"""
		h2 "Output:"
		println output?:""
		
		h5 "Versions: Java=" + System.properties["java.version"] + ", Groovy=" + GroovySystem.version

	
		//for(i in session.getAttributeNames())
		//h5 i + "->" + session[i]


		

	}
}

