
def f = new File('target/generated-webapp/include.inc')
f.write ''
new File('target/generated-webapp/codemirror').eachFileRecurse groovy.io.FileType.FILES, {
  def relativePath =  {i ->
      def index = i.path.indexOf('codemirror')
        i.path.substring(index).replace('\\','/')
    }
    
    switch(it.name){
      case ~'^.*\\.js$':
        f.append '<script src="' + relativePath(it) + '"></script>\n'
        break

      case ~'^.*\\.css$':
        f.append '<link rel="stylesheet" href="' +  relativePath(it) + '">\n'
        break
    }
 }


 