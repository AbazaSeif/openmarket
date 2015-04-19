package com.openmarket;

import java.util.concurrent.ExecutionException;

import org.joda.money.CurrencyUnit;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.bitmerchant.tools.CurrencyConverter;
import com.bitmerchant.wallet.LocalWallet;
import com.openmarket.tools.DataSources;
import com.openmarket.tools.Tools;
import com.openmarket.webservice.WebService;

public class Main {

	static  Logger log = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);


	@Option(name="-testnet",usage="Run using the Bitcoin testnet3, and a test DB")
	private boolean testnet;

	@Option(name="-deleteDB",usage="Delete the sqlite DB before running.(WARNING, this deletes your wallet)")
	private boolean deleteDB;
	
	@Option(name="-uninstall",usage="Uninstall OpenMarket.(WARNING, this deletes your wallet)")
	private boolean uninstall;

	@Option(name="-loglevel", usage="Sets the log level [INFO, DEBUG, etc.]")     
	private String loglevel = "INFO";

	@Option(name="-join", usage="Startup OpenMarket joining a master node" + 
			"IE, 127.0.0.1:4001")   
	private String customMasterNode;
	
	@Option(name="-rqlport", usage="Startup rql on a different port(default is 4570)")
	private Integer customRqlPort;

	@Option(name="-port", usage="Startup your webserver on a different port(default is 4567)")
	private Integer port;
	
	




	public void doMain(String[] args) {


		parseArguments(args);
		
		// get the correct network
		DataSources.TESTNET = testnet;
		com.bitmerchant.tools.DataSources.HOME_DIR = DataSources.HOME_DIR();
		
		// See if the user wants to uninstall it
		if (uninstall) {
			Tools.uninstall();
		}

		setRQLVars(customMasterNode);

		setPort(port);

		log.setLevel(Level.toLevel(loglevel));


		// Make sure go1.4 is installed
		Tools.ensureGoInstalled();

		// Initialize the replicated db
		Tools.initializeDBAndSetupDirectories(deleteDB);

		//		Tools.initializeSSL();

		Tools.addExternalWebServiceVarToTools();

		// Start the bitmerchant wallet
		LocalWallet.startService(DataSources.HOME_DIR(), loglevel, testnet, deleteDB, false);

		// Start the webservice
		WebService.start();
		
		// Load up in a browser
		Tools.pollAndOpenStartPage();

		Tools.cacheCurrency("USD");


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
			System.err.println("java -jar openmarket.jar [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();
			System.exit(0);


			return;
		}
	}

	private void setRQLVars(String customMasterNode) {
		if (customMasterNode != null) {
			String[] split = customMasterNode.split(":");

			DataSources.RQL_MASTER_NODE_IP = split[0];
			DataSources.RQL_MASTER_NODE_PORT = Integer.parseInt(split[1]);
		} else {
			DataSources.RQL_MASTER_NODE_PORT = (!DataSources.TESTNET) ? 
					DataSources.RQL_MAIN_PORT:
						DataSources.RQL_TEST_PORT;
		}
		
		if (customRqlPort != null) {
			DataSources.RQL_MY_NODE_PORT = customRqlPort;
		}
		
		
	}

	private void setPort(Integer port) {
		if (port != null) {
			DataSources.SPARK_WEB_PORT = port;
		}

	}



}
