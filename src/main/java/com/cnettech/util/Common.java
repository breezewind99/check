package com.cnettech.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Common {
	static String ResourceFile = "application.properties";
	static String MybatisFile = "mybatis-config.xml";

	public static Properties getProperties() {

		FileInputStream fs = null;
		Properties props = null;
		try {
			props = new Properties();
			String path = System.getProperty("user.dir");
			System.out.println("Working Directory = " + path);
			System.out.println(GetProgramDirectory(ResourceFile));
			InputStream resource = new FileInputStream(GetProgramDirectory(ResourceFile));
			
			props.load(resource);
		} catch (IOException ex) {
			ex.printStackTrace();
			props = null;
		} finally {
			if (fs != null) {
				try {
					fs.close();
				} catch (Exception e) {
				}
			}
		}
		return props;
	}

	public static Properties getMybatisConfig() {
		FileInputStream fs = null;
		Properties props = null;
		try {

			props = new Properties();
			InputStream resource = new FileInputStream(GetProgramDirectory(MybatisFile));
			props.loadFromXML(resource);

		}

		catch (IOException ex) {
			ex.printStackTrace();
			props = null;
		} finally {
			if (fs != null) {
				try {
					fs.close();
				} catch (Exception e) {
				}
			}
		}
		return props;
	}

	public static boolean isNullOrEmpty(String string) {
		return string == null || string.isEmpty();
	}

	public static String GetProgramDirectory(String ResourceName) {
		String PROGRAM_DIRECTORY = "";
		try {
//			System.out.println("Resource = " + ResourceName);
			String Origin_Path = Common.class.getClassLoader().getResource(ResourceName).getPath();
//			System.out.println("Resource2 = " + Origin_Path);

			String tempArray[] = Origin_Path.split("/");
			String outputString = "";
			for (String temp : tempArray) {
				if (temp.indexOf("jar") < 0 && !temp.equals("")) {
					outputString += "/" + temp;
				}
			}

			PROGRAM_DIRECTORY = outputString;
			try {
				PROGRAM_DIRECTORY = PROGRAM_DIRECTORY.substring(0, PROGRAM_DIRECTORY.lastIndexOf('!'));
			} catch (Exception e) {
			}
			String SystemType = System.getProperty("os.name");
//			System.out.println("System Type = " + SystemType);
//			System.out.println("PROGRAM_DIRECTORY : " + PROGRAM_DIRECTORY);
			if (SystemType.contains("Windows")) {
				if (PROGRAM_DIRECTORY.startsWith("/"))
					PROGRAM_DIRECTORY = PROGRAM_DIRECTORY.substring(1, PROGRAM_DIRECTORY.length());
				if (PROGRAM_DIRECTORY.startsWith("file:/"))
					PROGRAM_DIRECTORY = PROGRAM_DIRECTORY.substring(6, PROGRAM_DIRECTORY.length());
			} else {
				//System.out.println("PROGRAM_DIRECTORY2 : " + PROGRAM_DIRECTORY);
				if (PROGRAM_DIRECTORY.startsWith("/file:/"))
					PROGRAM_DIRECTORY = PROGRAM_DIRECTORY.substring(6, PROGRAM_DIRECTORY.length());
			}
		} catch (Exception e) {
			PROGRAM_DIRECTORY = "";
		}
		return PROGRAM_DIRECTORY;
	}
}
