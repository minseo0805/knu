<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="popup-container" id="del">
    <!-- 삭제 확인 팝업 -->
    <div class="popup">
        <h2 class="popup-title">회원 삭제</h2>
        <div class="popup-content">
            <p class="desc">선택한 회원을 삭제하시겠습니까?</p>
        </div>
        <div class="popup-btn-group">
            <button class="btn-line sm" id="delete-modal-cancel">취소</button>
            <button class="btn-default sm" id="delete-modal-confirm">삭제</button>
        </div>
    </div>
</div>

<!-- Alert Modal 구조 추가 -->
<div class="popup-container" id="alertModal">
    <div class="popup">
        <h2 class="popup-title" id="alertModalTitle">알림</h2>
        <div class="popup-content">
            <p class="desc" id="alertModalDesc"></p>
        </div>
        <div class="popup-btn-group">
            <button class="btn-default sm" id="alert-modal-ok">확인</button>
        </div>
    </div>
</div>

<script type="text/javascript">
$(document).ready(function() {
    // 삭제 확인 버튼 클릭 이벤트 바인딩
    $('#delete-modal-confirm').on('click', function() {
        var usrId = $('#usrId').val(); 
        
        // 서버에 삭제 요청 전송
        $.ajax({
            url: __CONTEXT_PATH__ + 'tm/tml/delete', // 백엔드 API에 맞게 url 수정 필요
            type: 'POST',
            contentType: 'application/json',
            dataType: 'json',
            data: JSON.stringify({ 
            	usrId: usrId 
            }),
            success: function(response) {
            	if(response.success) {
                    $('#alertModalTitle').text("삭제 성공");
                    $('#alertModalDesc').text("회원 삭제가 완료되었습니다.");
                } else {
                    $('#alertModalTitle').text("삭제 실패");
                    $('#alertModalDesc').text("회원 삭제에 실패했습니다: " + response.message);
                }
                // Alert Modal 표시
                $('#alertModal').addClass("show");
                // 삭제 확인 팝업 숨기기
                $('#del').removeClass("show");
            },
            error: function(xhr, status, error) {
                // Alert Modal에 에러 메시지 설정
                $('#alertModalTitle').text("삭제 오류");
                $('#alertModalDesc').text("회원 삭제 요청이 실패했습니다.");
                // Alert Modal 표시
                $('#alertModal').addClass("show");
                $('#del').removeClass("show");
            }
        });
    });

    // Alert Modal 확인 버튼 클릭 이벤트 바인딩
    $('#alert-modal-ok').on('click', function() {
        // Alert Modal 숨기기
        $('#alertModal').removeClass("show");
        $('#del').removeClass("show");
    });
    
    // 삭제 취소 버튼 클릭 이벤트 바인딩
    $('#delete-modal-cancel').on('click', function() {
        $('#del').removeClass("show");
    });
});
</script>
