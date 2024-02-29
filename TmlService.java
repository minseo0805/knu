package kr.co.claion.hmp.admin.tml.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import kr.co.claion.hmp.admin.tml.dto.Tml.KnuUserEntity;
import kr.co.claion.hmp.admin.tml.repository.TmlRepository;
import kr.co.claion.hmp.admin.cmm.exception.ClaionRepositoryException;
import kr.co.claion.hmp.admin.file.service.FileService;
import kr.co.claion.hmp.admin.hcxu.dto.HcxSubAccount;
import kr.co.claion.hmp.admin.hcxu.dto.HcxSubAccount.HcxSubAccountEntity;
import kr.co.claion.hmp.admin.hcxu.repository.HcxuRepository;
import kr.co.claion.hmp.admin.napi.dto.NcloudRequest.SubAccountRequest;
import kr.co.claion.hmp.admin.napi.dto.NcloudResponse.SubAccountResponse;
import kr.co.claion.hmp.admin.napi.service.NapiService;
import kr.co.claion.hmp.admin.tml.dto.Tml;
import kr.co.claion.hmp.admin.tml.dto.Tml.DetailSearchCondition;
import kr.co.claion.hmp.admin.tml.dto.Tml.SearchCondition;

@Service
public class TmlService {

    private static final Logger logger = LoggerFactory.getLogger(TmlService.class);

    @Autowired
    private TmlRepository tmlRepository;
    
    @Autowired
    private HcxuRepository hcxuRepository;

    @Autowired
    private NapiService napiService;
    
    @Autowired
    private FileService fileService;
    
   
    public List<KnuUserEntity> retrieveList(SearchCondition request, int iStartPage, int iEndPage) {
        List<KnuUserEntity> list = new ArrayList<KnuUserEntity>();
        try {
            list = tmlRepository.retrieveList(request, iStartPage, iEndPage);
        } catch (Exception e) {
            logger.error(":: 사용자 정보 목록 조회 중 오류 발생 :: ", e);
        }
        return list;
    }

    public int retrieveListCount(String type, SearchCondition request) {
        int total_count = 0;
        try {
        	total_count = tmlRepository.retrieveListCount(type, request);
        } catch (Exception e) {
            logger.error(":: 사용자 목록 카운트 수 조회 중 오류 발생 :: ", e);
        }
        return total_count;
    }
    
    
    public KnuUserEntity retrieveDetail(DetailSearchCondition request) {
    	KnuUserEntity detail = new KnuUserEntity();
        try {
        	String usrId = request.getUsrId();
            detail = tmlRepository.retrieveDetail(usrId);
        } catch (Exception e) {
            logger.error(":: tml 정보 상세 조회 중 오류 발생 :: ", e);
            logger.error(" >>> 상세 : ",e.getMessage(),e);
        }

        return detail;
    }

      
    public Map<String, Object> updateMember(final KnuUserEntity memberInfo) {
        Map<String, Object> result = new HashMap<>();
        try {
            //데이터베이스에 저장된 회원 정보를 업데이트
            boolean isUpdated = tmlRepository.updateMember(memberInfo);

            if (isUpdated) {
                result.put("success", true);
                result.put("message", "회원 정보가 성공적으로 수정되었습니다.");
            } else {
                result.put("success", false);
                result.put("message", "회원 정보 수정에 실패하였습니다. 변경된 내용이 없습니다.");
            }
        } catch (ClaionRepositoryException e) {
            logger.error("회원 정보 수정 중 오류 발생", e);
            result.put("success", false);
            result.put("message", "회원 정보 수정 중 오류가 발생하였습니다.");
        } catch (Exception e) {
            logger.error("회원 정보 수정 중 예상치 못한 오류 발생", e);
            result.put("success", false);
            result.put("message", "회원 정보 수정 중 예상치 못한 오류가 발생하였습니다.");
        }
        return result;
    }
    
    public Map<String, Object> deleteMember(final String userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            //데이터베이스에 저장된 회원 정보를 업데이트
        	KnuUserEntity params = new KnuUserEntity();
        	params.setUsrId(userId);
        	params.setStatus("delete");
            boolean isUpdated = tmlRepository.updateMember(params);
            
            if (isUpdated) {
            	HcxSubAccountEntity subAcntInfo = hcxuRepository.retrieveDetail(userId);
            	// sub 계정 정보가 있는지 판단
            	if (StringUtils.isEmpty(subAcntInfo.getEmail())) {
                    result.put("success", true);
                    result.put("message", "회원 정보가 성공적으로 삭제되었습니다.");
                    return result;
            	}
            	
            	// 1. 해당되는 user sub account id값 여부 조회
            	String subAcntId = subAcntInfo.getSubAcntId();
            	// 2. 있는 경우 ncp sub account 삭제 api 호출
            	if (!StringUtils.isEmpty(subAcntId)) {
            		SubAccountRequest request = new SubAccountRequest();
            		request.setSubAccountId(subAcntId);
            		Map<String, Object> subAcntDelRes = napiService.deleteSubAccounts(request);
            		boolean success = (boolean) subAcntDelRes.get("success");
            		if (!success) throw new Exception("NCP sub account delete fail.");
        			// ncp 서브 계정 삭제 성공시 데이터베이스 업데이트
            		SubAccountResponse subAcntDelResData = (SubAccountResponse) subAcntDelRes.get("data");
            		
            		if (subAcntDelResData.getErrorCode() != 0) {
    					logger.error("서브 계정 삭제 ErrorCode :: {}", subAcntDelResData.getErrorCode());
    					logger.error("서브 계정 삭제 Message   :: {}", subAcntDelResData.getMessage());
            		}
            		
            	}
            	// sub 계정 정보가 있는 경우 업데이트 진행
        		HcxSubAccount.DetailSearchCondition updtParam = new HcxSubAccount.DetailSearchCondition();
        		updtParam.setLoginId(userId);
        		updtParam.setUpdtType("delete");
    			boolean updtRes = hcxuRepository.updateSubAccountStatus(updtParam);
    			
    			if (!updtRes) throw new ClaionRepositoryException("NCP sub account table update fail. [Login Id :: " + userId + "]");
    			
                result.put("success", true);
                result.put("message", "회원 정보가 성공적으로 삭제되었습니다.");
            } else {
                result.put("success", false);
                result.put("message", "회원 정보 삭제에 실패하였습니다.");
            }
            
        } catch (ClaionRepositoryException e) {
            logger.error("회원 정보 삭제 중 오류 발생", e);
            result.put("success", false);
            result.put("message", "회원 정보 삭제 중 오류가 발생하였습니다.");
        } catch (Exception e) {
            logger.error("회원 정보 삭제 중 예상치 못한 오류 발생", e);
            result.put("success", false);
            result.put("message", "회원 정보 삭제 중 예상치 못한 오류가 발생하였습니다.");
        }
        return result;
    }
    
    public Map<String, Object> resetPassword(final String userId, final String newPassword) {
        Map<String, Object> result = new HashMap<>();
        try {    

           // SHA-3 알고리즘 생성 (512비트)
            SHA3.Digest512 digest512 = new SHA3.Digest512();

            // 메시지에 대한 해시 계산
            byte[] hash = digest512.digest(newPassword.getBytes());

            // 해시를 Hex 문자열로 변환하여 출력
            String hexHash = Hex.toHexString(hash);
            logger.info("암호화 :: {}", hexHash);
            
            //비밀번호 재설정
            boolean isPasswordReset = tmlRepository.resetPassword(userId, hexHash);

            if (isPasswordReset) {
                result.put("success", true);
                result.put("message", "비밀번호가 성공적으로 수정되었습니다.");
            } else {
                result.put("success", false);
                result.put("message", "비밀번호 수정에 실패하였습니다.");
            }
        } catch (Exception e) {
            logger.error("비밀번호 재설정 중 예상치 못한 오류 발생", e);
            result.put("success", false);
            result.put("message", "비밀번호 재설정 중 예상치 못한 오류가 발생하였습니다.");
        }
       
        return result;
    }
    
   public Map<String, Object> retrieveUsageStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        try {
            // tmlRepository에서 직접 사용자 통계를 조회하는 메소드 호출
            Map<String, Object> userStatistics = tmlRepository.findUserStatistics();
            logger.info("User statistics from repository: {}", userStatistics);

            // 필요한 통계 데이터를 확인하고 추가
            statistics.put("totalMembers", userStatistics.get("totalMembers")); // "totalUsers"를 "totalMembers"로 매핑
            statistics.put("newMembersToday", userStatistics.get("newMembersToday")); // "newUsersToday"를 "newMembersToday"로 매핑
            statistics.put("averageVisits", userStatistics.get("averageVisits")); // "averageLoginCount"를 "averageVisits"로 매핑
            
            // 탈퇴 회원 수 추가
            int churnUserCount = tmlRepository.retrieveChurnUserCount(); // 탈퇴 회원 수를 조회하는 새로운 메서드 호출
            statistics.put("churnUserCount", churnUserCount); // 탈퇴 회원 수를 통계 맵에 추가

        } catch (DataAccessException e) {
            logger.error("DataAccessException: Error accessing the database", e);
        } catch (Exception e) {
            logger.error("Exception: An unknown error occurred", e);
        }
        // 데이터 처리 후 반환 직전
        logger.info("User statistics to return: {}", statistics);
        return statistics;
    } 
   
   	public Map<String, Object> retrieveSignUpStatsForWeek() {
	    return retrieveSignUpStats("week");
	}

	public Map<String, Object> retrieveSignUpStatsForMonth() {
	    return retrieveSignUpStats("month");
	}

	private Map<String, Object> retrieveSignUpStats(String period) {
	    Map<String, Object> signUpStats = new HashMap<>();
	    try {
	        // 회원 가입 통계 데이터 검색
	    	logger.info("Retrieving sign up stats for the period: {}", period); // 데이터 검색 전 로그 추가
	        Map<String, Object> memberSignUpStats = tmlRepository.findMemberSignUpStats(period);
	        // 검색된 데이터 로그
	        logger.info("Retrieved data for period '{}': {}", period, memberSignUpStats);
	        
	        signUpStats.put("labels", memberSignUpStats.get("labels")); 
	        signUpStats.put("data", memberSignUpStats.get("data")); 
	        

	        logger.info("Sign up statistics data retrieved successfully for {}.", period);
	    } catch (DataAccessException e) {
	        logger.error("Database access error while fetching sign up stats data for {}", period, e);
	    } catch (Exception e) {
	        logger.error("An unexpected error occurred while fetching sign up stats data for {}", period, e);
	    }
	    return signUpStats;
	}
	
	   public Map<String, Object> retrieveUserAgeStats() {
		    Map<String, Object> userAgeStats = new HashMap<>();
		    try {
		        // tmlRepository에서 직접 회원 연령 통계 데이터를 조회하는 메소드 호출
		        Map<String, Object> ageStats = tmlRepository.findMemberAgeStats();
		        logger.info("User age statistics from repository: {}", ageStats);

		        // 필요한 통계 데이터를 확인하고 추가
		        userAgeStats.put("labels", ageStats.getOrDefault("labels", new ArrayList<>()));
		        userAgeStats.put("data", ageStats.getOrDefault("data", new ArrayList<>()));

		    } catch (DataAccessException e) {
		        logger.error("DataAccessException: Error accessing the database", e);
		    } catch (Exception e) {
		        logger.error("Exception: An unknown error occurred", e);
		    }
		    // 데이터 처리 후 반환 직전
		    logger.info("User age statistics to return: {}", userAgeStats);
		    return userAgeStats;
		}

   
   public Map<String, Object> retrieveAllChartsData() {
	    Map<String, Object> allChartsData = new HashMap<>();
	    try {

	        // 회원 유형 통계 데이터 검색
	        Map<String, Object> memberTypeStats = tmlRepository.findMemberTypeStats();
	        allChartsData.put("memberTypeStats", memberTypeStats);

	        // 회원 성별 통계 데이터 검색
	        Map<String, Object> memberGenderStats = tmlRepository.findMemberGenderStats();
	        allChartsData.put("memberGenderStats", memberGenderStats);


	        logger.info("All charts data retrieved successfully.");
	    } catch (DataAccessException e) {
	        logger.error("Database access error while fetching charts data", e);
	    } catch (Exception e) {
	        logger.error("An unexpected error occurred while fetching charts data", e);
	    }
	    return allChartsData;
	}
   



    

}



    
    
  