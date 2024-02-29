<%@ page language="java" contentType="text/html; charset=UTF-8" 
   pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html>

<%@include file="../common/header.jsp" %>

<body>
	<div class="wrap">
     <%@include file="../common/left.jsp" %>
     <%@include file="../common/content_header.jsp" %>
         
         
<!-- main -->
<main class="main"> 
    <h2 class="title">체험 회원 목록</h2>
    <div class="content">
        <div class="content-top">
            <div class="column-left">
                <select required id="memberTypeFilter">
                    <option value="" disabled selected>필터</option>
                    <option value="">전체</option>
                    <option value="01">학생</option>
                    <option value="02">연구자</option>
                    <option value="03">교수 및 교직원</option>
                    <option value="04">일반</option>
                </select>
                <select required name="sort_type" id="sort_type">
                    <option value="" disabled selected>정렬</option>
                    <option value="crtDt">가입순</option>
                    <option value="usrNm">이름순</option>
                    <option value="usrId">계정명순</option>
                    <option value="lastDt">마지막접속순</option>
                    <option value="birthDate">생년월일순</option>
                </select>
            </div>
            <div class="search-form">
                <select required name="search_type" id="search_type">
                    <option value="" disabled selected>이름</option>
                    <option value="usrNm">이름</option>
                    <option value="usrId">계정명</option>
                    <option value="usrTelno">연락처</option>
                </select>
                <input type="text" class="input-search" name="keyword" id="keyword" placeholder="검색어를 입력하세요.">
                <button class="btn-line sm" id="search_btn">검색</button>
            </div>
        </div>

	      


    <div class="content-body">
     <div class="table-wrap">
        <table id="_tmlTable" class="table table-bordered datatable">
            <caption>검색 결과 목록</caption>
            <colgroup>
                <col width="80px">
                <col width="160px">
                <col width="100%">
                <col width="160px">
                <col width="160px">
                <col width="100px">
                <col width="120px">
                <col width="160px">
                <col width="160px">
                <col width="160px">
                <col width="160px">
            </colgroup>
            <thead>
                <tr>
                    <th class="text-center">번호</th>
                    <th class="text-center">회원구분</th>
                    <th class="text-center">계정명</th>
                    <th class="text-center">이름</th>
                    <th class="text-center">연락처</th>
                    <th class="text-center">성별</th>
                    <th class="text-center">생년월일</th>
                    <th class="text-center">가입일</th>
                    <th class="text-center">마지막접속일</th>
                    <th class="text-center">상태</th>
                    <th class="text-center">회원관리</th>
                </tr>
            </thead>
        </table>
    </div>
</main>
         
		<script src="<c:url value='/'/>js/moment.min.js"></script>
        <script type="text/javascript">
         var __CONTEXT_PATH__ = '<c:url value="/"/>';
       var editor;

         $(function(){
            $('#search_btn').click(function() {
               $('#_tmlTable').dataTable().fnDraw();
            });
            
            
            $('#search').keyup(function(e) {
               if(e.keyCode == 13) {
                  $('#_tmlTable').dataTable().fnDraw();
               }   
            });
            
            
            
            var $tmlTable = $('#_tmlTable');
            
            
            $tmlTable.DataTable({
            	"lengthChange": false,
                "language": {
                    "paginate": {
                        "first": '<span class="prev-end"><img src="../../../images/ico_pagination_arrow_end" /></span>',
                        "previous": '<span class="prev"><img src="../../../images/ico_pagination_arrow.svg" /></span>',
                        "next": '<span class="next"><img src="../../../images/ico_pagination_arrow.svg" /></span>',
                        "last": '<span class="next-end"><img src="../../../images/ico_pagination_arrow_end" /></span>'
                    },
                },
                "info": false,
                "bStateSave": true,
                "bServerSide": true,
                "bProcessing": true,
                "searching": false,
                "ordering": false,
                "columnDefs": [
                    // 번호
                    {
                        "data": "usrSeq",
                        "className": "text-center",
                        "targets": 0,
                    },
                    // 회원구분
                    {
                        "data": "memberType",
                        "className": "text-center",
                        "targets": 1
                    },
                    // 계정명
                    {
                        "data": "usrId",
                        "className": "text-center",
                        "targets": 2
                    },
                    // 이름
                    {
                        "data": "usrNm",
                        "className": "text-center",
                        "targets": 3
                    },
                    // 연락처(추가)
                    {
                        "data": "usrTelno",
                        "className": "text-center",
                        "targets": 4
                    }, 
                    // 성별(추가)
                    {
                        "data": "gender",
                        "className": "text-center",
                        "targets": 5
                    }, 
                    // 생년월일(추가)
                    {
                        "data": "birthDate",
                        "className": "text-center",
                        "targets": 6
                    }, 
                    // 가입일
                    {
                        "data": "crtDt",
                        "className": "text-center",
                        "targets": 7,
                        "render": function (data, type, row) {
                            var formattedDate = data ? moment(data).format('YYYY-MM-DD') : '-';
                            return formattedDate;
                        }
                    },
                    // 마지막접속일
                    {
                        "data": "lastDt",
                        "className": "text-center",
                        "targets": 8,
                        "render": function (data, type, row) {
                            var formattedDate = data ? moment(data).format('YYYY-MM-DD') : '-';
                            return formattedDate;
                        }
                    },
                    
                    // 회원관리
                    {
                        "data": "status",
                        "className": "text-center",
                        "targets": 9,
                        "render": function (data, type, row) {
                            if (data === "active") return "활성화";
                            else if (data === "delete") return "계정삭제";
                            else return "비활성화";
                        }
                    },
                    
                    // 회원관리
                    {
                        "data": null,
                        "className": "text-center",
                        "targets": 10,
                        "render": function (data, type, row) {
                        	return '<button class="btn-line btn-check tml-modal-show" data-usr-id="' + row.usrId +'" data-status="' + row.status +'">회원관리</button>'
                        }
                    },
                ],
                select: {
                     style:    'os',
                     selector: 'td:first-child'
                 },
               "sAjaxSource" : __CONTEXT_PATH__ + 'tm/tml/all/list/',
               "fnServerData" : function (sSource, aoData, fnCallBack) {
            	   var json_aoData = JSON.stringify({
                       data: aoData,
                       searchType: $('#search_type').val(),
                       keyword: $('#keyword').val(),
                       memberTypeFilter: $('#memberTypeFilter').val(), // 회원 유형 필터 추가
                       sortType: $('#sort_type').val() // 정렬 타입 추가
                   });
                  $.ajax({
                     'contentType' : 'application/json',
                     'dataType' : "json",
                     'type' : 'POST',
                     'url' : sSource,
                     'data' : json_aoData,
                     'success' : fnCallBack
                  });
               }
            });
            
            // 회원구분 필터 이벤트 핸들러
            $('#memberTypeFilter').on('change', function() {
                var selectedType = $(this).val();
                $tmlTable.DataTable().column(1).search(selectedType).draw();
            });
            
            $('#sort_type').on('change', function() {
                var selectedSortType = $(this).val();
                var orderSetting = [[0, 'asc']]; // 기본 정렬 설정

                // 사용자가 선택한 정렬 조건에 따른 DataTables 정렬 설정 (오름차순)
                switch (selectedSortType) {
                    case 'crtDt':
                        orderSetting = [[7, 'asc']]; // '가입순'
                    case 'usrNm':
                        orderSetting = [[3, 'asc']]; // '이름순' 
                        break;
                    case 'usrId':
                        orderSetting = [[2, 'asc']]; // '계정명순' 
                        break;
                    case 'lastDt':
                        orderSetting = [[8, 'asc']]; // '마지막접속순' 
                        break;
                    case 'birthDate':
                        orderSetting = [[6, 'asc']]; // '생년월일순' 
                        break;
                    default:
                        // 기본 정렬
                        break;
                }

                // 선택된 정렬 조건에 따라 테이블 재정렬
                $tmlTable.DataTable().order(orderSetting).draw();
            });
            
         })
     </script>
     
   </div>
   <%@include file="./modal/tmlModal.jsp" %>
 </body>
 </html>  
   
   