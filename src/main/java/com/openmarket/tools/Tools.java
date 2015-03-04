package com.openmarket.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.Model;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Request;
import spark.Response;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.openmarket.db.InitializeTables;

public class Tools {

	static final Logger log = LoggerFactory.getLogger(Tools.class);

	public static final Gson GSON = new Gson();
	public static final Gson GSON2 = new GsonBuilder().setPrettyPrinting().create();


	public static final DateTimeFormatter DTF2 = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss").
			withZone(DateTimeZone.UTC);
	public static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").
			withZone(DateTimeZone.UTC);

	public static final StrongPasswordEncryptor PASS_ENCRYPT = new StrongPasswordEncryptor();

	// Instead of using session ids, use a java secure random ID
	private static final SecureRandom RANDOM = new SecureRandom();

	
	public static String toUpdate(String tableName, String id, Object... namesAndValues) {
		
		
		StringBuilder s = new StringBuilder();
		
		log.info("got here 2");
		
		s.append("UPDATE " + tableName + " SET ");
		
		for (int i = 0; i < namesAndValues.length - 1; i+=2) {
			Object field = namesAndValues[i];
			Object value = namesAndValues[i+1];
			s.append(field + " = " + "'" + value + "'");
			
			if (i+2 < namesAndValues.length) {
				s.append(" , ");
			}
		}
		
		s.append(" WHERE id = " + id + ";");
		
		log.info("got here 3");
		
		return s.toString();
		
	}

	public static String parseMustache(Map<String, Object> vars, String templateFile) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		String templateString;
		try {
//			templateString = new String(java.nio.file.Files.readAllBytes(Paths.get(templateFile)));


			Writer writer = new OutputStreamWriter(baos);
			MustacheFactory mf = new DefaultMustacheFactory();
			Mustache mustache = mf.compile(new FileReader(templateFile), "example");
			mustache.execute(writer, vars);
			

			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String output = baos.toString();
		log.info(output);



		return output;






	}

	public static Properties loadProperties(String propertiesFileLocation) {

		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(propertiesFileLocation);

			// load a properties file
			prop.load(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prop;

	}


	public static String sendEmail(String email, String subject, String text) {

		Properties props = Tools.loadProperties(DataSources.EMAIL_PROP);
		final String username = props.getProperty("username");
		log.info("user-email-name = " + username);
		final String password =  props.getProperty("password");

		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props,
				new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("Noreply_bitpieces@gmail.com"));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(email));
			message.setSubject(subject);

			//			message.setText(text);
			message.setContent(text, "text/html");


			Transport.send(message);

			log.info("Done");

		} catch (MessagingException e) {
			throw new NoSuchElementException(e.getMessage());
		}

		String message = "Email sent to " + email;

		return message;

	}

	public static String generateSecureRandom() {
		return new BigInteger(256, RANDOM).toString(32);
	}

	public static void allowOnlyLocalHeaders(Request req, Response res) {


		log.info("req ip = " + req.ip());


		//		res.header("Access-Control-Allow-Origin", "http://mozilla.com");
		//		res.header("Access-Control-Allow-Origin", "null");
		//		res.header("Access-Control-Allow-Origin", "*");
		//		res.header("Access-Control-Allow-Credentials", "true");

		if (!(req.ip().equals("127.0.0.1") || req.ip().equals("0:0:0:0:0:0:0:1"))) {
			throw new NoSuchElementException("Not a local ip, can't access");
		}
	}

	public static void allowAllHeaders(Request req, Response res) {
		String origin = req.headers("Origin");
		res.header("Access-Control-Allow-Credentials", "true");
		res.header("Access-Control-Allow-Origin", origin);


	}
	
	

	public static void logRequestInfo(Request req) {
		String origin = req.headers("Origin");
		String origin2 = req.headers("origin");
		String host = req.headers("Host");


		log.debug("request host: " + host);
		log.debug("request origin: " + origin);
		log.debug("request origin2: " + origin2);


		//		System.out.println("origin = " + origin);
		//		if (DataSources.ALLOW_ACCESS_ADDRESSES.contains(req.headers("Origin"))) {
		//			res.header("Access-Control-Allow-Origin", origin);
		//		}
		for (String header : req.headers()) {
			log.debug("request header | " + header + " : " + req.headers(header));
		}
		log.debug("request ip = " + req.ip());
		log.debug("request pathInfo = " + req.pathInfo());
		log.debug("request host = " + req.host());
		log.debug("request url = " + req.url());
	}

	public static final Map<String, String> createMapFromAjaxPost(String reqBody) {
		log.debug(reqBody);
		Map<String, String> postMap = new HashMap<String, String>();
		String[] split = reqBody.split("&");
		for (int i = 0; i < split.length; i++) {
			String[] keyValue = split[i].split("=");
			try {
				if (keyValue.length > 1) {
					postMap.put(URLDecoder.decode(keyValue[0], "UTF-8"),URLDecoder.decode(keyValue[1], "UTF-8"));
				}
			} catch (UnsupportedEncodingException |ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
				throw new NoSuchElementException(e.getMessage());
			}
		}

		log.debug(GSON2.toJson(postMap));

		return postMap;

	}

	public static void runScript(String path) {
		try {
			File file = new File(path);
			file.setExecutable(true);
			ProcessBuilder pb = new ProcessBuilder(
					path).inheritIO();
			Process p = pb.start();     // Start the process.
			log.info("Executing script " + path);
			p.waitFor();                // Wait for the process to finish.
			log.info("Script executed successfully");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static final void dbInit() {
		try {
			Base.open("org.sqlite.JDBC", "jdbc:sqlite:" + DataSources.DB_FILE, "root", "p@ssw0rd");
		} catch (DBException e) {
			dbClose();
			dbInit();
		}

	}

	public static final void dbClose() {
		Base.close();
	}

	public static final String httpGet(String url) {
		String res = "";
		try {
			URL externalURL = new URL(url);

			URLConnection yc = externalURL.openConnection();
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							yc.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) 
				res+="\n" + inputLine;
			in.close();

			return res;
		} catch(IOException e) {}
		return res;
	}

	public static void runSQLFile(Connection c,File sqlFile) {

		try {
			Statement stmt = null;
			stmt = c.createStatement();
			String sql;

			sql = Files.toString(sqlFile, Charset.defaultCharset());

			stmt.executeUpdate(sql);
			stmt.close();
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
	}

	public static void runRQLFile(File sqlFile) {
		try {
			String sql;

			sql = Files.toString(sqlFile, Charset.defaultCharset());

			writeRQL(sql);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String reformatSQLForRQL(String sql) {

		String reformat = sql.replace("\n", "").replace("\r", "").replace("'", "\"").replace(");", ");\n");;


		return reformat;
	}

	public static String writeRQL(String cmd) {

		String reformatted = reformatSQLForRQL(cmd);
		log.info("rql write string : " + reformatted);

		String postURL = DataSources.MASTER_NODE_URL + "/db?pretty";

		String message = "";
		try {

			CloseableHttpClient httpClient = HttpClients.createDefault();

			HttpPost httpPost = new HttpPost(postURL);
			httpPost.setEntity(new StringEntity(reformatted));

			ResponseHandler<String> handler = new BasicResponseHandler();

			CloseableHttpResponse response = httpClient.execute(httpPost);

			message = handler.handleResponse(response);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		message = "Rqlite write status : " + message;
		log.info(message);
		return message;
	}

	public static void initializeDBAndSetupDirectories(Boolean delete) {

		if (delete) {
			try {
				FileUtils.deleteDirectory(new File(DataSources.HOME_DIR));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		setupDirectories();

		copyResourcesToHomeDir();

		// Initialize the DB if it hasn't already
		InitializeTables.init(delete);


	}



	public static void setupDirectories() {
		if (!new File(DataSources.HOME_DIR).exists()) {
			log.info("Setting up ~/." + DataSources.APP_NAME + " dirs");
			new File(DataSources.HOME_DIR).mkdirs();
		} else {
			log.info("Home directory already exists");
		}
	}

	public static void copyResourcesToHomeDir() {


		String zipFile = null;

		if (!new File(DataSources.SOURCE_CODE_HOME).exists()) {
			log.info("Copying resources to  ~/." + DataSources.APP_NAME + " dirs");

			try {
				if (new File(DataSources.SHADED_JAR_FILE).exists()) {
					java.nio.file.Files.copy(Paths.get(DataSources.SHADED_JAR_FILE), Paths.get(DataSources.ZIP_FILE), 
							StandardCopyOption.REPLACE_EXISTING);
					zipFile = DataSources.SHADED_JAR_FILE;

				} else if (new File(DataSources.SHADED_JAR_FILE_2).exists()) {
					java.nio.file.Files.copy(Paths.get(DataSources.SHADED_JAR_FILE_2), Paths.get(DataSources.ZIP_FILE),
							StandardCopyOption.REPLACE_EXISTING);
					zipFile = DataSources.SHADED_JAR_FILE_2;
				} else {
					log.info("you need to build the project first");
				}
			} catch (IOException e) {

				e.printStackTrace();
			}
			Tools.unzip(new File(zipFile), new File(DataSources.SOURCE_CODE_HOME));
			//		new Tools().copyJarResourcesRecursively("src", configHome);
		} else {
			log.info("The source directory already exists");
		}
	}

	public static void unzip(File zipfile, File directory) {
		try {
			ZipFile zfile = new ZipFile(zipfile);
			Enumeration<? extends ZipEntry> entries = zfile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				File file = new File(directory, entry.getName());
				if (entry.isDirectory()) {
					file.mkdirs();
				} else {
					file.getParentFile().mkdirs();
					InputStream in = zfile.getInputStream(entry);
					try {
						copy(in, file);
					} finally {
						in.close();
					}
				}
			}

			zfile.close();


		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		while (true) {
			int readCount = in.read(buffer);
			if (readCount < 0) {
				break;
			}
			out.write(buffer, 0, readCount);
		}
	}

	private static void copy(File file, OutputStream out) throws IOException {
		InputStream in = new FileInputStream(file);
		try {
			copy(in, out);
		} finally {
			in.close();
		}
	}

	private static void copy(InputStream in, File file) throws IOException {
		OutputStream out = new FileOutputStream(file);
		try {
			copy(in, out);
		} finally {
			out.close();
		}
	}

}
