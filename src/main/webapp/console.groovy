//def shell = application.shell ?: {application.shell = new GroovyShell();application.shell}



def output

if (!session) {
  session = request.getSession(true)
}

def baseDir = System.getProperty("user.home") + "/groovy_script/"

new File(baseDir).mkdirs()

def code = params.code ?: ""

def file = params.file ?: "~"

def classpath

def message 

def reload = {
  try{
    code = new File(baseDir + file).getText()
    session["file:" + file] = null
   message = "Loaded from file :" + file
}
catch(e){
  //h2 "Load Failed: "  + file + "->" + e
}}

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
      message =  "Loaded from buffer :" + file
    }

    session["prevfile"] = file
    break

  case "save":
    file = params.newfile.trim()
    try{
      new File(baseDir + file).write(code)
      session["file:" + file] = null
      message =  "Saved " + file
    }
    catch(e){
      message =  "Save Failed:" + file  + e
    }
    session["prevfile"] = file
    break

  case "run":

    def config = new org.codehaus.groovy.control.CompilerConfiguration()
    
    classpath = config.classpath
    
    config.setClasspath(baseDir)
    try{    
      config.setScriptBaseClass('BaseScript')
    }
    catch(e){
      e.printStackTrace(new PrintWriter(writer))
    }
    
    def binding = new Binding(['self':file,'baseDir':baseDir])
    
    def shell = new GroovyShell(binding,config)
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

def files = []

request.setAttribute('files',files)

request.setAttribute('output',output?:'')

request.setAttribute('message',message?:'')

request.setAttribute('code',code?:'')

request.setAttribute('info','Base dir->' + baseDir +
  ', Java->' + System.getProperty("java.version") + 
  //', Classpath->' + classpath +
  ', Groovy->' + GroovySystem.version
)


new File(baseDir).eachFile {
  //show * if in buffer
  def mod = session["file:" + it.name] ? "*" : ""
  files.add([it.name==file ?'selected':'',it.name,mod])   
}


context.getRequestDispatcher("/WEB-INF/jsp/console.jsp").forward(request,response)

