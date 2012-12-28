<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>	
<%-- @ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"--%>
<HTML>
<HEAD>
<TITLE>Groovy Web Console</TITLE>

<%@ include file="/include.inc"%>

<style type="text/css">
.CodeMirror {
	border-top: 1px solid #500;
	border-bottom: 1px solid #500;
	height: auto;
}

.CodeMirror-scroll {
  height: auto;
  overflow-y: hidden;
  overflow-x: auto;
}

.CodeMirror-gutter {
	width: 3em;
	background: white;
}

.CodeMirror-fullscreen {
	display: block;
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	z-index: 9999;
}

.cm-tab {
	background:
		url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAMCAYAAAAkuj5RAAAAAXNSR0IArs4c6QAAAGFJREFUSMft1LsRQFAQheHPowAKoACx3IgEKtaEHujDjORSgWTH/ZOdnZOcM/sgk/kFFWY0qV8foQwS4MKBCS3qR6ixBJvElOobYAtivseIE120FaowJPN75GMu8j/LfMwNjh4HUpwg4LUAAAAASUVORK5CYII=);
	background-position: right;
	background-repeat: no-repeat;
}

</style>
<script src="jquery-1.8.2.js"></script>

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
      
  
  function selectTheme() {
	var selecttheme = document.getElementById("selecttheme");
    var theme = selecttheme.options[selecttheme.selectedIndex].innerHTML;
    editor.setOption("theme", theme);
  }
  var choice = document.location.search && document.location.search.slice(1);
  if (choice) {
	  var selecttheme = document.getElementById("selecttheme");
	  selecttheme.value = choice;
    editor.setOption("theme", choice);
  }           
    </script>


</HEAD>
<BODY><h4><%=    request.getAttribute("info") %></h4>
	<FORM method="post">
		Theme:<SELECT id="selecttheme" onchange="selectTheme()"> 			
			<option>default</option>
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
            </SELECT>


		<A href="http://groovy.codehaus.org/api" target="_blank">gapi</A> <A
			href="http://groovy.codehaus.org/groovy-jdk/" target="_blank">gjdk-api</A>

		<h3><%= request.getAttribute("message") %></h3>

		<h2>GWC Enter code:</h2>

		<textarea id="code" name="code" ><%= request.getAttribute("code") %></textarea>

		<select name="file"
			onchange="forms[0].action.value='changefile';forms[0].submit()">
			<option>~</option>
			<% for(Object f : (ArrayList) request.getAttribute("files")){ 
          ArrayList fileAttrs = (ArrayList) f;                          
        	%>
			<option <%= fileAttrs.get(0)  %> value="<%= fileAttrs.get(1) %>">
				<%= fileAttrs.get(1) %><%= fileAttrs.get(2) %>
			</option>
			<% } %>
		</select> <input type="hidden" name="newfile" /> <input type="hidden"
			name="action" /> <input type="button" value="run"
			onclick="forms[0].action.value='run';forms[0].submit()" /> <input
			type="button" value="save"
			onclick="forms[0].newfile.value = prompt ('filename ?','<%= request.getParameter("file") %>');if(forms[0].newfile.value) forms[0].action.value='save';forms[0].submit()" />
		<input type="button" value="reload"
			onclick="forms[0].action.value='reload';forms[0].submit()" /> <input
			type="button" value="clear"
			onclick="forms[0].action.value='clear';forms[0].submit()" /> <input
			type="button" value="format" onclick="autoFormatSelection()" />


		<script>
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
	        });
		
    </script>

		<h2>Output:</h2><%= request.getAttribute("output") %>
		
				

	</FORM>
</BODY>
</HTML>


