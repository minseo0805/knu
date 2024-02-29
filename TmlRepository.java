package kr.co.claion.hmp.admin.tml.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;

import kr.co.claion.hmp.admin.board.dto.Board.BoardRequest;
import kr.co.claion.hmp.admin.cmm.exception.ClaionRepositoryException;
import kr.co.claion.hmp.admin.cmm.library.DBManager;
import kr.co.claion.hmp.admin.tml.dto.Tml.KnuUserEntity;
import kr.co.claion.hmp.admin.tml.repository.TmlRepository;
import kr.co.claion.hmp.admin.tml.service.TmlService;
import kr.co.claion.hmp.admin.tml.dto.Tml.DetailSearchCondition;
import kr.co.claion.hmp.admin.tml.dto.Tml.SearchCondition;


@Repository
public class TmlRepository {
	 private final Logger logger = LoggerFactory.getLogger(TmlRepository.class);
	 
	 
	 @Autowired
	 private WebApplicationContext context;

	 private DBManager getDbManager(String dbBeanName) {
		 return (DBManager) context.getBean(dbBeanName);
	   }
	

	//KNU_USR_TB 메소드
	 public List<KnuUserEntity> retrieveList(SearchCondition request, int startPage, int endPage)   
	         throws ClaionRepositoryException {
	     List<KnuUserEntity> list = new ArrayList<KnuUserEntity>();
	     DBManager db = getDbManager("claion");
	     ResultSet rs = null;

	     try {
	         if (!db.connectionDB()) {
	             logger.error("DataBase Connection fail.");
	             throw new ClaionRepositoryException("DataBase Connection fail.");
	         }

	         String query = "SELECT `USR_SEQ`, `USR_ID`, `PASSWD`, `USR_NM`, `MEMBER_TYPE`, `STATUS`, `CRT_DT`, `CRT_OPRTR_ID`, `UPDT_DT`, `LAST_LGIN_DTTM`, `REFRESH_TOKEN_HASH`, `USR_TELNO`, `GENDER`, `BIRTH_DATE`, `LOGIN_CNT` " +
	                     "FROM `KNU_USR_TB` WHERE 1=1";

	         // 조건에 따라 쿼리 수정
	         if (!StringUtils.isEmpty(request.getSearchType()) && request.getSearchType().equals("usrId")) {
	             query += " AND USR_ID LIKE ?";
	         }

	         if (!StringUtils.isEmpty(request.getSearchType()) && request.getSearchType().equals("usrNm")) {
	             query += " AND USR_NM LIKE ?";
	         }

	         if (!StringUtils.isEmpty(request.getSearchType()) && request.getSearchType().equals("usrTelno")) {
	             query += " AND USR_TELNO LIKE ?";
	         }

	         // 회원 유형 필터링 추가
	         if (!StringUtils.isEmpty(request.getMemberTypeFilter())) {
	             query += " AND MEMBER_TYPE = ?";
	         }

	         // 정렬 조건 추가
	         String sortBy = "`USR_SEQ`"; // 기본 정렬
	         String sortDirection = "ASC"; //오름차
	         if (!StringUtils.isEmpty(request.getSortType())) {
	             switch (request.getSortType()) {
	                 case "crtDt": sortBy = "`CRT_DT`"; break;
	                 case "usrId": sortBy = "`USR_ID`"; break;
	                 case "usrNm": sortBy = "`USR_NM`"; break;
	                 case "lastDt": sortBy = "`LAST_LGIN_DTTM`"; break;
	                 case "birthDate": sortBy = "`BIRTH_DATE`"; break;
	             }
	         }

	         query += " ORDER BY " + sortBy + " " + sortDirection;

	         if (endPage > 0) {
	             query += " LIMIT ?, ?";
	         }

	         if (!db.startPrepareStatement(query)) {
	             logger.error("PreparedStatement set fail.");
	             throw new ClaionRepositoryException("User조회 실패");
	         }

			int idx = 0;

			if (!StringUtils.isEmpty(request.getSearchType()) && request.getSearchType().equals("usrId")) {
				db.setPrepareColumn(++idx, "%" + request.getKeyword() + "%");
			}
			
			if (!StringUtils.isEmpty(request.getSearchType()) && request.getSearchType().equals("usrNm")) {
				db.setPrepareColumn(++idx, "%" + request.getKeyword() + "%");
			}
			
			if (!StringUtils.isEmpty(request.getSearchType()) && request.getSearchType().equals("usrTelno")) {
				db.setPrepareColumn(++idx, "%" + request.getKeyword() + "%");
			}
			
			// 회원 유형 필터링 파라미터 바인딩
	        if (!StringUtils.isEmpty(request.getMemberTypeFilter())) {
	            db.setPrepareColumn(++idx, request.getMemberTypeFilter());
	        }


			if (endPage > 0) {
				db.setPrepareColumn(++idx, startPage);
				db.setPrepareColumn(++idx, endPage);
			}

            rs = db.selectPrepareStatement();
            
            if (rs == null) {
                logger.error("ResultSet error.");
                throw new ClaionRepositoryException("User조회 실패");
            }
            
            try {
               while (rs.next()) {       
                   KnuUserEntity user = new KnuUserEntity();
                   user.setUsrSeq(rs.getLong("USR_SEQ"));
                   user.setUsrId(rs.getString("USR_ID"));
                   user.setPasswd(rs.getString("PASSWD"));
                   user.setUsrNm(rs.getString("USR_NM"));
                   // memberType 코드를 이름으로 변환
                   String memberTypeCode = rs.getString("MEMBER_TYPE");
                   String memberTypeName = "";
                   switch (memberTypeCode) {
                       case "01":
                           memberTypeName = "학생";
                           break;
                       case "02":
                           memberTypeName = "연구자";
                           break;
                       case "03":
                           memberTypeName = "교수 및 교직원";
                           break;
                       case "04":
                           memberTypeName = "일반";    
                           break;
                   }
                   user.setMemberType(memberTypeName);
                   user.setCrtDt(rs.getString("CRT_DT"));
                   user.setCrtOprtrId(rs.getString("CRT_OPRTR_ID"));;
                   user.setUpdtDt(rs.getString("UPDT_DT"));
                   user.setLastDt(rs.getString("LAST_LGIN_DTTM"));
                   user.setStatus(rs.getString("STATUS"));
                   user.setRefreshTokenHash(rs.getString("REFRESH_TOKEN_HASH"));
                   user.setUsrTelno(rs.getString("USR_TELNO"));
                   // gender 코드를 이름으로 변환
                   String genderCode = rs.getString("GENDER");
                   String genderName = "";
                   switch (genderCode) {
                       case "01":
                           genderName = "남자";
                           break;
                       case "02":
                           genderName = "여자";
                           break;
                   }
                   user.setGender(genderName); // 수정: 변환된 성별 이름을 설정
                   user.setBirthDate(rs.getString("BIRTH_DATE"));
                   user.setLoginCnt(rs.getLong("LOGIN_CNT"));//회원 방문 횟수 추가 
                               
                  
                   list.add(user);
            }

        } catch (SQLException e) {
            logger.error("SQL Exception: ", e);
            throw new ClaionRepositoryException("User조회 실패");
        }
            
        } finally {
            try {
                if (rs != null) 
                   rs.close();
            } catch (SQLException e) {               
            }
            db.closeConnection();
        }
        return list;
        
    }
 

    
     public int retrieveListCount(String type, SearchCondition request) throws ClaionRepositoryException {
        DBManager db = getDbManager("claion");
        ResultSet rs = null;
        int total_count = 0;

        try {
            if (db.connectionDB() == false) {
                logger.error("DataBase Connection fail.");
                throw new ClaionRepositoryException("DataBase Connection fail.");
            }

            String query = "SELECT COUNT(*) FROM KNU_USR_TB WHERE 1=1";
            
            if (!StringUtils.isEmpty(request.getSearchType()) && request.getSearchType().equals("usrId")) {
				query += " AND USR_ID LIKE ? ";
			}

			if (!StringUtils.isEmpty(request.getSearchType()) && request.getSearchType().equals("usrNm")) {
				query += " AND USR_NM LIKE ? ";
			}
			
			if (!StringUtils.isEmpty(request.getSearchType()) && request.getSearchType().equals("usrTelno")) {
				query += " AND USR_TELNO LIKE ? ";
			}
			
			query += " ORDER BY `USR_SEQ` DESC ";

            if (db.startPrepareStatement(query) == false) {
                logger.error("PreparedStatement set fail.");
                throw new ClaionRepositoryException("User조회 실패");
            }
            
            int idx = 0;
            
            if (!StringUtils.isEmpty(request.getSearchType()) && request.getSearchType().equals("usrId")) {
				db.setPrepareColumn(++idx, "%" + request.getKeyword() + "%");
			}
			
			if (!StringUtils.isEmpty(request.getSearchType()) && request.getSearchType().equals("lastDt")) {
				db.setPrepareColumn(++idx, "%" + request.getKeyword() + "%");
			}
			
			if (!StringUtils.isEmpty(request.getSearchType()) && request.getSearchType().equals("crtDt")) {
				db.setPrepareColumn(++idx, "%" + request.getKeyword() + "%");
			}


            rs = db.selectPrepareStatement();

			if (rs == null) {
				logger.error("ResultSet error.");
				throw new ClaionRepositoryException("User조회 실패");
			}

			try {
				if (rs.next()) {
					total_count = rs.getInt(1);
				}
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
				throw new ClaionRepositoryException("User조회 실패");
			}

		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
			}
			db.closeConnection();
		}
		return total_count;
	}
     
     public KnuUserEntity retrieveDetail(String usrId) throws ClaionRepositoryException {
    	    KnuUserEntity detail = new KnuUserEntity();
    	    
    	    DBManager db = getDbManager("claion");
    	    ResultSet rs = null;

    	    try {
    	        if (!db.connectionDB()) {
    	            logger.error("데이터베이스 연결 실패.");
    	            throw new ClaionRepositoryException("데이터베이스 연결 실패.");
    	        }
    	       
    	        String query = "SELECT `USR_SEQ`, `USR_ID`, `PASSWD`, `USR_NM`, `MEMBER_TYPE`, `STATUS`, `CRT_DT`, `CRT_OPRTR_ID`, `UPDT_DT`, `LAST_LGIN_DTTM`, `REFRESH_TOKEN_HASH`, `USR_TELNO`, `GENDER`, `BIRTH_DATE`, `LOGIN_CNT` " +
                        "FROM `KNU_USR_TB` " +
                        " WHERE 1=1 ";
    	        
    	        if (!StringUtils.isEmpty(usrId)) {
    				query += " AND USR_ID = ? ";
    			}
    	        
    	        
    	        if (!db.startPrepareStatement(query)) {
    	            logger.error("PreparedStatement 설정 실패.");
    	            throw new ClaionRepositoryException("회원 상세 목록 조회 중 오류가 발생했습니다.");
    	        }
    	        
    	        int idx = 0;
    	        
    	        if (!StringUtils.isEmpty(usrId)) {
    				db.setPrepareColumn(++idx, usrId);
    			}

    	        rs = db.selectPrepareStatement();

    	        if (rs == null) {
    	            logger.error("ResultSet 오류.");
    	            throw new ClaionRepositoryException("회원 상세 목록 조회 중 오류가 발생했습니다.");
    	        }

    	        try {
    				while (rs.next()) {
    					detail.setUsrSeq(rs.getLong("USR_SEQ"));
    					detail.setUsrId(rs.getString("USR_ID"));
    					detail.setPasswd(rs.getString("PASSWD"));
    					detail.setUsrNm(rs.getString("USR_NM"));
    					detail.setMemberType(rs.getString("MEMBER_TYPE"));
    					detail.setCrtDt(rs.getString("CRT_DT"));
    					detail.setCrtOprtrId(rs.getString("CRT_OPRTR_ID"));
    					detail.setUpdtDt(rs.getString("UPDT_DT"));
    					detail.setLastDt(rs.getString("LAST_LGIN_DTTM"));
    					detail.setStatus(rs.getString("STATUS"));
    					detail.setRefreshTokenHash(rs.getString("REFRESH_TOKEN_HASH"));
    					detail.setUsrTelno(rs.getString("USR_TELNO"));
    					detail.setGender(rs.getString("GENDER"));
    					detail.setBirthDate(rs.getString("BIRTH_DATE"));
    					detail.setLoginCnt(rs.getLong("LOGIN_CNT"));   	
    				}
    			} catch (SQLException e) {
    				logger.error(e.getMessage(), e);
    				throw new ClaionRepositoryException("회원 상세 목록 조회 중 오류가 발생하였습니다.");
    			}

    		} finally {
    			try {
    				if (rs != null)
    					rs.close();
    			} catch (SQLException e) {
    			}
    			db.closeConnection();
    		}
    		return detail;
    	}
     
     
     public boolean updateMember(KnuUserEntity user) throws ClaionRepositoryException {
    	    DBManager db = getDbManager("claion");
    	    try {
    	        if (!db.connectionDB()) {
    	            logger.error("데이터베이스 연결 실패.");
    	            throw new ClaionRepositoryException("회원 정보 수정 중 오류 발생.");
    	        }

    	        // UPDATE 쿼리 구성
    	        String updateQuery = "UPDATE KNU_USR_TB SET ";
    	        if (!StringUtils.isEmpty(user.getUsrNm())) {
    	            updateQuery += "USR_NM = ?,";
    	        }
    	        if (!StringUtils.isEmpty(user.getUsrTelno())) {
    	            updateQuery += "USR_TELNO = ?,";
    	        }
    	        if (!StringUtils.isEmpty(user.getMemberType())) {
    	            updateQuery += "MEMBER_TYPE = ?,";
    	        }
    	        if (!StringUtils.isEmpty(user.getGender())) {
    	            updateQuery += "GENDER = ?,";
    	        }
    	        if (!StringUtils.isEmpty(user.getBirthDate())) {
    	            updateQuery += "BIRTH_DATE = ?,";
    	        }
    	        // 계정 삭제시 상태값 update
    	        if (!StringUtils.isEmpty(user.getStatus())) {
    	            updateQuery += "STATUS = ?,";
    	        }
    	        // 쿼리의 마지막 컴마 제거 및 WHERE 절 추가
    	        updateQuery = updateQuery.replaceAll(",$", "") + " WHERE USR_ID = ?";

    	        if (!db.startPrepareStatement(updateQuery)) {
    	            logger.error("PreparedStatement 설정 실패.");
    	            throw new ClaionRepositoryException("회원 정보 수정 중 오류 발생.");
    	        }

    	        int idx = 0;
    	        // 조건에 맞게 동적으로 파라미터 설정
    	        if (!StringUtils.isEmpty(user.getUsrNm())) {
    	            db.setPrepareColumn(++idx, user.getUsrNm());
    	        }
    	        if (!StringUtils.isEmpty(user.getUsrTelno())) {
    	            db.setPrepareColumn(++idx, user.getUsrTelno());
    	        }
    	        if (!StringUtils.isEmpty(user.getMemberType())) {
    	            db.setPrepareColumn(++idx, user.getMemberType());
    	        }
    	        if (!StringUtils.isEmpty(user.getGender())) {
    	            db.setPrepareColumn(++idx, user.getGender());
    	        }
    	        if (!StringUtils.isEmpty(user.getBirthDate())) {
    	            db.setPrepareColumn(++idx, user.getBirthDate());
    	        }
    	        // 계정 삭제시 상태값 update
    	        if (!StringUtils.isEmpty(user.getStatus())) {
    	            db.setPrepareColumn(++idx, user.getStatus());
    	        }
    	        db.setPrepareColumn(++idx, user.getUsrId());

    	        // UPDATE 실행
    	        if (!db.commitPrepareStatement()) {
    	            logger.error("업데이트 실패.");
    	            throw new ClaionRepositoryException("회원 정보 수정 중 오류 발생.");
    	        }
    	        return true;

    	    } catch (Exception e) {
    	        logger.error("회원 정보 수정 중 예외 발생: ", e);
    	        throw new ClaionRepositoryException("회원 정보 수정 중 오류가 발생하였습니다.");
    	    } finally {
    	        db.closeConnection();
    	    }
    	}
     
     public boolean resetPassword(String userId, String newPassword) throws ClaionRepositoryException {
         DBManager db = getDbManager("claion");
         try {
             if (!db.connectionDB()) {
                 logger.error("데이터베이스 연결 실패.");
                 throw new ClaionRepositoryException("데이터베이스 연결 실패.");
             }

             // 데이터베이스 업데이트
             String updateQuery = "UPDATE KNU_USR_TB SET PASSWD = ?, UPDT_DT = NOW() WHERE USR_ID = ?";
             if (!db.startPrepareStatement(updateQuery)) {
                 logger.error("PreparedStatement 설정 실패.");
                 throw new ClaionRepositoryException("비밀번호 업데이트 실패.");
             }
             db.setPrepareColumn(1, newPassword); 
             db.setPrepareColumn(2, userId);

             if (!db.commitPrepareStatement()) {
                 logger.error("비밀번호 업데이트 실패.");
                 throw new ClaionRepositoryException("비밀번호 업데이트 중 오류 발생.");
             }
             return true;
         } catch (Exception e) {
             logger.error("비밀번호 재설정 중 예외 발생: ", e);
             throw new ClaionRepositoryException("비밀번호 재설정 중 오류가 발생하였습니다.");
         } finally {
             db.closeConnection();
         }
     }
     
    
     public Map<String, Object> findUserStatistics() throws ClaionRepositoryException {
    	    DBManager db = getDbManager("claion");
    	    ResultSet rs = null;
    	    Map<String, Object> statistics = new HashMap<>();

    	    try {
    	        if (!db.connectionDB()) {
    	            logger.error("데이터베이스 연결 실패.");
    	            throw new ClaionRepositoryException("데이터베이스 연결 실패.");
    	        }

    	        String query = "SELECT " +
    	                       "COUNT(*) AS totalMembers, " +
    	                       "SUM(CASE WHEN DATE(CRT_DT) = CURDATE() THEN 1 ELSE 0 END) AS newMembersToday, " +
    	                       "AVG(LOGIN_CNT) AS averageVisits " +
    	                       "FROM KNU_USR_TB";

    	        // 쿼리 실행을 위한 PreparedStatement 설정
    	        if (!db.startPrepareStatement(query)) {
    	            logger.error("PreparedStatement 설정 실패.");
    	            throw new ClaionRepositoryException("PreparedStatement 설정 실패.");
    	        }

    	        // 쿼리 실행
    	        rs = db.selectPrepareStatement();

    	        if (rs != null && rs.next()) {
    	            int totalMembers = rs.getInt("totalMembers");
    	            int newMembersToday = rs.getInt("newMembersToday");
    	            double averageVisits = rs.getDouble("averageVisits");

    	            // 로그 추가
    	            logger.info("총 회원수: {}, 오늘 신규 회원 수: {}, 회원 평균 방문 횟수: {}", totalMembers, newMembersToday, averageVisits);

    	            statistics.put("totalMembers", totalMembers);
    	            statistics.put("newMembersToday", newMembersToday);
    	            statistics.put("averageVisits", averageVisits);
    	        } else {
    	            // 조회된 데이터가 없을 경우 로그 출력
    	            logger.warn("No data found for user statistics");
    	        }
    	    } catch (SQLException e) {
    	        logger.error("사용자 통계 조회 중 오류 발생", e);
    	        throw new ClaionRepositoryException("사용자 통계 조회 중 오류 발생");
    	    } finally {
    	        try {
    	            if (rs != null) rs.close();
    	        } catch (SQLException e) {
    	            logger.error("ResultSet 닫기 실패", e);
    	        }
    	        db.closeConnection();
    	    }

    	    return statistics;
    	}
     
   // 회원 탈퇴 추가 
     public int retrieveChurnUserCount() throws ClaionRepositoryException {
         DBManager db = getDbManager("claion");
         ResultSet rs = null;
         int churnUserCount = 0;

         try {
             if (!db.connectionDB()) {
                 logger.error("데이터베이스 연결 실패.");
                 throw new ClaionRepositoryException("데이터베이스 연결 실패.");
             }

             String query = "SELECT COUNT(*) FROM KNU_USR_TB WHERE STATUS = 'delete'";

             if (!db.startPrepareStatement(query)) {
                 logger.error("PreparedStatement 설정 실패.");
                 throw new ClaionRepositoryException("PreparedStatement 설정 실패.");
             }

             rs = db.selectPrepareStatement();
             
             if (rs != null && rs.next()) {
                 churnUserCount = rs.getInt(1);
             }
         } catch (SQLException e) {
             logger.error("탈퇴 회원 수 조회 중 오류 발생", e);
             throw new ClaionRepositoryException("탈퇴 회원 수 조회 중 오류 발생");
         } finally {
             try {
                 if (rs != null) {
                     rs.close();
                 }
             } catch (SQLException e) {
                 logger.error("ResultSet 닫기 실패", e);
             }
             db.closeConnection();
         }

         return churnUserCount;
     }

     
     public Map<String, Object> findMemberSignUpStats(String period) throws ClaionRepositoryException {
    	 logger.info("findMemberSignUpStats 메서드 시작, period: {}", period); // 메서드 시작 로그
    	    String query;
    	    if ("week".equals(period)) {
    	        // 최근 1주일간의 데이터를 기준 데이터 조회 쿼리
    	        query = "SELECT DAYNAME(CRT_DT) AS weekday, COUNT(*) AS count " +
    	                "FROM KNU_USR_TB " +
    	                "WHERE CRT_DT >= date_sub(now(), INTERVAL 1 WEEK) " +
    	                "GROUP BY DAYNAME(CRT_DT) " +
    	                "ORDER BY FIELD(DAYNAME(CRT_DT), 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday')";
    	    } else if ("month".equals(period)) {
    	        // 월간 데이터 조회 쿼리
    	        query = "SELECT DATE_FORMAT(CRT_DT, '%Y-%m') AS month, COUNT(*) AS count " +
    	                "FROM KNU_USR_TB " +
    	                "WHERE CRT_DT >= date_sub(now(), INTERVAL 1 MONTH) " +
    	                "GROUP BY DATE_FORMAT(CRT_DT, '%Y-%m') " +
    	                "ORDER BY DATE_FORMAT(CRT_DT, '%Y-%m')";
    	    } else {
    	        throw new ClaionRepositoryException("Invalid period specified");
    	    }
    	    logger.info("findMemberSignUpStats 메서드 완료"); // 메서드 완료 로그   	    	
    	    return executeQueryForStats(query);
    	}

     
     public Map<String, Object> findMemberTypeStats() throws ClaionRepositoryException {
         // 회원 유형 통계 데이터 검색 쿼리 구현
         String query = "SELECT MEMBER_TYPE, COUNT(*) AS count FROM KNU_USR_TB GROUP BY MEMBER_TYPE";
         return executeQueryForStats(query);
     }

     public Map<String, Object> findMemberGenderStats() throws ClaionRepositoryException {
         // 회원 성별 통계 데이터 검색 쿼리 구현
         String query = "SELECT GENDER, COUNT(*) AS count FROM KNU_USR_TB GROUP BY GENDER";
         return executeQueryForStats(query);
     }
 
         // 회원 연령 통계 데이터 검색 쿼리 구현
     public Map<String, Object> findMemberAgeStats() throws ClaionRepositoryException {
    	 String query = "SELECT CONCAT(FLOOR(TIMESTAMPDIFF(YEAR, STR_TO_DATE(BIRTH_DATE, '%Y%m%d'), CURDATE()) / 10) * 10, 's') AS ageGroup, COUNT(*) AS count FROM KNU_USR_TB GROUP BY ageGroup";
    	 return executeQueryForStats(query);
    }

     
     private Map<String, Object> executeQueryForStats(String query) throws ClaionRepositoryException {
    	    DBManager db = getDbManager("claion");
    	    ResultSet rs = null;
    	    Map<String, Object> stats = new HashMap<>();
    	    try {
    	        logger.info("Executing query for statistics: {}", query); // 쿼리 실행 로그
    	        if (!db.connectionDB()) {
    	            logger.error("데이터베이스 연결 실패.");
    	            throw new ClaionRepositoryException("데이터베이스 연결 실패.");
    	        }
    	        if (!db.startPrepareStatement(query)) {
    	            logger.error("PreparedStatement 설정 실패.");
    	            throw new ClaionRepositoryException("PreparedStatement 설정 실패.");
    	        }
    	        rs = db.selectPrepareStatement();
    	        List<Map<String, Object>> dataList = new ArrayList<>();
    	        while (rs != null && rs.next()) {
    	            Map<String, Object> data = new HashMap<>();
    	            data.put("label", rs.getString(1));
    	            data.put("value", rs.getInt(2));
    	            dataList.add(data);
    	            logger.info("Data processed: label={}, value={}", rs.getString(1), rs.getInt(2)); // 각 행의 데이터 처리 로그
    	        }
    	        stats.put("data", dataList);
    	        logger.info("Query executed successfully. Total data points: {}", dataList.size()); // 쿼리 실행 성공 로그
    	    } catch (SQLException e) {
    	        logger.error("쿼리 실행 중 오류 발생", e);
    	        throw new ClaionRepositoryException("쿼리 실행 중 오류 발생");
    	    } finally {
    	        try {
    	            if (rs != null) rs.close();
    	        } catch (SQLException e) {
    	            logger.error("ResultSet 닫기 실패", e);
    	        }
    	        db.closeConnection();
    	    }
    	    return stats;
    	}
 
     }


