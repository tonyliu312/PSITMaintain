package uap.ps.itm.npr.service.schedule;

import java.text.SimpleDateFormat;
import java.util.Date;

import nc.monitor.Exception.RemoteException;
import nc.monitor.pub.HashVO;
import nc.monitor.pub.Toolkit;
import uap.ps.itm.npr.service.INPRConst;
import uap.ps.itm.npr.service.ReportCentral;
import nc.vo.pub.lang.UFDateTime;

/**
 * NPR调度线程
 * 
 * @author liuzy
 * 
 */
public class NPRScheduleThread extends Thread {

	public static final int PROC_REPORT = 0;

	public static final int PROC_CLEAR = 1;

	private boolean isRun = false;

	public static final int MILLIS_PER_HOUR = 60 * 60 * 1000;

	private int proc_type = -1;

	public NPRScheduleThread(String name, int procType) {
		super(name);
		this.proc_type = procType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		while (this.isRun) {
			try {
				proccessTask(false);
				sleep(1000 * 60);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void proccessTask(boolean noCheck) {
		switch (this.proc_type) {
		case NPRScheduleThread.PROC_REPORT:
			procSchedule(noCheck);
			break;
		case NPRScheduleThread.PROC_CLEAR:
			procClear();
			break;
		}
	}

	private void procClear() {
		HashVO configVO = NPRScheduleConfig.getInstance().getCurrNprCfg();
		if (Toolkit.isEmpty(configVO))
			return;
		String enable = configVO
				.getStringValue(INPRConst.NPR_CONFIG_CLEAR_TIMEING_ENABLE);
		if (Toolkit.isEmpty(enable) || "N".equals(enable))
			return;
		String time = configVO.getStringValue(INPRConst.NPR_CONFIG_CLEAR_TIME);
		if (Toolkit.isEmpty(time))
			return;
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		ReportCentral nprCentral = new ReportCentral();
		try {
			// time = sdf.format(new Date(new UFDateTime(time).getMillis()));
			String currTime = sdf.format(new Date());
			if (currTime.equals(time)) {
				nprCentral.clearNPRByDays();
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void procSchedule(boolean noCheck) {
		HashVO configVO = NPRScheduleConfig.getInstance().getCurrNprCfg();
		if (Toolkit.isEmpty(configVO))
			return;
		String enable = configVO
				.getStringValue(INPRConst.NPR_CONFIG_SCHEDULE_ENABLE);
		if (Toolkit.isEmpty(enable) || "N".equals(enable))
			return;
		String firstTime = configVO
				.getStringValue(INPRConst.NPR_CONFIG_FIRST_RUN_TIME);
		if (Toolkit.isEmpty(firstTime))
			return;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		ReportCentral nprCentral = new ReportCentral();
		try {
			firstTime = sdf.format(new Date(new UFDateTime(firstTime)
					.getMillis()));
			String currTime = sdf.format(new Date());
			String shotPath = null;
			String nprDir = null;
			UFDateTime ufdt_firstDateTime = new UFDateTime(firstTime + ":00");
			UFDateTime ufdt_currDateTime = new UFDateTime(currTime + ":00");
			int interval = configVO
					.getIntegerValue(INPRConst.NPR_CONFIG_INTERVAL_HOURS);
			if (noCheck) {
				shotPath = nprCentral.doSnapshot();
				nprDir = nprCentral.creatReport(shotPath, new UFDateTime(
						ufdt_currDateTime.getMillis()
								- (MILLIS_PER_HOUR * interval)),
						ufdt_currDateTime);
				nprCentral.sendMail(nprDir);
				return;
			}
			if (currTime.equals(firstTime)) {
				shotPath = nprCentral.doSnapshot();
				nprDir = nprCentral.creatReport(shotPath, null, null);
				nprCentral.sendMail(nprDir);
				return;
			}
			if (ufdt_currDateTime.compareTo(ufdt_firstDateTime) < 0)
				return;
			int hours = UFDateTime.getHoursBetween(ufdt_firstDateTime,
					ufdt_currDateTime);
			if (hours > 0 && hours % interval == 0) {
				if (/*
					 * (ufdt_firstDateTime.getHour() != ufdt_currDateTime
					 * .getHour()) ||
					 */(ufdt_firstDateTime.getMinute() != ufdt_currDateTime
						.getMinute())) {
					return;
				}
				shotPath = nprCentral.doSnapshot();
				// 默认分析时间段为，最后一次到目前的所有日志
				nprDir = nprCentral.creatReport(shotPath, new UFDateTime(
						ufdt_currDateTime.getMillis()
								- (MILLIS_PER_HOUR * interval)),
						ufdt_currDateTime);
				nprCentral.sendMail(nprDir);
			}
		} catch (RemoteException ex) {
			try {
				sleep(1000 * 6000);
				this.proccessTask(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * @param isRun
	 *            the isRun to set
	 */
	public void setRun(boolean isRun) {
		this.isRun = isRun;
	}

}
