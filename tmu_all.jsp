<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html>


<%@include file="../common/header.jsp" %>

<body>
    <div class="wrap">
    <%@include file="../common/content_header.jsp" %>
    <%@include file="../common/left.jsp" %>
    <!-- main -->
        <main class="main"> 
            <h2 class="title">체험 회원 현황</h2>
            <div class="content-group">
                <ul class="customer-state">
                    <li>
                        <span>데이터 라이브러리<br>총 회원 수</span>
                        <strong><span id="dataLibUserCnt"></span>명</strong>
                    </li>
                    <li>
                        <span>오늘의<br>신규 회원 수</span>
                        <strong><span id="dataLibTodayNewUserCnt"></span>명</strong>
                    </li>
                    <li>
                        <span>데이터 라이브러리<br>탈퇴 회원 수</span>
                        <strong><span id="dataLibTodayChurnUserCnt"></span>명</strong>
                    </li>
                    <li>
                        <span>회원 평균 체험페이지<br>방문 횟수</span>
                        <strong><span id="dataLibUserAvgUsedCnt"></span>회</strong>
                    </li>
                </ul>
                <div class="content">
                    <div class="content-top">
                        <h3 class="content-title">체험 회원 가입수 현황</h3>
                        <select id="chartPeriodSelect" required>
                        <option value="week">주간</option>
                   <option value="month">월간</option>
                        </select>
                    </div>
                    <div class="content-body">
                        <div class="chart">
                            <canvas id="dataLibUserChart"></canvas>
                        </div>
                    </div>
                </div>
                <div class="content">
                    <div class="content-top">
                        <h3 class="content-title">회원 구분 통계</h3>
                    </div>
                    <div class="content-body">
                        <div class="chart">
                            <canvas id="dataLibUserMemberTypeChart"></canvas>
                        </div>
                    </div>
                </div>
                <div class="content">
                    <div class="content-top">
                        <h3 class="content-title">회원 성별 비율</h3>
                    </div>
                    <div class="content-body">
                        <div class="chart">
                            <canvas id="dataLibUserGenderChart"></canvas>
                        </div>
                    </div>
                </div>
                <div class="content">
                    <div class="content-top">
                        <h3 class="content-title">회원 연령 비율</h3>
                    </div>
                    <div class="content-body">
                        <div class="chart">
                            <canvas id="dataLibUserAgeChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <script type="text/javascript">
       var __CONTEXT_PATH__ = "<c:url value='/' />";
       var editor;   
       var myChart = null; // 차트 인스턴스를 저장할 전역 변수
    
        // 데이터 라이브러리 총 회원 수, 오늘의 신규 회원 수, 회원 평균 이용시간, 회원 평균 방문 횟수 조회 API
        function retrieveDataLibUsageStatus() {
            $.ajax({
                'contentType': 'application/json',
                'dataType': "json",
                'type': 'GET',
                'url': __CONTEXT_PATH__ + 'tm/tmu/retrieve/usage',
                // 'data': data, 
            }).then((response) => {
                console.log(response);
                if (response.error) {
                    // 오류 처리: 사용자에게 메시지 표시 또는 로깅
                    console.error(response.error);
                } else {
                    // 성공적으로 데이터를 받아온 경우, 페이지의 요소에 데이터를 할당
                    $('#dataLibUserCnt').text(response.totalMembers || '0');
                    $('#dataLibTodayNewUserCnt').text(response.newMembersToday || '0');
                    $('#dataLibUserAvgUsedCnt').text(response.averageVisits || '0');
                    $('#dataLibTodayChurnUserCnt').text(response.churnUserCount || '0');
                }
            }).fail(function(jqXHR, textStatus, errorThrown) {
                // AJAX 요청 실패 처리
                console.error("AJAX 요청 실패: ", textStatus, errorThrown);
            });
        }

               
        $(document).ready(function() {
        	 retrieveDataLibUsageStatus();
           
           // 체험 회원 가입 수 현황 차트 데이터를 업데이트하는 함수
           function updateChartData(period) {
        	   console.log("Requesting data for period:", period); // 요청 중 로그
               $.ajax({
                   'contentType': 'application/json',
                   'dataType': "json",
                   'type': 'GET',
                   'url': __CONTEXT_PATH__ + 'tm/tmu/retrieveSignUpStats?period=' + period,
               }).done(function(response) {
            	   console.log("Response:", response); // 응답 로그
                   var labels = response.data.map(function(item) {
                       return item.label;
                   });
                   var data = response.data.map(function(item) {
                       return item.value;
                   });
                   
                   var ctx = $("#dataLibUserChart").get(0).getContext('2d');
                   
                   // 기존 차트가 있으면 제거
                   if (myChart) {
                       myChart.destroy();
                   }
                   
                   // 새 차트 생성
                   myChart = new Chart(ctx, {
                       type: 'line', // 라인 차트일 경우
                       data: {
                           labels: labels,
                           datasets: [{
                               label: '회원 수',
                               data: data,
                               borderWidth: 1
                           }]
                       },
                       options: {
                           // 차트 옵션 설정
                       }
                   });
               }).fail(function(jqXHR, textStatus, errorThrown) {
                   console.error("AJAX 요청 실패: ", textStatus, errorThrown);
               });
           }


           // `<select>` 요소의 선택이 변경될 때 이벤트 핸들러 등록
           $('#chartPeriodSelect').change(function() {
               var selectedPeriod = $(this).val();
               updateChartData(selectedPeriod);
           });

           // 페이지 로드 시 기본 차트 데이터 로딩
           $(document).ready(function() {
               updateChartData('week');
           });
           
       	   // 회원 연령 비율 차트 초기화
           function retrieveUserAgeStats() {
        	    $.ajax({
        	        'contentType': 'application/json',
        	        'dataType': "json",
        	        'type': 'GET',
        	        'url': __CONTEXT_PATH__ + 'tm/tmu/retrieveUserAgeStats', 
        	    }).done(function(response) {
        	        
        	        // 데이터에서 레이블과 값을 추출하여 새로운 배열을 생성
        	        var labels = response.memberAgeStats.data.map(function(item) {
        	            return item.label;
        	        });

        	        var data = response.memberAgeStats.data.map(function(item) {
        	            return item.value;
        	        });

        	        var dataLibUserAgeCtx = $("#dataLibUserAgeChart").get(0).getContext('2d');
        	        new Chart(dataLibUserAgeCtx, {
        	            type: 'bar',
        	            data: {
        	                labels: labels, // 레이블 배열을 사용
        	                datasets: [{
        	                    label: '연령대',
        	                    data: data, // 값 배열을 사용
        	                    borderWidth: 1
        	                }]
        	            },
        	            options: {
        	                scales: {
        	                    yAxes: [{ // 'yAxes' 배열을 사용하여 Y축 설정
        	                        ticks: {
        	                            beginAtZero: true // Y축이 0부터 시작하도록 설정
        	                        }
        	                    }]
        	                }
        	            }
        	        });
        	    }).fail(function(jqXHR, textStatus, errorThrown) {
        	        console.error("AJAX 요청 실패: ", textStatus, errorThrown);
        	    });
        	}

        	// 페이지 로드 시 기본 차트 데이터 로딩
        	$(document).ready(function() {
        	    retrieveUserAgeStats(); 
        	});

                        
            // 나머지 차트 데이터를 검색
            $.ajax({
                'contentType': 'application/json',
                'dataType': "json",
                'type': 'GET',
                'url': __CONTEXT_PATH__ + 'tm/tmu/retrieveAllChartsData', // 차트 데이터를 반환하는 엔드포인트
            }).done(function(response) {
               console.log("나머지 차트 응답 데이터:", response);
               
           	 // 회원 구분에 대한 이름을 매핑하는 객체
               const memberTypeMap = {
                 '01': '학생',
                 '02': '연구자',
                 '03': '교수 및 교직원',
                 '04': '일반'
               };
                
            // 회원 구분 통계 차트 초기화
               var dataLibUserMemberTypeCtx = $("#dataLibUserMemberTypeChart").get(0).getContext('2d');
               new Chart(dataLibUserMemberTypeCtx, {
                   type: 'pie',
                   data: {
                       // map 함수를 사용하여 각 데이터 항목의 label을 회원 구분 이름으로 변환
                       labels: response.memberTypeStats.data.map(item => memberTypeMap[item.label] || '기타'),
                       datasets: [{
                           label: '회원 구분',
                           // map 함수를 사용하여 각 데이터 항목의 value를 추출
                           data: response.memberTypeStats.data.map(item => item.value),
                           borderWidth: 1
                       }]
                   },
                   options: {
                       responsive: true,
                       maintainAspectRatio: false, // 종횡비 유지
                   }
               });
                
            	 // 성별 코드에 대한 이름을 매핑하는 객체
                const genderMap = {
                  '01': '남성',
                  '02': '여성'
                };

                // 회원 성별 비율 차트 초기화
                var dataLibUserGenderCtx = $("#dataLibUserGenderChart").get(0).getContext('2d');
                new Chart(dataLibUserGenderCtx, {
                    type: 'pie',
                    data: {
                    	// map 함수를 사용하여 각 데이터 항목의 label을 성별 이름으로 변환
                        labels: response.memberGenderStats.data.map(item => genderMap[item.label]),
                        datasets: [{
                            label: '성별',
                            // map 함수를 사용하여 각 데이터 항목의 value를 추출
                            data: response.memberGenderStats.data.map(item => item.value),
                            borderWidth: 1
                        }]
                    },
                    options: {
                    	responsive: true,
                    	maintainAspectRatio: false, // 종횡비 유지
                    }
                });

               
                
            }).fail(function(jqXHR, textStatus, errorThrown) {
                console.error("AJAX 요청 실패: ", textStatus, errorThrown);
            });
        });



    
    </script>
</body>
</html>