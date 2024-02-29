package kr.co.claion.hmp.admin.tml.dto;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import kr.co.claion.hmp.admin.cmm.dto.DataTablesWrapperRequestDto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class Tml {
	
	 @Getter
	 @Setter
	 public static class SearchCondition extends DataTablesWrapperRequestDto {
		 private String usrId;
		 private String searchType;
		 private String keyword;
		 private String memberTypeFilter; // 회원 구분 필터
		 private String sortType;         // 정렬 유형
	
	 }
	 
	 @Data
	 public static class DetailSearchCondition {
		 private int seq;
		 private String usrId;
		}
	 
	 
	 
	 @Data
     public static class KnuUserEntity {
         private Long usrSeq;
         
         private String usrId;
         
         private String passwd;
         
         private String usrNm;
         
         private String memberType;
         
         private String crtDt;
         
         private String crtOprtrId;
        
         private String updtDt;
         
         private String lastDt;
         
         private String status;
       
         private String RefreshTokenHash;
         
         private String usrTelno;
         
         private String gender;
         
         private String birthDate;
         
         private Long loginCnt;
         
	 }

}
