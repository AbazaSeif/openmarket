package openmarket;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.openmarket.db.Tables.User;
import com.openmarket.tools.Tools;

public class TesterDerp extends TestCase {
	static final Logger log = LoggerFactory.getLogger(TesterDerp.class);
	
	public void setUp() {
		Tools.initializeDBAndSetupDirectories(false);
	}
	
	public void testCreateUser() throws InterruptedException {
			
		
		Tools.dbInit();
		String stmt = User.create("name", "derp", "password_encrypted", "blarpy").toInsert();
		
		Tools.writeRQL(stmt);
		
		assertTrue(!User.where("name = ?", "derp").isEmpty());
		Tools.dbClose();
		
	}
	
	
	

	

}
