import java.io.*

def code = ""
def output
def buffers

if(params.code != null){
        code = params.code

        def writer = new StringWriter()
        def shell = new GroovyShell()
        shell.setProperty("out",writer)

        try{
                output = shell.evaluate(code)
        }
        catch(e){
                e.printStackTrace(new PrintWriter(writer))
        }

        buffers =  writer.getBuffer().toString().split("\n")
}


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

"""
        }
        body{
                h1 "Enter your Groovy code:"
                form(method:"post"){
                        textarea(id:"code", name:"code", rows:"40", cols:"80", "${code}")
                        input(type:"submit")
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


                h1 "Output:"
                if(output != null) p(output)
                if(buffers != null)
                buffers.each{
                        p(it)
                }

        }
}

