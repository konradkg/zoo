package zoo.daroo.h2.mem;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class Server implements InitializingBean, DisposableBean {

	@Inject
	@Named(InternalDbManager.BEAN_ID)
	private InternalDbManager internalDbManager;
	
	@Inject
	@Named(FlatFileManager.BEAN_ID)
	private FlatFileManager fileLoader;
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		internalDbManager.initDatabase();
		internalDbManager.createPexTempTable();
		fileLoader.init();
		//rename pex_temp and create FTL index
		internalDbManager.createFtlIndex();
		
		fileLoader.startWatch();
	}
	
	@Override
	public void destroy() throws Exception {
	}
	
}
