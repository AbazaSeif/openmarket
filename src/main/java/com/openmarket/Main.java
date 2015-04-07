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


	@Option(name="-test",usage="Run using the Bitcoin testnet3, and a test DB")
	private boolean testnet;

	@Option(name="-deleteDB",usage="Delete the sqlite DB before running.")
	private boolean deleteDB;

	@Option(name="-loglevel", usage="Sets the log level [INFO, DEBUG, etc.]")     
	private String loglevel = "INFO";

	@Option(name="-masternode", usage="Startup OpenMarket with a different master DB node" + 
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
		DataSources.HOME_DIR = (testnet) ? DataSources.HOME_DIR  + "/testnet" : DataSources.HOME_DIR;
		
//		Tools.dbInit();
//		Tools.dbClose();
		
//		com.bitmerchant.tools.Tools.dbInit();
//		Actions.ButtonActions.listButtons();
//		com.bitmerchant.tools.Tools.dbClose();
	

		// Initialize the replicated db
		Tools.initializeDBAndSetupDirectories(deleteDB);
		
		com.bitmerchant.tools.DataSources.HOME_DIR = DataSources.HOME_DIR;

		LocalWallet.startService(DataSources.HOME_DIR, loglevel, testnet, deleteDB, false);
//		
//		com.bitmerchant.tools.Tools.dbInit();
//		log.info(com.bitmerchant.tools.DataSources.DB_FILE());
//		log.info(Currency.findAll().toJson(true));
//		com.bitmerchant.tools.Tools.dbClose();
		
		// Start the webservice
		WebService.start();
		
		// Start up the currency converter just to pre cache it
		try {
			CurrencyConverter.INSTANCE.getBtcRatesCache().get(CurrencyUnit.of("USD"));
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		

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
