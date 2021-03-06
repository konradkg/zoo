package zoo.daroo.h2.mem.spring;

import java.sql.SQLException;

import org.h2.tools.Server;

public class TcpServerBean extends AbstractServerBean {
	
	public final static String BEAN_ID = "TcpServerBean";

	@Override
	protected Server createServer(String... args) throws SQLException {
		return Server.createTcpServer(args);
	}
	
	public void setAllowOthers(boolean allowOthers) {
		if(allowOthers) { 
			parameters.add("-tcpAllowOthers");
		}
	}

}
