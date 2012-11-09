import java.io.*

//def shell = application.shell ?: {application.shell = new GroovyShell();application.shell}

def output


if (!session) {
	session = request.getSession(true)
}

if(!application.externalFiles){
	
	def externalFiles = ''
	
	new File(context.getRealPath("/")).eachFileRecurse groovy.io.FileType.FILES, {
		def relativePath =  {i -> i.path.substring( i.path.indexOf("codemirror")).replace('\\','/')}
		switch(it.name){
			case ~'^.*\\.js$':
			externalFiles <<= '<script src="' + relativePath(it) + '"></script>\n'
			break
			
			case ~'^.*\\.css$':
			externalFiles <<= '<link rel="stylesheet" href="' +  relativePath(it) + '">\n'
			break
		}
	}
	
	application.externalFiles = externalFiles
}

def baseDir = System.getProperty("user.home") + "/groovy_script/"

new File(baseDir).mkdirs()

def code = params.code ?: ""

def file = params.file ?: "~"



html.html{
	head{
		title("Groovy Web Console")
		
		//println new File(context.getRealPath("/") + "/" + context.contextPath)

println """
${application.externalFiles}

<style type="text/css">
.CodeMirror {border-top: 1px solid #500; border-bottom: 1px solid #500;}
.CodeMirror-fullscreen {
        display: block;
        position: absolute;
        top: 0; left: 0;
        width: 100%;
        z-index: 9999;
       }
.cm-tab {
         background: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAMCAYAAAAkuj5RAAAAAXNSR0IArs4c6QAAAGFJREFUSMft1LsRQFAQheHPowAKoACx3IgEKtaEHujDjORSgWTH/ZOdnZOcM/sgk/kFFWY0qV8foQwS4MKBCS3qR6ixBJvElOobYAtivseIE120FaowJPN75GMu8j/LfMwNjh4HUpwg4LUAAAAASUVORK5CYII=);
         background-position: right;
         background-repeat: no-repeat;       
.CodeMirror-gutter {
        width: 3em;
        background: white;
      }         
.CodeMirror-scroll {
  height: auto;
  overflow-y: hidden;
  overflow-x: auto;
}
</style>
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

		println """Theme: <select onchange="selectTheme()" id=select>
    <option selected>default</option>
    <option>ambiance</option>
    <option>blackboard</option>
    <option>cobalt</option>
    <option>eclipse</option>
    <option>elegant</option>
    <option>erlang-dark</option>
    <option>lesser-dark</option>
    <option>monokai</option>
    <option>neat</option>
    <option>night</option>
    <option>rubyblue</option>
    <option>vibrant-ink</option>
    <option>xq-dark</option>
</select>"""
					
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
			input(type:"button", value:"format", onclick:"autoFormatSelection()")
		}
		println """
 <script>
 
     function isFullScreen(cm) {
      return /\bCodeMirror-fullscreen\b/.test(cm.getWrapperElement().className);
    }
    function winHeight() {
      return window.innerHeight || (document.documentElement || document.body).clientHeight;
    }
    function setFullScreen(cm, full) {
      var wrap = cm.getWrapperElement(), scroll = cm.getScrollerElement();
      if (full) {
        wrap.className += " CodeMirror-fullscreen";
        scroll.style.height = winHeight() + "px";
        document.documentElement.style.overflow = "hidden";
      } else {
        wrap.className = wrap.className.replace(" CodeMirror-fullscreen", "");
        scroll.style.height = "";
        document.documentElement.style.overflow = "";
      }
      cm.refresh();
    }
    CodeMirror.connect(window, "resize", function() {
      var showing = document.body.getElementsByClassName("CodeMirror-fullscreen")[0];
      if (!showing) return;
      showing.CodeMirror.getScrollerElement().style.height = winHeight() + "px";
    });
 
      var editor = CodeMirror.fromTextArea(document.getElementById("code"), {
        lineNumbers: true,
        matchBrackets: true,        
        tabSize: 2,
        indentUnit: 2,
        mode: 'text/x-groovy',
        extraKeys: {
          'F11': function(cm) {setFullScreen(cm, !isFullScreen(cm));},
          'Esc': function(cm) {if (isFullScreen(cm)) setFullScreen(cm, false);}
        }
      }
      );
   function getSelectedRange() {
        return { from: editor.getCursor(true), to: editor.getCursor(false) };
      }
      
      function autoFormatSelection() {
        var range = getSelectedRange();
        editor.autoFormatRange(range.from, range.to);
      }
      
      function commentSelection(isComment) {
        var range = getSelectedRange();
        editor.commentRange(isComment, range.from, range.to);
      }            
      
  var input = document.getElementById("select");
  function selectTheme() {
    var theme = input.options[input.selectedIndex].innerHTML;
    editor.setOption("theme", theme);
  }
  var choice = document.location.search && document.location.search.slice(1);
  if (choice) {
    input.value = choice;
    editor.setOption("theme", choice);
  }      
            
    </script>
"""
		h2 "Output:"
		println output?:""
		
		h5 "Versions: Java=" + System.properties["java.version"] + ", Groovy=" + GroovySystem.version

	
		//for(i in session.getAttributeNames())
		//h5 i + "->" + session[i]


		

	}
}
