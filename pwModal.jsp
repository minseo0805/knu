<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

    <div class="popup-container" id="pass">
        <!-- 비밀번호 재설정 팝업 -->
        <div class="popup">
            <h2 class="popup-title">비밀번호 재설정</h2>
            <div class="popup-content">
                <div class="password-reset-form">
                    <div class="input-group">
                        <span class="input-label">임시 비밀번호 설정</span>
                        <div class="input-item">
                         <input type="password" name="pw" id="pw">
                         </div>
                    </div>
                </div>
            </div>
            <div class="popup-btn-group">
                <button class="btn-line sm" id="pw-modal-close">취소</button>
                <button class="btn-default sm" id="pw-modal-registration">확인</button>
           </div>
    	</div>
    </div>
    <div class="popup-container" id="passwordAlertModal">
        <div class="popup">
            <div class="content">
                <h2 class="popup-title">비밀번호 변경</h2>
                <div class="popup-content">
                <p class="desc" id="pwAlerttext">
                    
                </p>
                <div class="popup-btn-group">
                    <button type="button" class="btn-default sm cancel-modal" >확인</button>
                </div>
            </div>
        </div>
    	</div>
    </div>
<%@include file="../../common/cmmConfirmModal.jsp" %>
<script type="text/javascript">

function validatePassword(password) {
    // Use a regular expression for password validation
    // Requires at least 8 characters, including at least 2 of: uppercase, lowercase, digit, special character
    var passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
    
    return passwordRegex.test(password);
}

$(document).ready(function() {
	
    $('#pw-modal-registration').on('click', function() {
    	var usrId = $('#usrId').val();
        var newPassword = $('#pw').val();
		
    	$('#passwordAlertModal').find(".cancel-modal").removeClass("pw-valid-false")
        // validation
        if (!validatePassword(newPassword)) {
        	$('#passwordAlertModal').find(".cancel-modal").addClass("pw-valid-false")
        	$('#pwAlerttext').html('숫자, 대소문자, 특수문자 중 최소 2가지 이상이 포함된 8자 이상의 비밀번호가 필요합니다.')
        	$('#passwordAlertModal').addClass("show");
            return false;
        }
        
        // 비밀번호 재설정을 위한 서버 요청
        $.ajax({
            url: __CONTEXT_PATH__ + 'tm/tml/resetPassword', 
            type: 'POST',
            contentType: 'application/json',
            dataType: 'json',
            data: JSON.stringify({
                usrId:  usrId,
                passwd: newPassword 
            }),
            success: function(response) {
                if(response.success) {
                	$('#pwAlerttext').html("성공")
                	$('#passwordAlertModal').addClass("show");
                } else {
                	$('#passwordAlertModal').find(".cancel-modal").addClass("pw-valid-false")
                	$('#pwAlerttext').html(response.message)
                	$('#passwordAlertModal').addClass("show");
                    //alert("비밀번호 재설정에 실패했습니다: " + response.message);
                }
                      
            },
            error: function(xhr, status, error) {
            	$('#passwordAlertModal').find(".cancel-modal").addClass("pw-valid-false")
            	$('#pwAlerttext').html('비밀번호 재설정 요청에 실패했습니다')
            	$('#passwordAlertModal').addClass("show");
                //alert("비밀번호 재설정 요청에 실패했습니다.");
            }
        });
    }); 
    // 비밀번호 재설정 팝업 닫기 버튼 이벤트
    $('body').on('click', '#pw-modal-close', function() {
    	$('#pass').removeClass("show");
    	$('#pw').val('');
    });
    
	// 비밀번호 재설정 확인 alert 모달  취소 되었을 때 클릭 이벤트 바인딩 
    $('#passwordAlertModal .cancel-modal').on('click', function() {
    	// alert모달이 종료되어야함 
    	$("#passwordAlertModal").removeClass("show");
    	
    	if (!$(this).hasClass("pw-valid-false")) {
        	$("#pass").removeClass("show");
        	// 비밀번호 값 imput 빈값으로 셋팅
        	$('#pw').val('')	
    	}
    });
});
</script>
