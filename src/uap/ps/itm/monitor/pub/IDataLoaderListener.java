package uap.ps.itm.monitor.pub;

public interface IDataLoaderListener {
	public void processEvent(DataloaderEvent event) throws Exception;
}
