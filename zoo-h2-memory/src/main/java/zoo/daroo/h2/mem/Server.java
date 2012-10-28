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
	@Named(FlatFileLoader.BEAN_ID)
	private FlatFileLoader fileLoader;
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		internalDbManager.initDatabase();
		internalDbManager.createPexTempTable();
		fileLoader.load();
		
		//rename pex_temp and create FTL index
		internalDbManager.createFtlIndex();
		//DONE
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					internalDbManager.createPexTempTable();
					fileLoader.load();
					
					internalDbManager.switchTables();
					
				} catch (Exception e) {
					e.printStackTrace();
					//if failed DROP PEX_TEMP
				}
				
			}
		}).start();
		
		
		
	}
	
	@Override
	public void destroy() throws Exception {
		// TODO Auto-generated method stub
		
	}

	
}
