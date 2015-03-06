package com.openmarket;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.openmarket.tools.DataSources;
import com.openmarket.tools.Tools;
import com.openmarket.webservice.WebService;

public class Main {

	static  Logger log = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);


	@Option(name="-testnet",usage="Run using the Bitcoin testnet3")
	private boolean testnet;

	@Option(name="-deleteDB",usage="Delete the sqlite DB before running.")
	private boolean deleteDB;

	@Option(name="-loglevel", usage="Sets the log level [INFO, DEBUG, etc.]")     
	private String loglevel = "INFO";

	@Option(name="-masterNode", usage="Startup OpenMarket with a different master DB node" + 
			"IE, 127.0.0.1:4001")   
	private String customMasterNode;
	
	@Option(name="-port", usage="Startup your webserver on a different port(default is 4567)")
	private Integer port;


	public void doMain(String[] args) {


		parseArguments(args);

		setRQLMasterNodeVars(customMasterNode);
		
		setPort(port);

		log.setLevel(Level.toLevel(loglevel));		

		// get the correct network
		//		params = (testnet) ? TestNet3Params.get() : MainNetParams.get();
		DataSources.HOME_DIR = (testnet) ? DataSources.HOME_DIR  + "/testnet" : DataSources.HOME_DIR;

		// Initialize the replicated db
		Tools.initializeDBAndSetupDirectories(deleteDB);

		// Start the webservice
		WebService.start();

		// Start the sellers wallet
		//		INSTANCE.init();



		//		Tools.pollAndOpenStartPage();


	}

	public static void main(String[] args) {
		new Main().doMain(args);

	}


	private void parseArguments(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);

		try {

			parser.parseArgument(args);

		} catch (CmdLineException e) {
			// if there's a problem in the command line,
			// you'll get this exception. this will report
			// an error message.
			System.err.println(e.getMessage());
			System.err.println("java LocalWallet [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();


			return;
		}
	}

	private void setRQLMasterNodeVars(String customMasterNode) {
		if (customMasterNode != null) {
			String[] split = customMasterNode.split(":");
			
			DataSources.MASTER_NODE_IP = split[0];
			DataSources.MASTER_NODE_PORT = split[1];
		}
	}
	
	private void setPort(Integer port) {
		if (port != null) {
			DataSources.SPARK_WEB_PORT = port;
		}
		
	}

}
