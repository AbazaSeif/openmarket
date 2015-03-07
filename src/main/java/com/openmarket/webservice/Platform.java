package com.openmarket.webservice;

import static spark.Spark.post;
import static spark.Spark.get;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmarket.db.Tables.User;
import com.openmarket.db.actions.Actions.UserActions;
import com.openmarket.tools.DataSources;
import com.openmarket.tools.Tools;

public class Platform {

	static final Logger log = LoggerFactory.getLogger(Platform.class);


	public static void setup() {

		

		
		get("/home/:derp", (req, res) -> {
			Tools.allowOnlyLocalHeaders(req, res);	
			return "ur a NEWB";
		});

		post("/send_signup_email", (req, res) -> {
			try {
				Tools.allowAllHeaders(req, res);
				Tools.logRequestInfo(req);
				String email = Tools.createMapFromAjaxPost(req.body()).get("email");

				Tools.dbInit();

				User user = UserActions.createUserSimple(email);

				String message = UserActions.sendSignUpEmail(user);

				Tools.dbClose();

				return message;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			}

		});

		post("/set_password", (req, res) -> {
			try {
				Tools.allowAllHeaders(req, res);
				Tools.logRequestInfo(req);

				Map<String, String> vars = Tools.createMapFromAjaxPost(req.body());
				String password = vars.get("password");
				String token = vars.get("token");

				Tools.dbInit();
				User user = UserActions.signupUserWithToken(token, password);
				String email = user.getString("email");

				String message = UserActions.userLogin(email, password, res);

				Tools.dbClose();

				return message;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			}

		});

		post("/login", (req, res) -> {
			try {
				Tools.allowAllHeaders(req, res);
				Tools.logRequestInfo(req);

				Map<String, String> vars = Tools.createMapFromAjaxPost(req.body());
				String password = vars.get("password");
				String email = vars.get("email");

				Tools.dbInit();

				String message = UserActions.userLogin(email, password, res);

				Tools.dbClose();

				return message;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			}

		});

		post("/logout", (req, res) -> {
			try {
				Tools.allowAllHeaders(req, res);
				Tools.logRequestInfo(req);

				String auth = req.cookie("authenticated_session_id");

				// remove the key, and save the map
				Tools.dbInit();
				String message = UserActions.userLogout(auth);
				Tools.dbClose();


				return message;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			}

		});
		
		
		
		// All the simple webpages
		get("/:page", (req, res) -> {
			Tools.allowOnlyLocalHeaders(req, res);	
			String pageName = req.params(":page");
			return Tools.readFile(DataSources.PAGES(pageName));
		});
		
		get("/", (req, res) -> {
			Tools.allowOnlyLocalHeaders(req, res);	
			return Tools.readFile(DataSources.PAGES("home"));
		});
	}
}
