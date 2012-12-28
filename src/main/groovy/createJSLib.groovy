
def files = []
new File('target/generated-webapp/codemirror').eachFileRecurse groovy.io.FileType.FILES, {
  def relativePath =  {i ->
      def index = i.path.indexOf('codemirror')
        i.path.substring(index).replace('\\','/')
    }
    
    switch(it.name){
      
      case ~'^.*run.*$':
      case ~'^.*lint.js$':
      case ~'^.*parse-js.js$':
      case ~'^.*phantom.*$':
      case ~'^.*test.*$': 
        files << '<%-- Skipping ' + relativePath(it) + ' --%>\n'
        break
      
      case ~'^.*\\.js$':
        def text = '<script src="' + relativePath(it) + '"></script>\n'
        if(it.name ==~ '^.*codemirror\\.js$'){         
          files.add(0,text)
        }
        else
          files << text
        break

      case ~'^.*\\.css$':
        def text = '<link rel="stylesheet" href="' +  relativePath(it) + '"/>\n'
        if(it.name ==~ '^.*codemirror\\.css$'){         
          files.add(0,text)
        }
        else
          files << text
        break
    }
 }

def f = new File('target/generated-webapp/include.inc')
f.write ''
files.each{
  f.append it
}



 