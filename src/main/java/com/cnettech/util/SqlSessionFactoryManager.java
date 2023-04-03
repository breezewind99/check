package com.cnettech.util;


import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class SqlSessionFactoryManager 
{
	private final static String RESOURCE = "src/main/resource/mybatis-config.xml";
	private static SqlSessionFactory FACTORY = null;
	
	static 
	{
		try 
		{		
			Properties	pro = Common.getProperties();
			//Reader reader = Resources.getResourceAsReader(RESOURCE);
			//Reader reader = Resources. (loader, resource) (Common.GetProgramDirectory(RESOURCE));
			InputStream  is = new FileInputStream(Common.GetProgramDirectory(RESOURCE));
			//System.out.println(Common.GetProgramDirectory(RESOURCE));
			FACTORY = new SqlSessionFactoryBuilder().build(is, "crec_master", pro);			
		} 
		catch (Exception e) 
		{
			throw new RuntimeException("SqlSessionFactoryManager Fatal Error : " + e, e);
		}		
	}
	
	/**
	 * getSqlSessionFactory
	 * @return
	 */
	public static SqlSessionFactory getSqlSessionFactory() 
	{
		if(FACTORY == null) 
		{
			try 
			{
				Properties	pro = Common.getProperties();
				//Reader reader = Resources.getResourceAsReader(RESOURCE);
				InputStream  is = new FileInputStream(Common.GetProgramDirectory(RESOURCE));
				FACTORY = new SqlSessionFactoryBuilder().build(is, "crec_master", pro);
			} 
			catch (Exception e) {}
		}
		
		return FACTORY;
	}
	
	// public static SqlSessionFactory getSqlSessionFactory2() 
	// {
	// 	if(FACTORY == null) 
	// 	{
	// 		try 
	// 		{
	// 			Properties	pro = Common.getProperties();		
	// 			InputStream  is = new FileInputStream(RESOURCE);
	// 			FACTORY = new SqlSessionFactoryBuilder().build(is, "crec_master", pro);
	// 		} 
	// 		catch (Exception e) {
	// 			throw new RuntimeException("SqlSessionFactoryManager Fatal Error : " + e, e);
	// 		}
	// 	}
		
	// 	return FACTORY;
	// }

	
	/**
	 * DB SqlSession
	 * @return
	 */
	public static SqlSession getSqlSession() 
	{
		try 
		{
			return FACTORY.openSession();
		} 
		catch (Exception e) 
		{
			return null;
		}
	}
	
	/**
	 * getSqlSession
	 * @param arg
	 * @return
	 */
	public static SqlSession getSqlSession(boolean arg) 
	{
		try 
		{
			return FACTORY.openSession(arg);
		} 
		catch (Exception e) 
		{
			return null;
		}
	}
}
