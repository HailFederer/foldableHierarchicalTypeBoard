package com.board;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.Preparable;
import com.util.MyUtil;
import com.util.dao.CommonDAO;
import com.util.dao.CommonDAOImpl;

public class BoardAction extends ActionSupport implements Preparable, ModelDriven<BoardDTO>{

	private static final long serialVersionUID = 1L;
	
	private BoardDTO dto;

	public BoardDTO getDto() {
		return dto;
	}

	@Override
	public BoardDTO getModel() {
		
		return dto;
	}

	@Override
	public void prepare() throws Exception {
		
		dto = new BoardDTO();
	}

	public String created() throws Exception {
		
		// 게시물 입력 화면
		HttpServletRequest request = ServletActionContext.getRequest();	// request 의 객체 얻는 방법
		
		if(dto==null || dto.getMode()==null || dto.getMode().equals("")){
			
			// mode에 따라 이름이 달라지게 함
			request.setAttribute("mode", "created");
			
			return INPUT;
		}
			
		// 게시물 저장
		CommonDAO dao = CommonDAOImpl.getInstance();
		
		int maxBoardNum = dao.getIntValue("board.maxBoardNum");
		
		dto.setBoardNum(maxBoardNum + 1);
		dto.setIpAddr(request.getRemoteAddr());
		dto.setGroupNum(dto.getBoardNum());
		dto.setDepth(0);
		dto.setOrderNo(0);
		dto.setParent(0);
		
		dao.insertData("board.insertData", dto);
		
		return SUCCESS;
	}

	public String list() throws Exception {
		
		// 게시물 리스트
		
		CommonDAO dao = CommonDAOImpl.getInstance();
		MyUtil myUtil = new MyUtil();
		
		HttpServletRequest request = ServletActionContext.getRequest();
		String cp = request.getContextPath();
		
		int numPerPage = 30;
		int totalPage = 0;
		int totalDataCount = 0;
		
		int currentPage = 1;
		
		if(dto.getPageNum() != null && !dto.getPageNum().equals(""))
			currentPage = Integer.parseInt(dto.getPageNum());
		
		if(dto.getSearchKey() == null || dto.getSearchKey().equals("")){
			
			dto.setSearchKey("subject");
			dto.setSearchValue("");
		}
		
		if(request.getMethod().equalsIgnoreCase("GET"))
			dto.setSearchValue(URLDecoder.decode(dto.getSearchValue(),"UTF-8"));
		
		// 전페페이지
		Map<String, Object> hMap = new HashMap<String, Object>();
		hMap.put("searchKey", dto.getSearchKey());
		hMap.put("searchValue", dto.getSearchValue());
		
		totalDataCount = dao.getIntValue("board.dataCount", hMap);
		
		if(totalDataCount != 0)
			totalPage = myUtil.getPageCount(numPerPage, totalDataCount);
		
		if(currentPage > totalPage)
			currentPage = totalPage;
		
		int start = (currentPage-1) * numPerPage + 1;
		int end = currentPage * numPerPage;
		
		hMap.put("start", start);
		hMap.put("end", end);
		
		List<Object> lists = (List<Object>)dao.getListData("board.listData", hMap);
		
		int listNum, n=0;
		
		ListIterator<Object> it = lists.listIterator();
		
		int depth = -1;
		
		while(it.hasNext()){
			
			BoardDTO vo = (BoardDTO)it.next();
			
			listNum = totalDataCount - (start+n-1);
			
			vo.setListNum(listNum);
			n++;
			
			vo.setDepthGap(depth - vo.getDepth() + 1);
			
			depth = vo.getDepth();
			
			hMap.put("groupNum", vo.getGroupNum());
			hMap.put("depth", vo.getDepth());
			hMap.put("boardNum", vo.getBoardNum());
			hMap.put("orderNo", vo.getOrderNo());
			hMap.put("parent", vo.getParent());
		
			vo.setReplyNum(dao.getIntValue("board.replyNum", hMap));
			
			vo.setSiblingNum(dao.getIntValue("board.siblingNum", hMap));
			
			List<Integer> verticalList = new ArrayList<Integer>();
			
			for(int i=1; i<vo.getDepth(); i++){
					
					hMap.put("depth", i);
					verticalList.add(dao.getIntValue("board.verticalList", hMap));
			}
			
			vo.setVerticalList(verticalList);
		}
		
		String param = "";
		String urlList = "";
		String urlArticle = "";
		
		if(!dto.getSearchValue().equals("")){
			
			param = "searchKey=" + dto.getSearchKey();
			param += "&searchValue=" + URLEncoder.encode(dto.getSearchValue(), "UTF-8");
		}
		
		urlList = cp + "/board/list.action";
		urlArticle = cp + "/board/article.action?pageNum=" + currentPage;
		
		if(!param.equals("")){
			
			urlList += "?" + param;
			urlArticle += "&" + param;
		}

		request.setAttribute("restDiv", n);
		request.setAttribute("lists", lists);
		request.setAttribute("totalDataCount", totalDataCount);
		request.setAttribute("pageIndexList", myUtil.pageIndexList(currentPage, totalPage, urlList));
		request.setAttribute("urlArticle", urlArticle);
		
		return SUCCESS;
	}

	public String article() throws Exception {
		
		HttpServletRequest request = ServletActionContext.getRequest();
		CommonDAO dao = CommonDAOImpl.getInstance();
		
		String searchKey = dto.getSearchKey();
		String searchValue = dto.getSearchValue();
		String pageNum = dto.getPageNum();
		int boardNum = dto.getBoardNum();
		
		if(searchKey == null || searchKey.equals("")){
			
			searchKey = "subject";
			searchValue = "";
		}
		
		if(request.getMethod().equalsIgnoreCase("GET"))
			searchValue = URLDecoder.decode(searchValue, "UTF-8");
		
		// 조회수 증가
		dao.updateData("board.hitCountUpdate", boardNum);
		
		// 레코드 가져오기
		dto = (BoardDTO)dao.getReadData("board.readData", boardNum);
		
		if(dto==null)
			return "read-error";
		
		// 라인 수
		int lineSu = dto.getContent().split("\r\n").length;
		
		// <br>변경
		dto.setContent(dto.getContent().replaceAll("\r\n", "<br/>"));
		
		// 이전글 다음글
		Map<String, Object> hMap = new HashMap<>();
		hMap.put("searchKey", searchKey);
		hMap.put("searchValue", searchValue);
		hMap.put("groupNum", dto.getGroupNum());
		hMap.put("orderNo", dto.getOrderNo());
		
		BoardDTO preDTO = (BoardDTO)dao.getReadData("board.preReadData", hMap);
		
		int preBoardNum = 0;
		String preSubject = "";
		
		if(preDTO != null){
			
			preBoardNum = preDTO.getBoardNum();
			preSubject = preDTO.getSubject();
		}
		
		BoardDTO nextDTO = (BoardDTO)dao.getReadData("board.nextReadData", hMap);
		
		int nextBoardNum = 0;
		String nextSubject = "";
		
		if(nextDTO != null){
			
			nextBoardNum = nextDTO.getBoardNum();
			nextSubject = nextDTO.getSubject();
		}
		
		String params = "pageNum=" + pageNum;
		
		if(!searchValue.equals("")){
			
			params += "&searchKey=" + searchKey;
			params += "&searchValue=" + URLEncoder.encode(searchValue, "UTF-8");
		}
		
		request.setAttribute("preBoardNum", preBoardNum);
		request.setAttribute("preSubject", preSubject);
		request.setAttribute("nextBoardNum", nextBoardNum);
		request.setAttribute("nextSubject", nextSubject);
		
		request.setAttribute("params", params);
		request.setAttribute("lineSu", lineSu);
		request.setAttribute("pageNum", pageNum);
		
		return SUCCESS;
	}

	public String updated() throws Exception {
		
		HttpServletRequest request = ServletActionContext.getRequest();
		CommonDAO dao = CommonDAOImpl.getInstance();
		
		if(dto.getMode()==null || dto.getMode().equals("")){
			
			String pageNum = dto.getPageNum();
			dto = (BoardDTO)dao.getReadData("board.readData", dto.getBoardNum());
			
			if(dto==null)
				return "read-error";
			
			request.setAttribute("mode", "updated");
			request.setAttribute("pageNum", pageNum);
			
			return INPUT;
		}
		
		// 게시물 수정
		dao.updateData("board.updateData", dto);
		
		return SUCCESS;
	}

	public String reply() throws Exception {
		
		HttpServletRequest request = ServletActionContext.getRequest();
		CommonDAO dao = CommonDAOImpl.getInstance();
		
		if(dto==null || dto.getMode()==null || dto.getMode().equals("")){
			
			String pageNum = dto.getPageNum();
			
			dto = (BoardDTO)dao.getReadData("board.readData", dto.getBoardNum());
			
			if(dto==null)
				return "read-error";
			
			String temp = "\r\n--------------------------\r\n\r\n";
			
			temp += "[답변]\r\n";
			dto.setContent(dto.getContent() + temp);
			dto.setSubject("[답변]" + dto.getSubject());
			dto.setName("");
			dto.setEmail("");
			dto.setPwd("");
			
			request.setAttribute("mode", "reply");
			request.setAttribute("pageNum", pageNum);
			
			return INPUT;
		}
		
		// 답변
		
		// orderNo를 변경
		Map<String, Object> hMap = new HashMap<>();
		hMap.put("groupNum", dto.getGroupNum());
		hMap.put("orderNo", dto.getOrderNo());
		
		dao.updateData("board.orderNoUpdate", hMap);
		
		// 답변등록
		int maxBoardNum = dao.getIntValue("board.maxBoardNum");
		
		dto.setBoardNum(maxBoardNum + 1);
		dto.setIpAddr(request.getRemoteAddr());
		
		dto.setDepth(dto.getDepth() + 1);
		dto.setOrderNo(dto.getOrderNo() + 1);
		
		dao.insertData("board.insertData", dto);
		
		return SUCCESS;
	}

	public String deleted() throws Exception {
		
		CommonDAO dao = CommonDAOImpl.getInstance();
		
		dao.deleteData("board.deleteData", dto.getBoardNum());
		
		return SUCCESS;
	}

	public String main() throws Exception {
		
		return SUCCESS;
	}
}

























