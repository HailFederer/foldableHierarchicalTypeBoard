package com.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import javax.servlet.http.HttpServletResponse;

public class FileManager {

	// path : 파일을 저장할 경로
	// 리턴 : 서버에 저장된 새로운 파일명
	public static String doFileUpload(File file, String originalFileName, String path) throws Exception {
		String newFileName = null;

		if(file == null)
			return null;
		
		// 클라이언트가 업로드한 파일의 이름
		if(originalFileName.equals(""))
			return null;
		
		// 확장자
		String fileExt = originalFileName.substring(originalFileName.lastIndexOf("."));
		if(fileExt == null || fileExt.equals(""))
			return null;
		
		// 서버에 저장할 새로운 파일명을 만든다.
		newFileName = String.format("%1$tY%1$tm%1$td%1$tH%1$tM%1$tS", 
				         Calendar.getInstance());
		newFileName += System.nanoTime();
		newFileName += fileExt;
		
		// 업로드할 경로가 존재하지 않는 경우 폴더를 생성 한다.
		File dir = new File(path);
		if(!dir.exists())
			dir.mkdirs();
		
		String fullFileName = path + File.separator + newFileName;
		
		FileInputStream fis = new FileInputStream(file);
		FileOutputStream fos = new FileOutputStream(fullFileName);
		int bytesRead = 0;
        byte[] buffer = new byte[1024];
        while ((bytesRead = fis.read(buffer, 0, 1024)) != -1) {
            fos.write(buffer, 0, bytesRead);
        }
        fos.close();
        fis.close();
		
		return newFileName;
	}
	
	// 파일 다운로드
	// saveFileName : 서버에 저장된 파일명
	// originalFileName : 클라이언트가 업로드한 파일명
	// path : 서버에 저장된 경로
	public static boolean doFileDownload(String saveFileName, String originalFileName, String path, HttpServletResponse response) {
		String load_dir = path + File.separator + saveFileName;
		
        try {
    		if(originalFileName == null || originalFileName.equals(""))
    			originalFileName = saveFileName;
        	originalFileName = new String(originalFileName.getBytes("euc-kr"),"8859_1");
        } catch (UnsupportedEncodingException e) {
        }

	    try {
	        File file = new File(load_dir);

	        if (file.exists()){
	            byte readByte[] = new byte[4096];

	            response.setContentType("application/octet-stream");
				response.setHeader(
						"Content-disposition",
						"attachment;filename=" + originalFileName);

	            BufferedInputStream  fin  = new BufferedInputStream(new FileInputStream(file));
	            //javax.servlet.ServletOutputStream outs =	response.getOutputStream();
	            OutputStream outs = response.getOutputStream();
	            
	   			int read;
	    		while ((read = fin.read(readByte, 0, 4096)) != -1)
	    				outs.write(readByte, 0, read);
	    		outs.flush();
	    		outs.close();
	            fin.close();
	            
	            return true;
	        }
	    } catch(Exception e) {
	    }
	    
	    return false;
	}
	
	// 실제 파일 삭제
	public static void doFileDelete(String fileName, String path) 
	        throws Exception {
		File file = null;
		String fullFileName = path + "\\"+ fileName;
        file = new java.io.File(fullFileName);
        if (file.exists())
           file.delete();
	}	
}
