package kr.co.claion.hmp.admin.tml.controller;

import java.util.HashMap;
import java.util.List;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataAccessException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.co.claion.hmp.admin.board.dto.Board;
import kr.co.claion.hmp.admin.cmm.dto.DataTablesWrapperResponseDto;
import kr.co.claion.hmp.admin.hcxu.dto.HcxSubAccount.HcxSubAccountEntity;
import kr.co.claion.hmp.admin.tml.service.TmlService;
import kr.co.claion.hmp.admin.tml.dto.Tml.KnuUserEntity;
import kr.co.claion.hmp.admin.tml.dto.Tml;
import kr.co.claion.hmp.admin.tml.dto.Tml.DetailSearchCondition;
import kr.co.claion.hmp.admin.tml.dto.Tml.SearchCondition;


@Controller
@RequestMapping(value = "/tm")
public class TmlController {
	
	private static final Logger logger = LoggerFactory.getLogger(TmlController.class);
	
	@Autowired
	private TmlService tmlService;
	
	@RequestMapping("tml/v/input")
	public String tmlView(ModelMap model) {
		model.addAttribute("title", "Claion | tml");
		model.addAttribute("left_selection", "_tmlinput");
		return "tml/input";
	}

	//tml (Trial Member List) 경로 처리 
	@GetMapping("/tml/v/all")
	public String tmlallView(ModelMap model) {
		model.addAttribute("left_selection", "_tmlall");
		model.addAttribute("pageTitImgUrl", "images/ico_gnb_02.svg");
		model.addAttribute("pageTitAlt", "체험 회원 관리 아이콘");
        model.addAttribute("pageTit", "체험 회원 관리");
        model.addAttribute("tml_active", "active");
		return "tml/tml_all";
	}	
	
	
	
	@PostMapping("/tml/all/list")
    public @ResponseBody ResponseEntity<DataTablesWrapperResponseDto> retriveAll(@RequestBody SearchCondition request) {
       logger.info("::사용자 전체 목록 조회 > 요청 정보 : [{}]", request.toString());

       request.convertWrapperDto();

       int _start = 0;
       int _end = 0;
       int displayLength = request.getDisplayLength();
       if (displayLength > 0) {
          _start = request.getDisplayStart(); // 시작 row
          _end = displayLength; // 종료 row
       }

      List<KnuUserEntity> data = tmlService.retrieveList(request, _start, _end);
      int total_count = tmlService.retrieveListCount("all", request);
      DataTablesWrapperResponseDto vo = new DataTablesWrapperResponseDto();
      vo.setData(data);  
      vo.setiTotalDisplayRecords(total_count);
      vo.setiTotalRecords(total_count);

      return new ResponseEntity<DataTablesWrapperResponseDto>(vo, HttpStatus.OK);
    }
	

	// 팝업을 위한 회원 상세 정보 검색 엔드포인트
	@PostMapping("/tml/retrieve/detail")
	public @ResponseBody ResponseEntity<KnuUserEntity> retrieveDetails(@RequestBody Tml.DetailSearchCondition request) {
		logger.info("::회원 상세 조회 > 요청 정보 : [{}]", request.toString());
		
		KnuUserEntity data = null;
		data = tmlService. retrieveDetail(request);
		logger.debug("::회원 정보:: 회원정보 상세 조회 > 결과 : {} ", data.toString());
		
	    return new ResponseEntity<KnuUserEntity>(data, HttpStatus.OK);
	}
	
	// 회원 정보 수정 
	@PostMapping("/tml/update")
	public @ResponseBody ResponseEntity<Map<String, Object>> updateMember(@RequestBody Tml.KnuUserEntity updateRequest) {
	    logger.info("::회원 정보 수정 요청 > 요청 정보 : [{}]", updateRequest.toString());
	    try {
	        Map<String, Object> result = tmlService.updateMember(updateRequest);        
	        
	        return new ResponseEntity<>(result, HttpStatus.OK);
	        
	    } catch (Exception e) {
	        logger.error("회원 정보 수정 중 오류 발생", e);
	        Map<String, Object> errorResult = new HashMap<>();
	        errorResult.put("success", false);
	        errorResult.put("message", "서버에서 회원 정보 수정 중 오류가 발생하였습니다.");
	        return new ResponseEntity<>(errorResult, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	// 회원 정보 삭제
	@PostMapping("/tml/delete")
	public @ResponseBody ResponseEntity<Map<String, Object>> deleteMember(@RequestBody Tml.KnuUserEntity request) {
	    logger.info("::회원 정보 삭제 요청 > 요청 정보 : [{}]", request.toString());
	    Map<String, Object> result = new HashMap<String, Object>();
	    result.put("success", false);
	    result.put("message", "서버에서 회원정보 삭제 중 오류가 발생하였습니다.");
	    
	    try {
	    	
	    	String usrId = request.getUsrId();
	    	
	    	if (usrId.isEmpty()) {
		        logger.error("요청 회원 아이디가 없습니다.");
			    result.put("message", "요청 회원 아이디가 없습니다.");
	    		return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
	    	}
	    	
	        result = tmlService.deleteMember(request.getUsrId()); 
	        
	        return new ResponseEntity<>(result, HttpStatus.OK);
	        
	    } catch (Exception e) {
	        logger.error("회원 정보 삭제 중 오류 발생", e);
	        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	// 비밀번호 재설정 
	@PostMapping("/tml/resetPassword")
	public @ResponseBody ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Tml.KnuUserEntity resetRequest) {
	    logger.info("::비밀번호 재설정 요청 > 요청 정보 : [{}]", resetRequest.toString());
	    try {
	        Map<String, Object> result = tmlService.resetPassword(resetRequest.getUsrId(), resetRequest.getPasswd()); 
	        
	        return new ResponseEntity<>(result, HttpStatus.OK);
	        
	    } catch (Exception e) {
	        logger.error("회원 정보 수정 중 오류 발생", e);
	        Map<String, Object> errorResult = new HashMap<>();
	        errorResult.put("success", false);
	        errorResult.put("message", "서버에서 비밀번호 수정 중 오류가 발생하였습니다.");
	        return new ResponseEntity<>(errorResult, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	//tmu (Trial Member Usage) 경로 처리 
	@GetMapping("/tmu/v/all")
	public String tmuallView(ModelMap model) {
		model.addAttribute("left_selection", "_tmlall");
		model.addAttribute("pageTitImgUrl", "images/ico_gnb_02.svg");
		model.addAttribute("pageTitAlt", "체험 회원 관리 아이콘");
        model.addAttribute("pageTit", "체험 회원 관리");
        model.addAttribute("tmu_active", "active");
		return "tml/tmu_all";
		}	
	
	
	
	@GetMapping("/tmu/retrieve/usage")
	public ResponseEntity<Map<String, Object>> retrieveUsageStatistics() {
	    Map<String, Object> response = new HashMap<>();
	    try {
	        // tmlService에서 retrieveUsageStatistics() 메소드를 호출하여 사용자 통계 정보 조회
	        Map<String, Object> statistics = tmlService.retrieveUsageStatistics();
	        
	        // 로그를 통해 statistics 맵의 내용을 출력
	        logger.info("Statistics retrieved: {}", statistics);

	        // 필요한 통계 데이터를 확인하고 추가
	        response.put("totalMembers", statistics.getOrDefault("totalMembers", 0));
	        response.put("newMembersToday", statistics.getOrDefault("newMembersToday", 0));
	        response.put("averageVisits", statistics.getOrDefault("averageVisits", 0));
	        // 탈퇴 회원 수 데이터를 추가
	        response.put("churnUserCount", statistics.getOrDefault("churnUserCount", 0));

	        return ResponseEntity.ok(response);
	    } catch (DataAccessException e) {
	        logger.error("데이터베이스 접근 중 오류 발생", e);
	        response.put("error", "데이터베이스 접근 오류가 발생했습니다.");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    } catch (Exception e) {
	        logger.error("알 수 없는 오류 발생", e);
	        response.put("error", "서버 오류가 발생했습니다.");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	} 
	
	@GetMapping("/tmu/retrieveSignUpStats")
	public ResponseEntity<Map<String, Object>> retrieveSignUpStats(@RequestParam(required = false) String period) {
	    logger.info("체험 회원 가입 수 현황 차트 데이터를 가져옵니다, 기간: {}", period);
	    
	    // 기본 기간을 "week"로 설정
	    if (period == null || (!"week".equals(period) && !"month".equals(period))) {	        
	        period = "week";
	    }
	
	    Map<String, Object> response = new HashMap<>();
	    try {
	        Map<String, Object> signUpStats;
	        if ("week".equals(period)) {
	            signUpStats = tmlService.retrieveSignUpStatsForWeek();
	            logger.info("Weekly sign-up statistics data: {}", signUpStats);
	        } else {
	            signUpStats = tmlService.retrieveSignUpStatsForMonth();
	        }
	        return ResponseEntity.ok(signUpStats);
	    } catch (Exception e) {
	        logger.error("가입 수 현황 차트 데이터를 가져오는 동안 오류 발생", e);
	        response.put("error", "서버에서 오류가 발생했습니다.");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}
	 
	@GetMapping("/tmu/retrieveUserAgeStats")
	public ResponseEntity<Map<String, Object>> retrieveUserAgeStats() {
	    logger.info("회원 연령 비율 데이터를 가져옵니다");
		
	    Map<String, Object> response = new HashMap<>();
	    try {
	        Map<String, Object> userAgeStats = tmlService.retrieveUserAgeStats();
		
	        logger.info("User Age Statistics retrieved: {}", userAgeStats);
	        response.put("memberAgeStats", userAgeStats);
		
	        return ResponseEntity.ok(response);
	    } catch (DataAccessException e) {
	        logger.error("데이터베이스 접근 중 오류 발생", e);
	        response.put("error", "데이터베이스 접근 오류가 발생했습니다.");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    } catch (Exception e) {
	        logger.error("알 수 없는 오류 발생", e);
	        response.put("error", "서버 오류가 발생했습니다.");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}

	@GetMapping("/tmu/retrieveAllChartsData")
	public ResponseEntity<Map<String, Object>> retrieveAllChartsData() {
	    logger.info("체험 회원들의 모든 차트 데이터를 가져옵니다");
	    Map<String, Object> response = new HashMap<>();
	    try {
	        // 서비스 메소드가 존재하고 필요한 모든 차트 데이터를 반환한다고 가정
	        Map<String, Object> allChartsData = tmlService.retrieveAllChartsData();

	        response.put("memberTypeStats", allChartsData.getOrDefault("memberTypeStats", new HashMap<>()));
	        response.put("memberGenderStats", allChartsData.getOrDefault("memberGenderStats", new HashMap<>()));

	        return ResponseEntity.ok(response);
	    } catch (DataAccessException e) {
	        logger.error("차트 데이터를 가져오는 동안 데이터베이스 접근 오류 발생", e);
	        response.put("error", "데이터베이스 접근 오류입니다.");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    } catch (Exception e) {
	        logger.error("차트 데이터를 가져오는 동안 알 수 없는 오류 발생", e);
	        response.put("error", "서버에서 오류가 발생했습니다.");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}
	
	
	
}	
	


