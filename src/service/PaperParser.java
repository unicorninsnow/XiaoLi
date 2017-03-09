package service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import util.StringUtil;
import buffer.UnitedBuffer;
import model.Log;
import model.MainInfo;
import model.MainStep;

public class PaperParser {
	private long lastTimeFileSize;
	
	private String paper;
	
	MainInfo mainInfo;
	private UnitedBuffer unBuffer;
	
	public PaperParser(String paper) {
		this.paper = paper;
		mainInfo = new MainInfo();
		lastTimeFileSize = 0;
	}
	
	public JSONObject poll() {
		String[] logs = getUpdateLog().split("\n");
		
		for(int i = 0; i < logs.length; i ++) {
			Log log = new Log(logs[i]);
			switch(log.getLogType()) {
			case Main:
				parseMain(log);
				break;
			case PaperInfo:
				parsePaperInfo(log);
				break;
			case ProblemInfo:
				parseProblemInfo(log);
				break;
			case 试题理解:
				parseSolverInfo(log);
				break;
			case 求解:
				parseSolverInfo(log);
				break;
			case 批改:
				parseResultInfo(log);
				break;
			default:
				break;
			}
		}
		
		return mainInfo.toJSON();
	}
	
	private void parseMain(Log log) {
		mainInfo.addMainLog("["+log.getSubType()+"]\t"+log.getContent());
		MainStep step = MainStep.valueOf(log.getSubType());
		if(!step.equals(mainInfo.getMsName())) {
			mainInfo.setMsName(step);
			mainInfo.stepPlus();
		}
	}
	
	private void parsePaperInfo(Log log) {
		if(log.getSubType().equals("选择题数")) {
			mainInfo.setSelNo(Integer.parseInt(log.getContent()));
		} else if(log.getSubType().equals("简答题数")) {
			mainInfo.setSaNo(Integer.parseInt(log.getContent()));
			unBuffer = new UnitedBuffer(mainInfo.getSelNo()+mainInfo.getSaNo());
		}
	}
	
	private void parseProblemInfo(Log log) {
		int pno = StringUtil.getProbelmIndex(log.getSubType());
		
	}
	
	private void parseSolverInfo(Log log) {
		int pno = StringUtil.getProbelmIndex(log.getSubType());
		if(log.getContent().split("\t")[0].equals("[Start]")) {
			setProblemStatus(pno, 1);
		}
		if(log.getContent().split("\t")[1].equals("[Finish]")) {
			setProblemStatus(pno, 2);
		}
	}
	
	private void setProblemStatus(int pno, int status) {
		if(pno >= mainInfo.getSelNo()) {
			pno = pno - mainInfo.getSelNo();
			mainInfo.setSaStatus(pno, status);
		} else {
			mainInfo.setSelStatus(pno, status);
		}
	}
	
	private void parseResultInfo(Log log) {
		int pno = StringUtil.getProbelmIndex(log.getSubType());
		
	}
	
	private String getUpdateLog() {
		String output = "";
		try {
			RandomAccessFile logFile = new RandomAccessFile(getLogFile(), "rw");
			logFile.seek(lastTimeFileSize);
			String temp = "";
			while((temp = logFile.readLine()) != null) {
				output += new String(temp.getBytes("ISO8859-1"), "utf-8")+"\n";
			}
			lastTimeFileSize = logFile.length();
			logFile.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return output;
	}
	
	private File getLogFile() {
		File logFile = new File("D:/Eclipse Workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp1/wtpwebapps/XiaoLi/WEB-INF/classes/simulation/output_"+paper+".log");
		return logFile;
	}
}
