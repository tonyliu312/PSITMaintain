/**
 * 
 */
package uap.ps.itm.npr.service.schedule;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import nc.monitor.pub.HashVO;
import nc.monitor.pub.IConfigConst;
import nc.monitor.pub.Toolkit;
import uap.ps.itm.npr.service.INPRConst;

/**
 * NPR调度信息设置与读取
 * 
 * @author Tony
 * 
 */
public class NPRScheduleConfig {

	private static NPRScheduleConfig instance = null;

	private Properties prop = null;

	private NPRScheduleConfig() {

	}

	public static NPRScheduleConfig getInstance() {
		if (null == instance)
			instance = new NPRScheduleConfig();
		return instance;
	}

	private Properties getProp() {
		if (Toolkit.isEmpty(prop)) {
			prop = new Properties();
			try {
				prop.load(new FileInputStream(IConfigConst.NPRCFGFILE));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return prop;
	}

	public void saveConfig(HashVO configVO) {
		try {
			String isEnable = configVO
					.getStringValue(INPRConst.NPR_CONFIG_SCHEDULE_ENABLE);
			if (!Toolkit.isEmpty(isEnable))
				getProp().setProperty(INPRConst.NPR_CONFIG_SCHEDULE_ENABLE,
						isEnable);
			String firstRunTime = configVO
					.getStringValue(INPRConst.NPR_CONFIG_FIRST_RUN_TIME);
			if (!Toolkit.isEmpty(firstRunTime))
				getProp().setProperty(INPRConst.NPR_CONFIG_FIRST_RUN_TIME,
						firstRunTime);
			String intervalHours = configVO
					.getStringValue(INPRConst.NPR_CONFIG_INTERVAL_HOURS);
			if (!Toolkit.isEmpty(intervalHours))
				getProp().setProperty(INPRConst.NPR_CONFIG_INTERVAL_HOURS,
						intervalHours);
			String emailText = configVO
					.getStringValue(INPRConst.NPR_CONFIG_EMAIL_ADDR);
			if (!Toolkit.isEmpty(emailText))
				getProp().setProperty(INPRConst.NPR_CONFIG_EMAIL_ADDR,
						emailText);
			String clearTimeEnable = configVO
					.getStringValue(INPRConst.NPR_CONFIG_CLEAR_TIMEING_ENABLE);
			if (!Toolkit.isEmpty(clearTimeEnable))
				getProp().setProperty(
						INPRConst.NPR_CONFIG_CLEAR_TIMEING_ENABLE,
						clearTimeEnable);
			String clearOnStartEnable = configVO
					.getStringValue(INPRConst.NPR_CONFIG_CLEAR_RESTART_ENABLE);
			if (!Toolkit.isEmpty(clearOnStartEnable))
				getProp().setProperty(
						INPRConst.NPR_CONFIG_CLEAR_RESTART_ENABLE,
						clearOnStartEnable);
			String cleartTime = configVO
					.getStringValue(INPRConst.NPR_CONFIG_CLEAR_TIME);
			if (!Toolkit.isEmpty(cleartTime))
				getProp().setProperty(INPRConst.NPR_CONFIG_CLEAR_TIME,
						cleartTime);
			String clearDays = configVO
					.getStringValue(INPRConst.NPR_CONFIG_CLEAR_SAVE_DAYS);
			if (!Toolkit.isEmpty(clearDays))
				getProp().setProperty(INPRConst.NPR_CONFIG_CLEAR_SAVE_DAYS,
						clearDays);
			getProp().store(new FileOutputStream(IConfigConst.NPRCFGFILE),
					"NPR配置");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public HashVO getCurrNprCfg() {
		File f = new File(IConfigConst.NPRCFGFILE);
		if (!f.exists())
			return null;

		String scheduleEnable = getProp().getProperty(
				INPRConst.NPR_CONFIG_SCHEDULE_ENABLE);
		String firstRunTime = getProp().getProperty(
				INPRConst.NPR_CONFIG_FIRST_RUN_TIME);
		String intervalHours = getProp().getProperty(
				INPRConst.NPR_CONFIG_INTERVAL_HOURS);
		String emailText = getProp().getProperty(
				INPRConst.NPR_CONFIG_EMAIL_ADDR);
		String clearTimeEnable = getProp().getProperty(
				INPRConst.NPR_CONFIG_CLEAR_TIMEING_ENABLE);
		String clearOnStartEnable = getProp().getProperty(
				INPRConst.NPR_CONFIG_CLEAR_RESTART_ENABLE);
		String clearTime = getProp().getProperty(
				INPRConst.NPR_CONFIG_CLEAR_TIME);
		String clearDays = getProp().getProperty(
				INPRConst.NPR_CONFIG_CLEAR_SAVE_DAYS);

		HashVO vo = new HashVO();
		vo.setAttributeValue(INPRConst.NPR_CONFIG_SCHEDULE_ENABLE,
				scheduleEnable);
		vo.setAttributeValue(INPRConst.NPR_CONFIG_FIRST_RUN_TIME, firstRunTime);
		vo.setAttributeValue(INPRConst.NPR_CONFIG_INTERVAL_HOURS, intervalHours);
		vo.setAttributeValue(INPRConst.NPR_CONFIG_EMAIL_ADDR, emailText);
		vo.setAttributeValue(INPRConst.NPR_CONFIG_CLEAR_TIMEING_ENABLE,
				clearTimeEnable);
		vo.setAttributeValue(INPRConst.NPR_CONFIG_CLEAR_RESTART_ENABLE,
				clearOnStartEnable);
		vo.setAttributeValue(INPRConst.NPR_CONFIG_CLEAR_TIME, clearTime);
		vo.setAttributeValue(INPRConst.NPR_CONFIG_CLEAR_SAVE_DAYS, clearDays);
		return vo;
	}

}
