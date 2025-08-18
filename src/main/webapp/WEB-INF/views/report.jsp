<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="viewport" content="width=device-width, user-scalable=no" />
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />

    <!-- i-AUD 보고서를 임베디드 할 때 적용시킬 AUD7 플랫폼용 소스 import -->

    <script
      type="text/javascript"
      src="${AUD7_FULL_PATH}/js/lib/audframework/release/bimatrix.lib.audframework.js"
    ></script>
    <script
      type="text/javascript"
      src="${AUD7_FULL_PATH}/js/lib/audframework/release/bimatrix.module.audframework.js"
    ></script>
    <link rel="stylesheet" type="text/css" href="${AUD7_SKIN_CSS_PATH}/bimatrix.module.audframework.css" />

    <!-- i-AUD 보고서 버튼 영역을 표현할 때 적용시킬 AUD7 플랫폼용 소스 import -->
    <link rel="stylesheet" type="text/css" href="${serverUrl}/portal/css/theme1/theme.css" />
    <script type="text/javascript" src="${serverUrl}/portal/js/jquery-3.6.0.min.js" flush="false"></script>
    <script type="text/javascript" src="${serverUrl}/portal/js/Base64.js"></script>
    <script type="text/javascript" src="${serverUrl}/portal/js/jquery.portal.common.js"></script>
    <script type="text/javascript" src="${serverUrl}/portal/js/jquery.cookie.js"></script>
    <script type="text/javascript" src="${serverUrl}/portal/js/authorityCheck_em.jsp"></script>
    <script type="text/javascript" src="${serverUrl}/portal/js/portal.message.jsp"></script>
    <script type="text/javascript" src="${serverUrl}/portal/js/portal.option.data.jsp"></script>
    <script type="text/javascript" src="${serverUrl}/portal/js/portal.content.top.js"></script>
    <script type="text/javascript" src="${serverUrl}/portal/js/matrix.script.comm.js"></script>
    <script type="text/javascript" src="${serverUrl}/portal/js/matrix.script.content.em.js"></script>
    <script type="text/javascript" src="${serverUrl}/portal/js/portal.content.bookmark.js"></script>
    <script type="text/javascript" src="${serverUrl}/portal/js/portal.content.condition.js"></script>
    <!-- 폰트 적용 부분 추가 -->
    <script type="text/javascript" src="${serverUrl}/extention/AUD/customscript.jsp"></script>
    <script type="text/javascript">
      var AUD7_PATH = "${AUD7_FULL_PATH}";

      var AUD7_SETTING_PATH = "${AUD7_FULL_PATH}";
      var globalParam = "${CUSTOM_PARAM}";
      var gvWebRootName = "${serverUrl}";
      var currUserId = "${userCode}";
      var ispopupView = true;

      //GFN_AUTHORITY.USER_AUTH_INFO();

      var reportCode = "${rCode}"; // 오픈할 보고서 코드
      var showTitle = false; // title , 버튼 영역 표시 여부
      if ("${showTitle}" == "true") showTitle = true;
    </script>
    <style>
      * {
        margin: 0;
      }

      #AUDview {
        position: absolute;
        width: 100%;
        height: 100%;
      }

      .rep_div {
        cursor: pointer;
        font-size: 14px;
        font-weight: bold;
      }

      /* react 용 iframe 테두리와 스크롤바 안보이게 추가 */
      .istudio-tab-content {
        border: none;
        overflow: visible !important;
      }
    </style>
  </head>

  <body>
    <script>
      $(document).ready(function () {});

      window.onload = function () {
        var biAccessToken = "${aud7Token}";
        GFN_AUTHORITY.setCookie("bimatrix_accessToken", biAccessToken);
        // 인증 자동 연장
        GFN_AUTHORITY.UpdateSession();
        //console.log("===bi:"+gfnGetCookie("bimatrix_accessToken"));
        // 리사이징
        winResizer();
        if (reportCode != "null") openReport(reportCode, showTitle);
      };

      var winResizer = function () {
        var win_w = $(window).eq(0).outerWidth();
        var win_h = $(window).eq(0).outerHeight();

        var title_panel_height = $(".titlebg").outerHeight();
        var isHidden = $(".titlebg").css("display") === "none";
        if (isHidden) title_panel_height = 0;

        $(".view_panel").css("height", win_h);
        $(".view_panel").css("width", win_w);

        $("#AUDview").css("height", win_h - title_panel_height);
      };

      /**
       * resize 함수 호출 시 i-AUD 보고서를 리사이징 시킬 때 추가해줘야 할 함수
       **/
      window.addEventListener("resize", function (event) {
        winResizer();
        AUD.GetMainViewer().ViewerSizeChanged();
      });

      /**
       * i-AUD 보고서 오픈
       **/
      var openReport = function (_reportCode, isShow) {
        reportCode = _reportCode;
        if (!GFN_AUTHORITY.CheckSession()) {
          //alert("AUD7 Platform auth invalid. page refresh....");
          location.reload();
          return;
        }

        GFN_AUTHORITY.USER_AUTH_INFO();

        if (isShow) {
          var btn_type = "";
          if (GFN_OPTION.OP04_VIEW_BTN == "TEXT") btn_type = "text_type";
          else if (GFN_OPTION.OP04_VIEW_BTN == "IMAGE") btn_type = "img_type";

          $(".topbtn_group").option_top("view_btn", {
            btn_type: btn_type,
            callbackFn: settingTitle,
            embedded: true,
          });
        } else {
          $(".titlebg").css("display", "none");
        }

        AUD.Init(AudOpenReport);
      };

      /**
       * i-AUD 보고서 오픈에 필요한 Init용 함수.
       * AUD.LoadDocument ('div id명' , 보고서코드 , 2) ;
       **/
      var AudOpenReport = function () {
        AUD.SetFileDialogCallback();
        AUD.LoadDocument("AUDview", reportCode, 2);

        if (globalParam.length > 0) AUD.SetCustomParams("${CUSTOM_PARAM}");
      };

      var settingTitle = function () {
        $(".titlebg").css("display", "");
        $(".topbtn_group").option_top("setting_title", {
          report_code: reportCode,
        });
      };

      function sendSearchSccParam(statsDe, pdTy, groupbyUnit, emoTy, keyword, keywordTy) {
        //console.log("sendSearchSccParam");
        var rtn = window.parent.searchSccDocList(statsDe, pdTy, groupbyUnit, emoTy, keyword, keywordTy);
        //console.log("SCC Result: "+rtn);
      }

      function sendSearchEidParam(srchfrmlaNo, keyword, keywordTy, histNo) {
        var pdBgnde;
        var pdEndde;
        console.log(histNo, "================", biAccessToken);
        if (histNo != null) {
          $.ajax({
            url: "${pageContext.request.contextPath}/dashboard/selectPdIssue.json",
            type: "POST",
            async: false,
            header: { bimatrix_accessToken: biAccessToken },
            data: { srchfrmlaNo: srchfrmlaNo, histNo: histNo },
            success: function (rs) {
              for (var i = 0; i < rs.pdIssueList.length; i++) {
                pdBgnde =
                  rs.pdIssueList[i].pdBgnde.substring(0, 4) +
                  rs.pdIssueList[i].pdBgnde.substring(5, 7) +
                  rs.pdIssueList[i].pdBgnde.substring(8, 10);
                pdEndde =
                  rs.pdIssueList[i].pdEndde.substring(0, 4) +
                  rs.pdIssueList[i].pdEndde.substring(5, 7) +
                  rs.pdIssueList[i].pdEndde.substring(8, 10);
              }
            },
            error: function (xhr) {
              //console.log("ERROR");
            },
          });
        }

        if (keyword != "" && keywordTy != "") {
          if (histNo != 0)
            var rtn = window.parent.searchEidDocList(srchfrmlaNo, keyword, keywordTy, "1", pdBgnde, pdEndde);
          else var rtn = window.parent.searchEidDocList(srchfrmlaNo, keyword, keywordTy, "1");
          //console.log("EID Result: "+rtn);
        }
      }
    </script>
    <div class="view_panel">
      <div class="titlebg" id="titlebg_main" style="display: none">
        <div class="title_area">
          <ul>
            <li><span id="dvReportName"></span></li>
          </ul>
        </div>
        <!-- title_area -->
        <div class="bookmark" id="bookmarkIcon" style="display: none"></div>
        <!-- 현재 경로표시 영역 -->
        <div class="location" style="display: block"></div>
        <div class="topbtn_group"></div>
      </div>
      <!--// titlebg -->
      <div id="AUDview" name="AUDview" class="istudio-common-viewer"></div>
    </div>
  </body>
</html>
