#!/usr/bin/env groovy

@Grab('log4j:log4j:1.2.17')

	import org.apache.commons.dbcp.*
  import org.apache.log4j.*
  import groovy.util.logging.*
  
  abstract class BaseScript extends Script{
    
    def log
    def bindings
    
    BaseScript(){
    }
    
    def init(bindings1){
      this.bindings = bindings1
      def logFile =  System.getProperty("java.io.tmpdir") + '/' + bindings.self + '.log.' + String.format('%tF', new Date())
      BasicConfigurator.configure(new FileAppender(new PatternLayout("%d{ABSOLUTE} %-5p [%c{1}] %m%n"),logFile))
      LogManager.rootLogger.level = Level.INFO
      log = Logger.getRootLogger()
    }
    
    def run(script,args=null){
      return this.run(new File(bindings.baseDir + '/' + script),args)
    }
    
  }