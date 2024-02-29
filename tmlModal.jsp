<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<div class="popup-container" id="tmc">
	<!-- 회원 정보 관리 팝업 -->
	<div class="popup">
		<h2 class="popup-title">회원 정보 관리</h2>
		<div class="popup-content">
			<div class="customer-info-form">
				<div class="input-group">
					<span class="input-label">계정</span>
					<div class="input-item">
						<input type="text" name="usrId" id="usrId" value="" disabled>
					</div>
				</div>
				<div class="input-group">
					<span class="input-label">이름</span>
					<div class="input-item">
						<input type="text" name="usrNm" id="usrNm" value="">
					</div>
				</div>
				<div class="input-group">
					<span class="input-label">연락처</span>
					<div class="input-item">
						<input type="text" name="usrTelno" id="usrTelno" value="">
					</div>
				</div>
				<div class="input-group">
					<span class="input-label">회원구분</span> 
					<select name='memberType' required>
						<option value="01">학생</option>
    					<option value="02">연구자</option>
    					<option value="03">교수 및 교직원</option>
    					<option value="04">일반</option>
					</select>
				</div>
				<div class="input-group">
				    <span class="input-label">성별</span>
				    <div class="input-item">
				        <select name="gender" required>
				            <option value="01">남자</option>
				            <option value="02">여자</option>
				        </select>
				    </div>
				</div>
				<div class="input-group">
					<span class="input-label">가입일</span>
					<div class="input-item">
						<input type="text" name="crtDt" id="crtDt" value="" disabled>
					</div>
				</div>
				<div class="input-group">
                        <span class="input-label">생년월일</span>
                        <div class="input-item">
                            <input type="text" name="birthDate" id="birthDate" value="">
                        </div>
                    </div>
				<div class="input-group">
					<span class="input-label">마지막접속일</span>
					<div class="input-item">
						<input type="text" name="lastDt" id="lastDt" value="" disabled>
					</div>
				</div>
				<div class="input-group btn-group">
					<button class="btn-line sm delete">회원 삭제</button>
					<button class="btn-line sm password-reset">비밀번호 재설정</button>
				</div>
			</div>
		</div>
		<div class="popup-btn-group">
			<button class="btn-line sm" id="tml-modal-close">취소</button>
			<button class="btn-default sm" id="tml-save">저장</button>
		</div>
	</div>
	<!-- //회원 정보 관리 팝업 -->
</div>

<%@include file="./pwModal.jsp" %>
<%@include file="./delModal.jsp" %>

<script type="text/javascript">
// 함수 작성

$(document).ready(function() {
    $('#_tmlTable').on('click', '.tml-modal-show', function() {
        var json_data = JSON.stringify({
            usrId: $(this).data("usrId")
        });
        $.ajax({
            'contentType': 'application/json',
            'dataType': "json",
            'type': 'POST',
            'url': __CONTEXT_PATH__ + 'tm/tml/retrieve/detail',
            'data': json_data,
        }).then((response) => {
            var crtDt = response.crtDt ? moment(response.crtDt).format('YYYY-MM-DD') : '';
            var lastDt = response.lastDt ? moment(response.lastDt).format('YYYY-MM-DD') : '';
            
            // 성별 코드를 선택 항목으로 변환
            $("select[name='gender']").val(response.gender); // 성별 선택
            
            // 기존 코드와 함께 나머지 필드 값 설정
            $('#usrId').val(response.usrId);
            $('#usrNm').val(response.usrNm);
            $('#usrTelno').val(response.usrTelno);
            $('#crtDt').val(crtDt);
            $('#lastDt').val(lastDt);
            $('#birthDate').val(response.birthDate); 

            $("select[name='memberType']").val(response.memberType);
            
            if (response.status === "delete")
            	$("#tmc").find(".btn-group").css("display", "none");
            else 
            	$("#tmc").find(".btn-group").css("display", "flex");
            
        });
        $('#tmc').addClass("show");
    });
    
    // 비밀번호 재설정 버튼 클릭 이벤트 
    $('.password-reset').on('click', function() {
        	$("#pass").addClass("show")      
    });
    
	// 회원 삭제 버튼 클릭 이벤트 추가
    $('.delete').on('click', function() {
    	$("#del").addClass("show");
    });

    
    $('#tml-save').on('click', function() {
    	    // 수정된 정보 가져오기
    	    var usrId = $('#usrId').val();
    	    var usrNm = $('#usrNm').val();
    	    var usrTelno = $('#usrTelno').val();
    	    var memberType = $("select[name='memberType']").val();
    	    var gender = $("select[name='gender']").val();
    	    var birthDate = $('#birthDate').val();

    	    // JSON 형식으로 데이터 생성
    	    var jsonData = JSON.stringify({
    	        usrId: usrId,
    	        usrNm: usrNm,
    	        usrTelno: usrTelno,
    	        memberType: memberType,
    	        gender: gender,
    	        birthDate: birthDate
    	    });

    	    // AJAX를 통해 서버에 데이터 전송
    	    $.ajax({
    	        'contentType': 'application/json',
    	        'dataType': "json",
    	        'type': 'POST',
    	        'url': __CONTEXT_PATH__ + 'tm/tml/update',
    	        'data': jsonData,
    	        success: function(response) {
    	            if (response.success) {
    	                alert(response.message); 
    	                $('.popup-container').removeClass("show"); // 팝업 닫기
    	            } else {
    	                alert(response.message); // 백엔드에서 전달된 실패 메시지 사용
    	            }
    	        },
    	        error: function() {
    	            alert('회원 정보 수정에 실패했습니다. 서버 오류가 발생하였습니다.');
    	        }
    	    });
    	});
    
    $('#tml-modal-close').on('click', function () {
        $('.popup-container').removeClass("show");
    });
});
</script>

